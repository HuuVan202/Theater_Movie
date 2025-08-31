package movie_theater_gr4.project_gr4.service;

import jakarta.transaction.Transactional;
import movie_theater_gr4.project_gr4.bookingMember.repository.SeatRepository;
import movie_theater_gr4.project_gr4.dto.CinemaRoomDTO;
import movie_theater_gr4.project_gr4.dto.SeatMapDTO;
import movie_theater_gr4.project_gr4.model.CinemaRoom;
import movie_theater_gr4.project_gr4.model.Seat;
import movie_theater_gr4.project_gr4.repository.CinemaRoomRepository;
import movie_theater_gr4.project_gr4.repository.ShowTimeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class CinemaRoomService {

    @Autowired
    private CinemaRoomRepository cinemaRoomRepository;
    @Autowired
    private SeatRepository seatRepository;
    @Autowired
    private ShowTimeRepository showTimeRepository;

    @Transactional
    public CinemaRoom saveCinemaRoom(CinemaRoomDTO cinemaRoomDTO) {
        CinemaRoom cinemaRoom = new CinemaRoom();
        cinemaRoom.setRoomName(cinemaRoomDTO.getRoomName());
        cinemaRoom.setSeatQuantity(cinemaRoomDTO.getSeatMap().calculateActualSeats());
        cinemaRoom.setType(cinemaRoomDTO.getType());
        cinemaRoom.setStatus(cinemaRoomDTO.getStatus());
        return cinemaRoomRepository.save(cinemaRoom);
    }

    public boolean isRoomUsedInShowtime(Long roomId) {
        return showTimeRepository.countByRoomId(roomId) > 0;
    }

    public List<CinemaRoom> findAll() {
        return cinemaRoomRepository.findAll();
    }

    public Map<Long, Integer> getActiveSeatCountByRoom() {
        List<Object[]> seatCounts = cinemaRoomRepository.findActiveSeatCountsByRoom();
        return seatCounts.stream()
                .collect(Collectors.toMap(
                        arr -> {
                            if (arr[0] instanceof Integer) {
                                return ((Integer) arr[0]).longValue();
                            } else if (arr[0] instanceof Long) {
                                return (Long) arr[0];
                            } else if (arr[0] instanceof BigInteger) {
                                return ((BigInteger) arr[0]).longValue();
                            }
                            return Long.valueOf(arr[0].toString());
                        },
                        arr -> {
                            if (arr[1] instanceof Integer) {
                                return (Integer) arr[1];
                            } else if (arr[1] instanceof Long) {
                                return ((Long) arr[1]).intValue();
                            } else if (arr[1] instanceof BigInteger) {
                                return ((BigInteger) arr[1]).intValue();
                            }
                            return Integer.valueOf(arr[1].toString());
                        }
                ));
    }

    public Page<CinemaRoom> findAllPaginated(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return cinemaRoomRepository.findAll(pageable);
    }

    public Optional<CinemaRoom> findById(Long id) {
        return cinemaRoomRepository.findById(id.longValue());
    }

    public CinemaRoomDTO findDTOById(Long id) {
        Optional<CinemaRoom> roomOptional = findById(id);
        if (roomOptional.isEmpty()) return null;

        CinemaRoom room = roomOptional.get();
        CinemaRoomDTO dto = new CinemaRoomDTO();
        dto.setRoomId(room.getRoomId());
        dto.setRoomName(room.getRoomName());
        dto.setSeatQuantity(room.getSeatQuantity());
        dto.setType(room.getType());
        dto.setStatus(room.getStatus());

        // Populate seat map
        List<Seat> seats = seatRepository.findByCinemaRoom(room);
        SeatMapDTO seatMapDTO = new SeatMapDTO();
        int maxRow = seats.stream().mapToInt(Seat::getSeatRow).max().orElse(10);
        int maxCol = seats.stream().map(Seat::getSeatColumn)
                .mapToInt(col -> col.charAt(0) - 'A' + 1).max().orElse(10);
        String[][] seatTypes = new String[maxRow][maxCol];
        for (String[] row : seatTypes) {
            Arrays.fill(row, "Empty");
        }
        // Tính toán cột cầu thang
        String staircaseColumns = seats.stream()
                .filter(seat -> "Staircase".equals(seat.getSeatType().getTypeName()))
                .map(seat -> String.valueOf(seat.getSeatColumn().charAt(0) - 'A' + 1))
                .distinct()
                .sorted()
                .collect(Collectors.joining(","));
        seatMapDTO.setStaircaseColumns(staircaseColumns.isEmpty() ? null : staircaseColumns);

        // Gán loại ghế
        for (Seat seat : seats) {
            int row = seat.getSeatRow() - 1;
            int col = seat.getSeatColumn().charAt(0) - 'A';
            seatTypes[row][col] = seat.getSeatType().getTypeName();
        }
        seatMapDTO.setRows(maxRow);
        seatMapDTO.setCols(maxCol);
        seatMapDTO.setSeatTypes(seatTypes);
        dto.setSeatMap(seatMapDTO);

        // Populate seat prices
        Map<String, Double> seatPrices = new HashMap<>();
        Map<String, List<Seat>> seatsByType = seats.stream()
                .filter(seat -> !"Staircase".equals(seat.getSeatType().getTypeName()) && !"Empty".equals(seat.getSeatType().getTypeName()))
                .collect(Collectors.groupingBy(seat -> seat.getSeatType().getTypeName().toLowerCase()));
        for (Map.Entry<String, List<Seat>> entry : seatsByType.entrySet()) {
            String type = entry.getKey();
            Double price = entry.getValue().stream()
                    .map(Seat::getSeatPrice)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(0.0); // Giá mặc định nếu không có
            seatPrices.put(type, price);
        }
        dto.setSeatPrices(seatPrices);

        return dto;
    }

    public List<String> getRoomTypes() {
        return Arrays.asList("2D", "3D", "IMAX", "4DX");
    }

    public void update(CinemaRoomDTO cinemaRoomDTO) {
        findById(cinemaRoomDTO.getRoomId()).ifPresent(room -> {
            room.setRoomName(cinemaRoomDTO.getRoomName());
            room.setType(cinemaRoomDTO.getType());
            room.setStatus(cinemaRoomDTO.getStatus());
            room.setSeatQuantity(cinemaRoomDTO.getSeatMap().calculateActualSeats());
            cinemaRoomRepository.save(room);
        });
    }

    public void delete(Long id) {
        cinemaRoomRepository.deleteById(id);
    }

    public void save(CinemaRoomDTO cinemaRoomDTO) {
        saveCinemaRoom(cinemaRoomDTO);
    }

    public void updateStatus(Long id, int status) {
        findById(id).ifPresent(room -> {
            room.setStatus(status);
            cinemaRoomRepository.save(room);
        });
    }
}