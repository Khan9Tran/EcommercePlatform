package com.hkteam.ecommerce_platform.controller;

import com.hkteam.ecommerce_platform.dto.request.ChatMessageRequest;
import com.hkteam.ecommerce_platform.dto.response.ChatMessageResponse;
import com.hkteam.ecommerce_platform.entity.user.User;
import com.hkteam.ecommerce_platform.service.ChatService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "Component Controller")
public class WebSocketController {
    ChatService chatService;

    @MessageMapping("/chat/{roomId}")
    @SendTo("/topic/room/{roomId}")
    public ChatMessageResponse sendMessage(@DestinationVariable String roomId, @Payload ChatMessageRequest message, SimpMessageHeaderAccessor headerAccessor) {
        User user = (User) headerAccessor.getSessionAttributes().get("user");
        if (user == null) {
            log.error("User not found");
            return null;
        }
        var rs = chatService.saveMessage(roomId, message, user);
        return rs;
    }
}
