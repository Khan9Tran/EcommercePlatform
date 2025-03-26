package com.hkteam.ecommerce_platform.repository;

import com.hkteam.ecommerce_platform.entity.chat.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, String> {

    Page<Message> findByRoomId(String id, Pageable pageable);
}
