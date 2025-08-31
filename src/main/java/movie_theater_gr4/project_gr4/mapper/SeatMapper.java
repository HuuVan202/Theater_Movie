package movie_theater_gr4.project_gr4.mapper;


import movie_theater_gr4.project_gr4.dto.SeatDTO;
import movie_theater_gr4.project_gr4.model.Seat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SeatMapper {
    @Autowired
    private SeatTypeMapper seatTypeMapper;
    @Autowired
    private CinemaRoomMapper cinemaRoomMapper;

    public SeatDTO toDTO(Seat seat){

       return SeatDTO.builder()
                .seatId(seat.getSeatId())
                .seatRow(seat.getSeatRow())
                .seatColumn(seat.getSeatColumn())
                .seatType(seatTypeMapper.toDTO(seat.getSeatType()))
                .room(cinemaRoomMapper.toDTO(seat.getCinemaRoom()))
                .build();
    }

}
