package com.backend.common.domain.techstack.controller;

import com.backend.common.domain.project.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/tech-stacks")
@RequiredArgsConstructor
public class TechStackController {

    private final ProjectService projectService;

    @GetMapping
    public List<String> getPopularTechStacks() {
        return projectService.getPopularTechStacks();
    }
}
