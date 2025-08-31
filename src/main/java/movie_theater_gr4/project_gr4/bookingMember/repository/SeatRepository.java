package movie_theater_gr4.project_gr4.bookingMember.repository;

import jakarta.transaction.Transactional;
import movie_theater_gr4.project_gr4.dto.MemberDTO;
import movie_theater_gr4.project_gr4.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    void deleteByCinemaRoom(CinemaRoom cinemaRoom);
    List<Seat> findByCinemaRoom(CinemaRoom cinemaRoom);
    Optional<Seat> findByCinemaRoomAndSeatRowAndSeatColumn(CinemaRoom cinemaRoom, Integer seatRow, String seatColumn);

    @Query(value = """

             SELECT\s
                s.seat_id,
                s.seat_row,
                s.seat_column,
                s.is_active,
                st.seat_type_id,
                st.type_name,
                cr.room_id,
                cr.room_name,
                cr.seat_quantity,
                cr.type,
                cr.status,
                ss.schedule_seat_id,
                s.seat_price,
                ss.status,
                sh.showtime_id,
                 CASE WHEN ss.status = 0 THEN  false ELSE true END AS is_booked
            FROM public.showtime sh
            JOIN public.cinema_room cr ON sh.room_id = cr.room_id
            JOIN public.seat s ON s.room_id = cr.room_id
            LEFT JOIN public.schedule_seat ss\s
                   ON ss.showtime_id = sh.showtime_id AND ss.seat_id = s.seat_id
            JOIN public.seat_type st ON s.seat_type_id = st.seat_type_id
            JOIN public.movie m ON m.movie_id = sh.movie_id
            WHERE sh.showtime_id = :scheduleId
              AND sh.movie_id = :movieId
              AND sh.version_id = :versionId
           

            
            """, nativeQuery = true)
    List<Object[]> findSeatSelectionInfo(@Param("scheduleId") Long scheduleId,
                                         @Param("movieId") Long movieId,
                                         @Param("versionId") Long versionId);

    @Query(value = """
    SELECT
        s.seat_id,
        s.seat_row,
        s.seat_column,
        s.is_active,
        st.seat_type_id,
        st.type_name,
        cr.room_id,
        cr.room_name,
        cr.seat_quantity,
        cr.type,
        cr.status AS room_status,
        ss.schedule_seat_id,
        ss.seat_price,
        ss.status AS seat_status,
        sh.showtime_id,
        CASE WHEN ss.status = 0 THEN false ELSE true END AS is_booked
    FROM public.showtime sh
    JOIN public.cinema_room cr ON sh.room_id = cr.room_id
    JOIN public.seat s ON s.room_id = cr.room_id
    LEFT JOIN public.schedule_seat ss
        ON ss.showtime_id = sh.showtime_id AND ss.seat_id = s.seat_id
    JOIN public.seat_type st ON s.seat_type_id = st.seat_type_id
    JOIN public.movie m ON m.movie_id = sh.movie_id
    WHERE sh.schedule_id = :scheduleId
      AND ss.schedule_seat_id IN (:scheduleSeatIds)
    """, nativeQuery = true)
    List<Object[]> findSeatSelectionInfo(
            @Param("scheduleId") Long scheduleId,
            @Param("scheduleSeatIds") List<Long> scheduleSeatIds);

    @Query(value = "SELECT schedule_seat_id, showtime_id, seat_id, seat_price, status " +
            "FROM public.schedule_seat WHERE schedule_seat_id = :schedule_seat_id", nativeQuery = true)
    ScheduleSeat findByScheduleSeatIds(@Param("schedule_seat_id") Long scheduleSeatIds);


    @Query("SELECT new movie_theater_gr4.project_gr4.dto.MemberDTO(" +
            "m.memberId, a.accountId, a.username, a.fullName, a.email, m.score, m.tier) " +
            "FROM Member m JOIN m.account a " +
            "WHERE a.username = :username")
    MemberDTO findMemberByUsername(@Param("username") String username);


    @Query("SELECT new movie_theater_gr4.project_gr4.dto.MemberDTO(" +
            "m.memberId, a.accountId, a.username, a.fullName, a.email, m.score, m.tier) " +
            "FROM Member m JOIN m.account a " +
            "WHERE m.memberId = :memberId")
    MemberDTO findMemberByMemberId(@Param("memberId") String memberId);


    @Query(value = """ 
            SELECT m.movie_name , v.version_name, mar.*, sd.*, s2.*, cr.*
            FROM public.movie m
            JOIN public.showtime s ON s.movie_id = m.movie_id
            JOIN public.version v ON v.version_id = s.version_id
            JOIN public.movie_age_rating mar ON m.rating_code = mar.rating_code
            JOIN public.show_dates sd ON s.show_date_id = sd.show_date_id
            JOIN public.schedule s2 ON s.schedule_id = s2.schedule_id
            JOIN public.cinema_room cr ON s.room_id = cr.room_id
            WHERE s.showtime_id = :showtimeId;
            
            """, nativeQuery = true)
    List<Object[]> getMovieInfoOfSelectSeatByShowtimeId(@Param("showtimeId") long showtimeId);

    @Query(value = """
            SELECT DISTINCT s.* FROM seat s
            JOIN cinema_room cr ON s.room_id = cr.room_id
            JOIN showtime sh ON sh.room_id = cr.room_id
            WHERE sh.schedule_id = :scheduleId
            AND sh.movie_id = :movieId
            AND sh.version_id = :versionId
            ORDER BY s.seat_row, s.seat_column
            """, nativeQuery = true)
    List<Seat> findSeatsByScheduleMovieVersion(
            @Param("scheduleId") Long scheduleId,
            @Param("movieId") Long movieId,
            @Param("versionId") Long versionId);


    @Query(value = """
    select * from ticket_type tt where tt.ticket_type_id = :ticket_type_id""", nativeQuery = true)
    TicketType getTicketType(@Param("ticket_type_id") long ticket_type_id);

    @Query(value = "SELECT * FROM promotion", nativeQuery = true)
    List<Promotion> findAllPromotions();



    @Modifying
    @Transactional
    @Query("UPDATE ScheduleSeat s SET s.status = :status WHERE s.scheduleSeatId IN :scheduleSeatId")
    int updateStatusByscheduleSeatIds(@Param("status") Integer status, @Param("scheduleSeatId") List<Long> scheduleSeatId);

    }
