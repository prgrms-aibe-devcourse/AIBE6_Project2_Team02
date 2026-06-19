package com.backend.common.domain.project.project.dto;

import com.backend.common.domain.member.entity.Member;

import java.util.List;

public record MyApplicant(
        List<Member> Applicant        // RECRUITING, IN_PROGRESS 등
) {

    public MyApplicant(List<Member> Applicant) {
        this.Applicant = Applicant;
    }
}
