package com.hkteam.ecommerce_platform.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hkteam.ecommerce_platform.entity.chat.Message;

public interface MessageRepository extends JpaRepository<Message, String> {

    Page<Message> findByRoomId(String id, Pageable pageable);

    @Query(
            "SELECT m FROM Message m WHERE m.room.id = :id AND (m.product IS NOT NULL OR m.order IS NOT NULL) ORDER BY m.createdAt DESC")
    List<Message> findTopByRoomIdWithValidProductOrOrder(@Param("id") String id, Pageable pageable);
}
