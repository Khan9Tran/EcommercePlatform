package com.hkteam.ecommerce_platform.configuration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.sockjs.transport.handler.WebSocketTransportHandler;
import org.springframework.web.socket.sockjs.transport.handler.XhrPollingTransportHandler;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
@Getter
@EnableWebSocket
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final WebSocketAuthInterceptor webSocketAuthInterceptor;
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic"); // Client sẽ subscribe vào "/topic"
        registry.setApplicationDestinationPrefixes("/app"); // Định tuyến tin nhắn đến Controller
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        WebSocketClient webSocketClient = new StandardWebSocketClient();
        registry.addEndpoint("/ws")
                .setAllowedOrigins("http://localhost:3000")
                .addInterceptors(webSocketAuthInterceptor)
                .withSockJS();
    }
}
