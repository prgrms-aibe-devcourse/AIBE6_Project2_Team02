package com.backend.common.domain.member.repository;

import com.backend.common.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByNickname(String nickname);

    List<Member> findByNicknameContaining(String nickname);

    List<Member> findAllByStatusAndSuspensionUntilBefore(String status, LocalDateTime now);

}
