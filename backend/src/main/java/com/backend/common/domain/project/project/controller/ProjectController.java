package com.backend.common.domain.project.project.controller;

import com.backend.common.domain.member.entity.Member;
import com.backend.common.domain.project.application.entity.ProjectApplication;
import com.backend.common.domain.project.dto.*;
import com.backend.common.domain.project.enums.ProjectStatus;
import com.backend.common.domain.project.exception.ProjectNotFoundException;
import com.backend.common.domain.project.project.dto.MyApplicant;
import com.backend.common.domain.project.project.entity.Project;
import com.backend.common.domain.project.project.entity.ProjectMember;
import com.backend.common.domain.project.project.service.ProjectService;
import com.backend.common.global.rsdata.RsData;
import com.backend.common.global.security.userdetails.CustomMemberDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
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
    public RsData<ProjectResponse> getProject(@PathVariable Long id,
                                              @AuthenticationPrincipal CustomMemberDetails userDetails) {
        try {
            ProjectResponse response = projectService.getProject(id);

            if (userDetails != null && userDetails.getMemberId() != null) {
                projectService.makeProjectView(id, userDetails.getMemberId());
            }

            return RsData.of("200", "프로젝트 조회 성공", response);
        } catch (NoSuchElementException ex) {
            throw new ProjectNotFoundException("404","Project not found");
        }
    }

        @GetMapping("/man/{id}")
    public RsData<ProjectResponse_manage> getProject_manage(@PathVariable Long id,
                                              @AuthenticationPrincipal CustomMemberDetails userDetails) {
        try {
            ProjectResponse_manage response = projectService.getProject_manage(id);

            if (userDetails != null && userDetails.getMemberId() != null) {
                projectService.makeProjectView(id, userDetails.getMemberId());
            }

            return RsData.of("200", "프로젝트 조회 성공", response);
        } catch (NoSuchElementException ex) {
            throw new ProjectNotFoundException("404","Project not found");
        }
    }


    @PostMapping
    public ResponseEntity<RsData<ProjectResponse>> createProject(
            @RequestBody ProjectCreateRequest req,
            @AuthenticationPrincipal CustomMemberDetails principal
    ) {
        if (principal == null) {
            throw new InsufficientAuthenticationException("Login is required");
        }

        ProjectResponse project = projectService.createProject(req, principal.getMemberId());
        return ResponseEntity.ok(RsData.of("200", "프로젝트 생성 성공", project));
    }
    @GetMapping("/{id}/permissions")
    public RsData<ProjectPermissionResponse> getProjectPermissions(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomMemberDetails principal
    ) {
        boolean canEdit = principal != null
                && projectService.canEditProject(id, principal.getMemberId());
        boolean isMember = principal != null
                && projectService.isProjectMember(id, principal.getMemberId());
        Long pendingApplicationId = principal == null
                ? null
                : projectService.findPendingApplicationId(id, principal.getMemberId()).orElse(null);
        return RsData.of(
                "200",
                "프로젝트 권한 조회 성공",
                new ProjectPermissionResponse(canEdit, isMember, pendingApplicationId)
        );
    }

    @PutMapping("/{id}")
    public RsData<ProjectResponse> updateProject(
            @PathVariable Long id,
            @RequestBody ProjectUpdateRequest req,
            @AuthenticationPrincipal CustomMemberDetails principal
    ) {
        if (principal == null) {
            throw new InsufficientAuthenticationException("Login is required");
        }

        return RsData.of(
                "200",
                "프로젝트 수정 성공",
                projectService.updateProject(id, req, principal.getMemberId())
        );
    }

    @PostMapping("/{id}/applications")
    public RsData<ProjectApplicationCreateResponse> applyProject(
            @PathVariable Long id,
            @RequestBody ProjectApplicationCreateRequest request,
            @AuthenticationPrincipal CustomMemberDetails principal
    ) {
        if (principal == null) {
            throw new InsufficientAuthenticationException("Login is required");
        }

        Long applicationId = projectService.applyProject(id, principal.getMemberId(), request);
        return RsData.of(
                "200",
                "프로젝트 지원 신청이 완료되었습니다.",
                new ProjectApplicationCreateResponse(applicationId)
        );
    }
    @Transactional
    @GetMapping("/manage/{id}")
    public RsData<MyApplicant> MyApplications(@PathVariable Long id,
                                              @AuthenticationPrincipal CustomMemberDetails userDetails) {
        System.out.println(id);
        try {
            ProjectResponse_manage response = projectService.getProject_manage(id);

            if (userDetails != null && userDetails.getMemberId() != null) {
                projectService.makeProjectView(id, userDetails.getMemberId());
            }

        } catch (NoSuchElementException ex) {
            throw new ProjectNotFoundException("404","Project not found");
        }
        List<Member> members = projectService.getProjectApplication(id);
        return RsData.of("200", "프로젝트 지원자조회 성공", new MyApplicant(members));
    }
    @Transactional
    @PostMapping("/manageToTeam/{id}")
    public RsData<MyApplicant> ToTeam(@PathVariable Long id,   @RequestBody ProjectManage request,
                                      @AuthenticationPrincipal CustomMemberDetails userDetails) {
        ProjectMember projectMember = projectService.addMember(id, request.ProjectID());

        ProjectApplication projectApplication = projectService.delMember(id, request.ProjectID());
        List<Member> members;
        try {
            members = projectService.getProjectApplication(request.ProjectID());

            if (userDetails != null && userDetails.getMemberId() != null) {
                projectService.makeProjectView(request.ProjectID(), userDetails.getMemberId());
            }

        } catch (NoSuchElementException ex) {
            throw new ProjectNotFoundException("404","Project not found");
        }

        return RsData.of("200", "프로젝트 지원자조회 성공", new MyApplicant(members));
    }

    @PutMapping("/{id}/change")
    public RsData<ProjectResponse_manage> updateProject(
            @PathVariable Long id,
            @RequestBody StatusModify req,
            @AuthenticationPrincipal CustomMemberDetails principal
    ) {
        /*if (principal == null) {
            throw new InsufficientAuthenticationException("Login is required");
        }*/
        Project project = projectService.updateProjectByStatus(id, ProjectStatus.valueOf(req.status()));
        try {
            ProjectResponse_manage response = projectService.getProject_manage(id);

            if (principal != null && principal.getMemberId() != null) {
                projectService.makeProjectView(id, principal.getMemberId());
            }

            return RsData.of("200", "프로젝트 조회 성공", response);
        } catch (NoSuchElementException ex) {
            throw new ProjectNotFoundException("404","Project not found");
        }
    }
}
