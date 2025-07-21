package de.thro.pipeline.repository;

import de.thro.pipeline.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByCompanyNameAndAddressStreet(String companyName, String addressStreet);
}
