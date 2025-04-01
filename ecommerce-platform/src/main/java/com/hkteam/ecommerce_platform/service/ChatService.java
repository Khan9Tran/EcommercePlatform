package com.hkteam.ecommerce_platform.service;

import com.hkteam.ecommerce_platform.dto.request.ChatMessageRequest;
import com.hkteam.ecommerce_platform.dto.request.CreateRoomRequest;
import com.hkteam.ecommerce_platform.dto.response.ChatMessageResponse;
import com.hkteam.ecommerce_platform.dto.response.CreateRoomResponse;
import com.hkteam.ecommerce_platform.dto.response.PaginationResponse;
import com.hkteam.ecommerce_platform.dto.response.RoomResponse;
import com.hkteam.ecommerce_platform.entity.chat.Message;
import com.hkteam.ecommerce_platform.entity.chat.Room;
import com.hkteam.ecommerce_platform.entity.order.Order;
import com.hkteam.ecommerce_platform.entity.product.Product;
import com.hkteam.ecommerce_platform.entity.user.Store;
import com.hkteam.ecommerce_platform.entity.user.User;
import com.hkteam.ecommerce_platform.exception.AppException;
import com.hkteam.ecommerce_platform.exception.ErrorCode;
import com.hkteam.ecommerce_platform.repository.*;
import com.hkteam.ecommerce_platform.util.AuthenticatedUserUtil;
import com.hkteam.ecommerce_platform.util.PageUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ChatService {
    MessageRepository messageRepository;
    RoomRepository roomRepository;
    AuthenticatedUserUtil authenticatedUserUtil;
    UserRepository userRepository;
    StoreRepository storeRepository;
    OrderRepository orderRepository;
    ProductRepository productRepository;

    public PaginationResponse<RoomResponse> getRoomsCommon(String page, String size, boolean isStore) {
        Sort sort = Sort.by(Sort.Order.desc("lastUpdatedAt"));
        var pagination = PageUtils.createPageable(page, size, sort);

        var user = authenticatedUserUtil.getAuthenticatedUser();

        var pageData = isStore
                ? roomRepository.findByStore(Optional.ofNullable(user.getStore())
                .orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND)), pagination)
                : roomRepository.findByUser(user, pagination);

        int pageInt = Integer.parseInt(page);

        return PaginationResponse.<RoomResponse>builder()
                .currentPage(pageInt)
                .pageSize(pageData.getSize())
                .totalPages(pageData.getTotalPages())
                .totalElements(pageData.getTotalElements())
                .hasNext(pageData.hasNext())
                .hasPrevious(pageData.hasPrevious())
                .nextPage(pageData.hasNext() ? pageInt + 1 : null)
                .previousPage(pageData.hasPrevious() ? pageInt - 1 : null)
                .data(pageData.getContent().stream().map(
                        room -> RoomResponse.builder()
                                .id(room.getId())
                                .storeId(room.getStore().getId())
                                .storeName(room.getStore().getName())
                                .storeImageUrl(room.getStore().getUser().getImageUrl())
                                .userId(room.getUser().getId())
                                .userName(room.getUser().getName())
                                .userImageUrl(room.getUser().getImageUrl())
                                .createdAt(Timestamp.from(room.getCreatedAt()))
                                .updatedAt(Timestamp.from(room.getLastUpdatedAt()))
                                .lastMessage(room.getLastMessage())
                                .lastTimeMessage(room.getLastTimeMessage() != null ? room.getLastTimeMessage().toString() : null)
                                .build()
                ).toList())
                .build();
    }

    public PaginationResponse<RoomResponse> getRooms(String page, String size) {
        return getRoomsCommon(page, size, false);
    }

    public PaginationResponse<RoomResponse> getRoomInStore(String page, String size) {
        return getRoomsCommon(page, size, true);
    }


    public CreateRoomResponse createRoom(CreateRoomRequest request) {
        User user;
        Store store;

        if (request.getStoreId().isEmpty()) {
            store = Optional.ofNullable(authenticatedUserUtil.getAuthenticatedUser().getStore())
                    .orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));

            user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        } else {
            store = storeRepository.findById(request.getStoreId())
                    .orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));

            user = authenticatedUserUtil.getAuthenticatedUser();
        }

        roomRepository.findRoomByStoreAndUser(store, user)
                .ifPresent(r -> { throw new AppException(ErrorCode.ROOM_ALREADY_EXISTED); });

        var room = Room.builder()
                .store(store)
                .user(user)
                .build();
        try {
            roomRepository.save(room);
        }
        catch (Exception e) {
            throw new AppException(ErrorCode.UNKNOWN_ERROR);
        }

        return CreateRoomResponse.builder()
                .id(room.getId())
                .build();
    }


    public PaginationResponse<ChatMessageResponse> getChatMessages(String roomId, String page, String size, boolean isStore) {
        var room = roomRepository.findById(roomId).orElseThrow(
                () -> new AppException(ErrorCode.ROOM_NOT_FOUND));

        if (isStore) {
            var store = authenticatedUserUtil.getAuthenticatedUser().getStore();
            if (store == null) {
                throw new AppException(ErrorCode.STORE_NOT_FOUND);
            }
            if (!room.getStore().equals(store)) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }
        } else {
            if (!room.getUser().getId().equals(authenticatedUserUtil.getAuthenticatedUser().getId())) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }
        }

        Sort sort = Sort.by(Sort.Order.desc("lastUpdatedAt"));
        var pagination = PageUtils.createPageable(page, size, sort);
        var pageData = messageRepository.findByRoomId(room.getId(), pagination);

        return PaginationResponse.<ChatMessageResponse>builder()
                .currentPage(Integer.parseInt(page))
                .pageSize(pageData.getSize())
                .totalPages(pageData.getTotalPages())
                .totalElements(pageData.getTotalElements())
                .hasNext(pageData.hasNext())
                .hasPrevious(pageData.hasPrevious())
                .nextPage(pageData.hasNext() ? Integer.parseInt(page) + 1 : null)
                .previousPage(pageData.hasPrevious() ? Integer.parseInt(page) - 1 : null)
                .data(pageData.getContent().stream().map(
                        message -> ChatMessageResponse.builder()
                                .id(message.getId())
                                .content(message.getContent())
                                .orderId(message.getOrder() != null ? message.getOrder().getId() : null)
                                .productId(message.getProduct() != null ? message.getProduct().getId() : null)
                                .senderId(message.getSender().getId())
                                .createdAt(Timestamp.from(message.getCreatedAt()).toString())
                                .build()
                ).toList())
                .build();
    }

    public PaginationResponse<ChatMessageResponse> getChatHistory(String roomId, String page, String size) {
        return getChatMessages(roomId, page, size, false);
    }

    public PaginationResponse<ChatMessageResponse> getChatInStore(String roomId, String page, String size) {
        return getChatMessages(roomId, page, size, true);
    }

    public ChatMessageResponse saveMessage(String roomId, ChatMessageRequest message, User user) {
        log.info("Saving message: {}", message);
        if (message.getContent().isEmpty()) {
            throw new AppException(ErrorCode.MESSAGE_EMPTY);
        }

        Order order = null;
        Product product = null;

        if (Objects.nonNull(message.getOrderId()) && message.getOrderId() != "") {
            order = orderRepository.findById(message.getOrderId()).orElseThrow(
                    () -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        }

        if (Objects.nonNull(message.getProductId()) && message.getProductId() != "") {
            product = productRepository.findById(message.getProductId()).orElseThrow(
                    () -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        }

        var room = roomRepository.findById(roomId).orElseThrow(
                () -> new AppException(ErrorCode.ROOM_NOT_FOUND));

        var msg = messageRepository.save(
                Message.builder()
                        .room(room)
                        .content(message.getContent())
                        .sender(user)
                        .order(order)
                        .product(product)
                        .build()
        );

        var rs = messageRepository.save(msg);
        room.setLastMessage(message.getContent());
        room.setLastTimeMessage(Timestamp.from(Instant.now()));
        roomRepository.save(room);


        return ChatMessageResponse.builder()
                .id(rs.getId())
                .content(rs.getContent())
                .createdAt(rs.getCreatedAt().toString())
                .senderId(rs.getSender().getId())
                .orderId(rs.getOrder() != null ? rs.getOrder().getId() : null)
                .productId(rs.getProduct() != null ? rs.getProduct().getId() : null)
                .build();
    }

}
