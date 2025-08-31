package movie_theater_gr4.project_gr4.bookingMember.repository;

import movie_theater_gr4.project_gr4.model.InvoicePromotion;
import movie_theater_gr4.project_gr4.model.InvoicePromotionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoicePromotionRepository extends JpaRepository<InvoicePromotion, InvoicePromotionId> {
    @Query(value = """
        SELECT ip.*
        FROM invoice i
        JOIN account a ON i.account_id = a.account_id
        JOIN invoice_promotion ip ON ip.invoice_id = i.invoice_id
        WHERE a.username = :username
    """, nativeQuery = true)
    List<InvoicePromotion> findInvoicePromotionAllByUsername(@Param("username") String username);
}
