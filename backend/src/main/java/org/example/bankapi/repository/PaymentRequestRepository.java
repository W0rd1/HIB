package org.example.bankapi.repository;
import org.example.bankapi.model.PaymentRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;
import java.time.Instant;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
public interface PaymentRequestRepository extends JpaRepository<PaymentRequest, UUID> {
    @Query("select r from PaymentRequest r where r.requester.id = :customerId or r.payer.id = :customerId order by r.createdAt desc")
    List<PaymentRequest> findForCustomer(@Param("customerId") UUID customerId);
    List<PaymentRequest> findByStatusAndExpiresAtBefore(PaymentRequest.Status status, Instant time);
}
