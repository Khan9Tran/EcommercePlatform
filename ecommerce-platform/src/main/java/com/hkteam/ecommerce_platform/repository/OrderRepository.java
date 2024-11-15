package com.hkteam.ecommerce_platform.repository;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.hkteam.ecommerce_platform.entity.order.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    @NotNull
    Optional<Order> findOrderById(@NotNull String orderId);

    @Query(
            value = """
                    select o from Order o where o.store.id = ?1
                    and (
                         (exists (
                       		select 1 from OrderStatusHistory osh
                       		where osh.order = o
                       		and osh.orderStatus.name = ?2
                       		and osh.createdAt = (
                       			select max(osh2.createdAt) from OrderStatusHistory osh2
                       			where osh2.order = osh.order
                       		)
                       	)
                    )
                    or (
                    	lower(o.id) like lower(concat('%', ?3, '%') )
                    	or cast(o.total as String) like concat('%', ?4, '%')
                    )
            )
            """
    )
    Page<Order> findAllOrderByStore(
            @Nullable String storeId,
            @Nullable String statusName,
			@Nullable String orderId,
            @Nullable String total,
            Pageable pageable);

    Page<Order> findByStore_IdContainsAndOrderStatusHistories_OrderStatus_NameLikeOrIdLike(@Nullable String id, @Nullable String name, @Nullable String id1, Pageable pageable);

}
