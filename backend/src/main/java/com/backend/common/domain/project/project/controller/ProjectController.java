package com.backend.common.domain.project.project.controller;

import com.backend.common.domain.project.exception.ProjectNotFoundException;
import com.backend.common.global.exception.exception.ResourceNotFoundException;
import com.backend.api.dto.ProjectResponse;
import com.backend.api.service.PublicApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final PublicApiService publicApiService;

    @GetMapping
    public List<ProjectResponse> getProjects() {
        return publicApiService.getProjects();
    }

    @GetMapping("/{id}")
    public ProjectResponse getProject(@PathVariable Long id) {
        try {
            return publicApiService.getProject(id);
        } catch (NoSuchElementException ex) {
            throw new ProjectNotFoundException("404","Project not found");
        }
    }


}
