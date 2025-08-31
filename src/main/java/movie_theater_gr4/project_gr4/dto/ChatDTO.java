package movie_theater_gr4.project_gr4.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatDTO {
    private int messageId;
    private int senderId;
    private String senderName;
    private int receiverId;
    private String messageContent;
    private Timestamp sentAt;
    private boolean senderDeleted;
    private boolean receiverDeleted;
    private int dialogueId;
    private boolean isSeen;
    private String tempId;
}