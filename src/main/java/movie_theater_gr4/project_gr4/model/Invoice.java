package movie_theater_gr4.project_gr4.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "invoice")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invoice_id")
    private Long invoiceId;

    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "booking_date")
    private LocalDateTime bookingDate;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "use_score")
    private Integer useScore;

    @Column(name = "add_score")
    private Integer addScore;

    @Column(name = "status")
    private Integer status;

    @Column(name = "movie_name")
    private String movieName;

    @Column(name = "seat_number")
    private String seatNumber;

    @Column(name = "employee_id")
    private Integer employeeId;

}



