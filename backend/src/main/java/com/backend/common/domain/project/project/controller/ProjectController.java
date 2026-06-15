package com.backend.common.domain.project.project.controller;

import com.backend.common.domain.project.dto.ProjectCreateRequest;
import com.backend.common.domain.project.dto.ProjectResponse;
import com.backend.common.domain.project.exception.ProjectNotFoundException;
import com.backend.common.domain.project.project.service.ProjectService;
import com.backend.common.global.rsdata.RsData;
import com.backend.common.global.security.MemberPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    public RsData<List<ProjectResponse>> getProjects() {
        return RsData.of("200", "프로젝트 목록 조회 성공", projectService.getProjects());
    }

    @GetMapping("/{id}")
    public RsData<ProjectResponse> getProject(@PathVariable Long id) {
        try {
            return RsData.of("200", "프로젝트 조회 성공", projectService.getProject(id));
        } catch (NoSuchElementException ex) {
            throw new ProjectNotFoundException("404","Project not found");
        }
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(
            @RequestBody ProjectCreateRequest req,
            @AuthenticationPrincipal MemberPrincipal principal
    ) {
        if (principal == null) {
            throw new InsufficientAuthenticationException("Login is required");
        }

        return ResponseEntity.ok(projectService.createProject(req, principal.memberId()));
    }
}
