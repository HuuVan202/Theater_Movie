package movie_theater_gr4.project_gr4.service;

import movie_theater_gr4.project_gr4.model.Dialogue;
import movie_theater_gr4.project_gr4.repository.DialogueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DialogueService {

    @Autowired
    private DialogueRepository dialogueRepository;

    public Dialogue getOrCreateDialogue(int user1Id, int user2Id) {
        return dialogueRepository.findByUserIds(user1Id, user2Id)
                .orElseGet(() -> {
                    Dialogue dialogue = new Dialogue();
                    dialogue.setUser1Id(user1Id);
                    dialogue.setUser2Id(user2Id);
                    dialogue.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
                    dialogue.setActive(true);
                    return dialogueRepository.save(dialogue);
                });
    }

    public List<Dialogue> getDialoguesByUserId(int userId) {
        return dialogueRepository.findByUserId(userId);
    }

    public List<Dialogue> getActiveDialoguesByUserId(int userId) {
        return dialogueRepository.findActiveDialoguesByUserId(userId);
    }

    public Dialogue getDialogueById(int dialogueId) {
        return dialogueRepository.findById(dialogueId)
                .orElseThrow(() -> new IllegalArgumentException("Dialogue not found"));
    }

    public void deactivateDialogue(int dialogueId) {
        Dialogue dialogue = getDialogueById(dialogueId);
        dialogue.setActive(false);
        dialogueRepository.save(dialogue);
    }

    public void activateDialogue(int dialogueId) {
        Dialogue dialogue = getDialogueById(dialogueId);
        if (!dialogue.isActive()) {
            dialogue.setActive(true);
            dialogueRepository.save(dialogue);
        }
    }

    public List<Dialogue> getInactiveDialoguesByUserId(int userId) {
        return dialogueRepository.findInactiveDialoguesByUserId(userId);
    }
}