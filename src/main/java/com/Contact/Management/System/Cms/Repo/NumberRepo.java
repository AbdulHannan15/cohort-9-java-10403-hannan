package com.Contact.Management.System.Cms.Repo;

import com.Contact.Management.System.Cms.Entity.PhoneNumberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NumberRepo extends JpaRepository<PhoneNumberEntity, Long> {

    boolean existsByNumber(String number);
}
