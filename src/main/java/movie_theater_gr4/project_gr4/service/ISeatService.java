package movie_theater_gr4.project_gr4.service;

import movie_theater_gr4.project_gr4.dto.SeatDTO;
import movie_theater_gr4.project_gr4.dto.SeatMapDTO;
import movie_theater_gr4.project_gr4.model.CinemaRoom;
import movie_theater_gr4.project_gr4.model.Seat;

import java.util.List;
import java.util.Map;

public interface ISeatService {
    void saveSeats(CinemaRoom cinemaRoom, SeatMapDTO seatMapDTO, Map<String, Double> seatPrices);
    void updateSeats(CinemaRoom room, SeatMapDTO seatMap, Map<String, Double> seatPrices);
    List<SeatDTO> getSeatsByScheduleMovieVersion(Long scheduleId, Long movieId, Long versionId);
}