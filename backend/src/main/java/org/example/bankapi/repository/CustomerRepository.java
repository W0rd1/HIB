package org.example.bankapi.repository;
import org.example.bankapi.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;
public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    Optional<Customer> findByUsername(String username);
    Optional<Customer> findByUsernameIgnoreCase(String username);
    Optional<Customer> findByEmailIgnoreCase(String email);
    boolean existsByUsername(String username);
    boolean existsByUsernameIgnoreCase(String username);
    boolean existsByEmailIgnoreCase(String email);
}
