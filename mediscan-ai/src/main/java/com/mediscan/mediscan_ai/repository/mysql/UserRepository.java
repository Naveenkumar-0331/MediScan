package com.mediscan.mediscan_ai.repository.mysql;

import java.util.Optional;
import com.mediscan.mediscan_ai.entity.mysql.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);


}
