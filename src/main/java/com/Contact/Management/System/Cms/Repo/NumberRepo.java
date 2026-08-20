package com.Contact.Management.System.Cms.Repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NumberRepo extends JpaRepository<Long,Number> {
}
