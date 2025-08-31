package movie_theater_gr4.project_gr4.bookingMember.repository;

import jakarta.transaction.Transactional;
import movie_theater_gr4.project_gr4.model.Employee;
import movie_theater_gr4.project_gr4.model.Invoice;
import movie_theater_gr4.project_gr4.model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    @Modifying
    @Query(value = """
    UPDATE schedule_seat SET status = 2 WHERE schedule_seat_id IN (:ids)
""", nativeQuery = true)
    void setStatusForListSeats(@Param("ids") List<Integer> ids);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO invoice (account_id, total_amount, payment_method, use_score, add_score, status, movie_name, seat_number, booking_date, employee_id) " +
            "VALUES (:accountId, :totalAmount, :paymentMethod, :useScore, :addScore, :status, :movieName, :seatNumber, :bookingDate, :employeeId)", nativeQuery = true)
    void insertInvoice(@Param("accountId") Integer accountId,
                       @Param("totalAmount") long totalAmount,
                       @Param("paymentMethod") String paymentMethod,
                       @Param("useScore") int useScore,
                       @Param("addScore") int addScore,
                       @Param("status") int status,
                       @Param("movieName") String movieName,
                       @Param("seatNumber") String seatNumber,
                       @Param("bookingDate") LocalDateTime bookingDate,
                       @Param("employeeId") Integer employeeId);


    @Query(value = """
    SELECT s.*
    FROM public.schedule_seat ss
    JOIN public.seat s ON ss.seat_id = s.seat_id
    WHERE ss.schedule_seat_id IN (:scheduleSeatIds)
    """, nativeQuery = true)
    List<Seat> findSeatsByScheduleSeatId(@Param("scheduleSeatIds") List<Integer> scheduleSeatIds);

    @Modifying
    @Transactional
    @Query("UPDATE Member m SET m.score = m.score + :score WHERE m.memberId = :id")
    void updateScore(@Param("id") long memberId, @Param("score") int score);

    @Query(value = """
            select i.* from invoice i  where i.account_id  = :account_id
            """,nativeQuery = true)
    List<Invoice> findInvoiceByAccountId(@Param("account_id") Integer account_id);

    @Query(value = "SELECT e.* FROM account a " +
            "JOIN employee e ON e.account_id = a.account_id " +
            "WHERE a.username = :username", nativeQuery = true)
    Employee findByAccountUsername(@Param("username") String username);


    @Modifying
    @Transactional
    @Query(value = "INSERT INTO ticket (invoice_id, schedule_seat_id, ticket_type_id, price) " +
            "VALUES (:invoiceId, :scheduleSeatId, :ticketTypeId, :price)", nativeQuery = true)
    void insertTicket(@Param("invoiceId") Long invoiceId,
                      @Param("scheduleSeatId") Long scheduleSeatId,
                      @Param("ticketTypeId") Long ticketTypeId,
                      @Param("price") Long price);

    @Transactional
    @Modifying
    @Query("UPDATE Invoice i SET i.status = :status WHERE i.invoiceId = :invoiceId")
    int updateStatusByInvoiceId(@Param("status") Integer status, @Param("invoiceId") Long invoiceId);

    @Transactional
    @Modifying
    @Query("UPDATE Invoice i SET i.totalAmount = :amount,i.useScore = :useScore,  i.addScore = :addScore, i.bookingDate = :bookingDate, i.status = :status " +
            "WHERE i.invoiceId = :invoiceId")
    int updateInvoiceDetails(@Param("invoiceId") Long invoiceId,
                             @Param("amount") long amount,
                             @Param("useScore") Integer useScore,
                             @Param("addScore") Integer addScore,
                             @Param("bookingDate") LocalDateTime bookingDate,
                             @Param("status") Integer status);

}
