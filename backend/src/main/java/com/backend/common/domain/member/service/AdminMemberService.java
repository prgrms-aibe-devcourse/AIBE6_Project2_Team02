package com.backend.common.domain.member.service;

import com.backend.common.domain.member.entity.Member;
import com.backend.common.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminMemberService {

    private final MemberRepository memberRepository;

    public List<Member> searchMembers(String nickname) {
        if (nickname == null || nickname.trim().isEmpty()) {
            return memberRepository.findAll();
        }
        return memberRepository.findByNicknameContaining(nickname.trim());
    }

    @Transactional
    public void suspendMember(Long memberId, int days) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException("Member not found"));
        member.suspend(days);
    }

    @Transactional
    public void activateMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException("Member not found"));
        member.activate();
    }
}
