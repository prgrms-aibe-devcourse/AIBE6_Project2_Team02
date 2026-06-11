package com.backend.common.domain.project.entity;

import com.backend.common.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "project_applications", indexes = {
        @Index(name = "idx_project_applicant", columnList = "project_id, applicant_id", unique = true)
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id")
    private Member applicant;

    private String position;

    @Column(columnDefinition = "TEXT")
    private String message;

    private String status; // PENDING, ACCEPTED, REJECTED, CANCELLED

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
