package movie_theater_gr4.project_gr4.employee.repository;


import movie_theater_gr4.project_gr4.bookingMember.dto.InvoiceDTO;
import movie_theater_gr4.project_gr4.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;



@Repository
public interface BookingRepositoryOfEmployee extends JpaRepository<Invoice, Long> {

    @Query(value = "SELECT i.* FROM invoice i WHERE i.employee_id = :employeeId ORDER BY i.invoice_id DESC limit 1", nativeQuery = true)
    List<InvoiceDTO> findLatestInvoiceByEmployeeId(@Param("employeeId") Integer employeeId);

}
