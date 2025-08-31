package movie_theater_gr4.project_gr4.model;

import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoicePromotionId implements Serializable {
    private Long invoice;
    private Integer promotion;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InvoicePromotionId)) return false;
        InvoicePromotionId that = (InvoicePromotionId) o;
        return Objects.equals(invoice, that.invoice) &&
                Objects.equals(promotion, that.promotion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(invoice, promotion);
    }
}
