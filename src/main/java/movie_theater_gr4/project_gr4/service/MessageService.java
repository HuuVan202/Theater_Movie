package movie_theater_gr4.project_gr4.service;

import movie_theater_gr4.project_gr4.dto.ChatDTO;
import movie_theater_gr4.project_gr4.model.Account;
import movie_theater_gr4.project_gr4.model.Chat;
import movie_theater_gr4.project_gr4.repository.AccountRepository;
import movie_theater_gr4.project_gr4.repository.ChatRepository;
import movie_theater_gr4.project_gr4.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ChatRepository chatRepository;

    public List<ChatDTO> getChatsByDialogueId(int dialogueId) {
        return messageRepository.findByDialogueIdOrderBySentAtAsc(dialogueId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public boolean hasUnreadMessages(int dialogueId) {
        List<Chat> chats = messageRepository.findByDialogueIdOrderBySentAtAsc(dialogueId);
        return chats.stream().anyMatch(chat -> !chat.isSeen());
    }

    public ChatDTO sendMessage(Chat chat) {
        if (chat.getSentAt() == null) {
            chat.setSentAt(Timestamp.valueOf(LocalDateTime.now()));
        }
        Chat savedChat = chatRepository.save(chat);
        ChatDTO chatDTO = new ChatDTO();
        chatDTO.setMessageId(savedChat.getMessageId());
        chatDTO.setDialogueId(savedChat.getDialogueId());
        chatDTO.setSenderId(savedChat.getSenderId());
        chatDTO.setMessageContent(savedChat.getMessageContent());
        chatDTO.setSentAt(savedChat.getSentAt());
        chatDTO.setSeen(savedChat.isSeen());
        return chatDTO;
    }

    private ChatDTO convertToDTO(Chat chat) {
        ChatDTO chatDTO = new ChatDTO();
        chatDTO.setMessageId(chat.getMessageId());
        chatDTO.setDialogueId(chat.getDialogueId());
        chatDTO.setSenderId(chat.getSenderId());
        chatDTO.setSenderName(accountRepository.findById(chat.getSenderId())
                .map(Account::getFullName)
                .orElse("Unknown"));
        chatDTO.setMessageContent(chat.getMessageContent());
        chatDTO.setSentAt(chat.getSentAt());
        chatDTO.setSeen(chat.isSeen());
        return chatDTO;
    }
}