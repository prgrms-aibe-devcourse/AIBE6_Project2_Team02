package com.backend.common.domain.member.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String nickname;

    private String profileImageUrl;

    private String fcmToken;

    @Column(length = 20)
    private String status = "ACTIVE"; // ACTIVE, SUSPENDED, BANNED

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private MemberRole role = MemberRole.ROLE_USER;

    private LocalDateTime suspensionUntil;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public static Member create(String nickname, String profileImageUrl) {
        Member member = new Member();
        member.nickname = nickname;
        member.profileImageUrl = profileImageUrl;
        member.status = "ACTIVE";
        member.role = MemberRole.ROLE_USER;
        member.createdAt = LocalDateTime.now();
        member.updatedAt = member.createdAt;
        return member;
    }

    // 더미 데이터용 관리자 계정 생성 팩토리 메서드
    public static Member createAdmin(String nickname, String profileImageUrl) {
        Member member = create(nickname, profileImageUrl);
        member.role = MemberRole.ROLE_ADMIN;
        return member;
    }

    public void updateProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
        this.updatedAt = LocalDateTime.now();
    }

    public void withdraw() {
        this.status = "WITHDRAWN";
        this.deletedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void suspend(int days) {
        this.status = "SUSPENDED";
        this.suspensionUntil = LocalDateTime.now().plusDays(days);
        this.updatedAt = LocalDateTime.now();
    }

    public void activate() {
        this.status = "ACTIVE";
        this.suspensionUntil = null;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateNickName(String nickname){
        this.nickname = nickname;
    }

    public void updateFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
        this.updatedAt = LocalDateTime.now();
    }

}
