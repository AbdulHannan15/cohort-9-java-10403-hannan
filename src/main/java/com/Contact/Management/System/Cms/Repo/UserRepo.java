package com.Contact.Management.System.Cms.Repo;

import com.Contact.Management.System.Cms.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User, Long> {

    Optional<User> findByLoginIdentifier(String loginIdentifier);

    boolean existsByLoginIdentifier(String loginIdentifier);

    boolean existsByRecoveryPhone(String recoveryPhone);
}
