package ma.safar.morocco.offer.entity;

import jakarta.persistence.*;
import lombok.*;
import ma.safar.morocco.destination.entity.Destination;
import ma.safar.morocco.offer.enums.OfferType;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "offers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Offer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "name_en", length = 200)
    private String nameEn;

    @Column(name = "name_fr", length = 200)
    private String nameFr;

    @Column(name = "name_ar", length = 200)
    private String nameAr;

    @Column(name = "name_es", length = 200)
    private String nameEs;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "description_en", columnDefinition = "TEXT")
    private String descriptionEn;

    @Column(name = "description_fr", columnDefinition = "TEXT")
    private String descriptionFr;

    @Column(name = "description_ar", columnDefinition = "TEXT")
    private String descriptionAr;

    @Column(name = "description_es", columnDefinition = "TEXT")
    private String descriptionEs;

    private Double price; // Base price for activity or general purpose

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private OfferType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_id", nullable = false)
    @JsonIgnore
    private Destination destination;

    @Builder.Default
    private boolean available = true;

    // HOTEL specific fields
    private Integer stars; // 1 to 5 stars
    @Column(length = 100)
    private String roomType;

    @Column(name = "room_type_en", length = 100)
    private String roomTypeEn;

    @Column(name = "room_type_fr", length = 100)
    private String roomTypeFr;

    @Column(name = "room_type_ar", length = 100)
    private String roomTypeAr;

    @Column(name = "room_type_es", length = 100)
    private String roomTypeEs;
    private Double pricePerNight;

    // RESTAURANT specific fields
    @Column(length = 100)
    private String cuisineType;

    @Column(name = "cuisine_type_en", length = 100)
    private String cuisineTypeEn;

    @Column(name = "cuisine_type_fr", length = 100)
    private String cuisineTypeFr;

    @Column(name = "cuisine_type_ar", length = 100)
    private String cuisineTypeAr;

    @Column(name = "cuisine_type_es", length = 100)
    private String cuisineTypeEs;
    private Double averagePrice;

    // ACTIVITY specific fields
    @Column(length = 50)
    private String duration; // e.g., "2 hours", "1 day"

    @Column(name = "duration_en", length = 50)
    private String durationEn;

    @Column(name = "duration_fr", length = 50)
    private String durationFr;

    @Column(name = "duration_ar", length = 50)
    private String durationAr;

    @Column(name = "duration_es", length = 50)
    private String durationEs;
    @Column(length = 100)
    private String activityType; // e.g., "surfing", "horse riding"

    @Column(name = "activity_type_en", length = 100)
    private String activityTypeEn;

    @Column(name = "activity_type_fr", length = 100)
    private String activityTypeFr;

    @Column(name = "activity_type_ar", length = 100)
    private String activityTypeAr;

    @Column(name = "activity_type_es", length = 100)
    private String activityTypeEs;

    @Column(nullable = false)
    @Builder.Default
    private boolean deleted = false;
}
