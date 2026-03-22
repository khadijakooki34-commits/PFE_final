package ma.safar.morocco.event.service;

import lombok.RequiredArgsConstructor;
import ma.safar.morocco.event.dto.EvenementRequestDTO;
import ma.safar.morocco.event.dto.EvenementResponseDTO;
import ma.safar.morocco.event.entity.EvenementCulturel;
import ma.safar.morocco.event.repository.EvenementCulturelRepository;
import ma.safar.morocco.util.Translator;
import ma.safar.morocco.destination.entity.Destination;
import ma.safar.morocco.destination.repository.DestinationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EvenementCulturelService {

    private final EvenementCulturelRepository evenementRepository;
    private final DestinationRepository destinationRepository;

    /**
     * Récupère tous les événements
     */
    public List<EvenementResponseDTO> findAll() {
        return evenementRepository.findAll().stream()
                .map(this::convertToDTO)
                .toList();
    }

    /**
     * Récupère un événement par ID
     */
    public Optional<EvenementResponseDTO> findById(Long id) {
        return evenementRepository.findById(id).map(this::convertToDTO);
    }

    /**
     * Récupère les événements d'une destination
     */
    public List<EvenementResponseDTO> findByDestinationId(Long destinationId) {
        return evenementRepository.findByDestinationId(destinationId).stream()
                .map(this::convertToDTO)
                .toList();
    }

    /**
     * Crée un nouvel événement
     */
    @Transactional
    public EvenementCulturel create(Long destinationId, EvenementRequestDTO request) {
        Destination destination = destinationRepository.findById(destinationId)
                .orElseThrow(() -> new IllegalArgumentException("Destination non trouvée"));

        EvenementCulturel evenement = EvenementCulturel.builder()
                .nom(request.getNom())
                .nameEn(request.getNameEn())
                .nameFr(request.getNameFr())
                .nameAr(request.getNameAr())
                .nameEs(request.getNameEs())
                .dateDebut(request.getDateDebut())
                .dateFin(request.getDateFin())
                .lieu(request.getLieu())
                .eventType(request.getEventType())
                .description(request.getDescription())
                .descriptionEn(request.getDescriptionEn())
                .descriptionFr(request.getDescriptionFr())
                .descriptionAr(request.getDescriptionAr())
                .descriptionEs(request.getDescriptionEs())
                .historique(request.getHistorique())
                .historiqueEn(request.getHistoriqueEn())
                .historiqueFr(request.getHistoriqueFr())
                .historiqueAr(request.getHistoriqueAr())
                .historiqueEs(request.getHistoriqueEs())
                .imageUrl(request.getImageUrl())
                .destination(destination)
                .build();
        return evenementRepository.save(evenement);
    }

    /**
     * Met à jour un événement
     */
    @Transactional
    public EvenementCulturel update(Long id, EvenementRequestDTO request) {
        EvenementCulturel existing = evenementRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Événement non trouvé"));

        existing.setNom(request.getNom());
        existing.setNameEn(request.getNameEn());
        existing.setNameFr(request.getNameFr());
        existing.setNameAr(request.getNameAr());
        existing.setNameEs(request.getNameEs());
        existing.setDescription(request.getDescription());
        existing.setDescriptionEn(request.getDescriptionEn());
        existing.setDescriptionFr(request.getDescriptionFr());
        existing.setDescriptionAr(request.getDescriptionAr());
        existing.setDescriptionEs(request.getDescriptionEs());
        existing.setDateDebut(request.getDateDebut());
        existing.setDateFin(request.getDateFin());
        existing.setLieu(request.getLieu());
        existing.setEventType(request.getEventType());
        existing.setHistorique(request.getHistorique());
        existing.setHistoriqueEn(request.getHistoriqueEn());
        existing.setHistoriqueFr(request.getHistoriqueFr());
        existing.setHistoriqueAr(request.getHistoriqueAr());
        existing.setHistoriqueEs(request.getHistoriqueEs());
        existing.setImageUrl(request.getImageUrl());

        return evenementRepository.save(existing);
    }

    /**
     * Supprime un événement
     */
    @Transactional
    public void delete(Long id) {
        if (!evenementRepository.existsById(id)) {
            throw new IllegalArgumentException("Événement non trouvé");
        }
        evenementRepository.deleteById(id);
    }

    /**
     * Récupère les événements à venir
     */
    public List<EvenementResponseDTO> findUpcoming() {
        LocalDateTime now = LocalDateTime.now();
        return evenementRepository.findAll().stream()
                .filter(e -> e.getDateDebut() != null && e.getDateDebut().isAfter(now))
                .sorted((e1, e2) -> e1.getDateDebut().compareTo(e2.getDateDebut()))
                .map(this::convertToDTO)
                .toList();
    }

    public List<EvenementResponseDTO> findOngoing() {
        LocalDateTime now = LocalDateTime.now();
        return evenementRepository.findAll().stream()
                .filter(e -> e.getDateDebut() != null && e.getDateFin() != null &&
                        e.getDateDebut().isBefore(now) && e.getDateFin().isAfter(now))
                .map(this::convertToDTO)
                .toList();
    }

    public List<EvenementResponseDTO> findPast() {
        LocalDateTime now = LocalDateTime.now();
        return evenementRepository.findAll().stream()
                .filter(e -> e.getDateFin() != null && e.getDateFin().isBefore(now))
                .sorted((e1, e2) -> e2.getDateFin().compareTo(e1.getDateFin()))
                .map(this::convertToDTO)
                .toList();
    }

    public List<EvenementResponseDTO> findByType(String type) {
        return evenementRepository.findAll().stream()
                .filter(e -> e.getEventType() != null && e.getEventType().equalsIgnoreCase(type))
                .map(this::convertToDTO)
                .toList();
    }

    public List<EvenementResponseDTO> findByLieu(String lieu) {
        return evenementRepository.findAll().stream()
                .filter(e -> e.getLieu() != null && e.getLieu().contains(lieu))
                .map(this::convertToDTO)
                .toList();
    }

    public EvenementResponseDTO convertToDTO(EvenementCulturel e) {
        if (e == null) return null;
        return EvenementResponseDTO.builder()
                .id(e.getId())
                .nom(Translator.translate(e, "name"))
                .nameEn(e.getNameEn())
                .nameFr(e.getNameFr())
                .nameAr(e.getNameAr())
                .nameEs(e.getNameEs())
                .description(Translator.translate(e, "description"))
                .descriptionEn(e.getDescriptionEn())
                .descriptionFr(e.getDescriptionFr())
                .descriptionAr(e.getDescriptionAr())
                .descriptionEs(e.getDescriptionEs())
                .dateDebut(e.getDateDebut())
                .dateFin(e.getDateFin())
                .lieu(e.getLieu())
                .eventType(e.getEventType())
                .historique(Translator.translate(e, "historique"))
                .historiqueEn(e.getHistoriqueEn())
                .historiqueFr(e.getHistoriqueFr())
                .historiqueAr(e.getHistoriqueAr())
                .historiqueEs(e.getHistoriqueEs())
                .imageUrl(e.getImageUrl())
                .build();
    }

    /**
     * Compte les événements d'une destination
     */
    public long countByDestinationId(Long destinationId) {
        return findByDestinationId(destinationId).size();
    }
}
