package datn.duong.FishSeller.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint để React kết nối: http://localhost:8080/ws
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("http://localhost:5173");
                // .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Client sẽ lắng nghe dữ liệu tại các prefix bắt đầu bằng /topic
        registry.enableSimpleBroker("/topic");
        
        // Client gửi dữ liệu lên server sẽ bắt đầu bằng /app (nếu cần)
        registry.setApplicationDestinationPrefixes("/app");
    }
}
