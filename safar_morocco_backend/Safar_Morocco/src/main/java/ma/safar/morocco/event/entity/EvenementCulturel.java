package ma.safar.morocco.event.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import ma.safar.morocco.destination.entity.Destination;

import java.time.LocalDateTime;

@Entity
@Table(name = "evenements_culturels")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvenementCulturel {

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

    private LocalDateTime dateDebut;

    private LocalDateTime dateFin;

    @Column(nullable = false, length = 200)
    private String lieu;

    @Column(name = "event_type", length = 100)
    private String eventType;

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
    private String historique;

    @Column(name = "historique_en", columnDefinition = "TEXT")
    private String historiqueEn;

    @Column(name = "historique_fr", columnDefinition = "TEXT")
    private String historiqueFr;

    @Column(name = "historique_ar", columnDefinition = "TEXT")
    private String historiqueAr;

    @Column(name = "historique_es", columnDefinition = "TEXT")
    private String historiqueEs;

    private String imageUrl;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_id")
    private Destination destination;

}
