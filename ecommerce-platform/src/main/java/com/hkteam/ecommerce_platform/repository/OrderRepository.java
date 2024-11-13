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
            """
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
		))
		or (
			lower(o.recipientName) like lower(concat('%', ?3, '%') )
			or lower(o.phone) like lower(concat('%', ?4, '%') )
			or cast(o.grandTotal as String) like concat('%', ?5, '%')
			or lower(o.code) like lower(concat('%', ?6, '%'))
		)
	)
	""")
    Page<Order> findAllOrderByStore(
            @Nullable String storeId,
            @Nullable String statusName,
            @Nullable String recipientName,
            @Nullable String phone,
            @Nullable String grandTotal,
            @Nullable String code,
            Pageable pageable);
}
