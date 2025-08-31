package movie_theater_gr4.project_gr4.repository;

import movie_theater_gr4.project_gr4.model.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Chat, Integer> {
    List<Chat> findByDialogueIdOrderBySentAtAsc(int dialogueId);

    List<Chat> findByDialogueIdAndSenderIdAndIsSeenFalse(int dialogueId, int senderId);
}