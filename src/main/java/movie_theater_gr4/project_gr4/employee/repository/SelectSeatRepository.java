package movie_theater_gr4.project_gr4.employee.repository;

import movie_theater_gr4.project_gr4.bookingMember.dto.InvoiceDTO;
import movie_theater_gr4.project_gr4.employee.dto.BookingDTO;
import movie_theater_gr4.project_gr4.employee.dto.MemberDTO;
import movie_theater_gr4.project_gr4.model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
//import java.util.Optional;

@Repository
public interface SelectSeatRepository  extends JpaRepository<Seat, Long> {

    @Query(value = """
    SELECT 
        m.member_id AS memberId,
        a.account_id AS accountId,
        a.username AS username,
        a.full_name AS fullName,
        a.email AS email,
        a.phone_number AS phoneNumber,
        a.address AS address,
        a.date_of_birth AS dateOfBirth,
        a.gender AS gender,
        a.identity_card AS identityCard,
        a.register_date AS registerDate,
        a.status AS status,
        a.role_id AS roleId,
        a.image AS image,
        a.is_google AS isGoogle,
        m.score AS score,
        m.tier AS tier
    FROM account a
    JOIN member m ON m.account_id = a.account_id
    WHERE a.phone_number = :keyword
       OR a.email = :keyword
       OR a.identity_card = :keyword
    """, nativeQuery = true)
    Optional<MemberDTO> findRawAccountAndMember(@Param("keyword") String keyword);


    @Query(value = """
        SELECT 
            i.invoice_id AS invoiceId,
            i.account_id AS accountId,
            i.booking_date AS bookingDate,
            i.total_amount AS totalAmount,
            i.payment_method AS paymentMethod,
            i.use_score AS useScore,
            i.add_score AS addScore,
            i.status AS status,
            i.movie_name AS movieName,
            i.seat_number  as seatNumber,
            i.employee_id AS employee_Id,
            a.full_name AS fullName,
            a.email AS email,
            a.phone_number AS phoneNumber,
            a.identity_card AS identityCard,
            COUNT(t.ticket_id) AS countTickets
        FROM invoice i
        LEFT JOIN account a ON i.account_id = a.account_id
        LEFT JOIN ticket t ON i.invoice_id = t.invoice_id
        WHERE t.schedule_seat_id IN (:scheduleSeatIds)
        GROUP BY 
            i.invoice_id, i.account_id, i.booking_date, i.total_amount,
            i.payment_method, i.use_score, i.add_score, i.status,
            i.employee_id, a.full_name, a.email, a.phone_number,
            a.identity_card, i.movie_name, i.seat_number
        """, nativeQuery = true)
    List<BookingDTO> findInvoicesByScheduleSeatIds(@Param("scheduleSeatIds") List<Long> scheduleSeatIds);



    @Query(value = """
    SELECT 
        i.invoice_id AS invoiceId,
        i.account_id AS accountId,
        i.booking_date AS bookingDate,
        i.total_amount AS totalAmount,
        i.payment_method AS paymentMethod,
        i.use_score AS useScore,
        i.add_score AS addScore,
        i.status AS status,
        i.movie_name AS movieName,
        i.seat_number AS seatNumber,
        i.employee_id AS employee_Id,
        a.full_name AS fullName,
        a.email AS email,
        a.phone_number AS phoneNumber,
        a.identity_card AS identityCard,
        COUNT(t.ticket_id) AS countTickets
    FROM invoice i
    LEFT JOIN account a ON i.account_id = a.account_id
    LEFT JOIN ticket t ON i.invoice_id = t.invoice_id
    GROUP BY 
        i.invoice_id, i.account_id, i.booking_date, i.total_amount,
        i.payment_method, i.use_score, i.add_score, i.status,
        i.employee_id, a.full_name, a.email, a.phone_number,
        a.identity_card, i.movie_name, i.seat_number
    """, nativeQuery = true)
    List<BookingDTO> getAllBooking();

    @Query(value = """
    SELECT 
        i.invoice_id AS invoiceId,
        i.account_id AS accountId,
        i.booking_date AS bookingDate,
        i.total_amount AS totalAmount,
        i.payment_method AS paymentMethod,
        i.use_score AS useScore,
        i.add_score AS addScore,
        i.status AS status,
        i.movie_name AS movieName,
        i.seat_number AS seatNumber,
        i.employee_id AS employee_Id,
        a.full_name AS fullName,
        a.email AS email,
        a.phone_number AS phoneNumber,
        a.identity_card AS identityCard,
        COUNT(t.ticket_id) AS countTickets
    FROM invoice i
    LEFT JOIN account a ON i.account_id = a.account_id
    LEFT JOIN ticket t ON i.invoice_id = t.invoice_id
    WHERE i.invoice_id >= :startId
    GROUP BY 
        i.invoice_id, i.account_id, i.booking_date, i.total_amount,
        i.payment_method, i.use_score, i.add_score, i.status,
        i.employee_id, a.full_name, a.email, a.phone_number,
        a.identity_card, i.movie_name, i.seat_number
    ORDER BY i.invoice_id
    LIMIT 15
    """, nativeQuery = true)
    List<BookingDTO> getBookingsFromId(@Param("startId") Long startId);

}
