package ma.safar.morocco.destination.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DestinationResponseDTO {
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
    
    // Localized fields
    private String histoire;
    private String bestTime;
    private String languages;
    
    private String historicalDescription;
    private String type;
    private Double latitude;
    private Double longitude;
    private String categorie;
    private Long viewCount;
    private Double averageRating;
    private Long reviewCount;
    private String thumbnailUrl;
    private List<ma.safar.morocco.media.dto.MediaDTO> medias;
    private List<String> imageUrls;
    private String bestTimeEn;
    private String bestTimeFr;
    private String bestTimeAr;
    private String bestTimeEs;
    
    private String languagesEn;
    private String languagesFr;
    private String languagesAr;
    private String languagesEs;
    
    private Double averageCost;
    private String videoUrl;
}
