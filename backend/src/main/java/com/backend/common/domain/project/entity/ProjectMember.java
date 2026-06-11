package com.backend.common.domain.project.entity;

import com.backend.common.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "project_members", indexes = {
        @Index(name = "idx_project_member", columnList = "project_id, member_id", unique = true)
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private String position;     // Backend, Frontend 등
    private String role;         // Leader, Manager, Member 등
    private String memberStatus; // ACTIVE, LEFT, REMOVED

    private LocalDateTime joinedAt;
    private LocalDateTime leftAt;

    private boolean isHidden;    // 마이페이지 숨김 여부 플래그
}
