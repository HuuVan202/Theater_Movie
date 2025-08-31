package movie_theater_gr4.project_gr4.model;

import lombok.*;

import java.util.concurrent.ConcurrentHashMap;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class OTPData {
    private String email;
    private String otp;
    private long timestamp;

    @Getter
    private static final ConcurrentHashMap<String, OTPData> otpStore = new ConcurrentHashMap<>();

}

