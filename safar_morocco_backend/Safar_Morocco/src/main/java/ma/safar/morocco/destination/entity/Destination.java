package ma.safar.morocco.destination.entity;

import jakarta.persistence.*;
import lombok.*;
import ma.safar.morocco.media.entity.Media;
import ma.safar.morocco.meteo.entity.Meteo;
import ma.safar.morocco.review.entity.Avis;
import ma.safar.morocco.event.entity.EvenementCulturel;
import ma.safar.morocco.offer.entity.Offer;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "destinations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Destination {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String nom;

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

    @Column(columnDefinition = "TEXT")
    private String histoire;

    @Column(name = "histoire_en", columnDefinition = "TEXT")
    private String histoireEn;

    @Column(name = "histoire_fr", columnDefinition = "TEXT")
    private String histoireFr;

    @Column(name = "histoire_ar", columnDefinition = "TEXT")
    private String histoireAr;

    @Column(name = "histoire_es", columnDefinition = "TEXT")
    private String histoireEs;

    @Column(columnDefinition = "TEXT", name = "historical_description")
    private String historicalDescription;

    @Column(length = 100)
    private String type;

    private Double latitude;

    private Double longitude;

    @Column(length = 100)
    private String categorie;

    @OneToMany(mappedBy = "destination", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Media> medias = new ArrayList<>();

    @OneToMany(mappedBy = "destination", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Avis> avis = new ArrayList<>();

    @OneToMany(mappedBy = "destination", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<EvenementCulturel> evenements = new ArrayList<>();

    @OneToOne(mappedBy = "destination", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Meteo meteo;

    @Column(name = "view_count")
    @Builder.Default
    private Long viewCount = 0L;

    @OneToMany(mappedBy = "destination", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnore
    private List<Offer> offers = new ArrayList<>();

    @Column(name = "best_time")
    private String bestTime;

    @Column(name = "best_time_en")
    private String bestTimeEn;

    @Column(name = "best_time_fr")
    private String bestTimeFr;

    @Column(name = "best_time_ar")
    private String bestTimeAr;

    @Column(name = "best_time_es")
    private String bestTimeEs;

    @Column(name = "languages")
    private String languages;

    @Column(name = "languages_en")
    private String languagesEn;

    @Column(name = "languages_fr")
    private String languagesFr;

    @Column(name = "languages_ar")
    private String languagesAr;

    @Column(name = "languages_es")
    private String languagesEs;

    @Column(name = "average_cost")
    private Double averageCost;

    @Column(name = "video_url", length = 500)
    private String videoUrl;

}
