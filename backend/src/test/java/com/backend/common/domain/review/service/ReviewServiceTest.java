package com.backend.common.domain.review.service;

import com.backend.common.domain.member.entity.Member;
import com.backend.common.domain.member.repository.MemberRepository;
import com.backend.common.domain.notification.entity.NotificationType;
import com.backend.common.domain.notification.service.NotificationService;
import com.backend.common.domain.project.enums.ProjectStatus;
import com.backend.common.domain.project.project.entity.Project;
import com.backend.common.domain.project.project.repository.ProjectMemberRepository;
import com.backend.common.domain.project.project.repository.ProjectRepository;
import com.backend.common.domain.review.dto.CreateReviewRequest;
import com.backend.common.domain.review.entity.Review;
import com.backend.common.domain.review.repository.ReviewRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock private ReviewRepository reviewRepository;
    @Mock private MemberRepository memberRepository;
    @Mock private ProjectRepository projectRepository;
    @Mock private ProjectMemberRepository projectMemberRepository;
    @Mock private ObjectMapper objectMapper;
    @Mock private NotificationService notificationService;

    @InjectMocks
    private ReviewService reviewService;

    private CreateReviewRequest buildRequest(Long projectId, Long revieweeId, Map<String, String> content) {
        CreateReviewRequest request = new CreateReviewRequest();
        ReflectionTestUtils.setField(request, "projectId", projectId);
        ReflectionTestUtils.setField(request, "revieweeId", revieweeId);
        ReflectionTestUtils.setField(request, "content", content);
        return request;
    }

    @Test
    @DisplayName("리뷰 작성 성공 - 리뷰 저장 및 리뷰 대상자에게 알림 발송")
    void createReview_success() throws Exception {
        Project project = mock(Project.class);
        Member reviewer = mock(Member.class);
        Member reviewee = mock(Member.class);
        Review savedReview = mock(Review.class);

        CreateReviewRequest request = buildRequest(1L, 2L, Map.of("q1", "잘했어요"));

        given(projectRepository.findById(1L)).willReturn(Optional.of(project));
        given(project.getId()).willReturn(1L);
        given(project.getStatus()).willReturn(ProjectStatus.COMPLETED);
        given(projectMemberRepository.existsByProjectIdAndMemberId(1L, 10L)).willReturn(true);
        given(projectMemberRepository.existsByProjectIdAndMemberId(1L, 2L)).willReturn(true);
        given(reviewRepository.existsByProjectIdAndReviewerIdAndRevieweeId(1L, 10L, 2L)).willReturn(false);
        given(memberRepository.findById(10L)).willReturn(Optional.of(reviewer));
        given(memberRepository.findById(2L)).willReturn(Optional.of(reviewee));
        given(reviewer.getNickname()).willReturn("철수");
        given(objectMapper.writeValueAsString(any())).willReturn("{\"q1\":\"잘했어요\"}");
        given(reviewRepository.save(any(Review.class))).willReturn(savedReview);
        given(savedReview.getId()).willReturn(300L);

        Long reviewId = reviewService.createReview(10L, request);

        assertThat(reviewId).isEqualTo(300L);
        verify(reviewRepository, times(1)).save(any(Review.class));
        verify(notificationService, times(1)).notify(
                eq(reviewee),
                eq(NotificationType.REVIEW_RECEIVED),
                anyString(),
                anyString(),
                eq("/mypage?tab=review"),
                eq(300L)
        );
    }

    @Test
    @DisplayName("리뷰 작성 실패 - 완료된 프로젝트가 아니면 예외 발생, 알림 미발송")
    void createReview_projectNotCompleted_throws() {
        Project project = mock(Project.class);
        CreateReviewRequest request = buildRequest(1L, 2L, Map.of("q1", "내용"));

        given(projectRepository.findById(1L)).willReturn(Optional.of(project));
        given(project.getStatus()).willReturn(ProjectStatus.RECRUITING);

        assertThatThrownBy(() -> reviewService.createReview(10L, request))
                .isInstanceOf(IllegalStateException.class);

        verify(reviewRepository, never()).save(any());
        verifyNoInteractions(notificationService);
    }

    @Test
    @DisplayName("리뷰 작성 실패 - 이미 작성한 리뷰가 있으면 예외 발생, 알림 미발송")
    void createReview_duplicateReview_throws() {
        Project project = mock(Project.class);
        CreateReviewRequest request = buildRequest(1L, 2L, Map.of("q1", "내용"));

        given(projectRepository.findById(1L)).willReturn(Optional.of(project));
        given(project.getId()).willReturn(1L);
        given(project.getStatus()).willReturn(ProjectStatus.COMPLETED);
        given(projectMemberRepository.existsByProjectIdAndMemberId(1L, 10L)).willReturn(true);
        given(projectMemberRepository.existsByProjectIdAndMemberId(1L, 2L)).willReturn(true);
        given(reviewRepository.existsByProjectIdAndReviewerIdAndRevieweeId(1L, 10L, 2L)).willReturn(true);

        assertThatThrownBy(() -> reviewService.createReview(10L, request))
                .isInstanceOf(IllegalStateException.class);

        verify(reviewRepository, never()).save(any());
        verifyNoInteractions(notificationService);
    }
}
