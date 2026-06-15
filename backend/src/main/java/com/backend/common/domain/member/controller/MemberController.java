package com.backend.common.domain.member.controller;

import com.backend.common.domain.member.exception.MemberNotFoundException;
import com.backend.common.domain.project.dto.UserResponse;
import com.backend.common.domain.project.project.service.ProjectService;
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
    public List<UserResponse> getMembers() {
        return projectService.getMembers();
    }

    @GetMapping("/{id}")
    public UserResponse getMember(@PathVariable Long id) {
        try {
            return projectService.getMember(id);
        } catch (NoSuchElementException ex) {
            throw new MemberNotFoundException("404","Member not found");
        }
    }
}
