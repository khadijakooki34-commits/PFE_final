package ma.safar.morocco.settings.controller;

import lombok.RequiredArgsConstructor;
import ma.safar.morocco.auth.dto.ChangePasswordRequest;
import ma.safar.morocco.auth.service.AuthService;
import ma.safar.morocco.settings.dto.SettingsDTO;
import ma.safar.morocco.user.entity.Utilisateur;
import ma.safar.morocco.user.service.UtilisateurService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final UtilisateurService utilisateurService;
    private final AuthService authService;

    @GetMapping
    public ResponseEntity<SettingsDTO> getSettings() {
        Utilisateur user = utilisateurService.getCurrentUser();
        // Map user properties to SettingsDTO
        // Note: In a real app, these might be stored in a separate Settings entity or
        // JSON field
        // For now, we mock/map what we have
        return ResponseEntity.ok(SettingsDTO.builder()
                .language(user.getLangue())
                .twoFactorEnabled(user.isTwoFactorEnabled())
                .build());
    }

    @PutMapping
    public ResponseEntity<SettingsDTO> updateSettings(@RequestBody SettingsDTO settings) {
        Utilisateur user = utilisateurService.getCurrentUser();

        // Update supported fields
        if (settings.getLanguage() != null) {
            user.setLangue(settings.getLanguage());
        }

        // Save using service
        utilisateurService.updateOwnProfile(ma.safar.morocco.user.dto.UtilisateurDTO.builder()
                .langue(user.getLangue())
                .build());

        return ResponseEntity.ok(settings);
    }

    @PutMapping("/password")
    public ResponseEntity<Void> updatePassword(@RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/account")
    public ResponseEntity<Void> deleteAccount() {
        Utilisateur user = utilisateurService.getCurrentUser();
        utilisateurService.deactivateUser(user.getId());
        // In a real app, might completely delete or anonymize
        return ResponseEntity.noContent().build();
    }
}
