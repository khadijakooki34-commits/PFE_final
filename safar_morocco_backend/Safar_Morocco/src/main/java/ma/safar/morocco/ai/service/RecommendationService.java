package ma.safar.morocco.ai.service;

import ma.safar.morocco.destination.entity.Destination;
import ma.safar.morocco.destination.repository.DestinationRepository;
import ma.safar.morocco.ai.dto.RecommendationDTO;
import ma.safar.morocco.favori.entity.Favori;
import ma.safar.morocco.favori.repository.FavoriRepository;
import ma.safar.morocco.reservation.entity.Reservation;
import ma.safar.morocco.reservation.repository.ReservationRepository;
import ma.safar.morocco.user.entity.Utilisateur;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RecommendationService {

    @Autowired
    private DestinationRepository destinationRepository;

    @Autowired
    private FavoriRepository favoriRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    //Génère des suggestions de destinations basées sur le message

    public List<String> generateSuggestions(String userMessage) {
        log.info("Génération de suggestions pour: {}", userMessage);

        Set<String> suggestions = new LinkedHashSet<>();
        String messageLower = userMessage.toLowerCase();

        // Mots-clés pour différentes catégories
        if (messageLower.contains("plage") || messageLower.contains("mer") ||
                messageLower.contains("beach") || messageLower.contains("océan")) {
            suggestions.addAll(getDestinationsByType("plage", 3));
        }

        if (messageLower.contains("montagne") || messageLower.contains("ski") ||
                messageLower.contains("randonnée") || messageLower.contains("atlas")) {
            suggestions.addAll(getDestinationsByType("montagne", 3));
        }

        if (messageLower.contains("culturel") || messageLower.contains("monument") ||
                messageLower.contains("musée") || messageLower.contains("médina")) {
            suggestions.addAll(getDestinationsByType("culturel", 3));
        }

        if (messageLower.contains("désert") || messageLower.contains("sahara") ||
                messageLower.contains("dunes")) {
            suggestions.addAll(getDestinationsByType("désert", 3));
        }

        // Villes spécifiques
        List<String> cities = Arrays.asList("marrakech", "fès", "casablanca",
                "rabat", "tanger", "agadir", "essaouira", "chefchaouen");

        for (String city : cities) {
            if (messageLower.contains(city)) {
                suggestions.add(capitalize(city));
            }
        }

        // Si pas de suggestions, proposer destinations populaires
        if (suggestions.isEmpty()) {
            suggestions.addAll(getPopularDestinations(5));
        }

        return new ArrayList<>(suggestions).stream()
                .limit(5)
                .collect(Collectors.toList());
    }

    private List<String> getDestinationsByType(String type, int limit) {
        try {
            return destinationRepository.findAll().stream()
                    .filter(d -> d.getType() != null && d.getType().toLowerCase().contains(type))
                    .limit(limit)
                    .map(Destination::getNom)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Erreur récupération destinations par type", e);
            return Collections.emptyList();
        }
    }

    private List<String> getPopularDestinations(int limit) {
        try {
            List<Destination> destinations = destinationRepository.findAll();
            if (destinations.isEmpty()) {
                return Arrays.asList("Marrakech", "Fès", "Chefchaouen", "Essaouira", "Merzouga");
            }
            return destinations.stream()
                    .limit(limit)
                    .map(Destination::getNom)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Erreur récupération destinations populaires", e);
            return Arrays.asList("Marrakech", "Fès", "Chefchaouen");
        }
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public List<RecommendationDTO> getPersonalizedRecommendations(Utilisateur user) {
        log.info("Generating personalized recommendations for user: {}", user.getEmail());

        List<Destination> allDestinations = destinationRepository.findAll();
        List<Favori> favoris = favoriRepository.findByUtilisateurId(user.getId());
        List<Reservation> reservations = reservationRepository.findByUtilisateurId(user.getId());

        Set<String> favoredCategories = favoris.stream()
                .map(f -> f.getDestination().getCategorie())
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<String> reservedCategories = reservations.stream()
                .filter(r -> r.getEvenement() != null && r.getEvenement().getDestination() != null)
                .map(r -> r.getEvenement().getDestination().getCategorie())
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<String> preferredCategories = new HashSet<>(favoredCategories);
        preferredCategories.addAll(reservedCategories);

        Set<Long> visitedOrFavoredDestIds = new HashSet<>();
        favoris.forEach(f -> visitedOrFavoredDestIds.add(f.getDestination().getId()));
        reservations.stream()
                .filter(r -> r.getEvenement() != null && r.getEvenement().getDestination() != null)
                .forEach(r -> visitedOrFavoredDestIds.add(r.getEvenement().getDestination().getId()));

        // Map to store destination score and reason
        Map<Destination, Integer> scores = new HashMap<>();
        Map<Destination, String> reasons = new HashMap<>();

        for (Destination dest : allDestinations) {
            // Skip already favored or reserved destinations for recommendations
            if (visitedOrFavoredDestIds.contains(dest.getId())) {
                continue;
            }

            int score = 0;
            String reason = "Popular choice";

            // 3 points for category match
            if (dest.getCategorie() != null && preferredCategories.contains(dest.getCategorie())) {
                score += 3;
                reason = "Because you like " + dest.getCategorie() + " destinations";
            }

            // 1 point for globally popular (e.g., highly viewed)
            if (dest.getViewCount() != null && dest.getViewCount() > 100) {
                score += 1;
                if (score == 1) {
                    reason = "Trending destination";
                }
            }
            
            // Note: In a more complex scenario we'd add 2 points for similar users here.

            if (score > 0) {
                scores.put(dest, score);
                reasons.put(dest, reason);
            }
        }

        List<Destination> topDestinations = scores.entrySet().stream()
                .sorted(Map.Entry.<Destination, Integer>comparingByValue().reversed())
                .limit(10)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // Fallback to popular destinations if no recommendations could be made
        if (topDestinations.isEmpty()) {
            topDestinations = allDestinations.stream()
                    .filter(d -> !visitedOrFavoredDestIds.contains(d.getId()))
                    .sorted((d1, d2) -> Long.compare(
                            d2.getViewCount() != null ? d2.getViewCount() : 0L,
                            d1.getViewCount() != null ? d1.getViewCount() : 0L))
                    .limit(5)
                    .collect(Collectors.toList());
            
            for (Destination d : topDestinations) {
                reasons.put(d, "Popular among users");
            }
        }

        return topDestinations.stream().map(dest -> {
            String imageUrl = null;
            if (dest.getMedias() != null && !dest.getMedias().isEmpty()) {
                imageUrl = dest.getMedias().get(0).getUrl();
            }
            return RecommendationDTO.builder()
                    .id(dest.getId())
                    .name(dest.getNom())
                    .description(dest.getDescription() != null && dest.getDescription().length() > 100 ? 
                            dest.getDescription().substring(0, 100) + "..." : dest.getDescription())
                    .reason(reasons.getOrDefault(dest, "Recommended for you"))
                    .imageUrl(imageUrl)
                    .build();
        }).collect(Collectors.toList());
    }
}