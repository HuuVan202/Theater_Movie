package movie_theater_gr4.project_gr4.service;

import movie_theater_gr4.project_gr4.dto.ChatDTO;
import movie_theater_gr4.project_gr4.model.Account;
import movie_theater_gr4.project_gr4.model.Chat;
import movie_theater_gr4.project_gr4.model.Dialogue;
import movie_theater_gr4.project_gr4.repository.AccountRepository;
import movie_theater_gr4.project_gr4.repository.DialogueRepository;
import movie_theater_gr4.project_gr4.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ChatService {
    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private DialogueRepository dialogueRepository;

    @Autowired
    private AccountRepository accountRepository;

    public String getCustomerNameByDialogue(int dialogueId) {
        Dialogue dialogue = dialogueRepository.findById(dialogueId)
                .orElseThrow(() -> new IllegalArgumentException("Dialogue not found"));
        Account customer = accountRepository.findById(dialogue.getUser1Id())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        return customer.getFullName();
    }

    public int getCustomerIdByDialogue(int dialogueId) {
        Dialogue dialogue = dialogueRepository.findById(dialogueId)
                .orElseThrow(() -> new IllegalArgumentException("Dialogue not found"));
        return dialogue.getUser1Id();
    }

    public boolean isValidDialogue(int dialogueId) {
        return dialogueRepository.existsById(dialogueId);
    }

//    public Integer getOrCreateDialogue(int user1Id, int user2Id) {
//        return dialogueRepository.findByUser1IdAndUser2Id(user1Id, user2Id)
//                .map(Dialogue::getDialogueId)
//                .orElseGet(() -> {
//                    Dialogue dialogue = new Dialogue();
//                    dialogue.setUser1Id(user1Id);
//                    dialogue.setUser2Id(user2Id);
//                    dialogue.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
//                    return dialogueRepository.save(dialogue).getDialogueId();
//                });
//    }

//    public List<ChatDTO> getChatsByDialogueId(int dialogueId) {
//        return messageRepository.findByDialogueId(dialogueId)
//                .stream()
//                .map(message -> {
//                    ChatDTO chatDTO = new ChatDTO();
//                    chatDTO.setMessageId(message.getMessageId());
//                    chatDTO.setDialogueId(message.getDialogueId());
//                    chatDTO.setSenderId(message.getSenderId());
//                    chatDTO.setSenderName(accountRepository.findById(message.getSenderId())
//                            .map(Account::getFullName)
//                            .orElse("Unknown"));
//                    chatDTO.setMessageContent(message.getMessageContent());
//                    chatDTO.setSentAt(message.getSentAt());
//                    chatDTO.setSeen(message.isSeen());
//                    return chatDTO;
//                })
//                .collect(Collectors.toList());
//    }

    public ChatDTO sendMessage(Chat chat) {
        chat.setSentAt(Timestamp.valueOf(LocalDateTime.now()));
        messageRepository.save(chat);
        ChatDTO chatDTO = new ChatDTO();
        chatDTO.setMessageId(chat.getMessageId());
        chatDTO.setDialogueId(chat.getDialogueId());
        chatDTO.setSenderId(chat.getSenderId());
        chatDTO.setSenderName(accountRepository.findById(chat.getSenderId())
                .map(Account::getFullName)
                .orElse("Unknown"));
        chatDTO.setMessageContent(chat.getMessageContent());
        chatDTO.setSentAt(chat.getSentAt());
        chatDTO.setSeen(false);
        return chatDTO;
    }

    public boolean updateSeenStatus(int dialogueId, int senderId, int receiverId) {
        List<Chat> messages = messageRepository.findByDialogueIdAndSenderIdAndIsSeenFalse(dialogueId, senderId);
        if (messages.isEmpty()) {
            return false;
        }
        messages.forEach(message -> {
            message.setSeen(true);
            messageRepository.save(message);
        });
        return true;
    }
}