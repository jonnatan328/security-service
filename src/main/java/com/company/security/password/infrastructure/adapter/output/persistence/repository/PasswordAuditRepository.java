package com.company.security.password.infrastructure.adapter.output.persistence.repository;

import com.company.security.password.infrastructure.adapter.output.persistence.document.PasswordAuditDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PasswordAuditRepository extends ReactiveMongoRepository<PasswordAuditDocument, String> {
}
