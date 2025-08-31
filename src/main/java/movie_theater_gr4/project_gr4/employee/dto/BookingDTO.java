package movie_theater_gr4.project_gr4.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingDTO {
    private Long invoiceId;

    private Long accountId;

    private Timestamp bookingDate;

    private BigDecimal totalAmount;

    private String paymentMethod;

    private Integer useScore;

    private Integer addScore;

    private Integer status;

    private String movieName;

    private String seatNumber;

    private Long employee_Id;

    private String fullName;

    private String email;

    private String phoneNumber;

    private String identityCard;

    private Long CountTickets;

}
