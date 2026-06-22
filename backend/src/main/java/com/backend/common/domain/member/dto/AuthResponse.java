package com.backend.common.domain.member.dto;

import com.backend.common.domain.member.entity.MemberRole;

public record AuthResponse(Long memberId, String nickname, String profileImageUrl, MemberRole role) {
}
