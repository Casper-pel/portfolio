package de.thro.pipeline.repository;

import de.thro.pipeline.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByOffer_OfferNumber(String offerNumber);
    List<Invoice> findByIsCheckedIsNull();
}
