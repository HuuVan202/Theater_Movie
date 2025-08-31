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
@Table(name = "dialogue")
public class Dialogue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dialogue_id")
    private int dialogueId;

    @Column(name = "user1_id")
    private int user1Id;

    @Column(name = "user2_id")
    private int user2Id;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "has_unread_messages", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean hasUnreadMessages;

    @Column(name = "active", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean active = true;

    // Transient field để hiển thị tên khách hàng trong giao diện
    @Transient
    private String customerName;
}