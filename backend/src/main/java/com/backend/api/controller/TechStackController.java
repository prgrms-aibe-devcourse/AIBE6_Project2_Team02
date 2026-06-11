package com.backend.api.controller;

import com.backend.api.service.PublicApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tech-stacks")
@RequiredArgsConstructor
public class TechStackController {

    private final PublicApiService publicApiService;

    @GetMapping
    public List<String> getPopularTechStacks() {
        return publicApiService.getPopularTechStacks();
    }
}
