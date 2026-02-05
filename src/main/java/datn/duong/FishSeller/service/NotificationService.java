package datn.duong.FishSeller.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Gửi thông báo real-time tới một user cụ thể
     * @param userId ID của người nhận
     * @param destination Topic phụ (ví dụ: /orders hoặc /appointments)
     * @param payload Dữ liệu gửi đi (DTO)
     */
    public void sendPrivateNotification(Long userId, String destination, Object payload) {
        String path = "/topic/user/" + userId + destination;
        // Ví dụ: /topic/user/1/orders hoặc /topic/user/1/appointments
        messagingTemplate.convertAndSend(path, payload);
    }

    // --- GỬI CHO TẤT CẢ ADMIN ---
    // Admin sẽ lắng nghe tại các topic: /topic/admin/orders hoặc /topic/admin/appointments
    public void sendAdminNotification(String destination, Object payload) {
        messagingTemplate.convertAndSend("/topic/admin" + destination, payload);
    }

    /**
     * Gửi thông báo cho toàn bộ người dùng (ví dụ: có khuyến mãi mới)
     */
    public void sendPublicNotification(String destination, Object payload) {
        messagingTemplate.convertAndSend("/topic/public" + destination, payload);
    }
}