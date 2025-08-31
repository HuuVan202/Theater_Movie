package movie_theater_gr4.project_gr4.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "invoice_promotion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(InvoicePromotionId.class) // Sử dụng composite key
public class InvoicePromotion {

    @Id
    @ManyToOne
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Id
    @ManyToOne
    @JoinColumn(name = "promotion_id", nullable = false)
    private Promotion promotion;
}
