package com.backend.common.domain.project.project.repository;

import com.backend.common.domain.project.enums.ProjectCategory;
import com.backend.common.domain.project.enums.ProjectStatus;
import com.backend.common.domain.project.project.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Set;

public interface ProjectRepositoryCustom {

    Page<Project> searchProjects(
            String search,
            ProjectCategory category,
            String tech,
            Set<ProjectStatus> statuses,
            Pageable pageable
    );
}
