package movie_theater_gr4.project_gr4.mapper;

import movie_theater_gr4.project_gr4.dto.SeatTypeDTO;
import movie_theater_gr4.project_gr4.model.SeatType;
import org.springframework.stereotype.Component;

@Component
public class SeatTypeMapper {
    public SeatTypeDTO  toDTO(SeatType seatType){
        if(seatType == null) return null;
        return SeatTypeDTO.builder()
                .seatTypeId(seatType.getSeatTypeId())
                .typeName(seatType.getTypeName())
                .description(seatType.getDescription())
                .build();
    }
}
