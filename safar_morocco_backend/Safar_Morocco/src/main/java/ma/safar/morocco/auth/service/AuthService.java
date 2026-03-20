package ma.safar.morocco.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.safar.morocco.auth.dto.*;
import ma.safar.morocco.security.JwtService;
import ma.safar.morocco.security.service.AuditService;
import ma.safar.morocco.user.entity.Utilisateur;
import ma.safar.morocco.user.repository.UtilisateurRepository;
import ma.safar.morocco.user.service.ActivityLogService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import ma.safar.morocco.user.entity.VerificationToken;
import ma.safar.morocco.user.repository.VerificationTokenRepository;
import ma.safar.morocco.user.entity.PasswordResetToken;
import ma.safar.morocco.user.repository.PasswordResetTokenRepository;
import dev.samstevens.totp.code.CodeGenerator;

import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import static dev.samstevens.totp.util.Utils.getDataUriForImage;

import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final ActivityLogService activityLogService;
    private final AuditService auditService;
    private final EmailService emailService;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (utilisateurRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Un compte existe déjà avec cet email");
        }

        var user = Utilisateur.builder()
                .nom(request.getNom())
                .email(request.getEmail())
                .motDePasseHache(passwordEncoder.encode(request.getMotDePasse()))
                .telephone(request.getTelephone())
                .langue(request.getLangue() != null ? request.getLangue() : "fr")
                .role("USER")
                .photoUrl("/uploads/users/default-avatar.png")
                .actif(false)
                .compteBloquer(false)
                .provider("LOCAL")
                .build();
        user = utilisateurRepository.save(user);

        // Generate Verification Token
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .utilisateur(user)
                .expirationTime(LocalDateTime.now().plusHours(24))
                .build();
        verificationTokenRepository.save(verificationToken);

        // Send Email
        emailService.sendVerificationEmail(user.getEmail(), user.getNom(), token);

        auditService.logAction(user.getId(), "USER_REGISTERED", "Utilisateur", user.getId(),
                "New user registered: " + user.getEmail());
        activityLogService.logActivity(user, "ACCOUNT_CREATED",
                "Compte créé via enregistrement. En attente de vérification.");

        // Return empty tokens as verification is required
        return AuthResponse.builder()
                .email(user.getEmail())
                .nom(user.getNom())
                .role(user.getRole())
                .build();
    }

    @Transactional
    public AuthResponse verifyEmailToken(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token invalide"));

        if (verificationToken.isExpired()) {
            throw new RuntimeException("Token expiré");
        }

        Utilisateur user = verificationToken.getUtilisateur();
        user.setActif(true);
        utilisateurRepository.save(user);

        verificationTokenRepository.delete(verificationToken);

        var accessToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .nom(user.getNom())
                .role(user.getRole())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getMotDePasse()));

        var user = utilisateurRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        auditService.logAction(user.getId(), "USER_LOGIN", "Utilisateur", user.getId(),
                "User logged in: " + user.getEmail());

        if (user.getCompteBloquer()) {
            throw new RuntimeException("Votre compte a été bloqué. Contactez l'administrateur.");
        }

        if (!user.getActif()) {
            throw new RuntimeException("Votre compte est désactivé.");
        }

        if (user.isTwoFactorEnabled()) {
            return AuthResponse.builder()
                    .requiresTwoFactor(true)
                    .build();
        }

        var accessToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .nom(user.getNom())
                .role(user.getRole())
                .build();
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        final String refreshToken = request.getRefreshToken();
        final String userEmail = jwtService.extractUsername(refreshToken);

        if (userEmail != null) {
            var user = utilisateurRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            if (jwtService.isTokenValid(refreshToken, user)) {
                var accessToken = jwtService.generateToken(user);
                var newRefreshToken = jwtService.generateRefreshToken(user);

                return AuthResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(newRefreshToken)
                        .tokenType("Bearer")
                        .userId(user.getId())
                        .email(user.getEmail())
                        .nom(user.getNom())
                        .role(user.getRole())
                        .build();
            }
        }
        throw new RuntimeException("Refresh token invalide");
    }

    @Transactional
    public void updateProfile(UpdateProfileRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Utilisateur user = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (request.getNom() != null) {
            user.setNom(request.getNom());
        }
        if (request.getTelephone() != null) {
            user.setTelephone(request.getTelephone());
        }
        if (request.getLangue() != null) {
            user.setLangue(request.getLangue());
        }

        utilisateurRepository.save(user);
        activityLogService.logActivity(user, "PROFILE_UPDATED", "Profil mis à jour via AuthService");
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Utilisateur user = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (!passwordEncoder.matches(request.getAncienMotDePasse(), user.getMotDePasseHache())) {
            throw new RuntimeException("Ancien mot de passe incorrect");
        }

        user.setMotDePasseHache(passwordEncoder.encode(request.getNouveauMotDePasse()));
        utilisateurRepository.save(user);
    }

    public Utilisateur getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        Utilisateur user = utilisateurRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec cet email"));

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .utilisateur(user)
                .expiryDate(LocalDateTime.now().plusMinutes(15))
                .build();
        
        passwordResetTokenRepository.save(resetToken);

        emailService.sendPasswordResetEmail(user.getEmail(), token);
    }
    
    public boolean validateResetToken(String token) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElse(null);
        
        if (resetToken == null || resetToken.isExpired()) {
            return false;
        }
        return true;
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new RuntimeException("Token invalide ou inexistant"));

        if (resetToken.isExpired()) {
            passwordResetTokenRepository.delete(resetToken);
            throw new RuntimeException("Token de réinitialisation expiré");
        }

        Utilisateur user = resetToken.getUtilisateur();
        user.setMotDePasseHache(passwordEncoder.encode(request.getNewPassword()));
        utilisateurRepository.save(user);
        
        passwordResetTokenRepository.delete(resetToken);
    }

    public String generate2FASecret() {
        SecretGenerator secretGenerator = new DefaultSecretGenerator(16);
        return secretGenerator.generate();
    }

    public String getQrCodeUri(String secret, String email) {
        QrData data = new QrData.Builder()
                .label(email)
                .secret(secret)
                .issuer("SafarMorocco")
                .algorithm(dev.samstevens.totp.code.HashingAlgorithm.SHA1)
                .digits(6)
                .period(30)
                .build();

        QrGenerator generator = new ZxingPngQrGenerator();
        byte[] imageData = new byte[0];
        try {
            imageData = generator.generate(data);
        } catch (QrGenerationException e) {
            throw new RuntimeException("Erreur de génération QR Code", e);
        }

        return getDataUriForImage(imageData, generator.getImageMimeType());
    }

    public boolean verify2FA(String secret, String code) {
        if (code == null || secret == null) {
            log.error("❌ 2FA Verify: Code or Secret is null");
            return false;
        }
        TimeProvider timeProvider = new SystemTimeProvider();
        CodeGenerator codeGenerator = new DefaultCodeGenerator();
        DefaultCodeVerifier codeVerifier = new DefaultCodeVerifier(codeGenerator, timeProvider);
        codeVerifier.setAllowedTimePeriodDiscrepancy(20); // Allow +/- 10 minutes for testing

        String sanitizedSecret = secret.trim().toUpperCase();
        String sanitizedCode = code.trim().replaceAll("\\s+", "");

        boolean isValid = codeVerifier.isValidCode(sanitizedSecret, sanitizedCode);

        // Debug logging for troubleshooting
        try {
            String expectedCode = codeGenerator.generate(sanitizedSecret, timeProvider.getTime() / 30);
            log.info("🔍 2FA DEBUG: Secret={}, ProvidedCode={}, ExpectedCode={}, CurrentTime={}, IsValid={}",
                    secret, code, expectedCode, timeProvider.getTime(), isValid);
        } catch (Exception e) {
            log.error("🔍 2FA DEBUG: Error generating expected code", e);
        }

        if (!isValid) {
            log.warn("❌ 2FA FAILED: SecretLength={}, Code={}, ServerTime={}", secret.length(), code,
                    timeProvider.getTime());
        } else {
            log.info("✅ 2FA SUCCESS");
        }

        return isValid;
    }

    @Transactional
    public void enable2FA(String secret, String code) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Utilisateur user = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (!verify2FA(secret, code)) {
            throw new RuntimeException("Code 2FA invalide");
        }

        user.setTwoFactorSecret(secret);
        user.setTwoFactorEnabled(true);
        utilisateurRepository.save(user);
    }

    @Transactional
    public void disable2FA() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Utilisateur user = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        user.setTwoFactorEnabled(false);
        user.setTwoFactorSecret(null);
        utilisateurRepository.save(user);
    }
}