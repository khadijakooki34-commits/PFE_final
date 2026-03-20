package ma.safar.morocco.auth.controller;

import lombok.RequiredArgsConstructor;
import ma.safar.morocco.auth.dto.AuthResponse;
import ma.safar.morocco.auth.dto.TwoFactorRequest;
import ma.safar.morocco.auth.service.AuthService;
import ma.safar.morocco.security.JwtService;
import ma.safar.morocco.user.entity.Utilisateur;
import ma.safar.morocco.user.service.UtilisateurService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/2fa")
@RequiredArgsConstructor
public class TwoFactorController {

    private final AuthService authService;
    private final UtilisateurService utilisateurService;
    private final JwtService jwtService;

    @PostMapping("/setup")
    public ResponseEntity<Map<String, String>> setup2FA() {
        String secret = authService.generate2FASecret();
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        String qrCodeUri = authService.getQrCodeUri(secret, email);

        Map<String, String> response = new HashMap<>();
        response.put("secret", secret);
        response.put("qrCodeUri", qrCodeUri);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify")
    public ResponseEntity<Map<String, String>> verify2FA(@RequestBody TwoFactorRequest request) {
        authService.enable2FA(request.getSecret(), request.getCode());

        Map<String, String> response = new HashMap<>();
        response.put("message", "2FA activé avec succès");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/disable")
    public ResponseEntity<Map<String, String>> disable2FA() {
        authService.disable2FA();

        Map<String, String> response = new HashMap<>();
        response.put("message", "2FA désactivé avec succès");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/validate-login")
    public ResponseEntity<AuthResponse> validateLogin(@RequestBody TwoFactorRequest request) {
        Utilisateur user = utilisateurService.getUserByEmailEntity(request.getEmail());

        if (!authService.verify2FA(user.getTwoFactorSecret(), request.getCode())) {
            throw new RuntimeException("Code 2FA invalide");
        }

        var accessToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        return ResponseEntity.ok(AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .nom(user.getNom())
                .role(user.getRole())
                .build());
    }
}
