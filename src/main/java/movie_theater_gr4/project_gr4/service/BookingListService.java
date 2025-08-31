package movie_theater_gr4.project_gr4.service;

import jakarta.transaction.Transactional;
import movie_theater_gr4.project_gr4.bookingMember.dto.InvoiceDTO;
import movie_theater_gr4.project_gr4.bookingMember.dto.InvoiceDetailsDTO;
import movie_theater_gr4.project_gr4.bookingMember.repository.BookingListRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class BookingListService {
    @Autowired
    private BookingListRepository bookingListRepository;

    public List<InvoiceDTO> getAllInvoiceByKeyword(String keyword) {
        return bookingListRepository.getAllInvoiceByUsernameOrEmail(keyword);
    }

    public List<InvoiceDetailsDTO> getFullInvoiceByKeyword(String keyword) {
        return bookingListRepository.getFullInvoiceByUsernameOrEmail(keyword);
    }

    public InvoiceDTO getInvoiceById( Long id) {
        return bookingListRepository.getInvoiceById(id);
    }
}
