package movie_theater_gr4.project_gr4.employee.service;

import lombok.RequiredArgsConstructor;
import movie_theater_gr4.project_gr4.bookingMember.dto.InvoiceDTO;
import movie_theater_gr4.project_gr4.employee.dto.BookingDTO;
import movie_theater_gr4.project_gr4.employee.dto.MemberDTO;
import movie_theater_gr4.project_gr4.employee.repository.SelectSeatRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SelectSeatService {
    private final SelectSeatRepository selectSeatRepository;

    public Optional<MemberDTO> findByKeyword(String keyword) {
        Optional<MemberDTO> result = selectSeatRepository.findRawAccountAndMember(keyword);

        return result;
    }

    public List<BookingDTO> getListInvoice(List<Long> listScheduleSeatId) {
        return selectSeatRepository.findInvoicesByScheduleSeatIds(listScheduleSeatId);
    }


    public List<BookingDTO> getAllBooking() {
        return selectSeatRepository.getAllBooking();
    }

    public List<BookingDTO> getBookingsFromId(Long startId) {
        return selectSeatRepository.getBookingsFromId(startId);
    }

}
