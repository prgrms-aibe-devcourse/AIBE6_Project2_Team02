package com.backend.common.domain.project.project.controller;

import com.backend.api.controller.ResourceNotFoundException;
import com.backend.api.dto.ProjectCreateRequest;
import com.backend.api.dto.ProjectResponse;
import com.backend.api.service.PublicApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
            throw new ResourceNotFoundException("Project not found");
        }
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(@RequestBody ProjectCreateRequest req) {
        try {
            ProjectResponse created = publicApiService.createProject(req);
            return ResponseEntity.ok(created);
        } catch (NoSuchElementException ex) {
            throw new ResourceNotFoundException(ex.getMessage());
        } catch (IllegalArgumentException ex) {
            throw new ResourceNotFoundException("Invalid request: " + ex.getMessage());
        }
    }
}


