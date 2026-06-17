package com.backend.common.domain.member.controller;

import com.backend.common.domain.member.entity.Member;
import com.backend.common.domain.member.exception.MemberNotFoundException;
import com.backend.common.domain.member.repository.MemberRepository;
import com.backend.common.domain.member.dto.UserResponse;
import com.backend.common.domain.project.project.service.ProjectService;
import com.backend.common.global.rsdata.RsData;
import com.backend.common.global.security.userdetails.CustomMemberDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final ProjectService projectService;
    private final MemberRepository memberRepository;

    @GetMapping
    public RsData<List<UserResponse>> getMembers() {
        return RsData.of("200", "회원 목록 조회 성공", projectService.getMembers());
    }

    @GetMapping("/{id}")
    public RsData<UserResponse> getMember(@PathVariable Long id) {
        try {
            return RsData.of("200", "회원 조회 성공", projectService.getMember(id));
        } catch (NoSuchElementException ex) {
            throw new MemberNotFoundException("404","Member not found");
        }
    }

    @DeleteMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public RsData<Void> withdrawMember(
            @AuthenticationPrincipal CustomMemberDetails userDetails
    ){
        Member member = memberRepository.findById(userDetails.getMemberId())
                .orElseThrow(() -> new MemberNotFoundException("404","회원 정보가 없습니다."));

        member.withdraw();
        memberRepository.save(member);
        return RsData.of("200","회원 탈퇴 성공");
    }
}
