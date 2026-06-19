package com.backend.common.domain.member.controller;

import com.backend.common.domain.member.entity.Member;
import com.backend.common.domain.member.service.AdminMemberService;
import com.backend.common.global.rsdata.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/members")
@RequiredArgsConstructor
public class AdminMemberController {

    private final AdminMemberService adminMemberService;

    @GetMapping
    public ResponseEntity<RsData<List<Member>>> searchMembers(@RequestParam(required = false) String nickname) {
        List<Member> members = adminMemberService.searchMembers(nickname);
        return ResponseEntity.ok(
                RsData.of("200", "유저 목록 조회 성공", members)
        );
    }

    @PatchMapping("/{memberId}/suspend")
    public ResponseEntity<RsData<Void>> suspendMember(
            @PathVariable Long memberId,
            @RequestParam int days
    ) {
        adminMemberService.suspendMember(memberId, days);
        return ResponseEntity.ok(
                RsData.of("200", "유저의 정지 기한이 업데이트되었습니다.")
        );
    }

    @PatchMapping("/{memberId}/activate")
    public ResponseEntity<RsData<Void>> activateMember(@PathVariable Long memberId) {
        adminMemberService.activateMember(memberId);
        return ResponseEntity.ok(
                RsData.of("200", "유저의 정지 기한이 초기화되었습니다.")
        );
    }
}
