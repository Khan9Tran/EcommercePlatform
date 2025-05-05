package com.hkteam.ecommerce_platform.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hkteam.ecommerce_platform.entity.chat.Room;
import com.hkteam.ecommerce_platform.entity.user.Store;
import com.hkteam.ecommerce_platform.entity.user.User;

@Repository
public interface RoomRepository extends JpaRepository<Room, String> {
    Optional<Room> findRoomByStoreAndUser(Store store, User user);

    Page<Room> findByUser(User user, Pageable pageable);

    Page<Room> findByStore(Store store, Pageable pageable);
}
