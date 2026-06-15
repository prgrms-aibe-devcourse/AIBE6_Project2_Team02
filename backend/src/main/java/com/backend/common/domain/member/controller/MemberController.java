package com.backend.common.domain.member.controller;

import com.backend.common.domain.member.exception.MemberNotFoundException;
import com.backend.common.domain.project.dto.UserResponse;
import com.backend.common.domain.project.project.service.ProjectService;
import com.backend.common.global.rsdata.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final ProjectService projectService;

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
}
