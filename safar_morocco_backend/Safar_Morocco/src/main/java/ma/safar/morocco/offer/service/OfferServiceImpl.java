package ma.safar.morocco.offer.service;

import lombok.RequiredArgsConstructor;
import ma.safar.morocco.util.Translator;
import ma.safar.morocco.destination.entity.Destination;
import ma.safar.morocco.destination.repository.DestinationRepository;
import ma.safar.morocco.offer.dto.OfferDTO;
import ma.safar.morocco.offer.entity.Offer;
import ma.safar.morocco.offer.repository.OfferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OfferServiceImpl implements OfferService {

    private static final String OFFER_NOT_FOUND_MSG = "Offer not found with id: ";

    private final OfferRepository offerRepository;
    private final DestinationRepository destinationRepository;

    @Override
    @Transactional
    public OfferDTO createOffer(OfferDTO offerDTO) {
        Destination destination = destinationRepository.findById(offerDTO.getDestinationId())
                .orElseThrow(
                        () -> new RuntimeException("Destination not found with id: " + offerDTO.getDestinationId()));

        Offer offer = mapToEntity(offerDTO);
        offer.setDestination(destination);

        Offer savedOffer = offerRepository.save(offer);
        return mapToDTO(savedOffer);
    }

    @Override
    public OfferDTO getOfferById(Long id) {
        Offer offer = offerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(OFFER_NOT_FOUND_MSG + id));
        return mapToDTO(offer);
    }

    @Override
    public List<OfferDTO> getOffersByDestination(Long destinationId) {
        return offerRepository.findByDestinationIdAndDeletedFalse(destinationId).stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public List<OfferDTO> getAllOffers() {
        return offerRepository.findAll().stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    @Transactional
    public OfferDTO updateOffer(Long id, OfferDTO offerDTO) {
        Offer offer = offerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(OFFER_NOT_FOUND_MSG + id));

        offer.setName(offerDTO.getName());
        offer.setDescription(offerDTO.getDescription());
        offer.setPrice(offerDTO.getPrice());
        offer.setType(offerDTO.getType());
        offer.setAvailable(offerDTO.getAvailable());
        offer.setStars(offerDTO.getStars());
        offer.setRoomType(offerDTO.getRoomType());
        offer.setPricePerNight(offerDTO.getPricePerNight());
        offer.setCuisineType(offerDTO.getCuisineType());
        offer.setAveragePrice(offerDTO.getAveragePrice());
        offer.setDuration(offerDTO.getDuration());
        offer.setActivityType(offerDTO.getActivityType());
        offer.setDeleted(offerDTO.getDeleted());

        if (offerDTO.getDestinationId() != null
                && !offerDTO.getDestinationId().equals(offer.getDestination().getId())) {
            Destination newDestination = destinationRepository.findById(offerDTO.getDestinationId())
                    .orElseThrow(() -> new RuntimeException(
                            "Destination not found with id: " + offerDTO.getDestinationId()));
            offer.setDestination(newDestination);
        }

        Offer updatedOffer = offerRepository.save(offer);
        return mapToDTO(updatedOffer);
    }

    @Override
    @Transactional
    public void deleteOffer(Long id) {
        Offer offer = offerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(OFFER_NOT_FOUND_MSG + id));
        offer.setDeleted(true);
        offerRepository.save(offer);
    }

    private Offer mapToEntity(OfferDTO dto) {
        return Offer.builder()
                .name(dto.getName())
                .nameEn(dto.getNameEn())
                .nameFr(dto.getNameFr())
                .nameAr(dto.getNameAr())
                .nameEs(dto.getNameEs())
                .description(dto.getDescription())
                .descriptionEn(dto.getDescriptionEn())
                .descriptionFr(dto.getDescriptionFr())
                .descriptionAr(dto.getDescriptionAr())
                .descriptionEs(dto.getDescriptionEs())
                .price(dto.getPrice())
                .type(dto.getType())
                .available(dto.getAvailable())
                .stars(dto.getStars())
                .roomType(dto.getRoomType())
                .roomTypeEn(dto.getRoomTypeEn())
                .roomTypeFr(dto.getRoomTypeFr())
                .roomTypeAr(dto.getRoomTypeAr())
                .roomTypeEs(dto.getRoomTypeEs())
                .pricePerNight(dto.getPricePerNight())
                .cuisineType(dto.getCuisineType())
                .cuisineTypeEn(dto.getCuisineTypeEn())
                .cuisineTypeFr(dto.getCuisineTypeFr())
                .cuisineTypeAr(dto.getCuisineTypeAr())
                .cuisineTypeEs(dto.getCuisineTypeEs())
                .averagePrice(dto.getAveragePrice())
                .duration(dto.getDuration())
                .durationEn(dto.getDurationEn())
                .durationFr(dto.getDurationFr())
                .durationAr(dto.getDurationAr())
                .durationEs(dto.getDurationEs())
                .activityType(dto.getActivityType())
                .activityTypeEn(dto.getActivityTypeEn())
                .activityTypeFr(dto.getActivityTypeFr())
                .activityTypeAr(dto.getActivityTypeAr())
                .activityTypeEs(dto.getActivityTypeEs())
                .deleted(dto.getDeleted())
                .build();
    }

    private OfferDTO mapToDTO(Offer offer) {
        Double displayPrice = null;
        if (offer.getType() != null) {
            switch (offer.getType()) {
                case HOTEL -> displayPrice = offer.getPricePerNight();
                case RESTAURANT -> displayPrice = offer.getAveragePrice();
                case ACTIVITY -> displayPrice = offer.getPrice();
            }
        }

        return OfferDTO.builder()
                .id(offer.getId())
                .name(Translator.translate(offer, "name"))
                .nameEn(offer.getNameEn())
                .nameFr(offer.getNameFr())
                .nameAr(offer.getNameAr())
                .nameEs(offer.getNameEs())
                .description(Translator.translate(offer, "description"))
                .descriptionEn(offer.getDescriptionEn())
                .descriptionFr(offer.getDescriptionFr())
                .descriptionAr(offer.getDescriptionAr())
                .descriptionEs(offer.getDescriptionEs())
                .price(offer.getPrice())
                .type(offer.getType())
                .destinationId(offer.getDestination() != null ? offer.getDestination().getId() : null)
                .available(offer.isAvailable())
                .stars(offer.getStars())
                .roomType(Translator.translate(offer, "roomType"))
                .roomTypeEn(offer.getRoomTypeEn())
                .roomTypeFr(offer.getRoomTypeFr())
                .roomTypeAr(offer.getRoomTypeAr())
                .roomTypeEs(offer.getRoomTypeEs())
                .pricePerNight(offer.getPricePerNight())
                .cuisineType(Translator.translate(offer, "cuisineType"))
                .cuisineTypeEn(offer.getCuisineTypeEn())
                .cuisineTypeFr(offer.getCuisineTypeFr())
                .cuisineTypeAr(offer.getCuisineTypeAr())
                .cuisineTypeEs(offer.getCuisineTypeEs())
                .averagePrice(offer.getAveragePrice())
                .displayPrice(displayPrice)
                .duration(Translator.translate(offer, "duration"))
                .durationEn(offer.getDurationEn())
                .durationFr(offer.getDurationFr())
                .durationAr(offer.getDurationAr())
                .durationEs(offer.getDurationEs())
                .activityType(Translator.translate(offer, "activityType"))
                .activityTypeEn(offer.getActivityTypeEn())
                .activityTypeFr(offer.getActivityTypeFr())
                .activityTypeAr(offer.getActivityTypeAr())
                .activityTypeEs(offer.getActivityTypeEs())
                .deleted(offer.isDeleted())
                .build();
    }
}
