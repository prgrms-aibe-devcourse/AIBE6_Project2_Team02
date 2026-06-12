package com.backend.common.domain.member.controller;

import com.backend.common.domain.member.exception.MemberNotFoundException;
import com.backend.common.global.exception.exception.ResourceNotFoundException;
import com.backend.api.dto.UserResponse;
import com.backend.api.service.PublicApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final PublicApiService publicApiService;

    @GetMapping
    public List<UserResponse> getMembers() {
        return publicApiService.getMembers();
    }

    @GetMapping("/{id}")
    public UserResponse getMember(@PathVariable Long id) {
        try {
            return publicApiService.getMember(id);
        } catch (NoSuchElementException ex) {
            throw new MemberNotFoundException("404","Member not found");
        }
    }
}
