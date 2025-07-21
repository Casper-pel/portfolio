package de.thro.pipeline.repository;

import de.thro.pipeline.entity.OfferItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OfferItemRepository extends JpaRepository<OfferItem, Long> {
}
