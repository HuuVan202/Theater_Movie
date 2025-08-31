package movie_theater_gr4.project_gr4.service;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import movie_theater_gr4.project_gr4.model.SeatType;
import movie_theater_gr4.project_gr4.repository.SeatTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SeatTypeService {
    @Autowired
    private SeatTypeRepository seatTypeRepository;

    @PostConstruct
    public void initDefaultSeatTypes() {
        // Danh sách các loại ghế mặc định cần tạo
        List<String[]> defaultSeatTypes = Arrays.asList(
                new String[]{"Normal", "Standard seat with basic comfort"},
                new String[]{"VIP", "Premium seat with extra legroom"},
                new String[]{"Couple", "Double seat for couples"},
                new String[]{"Empty", "Empty space, no seat available"},
                new String[]{"Staircase", "Staircase or aisle for movement"}
        );

        // Kiểm tra và tạo từng loại ghế nếu chưa tồn tại
        for (String[] seatTypeData : defaultSeatTypes) {
            String typeName = seatTypeData[0];
            String description = seatTypeData[1];

            Optional<SeatType> existingType = seatTypeRepository.findByTypeName(typeName);
            if (existingType.isEmpty()) {
                SeatType seatType = new SeatType();
                seatType.setTypeName(typeName);
                seatType.setDescription(description);
                seatTypeRepository.save(seatType);
                System.out.println("Created seat type: " + typeName);
            }
        }
    }

    public SeatType findOrCreateSeatType(String typeName, String description) {
        return seatTypeRepository.findByTypeName(typeName)
                .orElseGet(() -> {
                    SeatType seatType = new SeatType();
                    seatType.setTypeName(typeName);
                    seatType.setDescription(description);
                    return seatTypeRepository.save(seatType);
                });
    }
}
