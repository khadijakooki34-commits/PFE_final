package ma.safar.morocco.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.safar.morocco.user.dto.TwoFactorAuthDTO;
import ma.safar.morocco.user.entity.TwoFactorAuth;
import ma.safar.morocco.user.entity.Utilisateur;
import ma.safar.morocco.user.repository.UtilisateurRepository;
import ma.safar.morocco.user.service.TwoFactorAuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


@RestController
@RequestMapping("/api/auth/2fa")
@RequiredArgsConstructor
@Slf4j
public class
               TwoFactorAuthController {

    private final TwoFactorAuthService twoFactorAuthService;
    private final UtilisateurRepository utilisateurRepository;

    /**
     * Génère le QR code et le secret pour la configuration 2FA
     */
    @PostMapping("/setup")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> setupTwoFactor(Authentication authentication) {
        try {
            String email = authentication.getName();
            Utilisateur user = utilisateurRepository.findByEmail(email)
                    .orElseThrow(() -> new Exception("Utilisateur non trouvé"));

            TwoFactorAuth twoFactorAuth = twoFactorAuthService.generateSecretAndQrCode(user);

            TwoFactorAuthDTO dto = TwoFactorAuthDTO.builder()
                    .id(twoFactorAuth.getId())
                    .qrCodeUrl(twoFactorAuth.getQrCodeUrl())
                    .secretKey(twoFactorAuth.getSecretKey())
                    .enabled(twoFactorAuth.getEnabled())
                    .confirmed(twoFactorAuth.getConfirmed())
                    .build();

            log.info("2FA setup initiated for user: {}", email);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            log.error("Error setting up 2FA: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Erreur lors de la configuration du 2FA: " + e.getMessage());
        }
    }

    /**
     * Confirme le code 2FA avant activation
     */
    @PostMapping("/confirm")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> confirmCode(
            Authentication authentication,
            @RequestParam String code
    ) {
        try {
            String email = authentication.getName();
            Utilisateur user = utilisateurRepository.findByEmail(email)
                    .orElseThrow(() -> new Exception("Utilisateur non trouvé"));

            boolean isValid = twoFactorAuthService.verifyCode(user, code);

            if (isValid) {
                log.info("2FA code confirmed for user: {}", email);
                return ResponseEntity.ok("Code 2FA confirmé avec succès");
            } else {
                log.warn("Invalid 2FA code during confirmation for user: {}", email);
                return ResponseEntity.badRequest().body("Code 2FA invalide");
            }
        } catch (Exception e) {
            log.error("Error confirming 2FA code: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Erreur Interne: " + e.getMessage());
        }
    }

    /**
     * Vérifie le code 2FA et active la fonctionnalité
     */
    @PostMapping("/verify")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> verifyAndEnable(
            Authentication authentication,
            @RequestParam String code
    ) {
        try {
            String email = authentication.getName();
            Utilisateur user = utilisateurRepository.findByEmail(email)
                    .orElseThrow(() -> new Exception("Utilisateur non trouvé"));

            twoFactorAuthService.enableTwoFactorAuth(user, code);

            log.info("2FA enabled successfully for user: {}", email);
            return ResponseEntity.ok("2FA activé avec succès");
        } catch (Exception e) {
            log.error("Error verifying 2FA: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }

    /**
     * Valide un code 2FA (utilisé lors de la connexion)
     */
    @PostMapping("/validate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> validateCode(
            Authentication authentication,
            @RequestParam String code
    ) {
        try {
            String email = authentication.getName();
            Utilisateur user = utilisateurRepository.findByEmail(email)
                    .orElseThrow(() -> new Exception("Utilisateur non trouvé"));

            boolean isValid = twoFactorAuthService.verifyCode(user, code);

            if (isValid) {
                log.info("2FA code validated successfully for user: {}", email);
                return ResponseEntity.ok("Code 2FA valide");
            } else {
                log.warn("Invalid 2FA code for user: {}", email);
                return ResponseEntity.badRequest().body("Code 2FA invalide");
            }
        } catch (Exception e) {
            log.error("Error validating 2FA: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }

    /**
     * Désactive le 2FA pour l'utilisateur connecté
     */
    @PostMapping("/disable")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> disableTwoFactor(Authentication authentication) {
        try {
            String email = authentication.getName();
            Utilisateur user = utilisateurRepository.findByEmail(email)
                    .orElseThrow(() -> new Exception("Utilisateur non trouvé"));

            twoFactorAuthService.disableTwoFactorAuth(user);

            log.info("2FA disabled for user: {}", email);
            return ResponseEntity.ok("2FA désactivé");
        } catch (Exception e) {
            log.error("Error disabling 2FA: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }

    /**
     * Obtient l'état du 2FA de l'utilisateur
     */
    @GetMapping("/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getTwoFactorStatus(Authentication authentication) {
        try {
            String email = authentication.getName();
            Utilisateur user = utilisateurRepository.findByEmail(email)
                    .orElseThrow(() -> new Exception("Utilisateur non trouvé"));

            Optional<TwoFactorAuth> twoFA = twoFactorAuthService.getTwoFactorAuth(user);

            if (twoFA.isPresent()) {
                TwoFactorAuthDTO dto = TwoFactorAuthDTO.builder()
                        .enabled(twoFA.get().getEnabled())
                        .confirmed(twoFA.get().getConfirmed())
                        .build();
                return ResponseEntity.ok(dto);
            }

            return ResponseEntity.ok(TwoFactorAuthDTO.builder().enabled(false).confirmed(false).build());
        } catch (Exception e) {
            log.error("Error fetching 2FA status: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }
}
