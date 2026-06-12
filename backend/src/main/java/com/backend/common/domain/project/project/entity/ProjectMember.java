package com.backend.common.domain.project.project.entity;

import com.backend.common.domain.member.entity.Member;
import com.backend.common.domain.member.entity.MemberStatus;
import com.backend.common.domain.project.enums.PositionType;
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

    @Enumerated(EnumType.STRING)
    private PositionType position;     // BACKEND, FRONTEND 등

    @Enumerated(EnumType.STRING)
    private ProjectRole role;         // LEADER, MANAGER, MEMBER

    @Enumerated(EnumType.STRING)
    private MemberStatus memberStatus; // ACTIVE, LEFT, REMOVED

    private LocalDateTime joinedAt;
    private LocalDateTime leftAt;

    private boolean isHidden;    // 마이페이지 숨김 여부 플래그
    public static ProjectMember create(Project project, Member member, PositionType position, ProjectRole role) {
        ProjectMember projectMember = new ProjectMember();
        projectMember.project = project;
        projectMember.member = member;
        projectMember.position = position;
        projectMember.role = role;
        projectMember.memberStatus = MemberStatus.ACTIVE;
        projectMember.joinedAt = LocalDateTime.now();
        projectMember.isHidden = false;
        return projectMember;
    }
}
