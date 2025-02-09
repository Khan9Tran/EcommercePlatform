package com.hkteam.ecommerce_platform.repository;

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import com.hkteam.ecommerce_platform.entity.order.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    @NotNull
    Optional<Order> findOrderById(@NotNull String orderId);

    @Query(
            value =
                    """
					select o from Order o
					join o.orderStatusHistories osh
					where o.store.id = :storeId
					and (:orderId = '' or lower(o.id) like lower(concat('%', :orderId, '%')))
					and osh in (
							select osh1 from OrderStatusHistory osh1
							where osh1.order = o
							and (:statusName = '' or osh1.orderStatus.name = :statusName)
							and osh1.createdAt = (
								select max(osh2.createdAt) from OrderStatusHistory osh2 where osh2.order = osh1.order
							)
						)
			""")
    Page<Order> findAllOrderBySeller(
            @Nullable String storeId, @Nullable String orderId, @Nullable String statusName, Pageable pageable);

    @Query(
            value =
                    """
					select o from Order o
					join o.orderStatusHistories osh
					where
					(
						(:orderId = '' or lower(o.id) like lower(concat('%', :orderId, '%')))
						or (:phone = '' or lower(o.phone) like lower(concat('%', :phone, '%')))
						or (:province = '' or lower(o.province) like lower(concat('%', :province, '%')))
						or (:grandTotal = '' or cast(o.grandTotal as string) like lower(concat('%', :grandTotal, '%')))
					)
					and osh = (
						select osh1 from OrderStatusHistory osh1
						where osh1.order = o
						and (:statusName = '' or osh1.orderStatus.name = :statusName)
						and osh1.createdAt = (
							select max(osh2.createdAt) from OrderStatusHistory osh2 where osh2.order = osh1.order
						)
					)
			""")
    Page<Order> findAllOrderByAdmin(
            @Nullable String orderId,
            @Nullable String phone,
            @Nullable String province,
            @Nullable String grandTotal,
            @Nullable String statusName,
            Pageable pageable);

    @Query(
            value =
                    """
					select distinct o from Order o
					join o.orderStatusHistories osh
					join o.orderItems oi
					join oi.product p
					where o.user.id = :userId
					and (
						(:orderId = '' or lower(o.id) like lower(concat('%', :orderId, '%')))
						or (:storeName = '' or lower(o.store.name) like lower(concat('%', :storeName, '%')))
						or (:productName = '' or lower(p.name) like lower(concat('%', :productName, '%')))
					)
					and osh in (
						select osh1 from OrderStatusHistory osh1
						where osh1.order = o
						and (
								(:statusName = '' or osh1.orderStatus.name = :statusName)
								or (:statusName = 'WAITING_DELIVERY' and osh1.orderStatus.name in ('PICKED_UP', 'OUT_FOR_DELIVERY'))
								or (:statusName = 'IN_TRANSIT' and osh1.orderStatus.name in ('PENDING', 'CONFIRMED', 'PREPARING', 'WAITING_FOR_SHIPPING'))
						)
						and osh1.createdAt = (
							select max(osh2.createdAt) from OrderStatusHistory osh2 where osh2.order = osh1.order
						)
					)
			""")
    Page<Order> findAllOrderByUser(
            @Nullable String userId,
            @Nullable String orderId,
            @Nullable String storeName,
            @Nullable String productName,
            @Nullable String statusName,
            Pageable pageable);

    @Query(
            """
		select distinct o from Order o
		join o.orderStatusHistories osh
		join osh.orderStatus os
		where o.id = :orderId
		and os.name in :listStatus
		and osh.createdAt = (
					select max (osh2.createdAt)
					from OrderStatusHistory osh2
					where osh2.order = o
				)
	""")
    Optional<Order> findOneOrderUpdateOrCancel(
            @Param("orderId") String orderId, @Param("listStatus") List<String> listStatus);

    @Query(
            """
		select distinct o from Order o
		join o.orderStatusHistories osh
		join osh.orderStatus os
		where o.id IN :listOrderId
		and os.name IN :listStatus
		and osh.createdAt = (
					select max (osh2.createdAt)
					from OrderStatusHistory osh2
					where osh2.order = o
				)
	""")
    List<Order> findListOrderUpdateOrCancel(
            @Param("listOrderId") List<String> listOrderId, @Param("listStatus") List<String> listStatus);
}
