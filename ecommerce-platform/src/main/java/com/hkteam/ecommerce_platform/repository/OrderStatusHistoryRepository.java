package com.hkteam.ecommerce_platform.repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hkteam.ecommerce_platform.entity.order.OrderStatusHistory;

@Repository
public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, String> {
    @Query(
            """
			select count(osh) from OrderStatusHistory osh
			where osh.orderStatus.name = :statusName
			and osh.createdAt = (
				select max(osh2.createdAt)
				from OrderStatusHistory osh2
				where osh2.order.id = osh.order.id
			)
			""")
    long countByLatestStatus(@Param("statusName") String statusName);

    @Query(
            """
			select distinct coalesce(sum(o.grandTotal), 0)
			from OrderStatusHistory osh
			join osh.order o
			where osh.orderStatus.name = 'DELIVERED'
			and osh.createdAt = (
				select max (osh2.createdAt)
				from OrderStatusHistory osh2
				where osh2.order.id = osh.order.id
			)
			and (
				(:interval = 'isDay' and date_part('year', cast(osh.createdAt as timestamp) ) = date_part('year', cast(:time as timestamp))
				and date_part('month', cast(osh.createdAt as timestamp)) = date_part('month', cast(:time as timestamp))
				and date_part('week', cast(osh.createdAt as timestamp)) = date_part('week', cast(:time as timestamp))
				and date_part('day', cast(osh.createdAt as timestamp)) = date_part('day', cast(:time as timestamp)))
				or
				(:interval = 'isWeek' and date_part('year', cast(osh.createdAt as timestamp)) = date_part('year', cast(:time as timestamp))
				and date_part('month', cast(osh.createdAt as timestamp)) = date_part('month', cast(:time as timestamp))
				and date_part('week', cast(osh.createdAt as timestamp)) = date_part('week', cast(:time as timestamp)))
				or
				(:interval = 'isMonth' and date_part('year', cast(osh.createdAt as timestamp)) = date_part('year', cast(:time as timestamp))
				and date_part('month', cast(osh.createdAt as timestamp)) = date_part('month', cast(:time as timestamp)))
				or
				(:interval = 'isYear' and date_part('year', cast(osh.createdAt as timestamp)) = date_part('year', cast(:time as timestamp)))
			)
			""")
    BigDecimal calculateRevenueByIntervalAndTime(@Param("interval") String interval, @Param("time") Instant time);

    @Query(
            """
		select o.store.id, o.store.name, coalesce(cast(sum(o.total - o.discount) as BigDecimal), 0)
		from OrderStatusHistory osh
		join osh.order o
		where osh.orderStatus.name = 'DELIVERED'
		and osh.createdAt = (
			select max(osh2.createdAt)
			from OrderStatusHistory osh2
			where osh2.order.id = osh.order.id
		)
		group by o.store.id, o.store.name
		order by coalesce(sum(o.total - o.discount), 0) desc
		limit 5
	""")
    List<Object[]> findTop5StoresByRevenueRaw();

    @Query(
            """
			select sum(o.grandTotal) from OrderStatusHistory osh join osh.order o
			where osh.orderStatus.name = :statusName
			and osh.createdAt = (
				select max (osh2.createdAt)
				from OrderStatusHistory osh2
				where osh2.order.id = osh.order.id
			)
			and osh.createdAt >= :startDate and osh.createdAt < :endDate
			""")
    BigDecimal calculateDailyRevenue(
            @Param("statusName") String statusName,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);

    @Query(
            """
			select distinct coalesce(sum(o.grandTotal), 0)
			from OrderStatusHistory osh
			join osh.order o
			where osh.orderStatus.name = 'DELIVERED'
			and osh.createdAt = (
				select max (osh2.createdAt)
				from OrderStatusHistory osh2
				where osh2.order = osh.order
			)
			and date_part('year', cast(osh.createdAt as timestamp)) = :year
			""")
    BigDecimal calculateTotalRevenueOneYear(@Param("year") int year);
}
