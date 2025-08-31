package movie_theater_gr4.project_gr4.bookingMember.repository;


import movie_theater_gr4.project_gr4.bookingMember.dto.InvoiceDTO;
import movie_theater_gr4.project_gr4.bookingMember.dto.InvoiceDetailsDTO;
import movie_theater_gr4.project_gr4.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingListRepository  extends JpaRepository<Invoice, Long> {

    @Query(value = "SELECT i.* FROM invoice i " +
            "JOIN account a ON a.account_id = i.account_id " +
            "WHERE a.username = :keyword OR a.email = :keyword " +
            "ORDER BY i.invoice_id DESC",
            nativeQuery = true)
    List<InvoiceDTO> getAllInvoiceByUsernameOrEmail(@Param("keyword") String keyword);

    @Query(value = "SELECT i.*, sd.show_date AS showDate," +
            "       sch.schedule_time AS scheduleTime " +
            "FROM invoice i " +
            "JOIN account a ON a.account_id = i.account_id " +
            "JOIN ticket t ON t.invoice_id = i.invoice_id " +
            "JOIN schedule_seat ss ON ss.schedule_seat_id = t.schedule_seat_id " +
            "JOIN showtime st ON st.showtime_id = ss.showtime_id " +
            "JOIN show_dates sd ON sd.show_date_id = st.show_date_id " +
            "JOIN schedule sch ON sch.schedule_id = st.schedule_id " +
            "WHERE a.username = :keyword OR a.email = :keyword " +
            "ORDER BY i.invoice_id DESC",
            nativeQuery = true)
    List<InvoiceDetailsDTO> getFullInvoiceByUsernameOrEmail(@Param("keyword") String keyword);



    @Query(value = "SELECT * FROM invoice WHERE invoice_id = :invoiceId", nativeQuery = true)
    InvoiceDTO getInvoiceById(@Param("invoiceId") Long invoiceId);





}
