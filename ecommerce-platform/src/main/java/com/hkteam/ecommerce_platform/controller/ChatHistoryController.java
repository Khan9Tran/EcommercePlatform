package com.hkteam.ecommerce_platform.controller;

import com.cloudinary.Api;
import com.hkteam.ecommerce_platform.dto.request.CreateRoomRequest;
import com.hkteam.ecommerce_platform.dto.response.*;
import com.hkteam.ecommerce_platform.service.ChatService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chats")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "Chat History Controller")
public class ChatHistoryController {
    ChatService messageService;

    @GetMapping("/{roomId}/messages-user")
    public ApiResponse<PaginationResponse<ChatMessageResponse>> getChatHistory(@PathVariable String roomId,
        @RequestParam(value = "page", required = false, defaultValue = "1") String page,
        @RequestParam(value = "size", required = false, defaultValue = "10") String size
    ) {
        var rs =  messageService.getChatHistory(roomId, page, size);
        return ApiResponse.<PaginationResponse<ChatMessageResponse>>builder()
                .result(rs)
                .build();
    }

    @PostMapping("")
    public ApiResponse<CreateRoomResponse> createRoom(@RequestBody @Valid CreateRoomRequest request) {
        return ApiResponse.<CreateRoomResponse>builder()
                .result(messageService.createRoom(request))
                .build();
    }

    @GetMapping("/rooms-user")
    public ApiResponse<PaginationResponse<RoomResponse>> getRooms(
            @RequestParam(value = "page", required = false, defaultValue = "1") String page,
            @RequestParam(value = "size", required = false, defaultValue = "10") String size
    ) {
        var rs =  messageService.getRooms(page, size);
        return ApiResponse.<PaginationResponse<RoomResponse>> builder()
                .result(rs)
                .build();
    }

    @GetMapping("/rooms-store")
    public ApiResponse<PaginationResponse<RoomResponse>> getRoomInStore(
            @RequestParam(value = "page", required = false, defaultValue = "1") String page,
            @RequestParam(value = "size", required = false, defaultValue = "10") String size
    ) {
        var rs =  messageService.getRoomInStore(page, size);
        return ApiResponse.<PaginationResponse<RoomResponse>> builder()
                .result(rs)
                .build();
    }

    @GetMapping("/{roomId}/messages-store")
    public ApiResponse<PaginationResponse<ChatMessageResponse>> getChatHistoryInStore(
            @PathVariable String roomId,
            @RequestParam(value = "page", required = false, defaultValue = "1") String page,
            @RequestParam(value = "size", required = false, defaultValue = "10") String size
    ) {
        var rs =  messageService.getChatHistory(roomId, page, size);
        return ApiResponse.<PaginationResponse<ChatMessageResponse>>builder()
                .result(rs)
                .build();
    }
}
