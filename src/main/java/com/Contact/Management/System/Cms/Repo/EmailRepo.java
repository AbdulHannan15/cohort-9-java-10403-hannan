package com.Contact.Management.System.Cms.Repo;

import com.Contact.Management.System.Cms.Entity.EmailEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailRepo extends JpaRepository<EmailEntity, Long> {

    boolean existsByEmail(String email);
}
