package movie_theater_gr4.project_gr4.employee.service;


import lombok.RequiredArgsConstructor;
import movie_theater_gr4.project_gr4.bookingMember.dto.InvoiceDTO;
import movie_theater_gr4.project_gr4.employee.repository.BookingRepositoryOfEmployee;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookingServiceOfEmployee {

    private final  BookingRepositoryOfEmployee bookingRepositoryOfEmployee;

    public List<InvoiceDTO> getLatestInvoiceByEmployeeId(Integer employeeId) {
        return bookingRepositoryOfEmployee.findLatestInvoiceByEmployeeId(employeeId);
    }

}
