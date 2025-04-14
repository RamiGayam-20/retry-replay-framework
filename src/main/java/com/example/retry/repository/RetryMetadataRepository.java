package com.example.retry.repository;

import com.example.retry.entity.RetryMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RetryMetadataRepository extends JpaRepository<RetryMetadata, Long> {
}