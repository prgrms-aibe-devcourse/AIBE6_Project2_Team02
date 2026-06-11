package com.backend.common.domain.techstack.repository;

import com.backend.common.domain.techstack.entity.TechStack;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TechStackRepository extends JpaRepository<TechStack, Long> {
}
