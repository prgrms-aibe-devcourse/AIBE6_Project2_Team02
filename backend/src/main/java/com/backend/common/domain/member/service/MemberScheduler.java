package com.backend.common.domain.member.service;

import com.backend.common.domain.member.entity.Member;
import com.backend.common.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberScheduler {

    private final MemberRepository memberRepository;

    @Transactional
    @Scheduled(cron = "0 0 * * * *") // 매 시간 정각 실행
    public void checkSuspensionExpirations() {
        log.info("Checking for expired member suspensions...");
        LocalDateTime now = LocalDateTime.now();
        List<Member> expiredMembers = memberRepository.findAllByStatusAndSuspensionUntilBefore("SUSPENDED", now);

        for (Member member : expiredMembers) {
            log.info("Reactivating member: {}", member.getNickname());
            member.activate();
        }
    }
}
