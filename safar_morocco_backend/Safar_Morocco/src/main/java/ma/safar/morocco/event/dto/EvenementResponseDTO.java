package ma.safar.morocco.event.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvenementResponseDTO {
    private Long id;
    private String nom;
    private String nameEn;
    private String nameFr;
    private String nameAr;
    private String nameEs;
    
    private String description;
    private String descriptionEn;
    private String descriptionFr;
    private String descriptionAr;
    private String descriptionEs;
    
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private String lieu;
    private String eventType;
    private String historique;
    
    // Multi-language historical fields
    private String historiqueEn;
    private String historiqueFr;
    private String historiqueAr;
    private String historiqueEs;
    
    private String imageUrl;
}
