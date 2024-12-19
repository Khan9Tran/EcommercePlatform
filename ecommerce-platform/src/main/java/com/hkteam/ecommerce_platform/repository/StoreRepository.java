package com.hkteam.ecommerce_platform.repository;

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hkteam.ecommerce_platform.entity.user.Store;
import com.hkteam.ecommerce_platform.enums.RoleName;

@Repository
public interface StoreRepository extends JpaRepository<Store, String> {
    @NotNull
    Optional<Store> findById(@NotNull String id);

    @NotNull
    Optional<Store> findByUserId(@NotNull String userId);

    @NotNull
    Page<Store> findAll(@NotNull Pageable pageable);

    @Query(
            "select s from Store s where (lower(s.name) like lower(concat('%', ?1, '%')) or lower(s.user.username) like lower(concat('%', ?2, '%'))) and s.isBanned = ?3")
    Page<Store> searchAllStore(String name, String username, Pageable pageable, boolean isBanned);

    @Query(
            """
		select count(s) from Store s
		join s.user u join u.roles roles
		where roles.name in :roleName
		and (
				(:interval = 'isDay' and date_part('year', cast(s.createdAt as timestamp) ) = date_part('year', cast(:time as timestamp))
				and date_part('month', cast(s.createdAt as timestamp)) = date_part('month', cast(:time as timestamp))
				and date_part('week', cast(s.createdAt as timestamp)) = date_part('week', cast(:time as timestamp))
				and date_part('day', cast(s.createdAt as timestamp)) = date_part('day', cast(:time as timestamp)))
				or
				(:interval = 'isWeek' and date_part('year', cast(s.createdAt as timestamp)) = date_part('year', cast(:time as timestamp))
				and date_part('month', cast(s.createdAt as timestamp)) = date_part('month', cast(:time as timestamp))
				and date_part('week', cast(s.createdAt as timestamp)) = date_part('week', cast(:time as timestamp)))
				or
				(:interval = 'isMonth' and date_part('year', cast(s.createdAt as timestamp)) = date_part('year', cast(:time as timestamp))
				and date_part('month', cast(s.createdAt as timestamp)) = date_part('month', cast(:time as timestamp)))
				or
				(:interval = 'isYear' and date_part('year', cast(s.createdAt as timestamp)) = date_part('year', cast(:time as timestamp)))
			)
		""")
    long countStoreByIntervalAndTime(
            @Param("roleName") Collection<RoleName> roleName,
            @Param("interval") String interval,
            @Param("time") Instant time);
}
