package com.backend.common.domain.member.repository;

import com.backend.common.domain.techstack.entity.MemberTechStack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MemberTechStackRepository extends JpaRepository<MemberTechStack, Long> {

    List<MemberTechStack> findByMemberId(Long memberId);

    @Query("SELECT mts FROM MemberTechStack mts JOIN FETCH mts.techStack WHERE mts.member.id = :memberId")
    List<MemberTechStack> findByMemberIdWithTechStack(@Param("memberId") Long memberId);
}
