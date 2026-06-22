package com.backend.common.domain.project.project.service;

import com.backend.common.domain.member.entity.Member;
import com.backend.common.domain.notification.entity.NotificationType;
import com.backend.common.domain.notification.service.NotificationService;
import com.backend.common.domain.project.application.entity.ProjectApplication;
import com.backend.common.domain.project.application.repository.ProjectApplicationRepository;
import com.backend.common.domain.project.enums.PositionType;
import com.backend.common.domain.project.enums.SelectionStatus;
import com.backend.common.domain.project.project.entity.Project;
import com.backend.common.domain.project.project.entity.ProjectMember;
import com.backend.common.domain.project.project.repository.ProjectMemberRepository;
import com.backend.common.domain.project.project.repository.ProjectRepository;
import com.backend.common.domain.project.project.repository.ProjectViewRepository;
import com.backend.common.global.exception.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MyPageProjectServiceTest {

    @Mock private ProjectRepository projectRepository;
    @Mock private ProjectApplicationRepository projectApplicationRepository;
    @Mock private ProjectViewRepository projectViewRepository;
    @Mock private ProjectMemberRepository projectMemberRepository;
    @Mock private NotificationService notificationService;

    @InjectMocks private MyPageProjectService myPageProjectService;

    // ================= 최근 본 목록 삭제 테스트 =================

    @Test
    @DisplayName("최근 본 프로젝트 내역 개별 삭제 - 레포지토리 호출 위임 검증")
    void deleteRecentlyViewedProject_success() {
        // when
        myPageProjectService.deleteRecentlyViewedProject(1L, 10L);

        // then
        verify(projectViewRepository, times(1)).deleteByMemberIdAndProjectId(1L, 10L);
    }

    // ================= 프로젝트 공고 지원서 승인/거절 테스트 =================

    @Test
    @DisplayName("지원서 수락 성공 - 팀원 명부 추가 및 지원자에게 수락 알림 발송")
    void handleApplicationAction_accept_success() {
        // given
        ProjectApplication application = mock(ProjectApplication.class);
        Project project = mock(Project.class);
        Member applicant = mock(Member.class);

        given(projectApplicationRepository.findById(50L)).willReturn(Optional.of(application));
        given(application.getProject()).willReturn(project);
        given(application.getApplicant()).willReturn(applicant);
        given(application.getPosition()).willReturn(PositionType.BACKEND);
        given(application.getId()).willReturn(50L);
        given(project.getId()).willReturn(10L);
        given(project.getTitle()).willReturn("테스트 프로젝트");

        // when
        myPageProjectService.handleApplicationAction(1L, 50L, true);

        // then
        verify(application, times(1)).accept();
        verify(projectMemberRepository, times(1)).save(any(ProjectMember.class));
        verify(notificationService, times(1)).notify(
                eq(applicant), eq(NotificationType.APPLICATION_ACCEPTED),
                anyString(), anyString(), eq("/projects/10"), eq(50L));
    }

    @Test
    @DisplayName("지원서 거절 성공 - 지원 내역 삭제 및 지원자에게 거절 알림 발송")
    void handleApplicationAction_reject_success() {
        // given
        ProjectApplication application = mock(ProjectApplication.class);
        Project project = mock(Project.class);
        Member applicant = mock(Member.class);

        given(projectApplicationRepository.findById(50L)).willReturn(Optional.of(application));
        given(application.getProject()).willReturn(project);
        given(application.getApplicant()).willReturn(applicant);
        given(application.getId()).willReturn(50L);
        given(project.getTitle()).willReturn("테스트 프로젝트");

        // when
        myPageProjectService.handleApplicationAction(1L, 50L, false);

        // then
        verify(projectApplicationRepository, times(1)).delete(application);
        verify(projectMemberRepository, never()).save(any());
        verify(notificationService, times(1)).notify(
                eq(applicant), eq(NotificationType.APPLICATION_REJECTED),
                anyString(), anyString(), isNull(), eq(50L));
    }

    @Test
    @DisplayName("지원서 처리 예외 - 존재하지 않는 지원서 ID는 ResourceNotFoundException 발생")
    void handleApplicationAction_notFound_throws() {
        given(projectApplicationRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> myPageProjectService.handleApplicationAction(1L, 999L, true))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ================= 내가 신청한 지원 취소(철회) 테스트 =================

    @Test
    @DisplayName("내가 넣은 프로젝트 지원 취소 성공")
    void cancelProjectApplication_success() {
        // given
        ProjectApplication application = mock(ProjectApplication.class);
        given(projectApplicationRepository.findById(50L)).willReturn(Optional.of(application));
        given(application.getStatus()).willReturn(SelectionStatus.PENDING);

        // when
        myPageProjectService.cancelProjectApplication(1L, 50L);

        // then
        verify(projectApplicationRepository, times(1)).delete(application);
    }

    @Test
    @DisplayName("지원 취소 실패 - 내역 없음 예외")
    void cancelProjectApplication_notFound_throws() {
        // when & then
        assertThatThrownBy(() -> myPageProjectService.cancelProjectApplication(1L, 999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
