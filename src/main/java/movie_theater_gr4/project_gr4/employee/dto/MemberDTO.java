package movie_theater_gr4.project_gr4.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

public interface MemberDTO {
    Long getMemberId();       // => AS memberId
    Long getAccountId();      // => AS accountId
    String getUsername();     // => AS username
    String getFullName();     // => AS fullName
    String getEmail();        // => AS email
    String getPhoneNumber();  // => AS phoneNumber
    String getAddress();      // => AS address
    LocalDate getDateOfBirth();  // => AS dateOfBirth
    String getGender();       // => AS gender
    String getIdentityCard(); // => AS identityCard
    LocalDate getRegisterDate(); // => AS registerDate
    Integer getStatus();      // => AS status
    Long getRoleId();         // => AS roleId
    String getImage();        // => AS image
    Boolean getIsGoogle();    // => AS isGoogle
    Integer getScore();       // => AS score
    String getTier();         // => AS tier
}

