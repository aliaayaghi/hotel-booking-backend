package com.HotelBook.catalog.user.repository;



import com.HotelBook.catalog.user.entity.User;
import com.HotelBook.catalog.user.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findAllByRole(UserRole role);

    List<User> findAllByRoleAndIsActive(UserRole role, boolean isActive);
}