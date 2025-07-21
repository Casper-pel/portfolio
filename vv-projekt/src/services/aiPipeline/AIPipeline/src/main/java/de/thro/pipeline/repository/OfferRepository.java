package de.thro.pipeline.repository;

import de.thro.pipeline.entity.Offer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OfferRepository extends JpaRepository<Offer, String> {
    Optional<Offer> findByOfferNumber(String offerNumber);
    List<Offer> findByCustomerId(Long customerId);
}
