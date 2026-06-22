package com.backend.common.domain.project.project.entity;

import com.backend.common.domain.member.entity.Member;
import com.backend.common.domain.project.enums.PositionType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
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
    private ProjectMemberStatus memberStatus; // ACTIVE, LEFT, REMOVED

    private LocalDateTime joinedAt;
    private LocalDateTime leftAt;

    private boolean isHidden;    // 마이페이지 숨김 여부 플래그

    @Builder
    public ProjectMember(Project project, Member member, PositionType position, ProjectRole role) {
        this.project = project;
        this.member = member;
        this.position = position;
        this.role = role;

        // 중요: 초기 생성 시점의 기본 비즈니스 규칙은 빌더로 받지 않고 내부에서 강제 세팅!
        this.memberStatus = ProjectMemberStatus.ACTIVE;
        this.joinedAt = LocalDateTime.now();
        this.isHidden = false;
    }

    public void updatePosition(PositionType position) {
        this.position = position;
    }

    public void kickMember() {
        if (this.role == ProjectRole.LEADER) {
            throw new IllegalStateException("프로젝트 팀장은 자기 자신을 방출할 수 없습니다.");
        }
        if (this.memberStatus == ProjectMemberStatus.REMOVED || this.memberStatus == ProjectMemberStatus.LEFT) {
            throw new IllegalStateException("이미 프로젝트를 나갔거나 방출된 회원입니다.");
        }

        // 상태 변경 및 탈퇴 시간 기록
        this.memberStatus = ProjectMemberStatus.REMOVED;
        this.leftAt = LocalDateTime.now();
    }

    public void leaveTeam() {
        if (this.role == ProjectRole.LEADER) {
            throw new IllegalStateException("프로젝트 리더는 탈퇴할 수 없습니다.");
        }
        if (this.memberStatus == ProjectMemberStatus.REMOVED || this.memberStatus == ProjectMemberStatus.LEFT) {
            throw new IllegalStateException("이미 프로젝트를 나갔거나 방출된 회원입니다.");
        }

        this.memberStatus = ProjectMemberStatus.LEFT;
        this.leftAt = LocalDateTime.now();
    }

    /**
     *  프로젝트 멤버 권한 수정 도메인 로직
     */
    public void updateRole(ProjectRole newRole) {
        if (this.memberStatus != ProjectMemberStatus.ACTIVE) {
            throw new IllegalStateException("현재 활성화 상태가 아닌 멤버의 권한은 수정할 수 없습니다.");
        }
        if (newRole == null) {
            throw new IllegalArgumentException("변경할 권한 값이 유효하지 않습니다.");
        }

        // 권한 등급 덮어쓰기
        this.role = newRole;
    }

}
