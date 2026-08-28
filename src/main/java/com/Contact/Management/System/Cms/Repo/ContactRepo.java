package com.Contact.Management.System.Cms.Repo;

import com.Contact.Management.System.Cms.Entity.Contact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactRepo extends JpaRepository<Contact, Long> {

    Page<Contact> findByUserId(Long userId, Pageable pageable);

    @Query("SELECT c FROM Contact c WHERE c.user.id = :userId " +
            "AND (LOWER(c.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(c.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Contact> searchByUserIdAndName(@Param("userId") Long userId,
                                         @Param("keyword") String keyword,
                                         Pageable pageable);
}
