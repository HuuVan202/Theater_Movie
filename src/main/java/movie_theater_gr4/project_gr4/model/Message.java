package movie_theater_gr4.project_gr4.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message {
    private Long messageId;
    private int senderId;
    private int receiverId;
    private String messageContent;
    private LocalDateTime sentAt;
    private boolean isSeen;
}