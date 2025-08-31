package movie_theater_gr4.project_gr4.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "message")
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private int messageId;

    @Column(name = "dialogue_id")
    private int dialogueId;

    @Column(name = "sender_id")
    private int senderId;

    @Column(name = "message_content")
    private String messageContent;

    @Column(name = "sent_at")
    private Timestamp sentAt;

    @Column(name = "is_seen")
    private boolean isSeen;
}