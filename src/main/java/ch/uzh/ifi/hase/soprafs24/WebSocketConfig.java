package ch.uzh.ifi.hase.soprafs24;

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
        // Specify allowed origins if your client is not served from the same domain
        registry.addEndpoint("/ws").setAllowedOrigins("http://localhost:8000","http://localhost:3000", "http://127.0.0.1:3000","https://sopra-fs24-group-20-client.oa.r.appspot.com","https://sopra-fs24-group-20-server.oa.r.appspot.com").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }
}
