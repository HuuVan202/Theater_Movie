package movie_theater_gr4.project_gr4.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class DialogueDTO {
    private int dialogueId;
    private String userName;
    private boolean hasUnreadMessages;
    private String lastMessage;
    private Timestamp lastMessageTime;
}