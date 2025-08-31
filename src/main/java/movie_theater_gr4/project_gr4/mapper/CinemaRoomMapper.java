package movie_theater_gr4.project_gr4.mapper;

import movie_theater_gr4.project_gr4.dto.CinemaRoomDTO;
import movie_theater_gr4.project_gr4.model.CinemaRoom;
import org.springframework.stereotype.Component;

@Component
public class CinemaRoomMapper {
    public CinemaRoomDTO toDTO(CinemaRoom cinemaRoom){
        if(cinemaRoom == null) return null;
        return CinemaRoomDTO.builder()
                .roomId(cinemaRoom.getRoomId())
                .roomName(cinemaRoom.getRoomName())
                .seatQuantity(cinemaRoom.getSeatQuantity())
                .build();
    }
}
