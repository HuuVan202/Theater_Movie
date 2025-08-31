package movie_theater_gr4.project_gr4.bookingMember.service;

import jakarta.transaction.Transactional;
import movie_theater_gr4.project_gr4.model.Employee;
import movie_theater_gr4.project_gr4.model.Invoice;
import movie_theater_gr4.project_gr4.model.Member;
import movie_theater_gr4.project_gr4.model.Seat;
import movie_theater_gr4.project_gr4.bookingMember.repository.InvoiceRepository;
import movie_theater_gr4.project_gr4.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InvoiceService {
    @Autowired
    private InvoiceRepository invoiceRepository;
    @Autowired
    MemberRepository memberRepository;

    @Transactional
    public void setStatusForListSeats(List<Integer> ids) {
        invoiceRepository.setStatusForListSeats(ids);
    }


    @Transactional
    public void createInvoice(Integer accountId,
                              long totalAmount,
                              String paymentMethod,
                              int useScore,
                              int addScore,
                              int status,
                              String movieName,
                              String seatNumber,
                              LocalDateTime bookingDate,
                              Integer employeeId) {  // Thêm employeeId

        invoiceRepository.insertInvoice(
                accountId,
                totalAmount,
                paymentMethod,
                useScore,
                addScore,
                status,
                movieName,
                seatNumber,
                bookingDate,
                employeeId // Truyền employeeId vào đây
        );
    }
    public Employee findByAccountUsername (String username){
        return  invoiceRepository.findByAccountUsername(username);
    }



    public boolean updateInvoiceStatus(Long invoiceId, Integer newStatus) {
        int rowsAffected = invoiceRepository.updateStatusByInvoiceId(newStatus, invoiceId);
        return rowsAffected > 0;
    }

    @Transactional
    public void insertTicket(Long invoiceId,
                             Long scheduleSeatId,
                             Long ticketTypeId,
                             Long price) {
        invoiceRepository.insertTicket(
                invoiceId,
                scheduleSeatId,
                ticketTypeId,
                price
        );
    }


    public void updateInvoiceDetails(Long invoiceId, long amount,Integer useScore, Integer addScore, String orderInfo, LocalDateTime sqlDate , Integer newStatus) {
        int updated = invoiceRepository.updateInvoiceDetails(
                invoiceId,
                amount,
                useScore,
                addScore,
                sqlDate,
                newStatus
        );

        if (updated == 0) {
            throw new RuntimeException("Không tìm thấy hoặc không cập nhật được hóa đơn với ID: " + invoiceId);
        }
    }

    // Lấy tên các ghế từ danh sách schedule_seat_id
    public String getSeatNamesByScheduleSeatIds(List<Integer> scheduleSeatIds) {
        List<Seat> seats = invoiceRepository.findSeatsByScheduleSeatId(scheduleSeatIds);
        return seats.stream()
                .map(Seat-> Seat.getSeatRow()+"-"+Seat.getSeatColumn()) // đảm bảo model Seat có phương thức getSeatName()
                .collect(Collectors.joining(", "));
    }

    public List<Invoice> getListInvoiceById(Integer accountId) {
        return invoiceRepository.findInvoiceByAccountId(accountId);
    }


    public void updateScore(long memberId, int scoreChange) {
        // Lấy thành viên từ DB
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        int currentScore = member.getScore() != null ? member.getScore() : 0;
        int newScore = currentScore + scoreChange;

        // Không cho điểm bị âm
        if (newScore < 0) {
            throw new IllegalArgumentException("Không đủ điểm để trừ!");
        }
        // Gọi repository để cập nhật
        invoiceRepository.updateScore(memberId, scoreChange);
    }



}
