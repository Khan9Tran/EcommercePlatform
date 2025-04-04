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

import com.hkteam.ecommerce_platform.entity.user.User;
import com.hkteam.ecommerce_platform.enums.RoleName;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsername(String username);

    @NotNull
    Optional<User> findById(@NotNull String id);

    Optional<User> findByEmailValidationToken(String token);

    Optional<User> findByEmail(String email);

    @Query(
            """
		select u from User u inner join u.roles roles
		where roles.name in ?1 and u.isBlocked in ?2
		and (lower(u.username) like lower(concat('%', ?3, '%'))
		or lower(u.name) like lower(concat('%', ?4, '%')))
		""")
    Page<User> searchByBlocked(
            Collection<RoleName> names,
            Collection<Boolean> isBlockeds,
            String username,
            String name,
            Pageable pageable);

    @Query(
            """
		select u from User u inner join u.roles roles
		where roles.name in ?1
		and (lower(u.username) like lower(concat('%', ?2, '%'))
		or lower(u.name) like lower(concat('%', ?3, '%')))
		""")
    Page<User> search(Collection<RoleName> names, String username, String name, Pageable pageable);

    long countByRolesName(@NotNull RoleName roleName);

    @Query(
            """
		select count(u) from User u join u.roles roles
		where roles.name in :roleName
		and (
				(:interval = 'isDay' and date_part('year', cast(u.createdAt as timestamp) ) = date_part('year', cast(:time as timestamp))
				and date_part('month', cast(u.createdAt as timestamp)) = date_part('month', cast(:time as timestamp))
				and date_part('week', cast(u.createdAt as timestamp)) = date_part('week', cast(:time as timestamp))
				and date_part('day', cast(u.createdAt as timestamp)) = date_part('day', cast(:time as timestamp)))
				or
				(:interval = 'isWeek' and date_part('year', cast(u.createdAt as timestamp)) = date_part('year', cast(:time as timestamp))
				and date_part('month', cast(u.createdAt as timestamp)) = date_part('month', cast(:time as timestamp))
				and date_part('week', cast(u.createdAt as timestamp)) = date_part('week', cast(:time as timestamp)))
				or
				(:interval = 'isMonth' and date_part('year', cast(u.createdAt as timestamp)) = date_part('year', cast(:time as timestamp))
				and date_part('month', cast(u.createdAt as timestamp)) = date_part('month', cast(:time as timestamp)))
				or
				(:interval = 'isYear' and date_part('year', cast(u.createdAt as timestamp)) = date_part('year', cast(:time as timestamp)))
			)
		""")
    long countUserByIntervalAndTime(
            @Param("roleName") Collection<RoleName> roleName,
            @Param("interval") String interval,
            @Param("time") Instant time);
}
