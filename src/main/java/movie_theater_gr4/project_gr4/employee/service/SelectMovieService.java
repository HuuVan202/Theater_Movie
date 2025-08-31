package movie_theater_gr4.project_gr4.employee.service;

import lombok.RequiredArgsConstructor;
import movie_theater_gr4.project_gr4.bookingMember.dto.InvoiceDTO;
import movie_theater_gr4.project_gr4.bookingMember.dto.SeatSelectionInfoDTO;
import movie_theater_gr4.project_gr4.employee.dto.BookingInfoDTO;
import movie_theater_gr4.project_gr4.employee.dto.SelectMovieDTO;
import movie_theater_gr4.project_gr4.employee.dto.ShowtimeDTO;
import movie_theater_gr4.project_gr4.employee.dto.VersionDTO;
import movie_theater_gr4.project_gr4.employee.repository.SelectMovieRepository;
import movie_theater_gr4.project_gr4.employee.repository.SelectSeatRepository;
import movie_theater_gr4.project_gr4.model.MovieAgeRating;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SelectMovieService {

    private final SelectMovieRepository selectMovieRepository;

    public List<SelectMovieDTO> getAllMoviesWithShowtimes() {
        List<Object[]> rows = selectMovieRepository.getAllShowtimeMovieData();
        return mapToDTO(rows);
    }
    public List<SelectMovieDTO> getInfoShowtimeByMovieIdAndShowtimeId(Long movie_id, Long showtime_id) {
        List<Object[]> rows = selectMovieRepository.getInfoShowtimeByMovieIdAndShowtimeId( movie_id,  showtime_id);
        return mapToDTO(rows);
    }
    private List<SelectMovieDTO> mapToDTO(List<Object[]> rows) {
        Map<Long, SelectMovieDTO> movieMap = new LinkedHashMap<>();

        for (Object[] row : rows) {
            Long movieId = getLong(row[1]);
            Long versionId = getLong(row[5]);

            SelectMovieDTO movie = movieMap.get(movieId);
            if (movie == null) {
                movie = SelectMovieDTO.builder()
                        .movieId(movieId)
                        .movieName((String) row[10])
                        .movieNameEn((String) row[11])
                        .movieNameVn((String) row[12])
                        .director((String) row[13])
                        .actor((String) row[14])
                        .content((String) row[15])
                        .duration(getInt(row[16]))
                        .productionCompany((String) row[17])
                        .ratingCode((String) row[18])
                        .fromDate(toLocalDate(row[19]))
                        .toDate(toLocalDate(row[20]))
                        .largeImageUrl((String) row[21])
                        .smallImageUrl((String) row[22])
                        .trailerUrl((String) row[23])
                        .movieAgeRating(MovieAgeRating.builder()
                                .ratingName((String) row[24])
                                .description((String) row[25])
                                .build())
                        .versions(new ArrayList<>())
                        .showtimes(new ArrayList<>())
                        .build();
                movieMap.put(movieId, movie);
            }

            movie.getShowtimes().add(ShowtimeDTO.builder()
                    .showtimeId(getLong(row[0]))
                    .roomId(getLong(row[4]))
                    .roomName((String) row[27])
                    .showDate(toLocalDate(row[9]))
                    .scheduleTime(toLocalTime(row[26]))
                    .availableSeats(getInt(row[6]))
                    .build());

            boolean hasVersion = movie.getVersions().stream()
                    .anyMatch(v -> v.getVersionId().equals(versionId));
            if (!hasVersion && versionId != null) {
                movie.getVersions().add(VersionDTO.builder()
                        .versionId(versionId)
                        .versionName((String) row[7])
                        .description((String) row[8])
                        .build());
            }
        }

        return new ArrayList<>(movieMap.values());
    }

    private Long getLong(Object obj) {
        return obj != null ? ((Number) obj).longValue() : null;
    }

    private Integer getInt(Object obj) {
        return obj != null ? ((Number) obj).intValue() : null;
    }

    private LocalDate toLocalDate(Object obj) {
        return obj != null ? ((Date) obj).toLocalDate() : null;
    }

    private LocalTime toLocalTime(Object obj) {
        return obj != null ? ((Time) obj).toLocalTime() : null;
    }

    public List<SeatSelectionInfoDTO> getSeatsByShowtime(Long showtime_id, Long movieId) {
        List<Object[]> rows = selectMovieRepository.findSeatSelectionInfo(showtime_id, movieId);

        return rows.stream().map(r -> new SeatSelectionInfoDTO(
                (Number) r[0],        // seat_id
                (Integer) r[1],       // seat_row
                (String) r[2],        // seat_column
                (Boolean) r[3],       // is_active

                (Number) r[4],        // seat_type_id
                (String) r[5],        // seat_type_name

                (Number) r[6],        // room_id
                (String) r[7],        // room_name
                (Integer) r[8],       // seat_quantity
                (String) r[9],        // room_type
                (Integer) r[10],      // room_status

                r[11] != null ? (Number) r[11] : null,   // schedule_seat_id (nullable)
                r[12] != null ? toBigDecimal(r[12]) : null,   // seat_price
                (Integer) r[13],      // booking_status
                (Long)r[14],
                (Boolean) r[15]       // is_booked
        )).collect(Collectors.toList());
    }
    private BigDecimal toBigDecimal(Object val) {
        if (val instanceof BigDecimal) {
            return (BigDecimal) val;
        } else if (val instanceof Double) {
            return BigDecimal.valueOf((Double) val);
        } else if (val instanceof Number) {
            return BigDecimal.valueOf(((Number) val).doubleValue());
        } else if (val instanceof String) {
            return new BigDecimal((String) val);
        } else {
            return null; // hoặc throw new IllegalArgumentException(...)
        }
    }
    public List<BookingInfoDTO> findBookingInfoByInvoiceId(Long invoiceId) {
        return selectMovieRepository.findBookingInfoByInvoiceId(invoiceId);
    }

}
