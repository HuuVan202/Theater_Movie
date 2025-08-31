package movie_theater_gr4.project_gr4.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import movie_theater_gr4.project_gr4.dto.ChatDTO;
import movie_theater_gr4.project_gr4.model.Account;
import movie_theater_gr4.project_gr4.model.Chat;
import movie_theater_gr4.project_gr4.model.Dialogue;
import movie_theater_gr4.project_gr4.repository.ChatRepository;
import movie_theater_gr4.project_gr4.service.AccountService;
import movie_theater_gr4.project_gr4.service.DialogueService;
import movie_theater_gr4.project_gr4.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/chat")
public class ChatController {
    private static final int SUPPORT_ID = 2;

    @Autowired
    private DialogueService dialogueService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private Cloudinary cloudinary;

    private String saveFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "resource_type", "image",
                    "folder", "chat_images",
                    "public_id", UUID.randomUUID().toString()
            ));
            return (String) uploadResult.get("secure_url");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @GetMapping
    public String showChatPage(Authentication authentication, @RequestParam(required = false) Integer dialogueId, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        String username = authentication.getName();
        Account account = accountService.loadUserByUsername(username);
        if (account == null) {
            return "redirect:/login";
        }

        model.addAttribute("account", account);

        if ("ADMIN_SUPPORT".equals(account.getRole().name()) || "ADMIN".equals(account.getRole().name()) || "EMPLOYEE".equals(account.getRole().name())) {
            List<Dialogue> dialogues = dialogueService.getActiveDialoguesByUserId(account.getAccountId());
            for (Dialogue dialogue : dialogues) {
                dialogue.setHasUnreadMessages(messageService.hasUnreadMessages(dialogue.getDialogueId()));
                int customerId = dialogue.getUser1Id() == account.getAccountId() ? dialogue.getUser2Id() : dialogue.getUser1Id();
                Account customer = accountService.findAccountById(customerId);
                dialogue.setCustomerName(customer != null ? customer.getFullName() : "Khách hàng #" + customerId);
            }
            model.addAttribute("dialogues", dialogues);
            Dialogue selectedDialogue = null;
            if (dialogueId != null) {
                selectedDialogue = dialogueService.getDialogueById(dialogueId);
                if (selectedDialogue != null && (selectedDialogue.getUser1Id() == account.getAccountId() || selectedDialogue.getUser2Id() == account.getAccountId())) {
                    List<ChatDTO> chats = messageService.getChatsByDialogueId(dialogueId);
                    model.addAttribute("chats", chats);
                    int customerId = selectedDialogue.getUser1Id() == account.getAccountId() ? selectedDialogue.getUser2Id() : selectedDialogue.getUser1Id();
                    Account customer = accountService.findAccountById(customerId);
                    selectedDialogue.setCustomerName(customer != null ? customer.getFullName() : "Khách hàng #" + customerId);
                    model.addAttribute("selectedDialogue", selectedDialogue);
                }
            }
            List<Dialogue> inactiveDialogues = dialogueService.getInactiveDialoguesByUserId(account.getAccountId());
            model.addAttribute("inactiveDialogues", inactiveDialogues);
            model.addAttribute("selectedDialogue", selectedDialogue);
            return "admin_chat";
        } else {
            Dialogue dialogue = dialogueService.getOrCreateDialogue(account.getAccountId(), SUPPORT_ID);
            dialogue.setHasUnreadMessages(messageService.hasUnreadMessages(dialogue.getDialogueId()));
            List<ChatDTO> chats = messageService.getChatsByDialogueId(dialogue.getDialogueId());
            model.addAttribute("chats", chats);
            model.addAttribute("selectedDialogue", dialogue);
            return "chat";
        }
    }

    @GetMapping("/history")
    public String showChatHistoryPage(Authentication authentication, @RequestParam(required = false) Integer dialogueId, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        String username = authentication.getName();
        Account account = accountService.loadUserByUsername(username);
        if (account == null) {
            return "redirect:/login";
        }
        model.addAttribute("account", account);

        // Only for admin/employee/support
        if ("ADMIN".equals(account.getRole().name()) || "EMPLOYEE".equals(account.getRole().name()) || "ADMIN_SUPPORT".equals(account.getRole().name())) {
            List<Dialogue> inactiveDialogues = dialogueService.getInactiveDialoguesByUserId(account.getAccountId());
            for (Dialogue dialogue : inactiveDialogues) {
                int customerId = dialogue.getUser1Id() == account.getAccountId() ? dialogue.getUser2Id() : dialogue.getUser1Id();
                Account customer = accountService.findAccountById(customerId);
                dialogue.setCustomerName(customer != null ? customer.getFullName() : "Khách hàng #" + customerId);
            }
            model.addAttribute("inactiveDialogues", inactiveDialogues);

            Dialogue selectedDialogue = null;
            if (dialogueId != null) {
                selectedDialogue = dialogueService.getDialogueById(dialogueId);
                if (selectedDialogue != null && (selectedDialogue.getUser1Id() == account.getAccountId() || selectedDialogue.getUser2Id() == account.getAccountId())) {
                    List<ChatDTO> chats = messageService.getChatsByDialogueId(dialogueId);
                    model.addAttribute("chats", chats);
                    int customerId = selectedDialogue.getUser1Id() == account.getAccountId() ? selectedDialogue.getUser2Id() : selectedDialogue.getUser1Id();
                    Account customer = accountService.findAccountById(customerId);
                    selectedDialogue.setCustomerName(customer != null ? customer.getFullName() : "Khách hàng #" + customerId);
                }
            }
            model.addAttribute("selectedDialogue", selectedDialogue);
            return "history_chat";
        }
        return "redirect:/chat";
    }

    @MessageMapping("/chat/{dialogueId}")
    @SendTo("/topic/chat/{dialogueId}")
    public ChatDTO sendMessage(@DestinationVariable Integer dialogueId, ChatDTO chatDTO, Authentication authentication) {
        String username = authentication.getName();
        Account account = accountService.loadUserByUsername(username);
        Dialogue dialogue = dialogueService.getDialogueById(dialogueId);

        if (dialogue == null || (dialogue.getUser1Id() != account.getAccountId() && dialogue.getUser2Id() != account.getAccountId())) {
            throw new IllegalArgumentException("Invalid dialogue or permission denied");
        }

        if (!dialogue.isActive()) {
            dialogueService.activateDialogue(dialogueId);
        }

        Chat chat = new Chat();
        chat.setDialogueId(dialogueId);
        chat.setSenderId(account.getAccountId());
        chat.setMessageContent(chatDTO.getMessageContent());
        ChatDTO savedChat = messageService.sendMessage(chat);
        savedChat.setTempId(chatDTO.getTempId());
        return savedChat;
    }

    @PostMapping("/send")
    @ResponseBody
    public ResponseEntity<ChatDTO> sendMessagePost(@RequestParam("dialogueId") Integer dialogueId,
                                                   @RequestParam("senderId") Integer senderId,
                                                   @RequestParam(value = "messageContent", required = false) String messageContent,
                                                   @RequestParam(value = "file", required = false) MultipartFile file,
                                                   Authentication authentication) {
        String username = authentication.getName();
        Account account = accountService.loadUserByUsername(username);
        Dialogue dialogue = dialogueService.getDialogueById(dialogueId);

        if (dialogue == null || (dialogue.getUser1Id() != account.getAccountId() && dialogue.getUser2Id() != account.getAccountId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        if (!dialogue.isActive()) {
            dialogueService.activateDialogue(dialogueId);
        }

        Chat chat = new Chat();
        chat.setDialogueId(dialogueId);
        chat.setSenderId(account.getAccountId());
        String finalMessageContent = messageContent != null ? messageContent : "";

        if (file != null && !file.isEmpty()) {
            String imageUrl = saveFile(file);
            if (imageUrl != null) {
                finalMessageContent = "IMAGE:" + imageUrl;
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            }
        }

        chat.setMessageContent(finalMessageContent);
        ChatDTO savedChat = messageService.sendMessage(chat);
        savedChat.setTempId(null);
        return ResponseEntity.ok(savedChat);
    }

    @PostMapping("/end/{dialogueId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> endChat(@PathVariable int dialogueId, Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        try {
            String username = authentication.getName();
            Account account = accountService.loadUserByUsername(username);
            Dialogue dialogue = dialogueService.getDialogueById(dialogueId);
            if (dialogue == null) {
                response.put("status", "error");
                response.put("message", "Cuộc hội thoại không tồn tại");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            if (dialogue.getUser1Id() != account.getAccountId() && dialogue.getUser2Id() != account.getAccountId()) {
                response.put("status", "error");
                response.put("message", "Bạn không có quyền kết thúc cuộc hội thoại này");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            dialogueService.deactivateDialogue(dialogueId);
            response.put("status", "success");
            response.put("message", "Đã kết thúc chat thành công");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Có lỗi xảy ra: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}