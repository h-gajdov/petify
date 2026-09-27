package com.petify.petify.service;

import com.petify.petify.domain.ClinicReview;
import com.petify.petify.domain.Review;
import com.petify.petify.domain.User;
import com.petify.petify.domain.UserReview;
import com.petify.petify.dto.CreateReviewRequest;
import com.petify.petify.dto.ReviewDTO;
import com.petify.petify.repo.AppointmentRepository;
import com.petify.petify.repo.ClinicReviewRepository;
import com.petify.petify.repo.ReviewRepository;
import com.petify.petify.repo.UserReviewRepository;
import com.petify.petify.repo.UserRepository;
import com.petify.petify.repo.VetClinicRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReviewServiceTest {

    private static final Long REVIEWER_ID = 7L;
    private static final Long TARGET_ID = 8L;
    private static final Long CLINIC_ID = 3L;
    private static final Long REVIEW_ID = 101L;

    @Mock private ReviewRepository reviewRepository;
    @Mock private UserReviewRepository userReviewRepository;
    @Mock private ClinicReviewRepository clinicReviewRepository;
    @Mock private UserRepository userRepository;
    @Mock private VetClinicRepository vetClinicRepository;
    @Mock private AppointmentRepository appointmentRepository;

    @InjectMocks private ReviewService reviewService;

    private static User user(Long id) {
        User user = new User("user" + id, "user" + id + "@petify.test", "secret", "Ana", "Ilieva");
        user.setUserId(id);
        return user;
    }

    private static CreateReviewRequest request(Integer rating) {
        return new CreateReviewRequest(rating, "Great service");
    }

    private static Review review(Long id, User reviewer, int rating, boolean deleted) {
        Review review = new Review(reviewer, rating, "existing comment");
        review.setReviewId(id);
        review.setIsDeleted(deleted);
        return review;
    }

    @Test
    void createReview_ratingInvalid() {
        assertThatThrownBy(() -> reviewService.createReview(REVIEWER_ID, TARGET_ID, request(null)))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Rating must be between 1 and 5");

        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    void createReview_reviewerNotFound() {
        when(userRepository.findById(REVIEWER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.createReview(REVIEWER_ID, TARGET_ID, request(5)))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Reviewer not found");
    }

    @Test
    void createReview_targetNotFound() {
        when(userRepository.findById(REVIEWER_ID)).thenReturn(Optional.of(user(REVIEWER_ID)));
        when(userRepository.findById(TARGET_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.createReview(REVIEWER_ID, TARGET_ID, request(5)))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Target user not found");
    }

    @Test
    void createReview_alreadyReviewedAndNotDeleted() {
        when(userRepository.findById(REVIEWER_ID)).thenReturn(Optional.of(user(REVIEWER_ID)));
        when(userRepository.findById(TARGET_ID)).thenReturn(Optional.of(user(TARGET_ID)));

        Review existing = review(REVIEW_ID, user(REVIEWER_ID), 4, false);
        UserReview existingUserReview = new UserReview(existing, TARGET_ID);
        when(userReviewRepository.findTopByReviewReviewerUserIdAndTargetUserIdAndReviewIsDeletedFalseOrderByReviewCreatedAtDesc(REVIEWER_ID, TARGET_ID))
            .thenReturn(Optional.of(existingUserReview));

        assertThatThrownBy(() -> reviewService.createReview(REVIEWER_ID, TARGET_ID, request(5)))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("You have already reviewed this user");

        verify(reviewRepository, never()).saveAndFlush(any());
    }

    @Test
    void createReview_existingDeletedReview_thenSaveFails() {
        when(userRepository.findById(REVIEWER_ID)).thenReturn(Optional.of(user(REVIEWER_ID)));
        when(userRepository.findById(TARGET_ID)).thenReturn(Optional.of(user(TARGET_ID)));

        Review existing = review(REVIEW_ID, user(REVIEWER_ID), 4, true);
        UserReview existingUserReview = new UserReview(existing, TARGET_ID);
        when(userReviewRepository.findTopByReviewReviewerUserIdAndTargetUserIdAndReviewIsDeletedFalseOrderByReviewCreatedAtDesc(REVIEWER_ID, TARGET_ID))
            .thenReturn(Optional.of(existingUserReview));
        when(reviewRepository.saveAndFlush(any(Review.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThatThrownBy(() -> reviewService.createReview(REVIEWER_ID, TARGET_ID, request(5)))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Failed to save review - ID is null");

        verify(userReviewRepository, never()).saveAndFlush(any());
    }

    @Test
    void createReview_existingDeletedReview_thenSuccess() {
        when(userRepository.findById(REVIEWER_ID)).thenReturn(Optional.of(user(REVIEWER_ID)));
        when(userRepository.findById(TARGET_ID)).thenReturn(Optional.of(user(TARGET_ID)));

        Review existing = review(REVIEW_ID, user(REVIEWER_ID), 4, true);
        UserReview existingUserReview = new UserReview(existing, TARGET_ID);
        when(userReviewRepository.findTopByReviewReviewerUserIdAndTargetUserIdAndReviewIsDeletedFalseOrderByReviewCreatedAtDesc(REVIEWER_ID, TARGET_ID))
            .thenReturn(Optional.of(existingUserReview));
        when(reviewRepository.saveAndFlush(any(Review.class))).thenAnswer(inv -> {
            Review r = inv.getArgument(0);
            r.setReviewId(202L);
            return r;
        });
        when(userReviewRepository.saveAndFlush(any(UserReview.class))).thenAnswer(inv -> inv.getArgument(0));

        ReviewDTO result = reviewService.createReview(REVIEWER_ID, TARGET_ID, request(5));

        assertThat(result.getReviewId()).isEqualTo(202L);
        assertThat(result.getRating()).isEqualTo(5);
    }

    @Test
    void createReview_noExistingReview_thenSaveFails() {
        when(userRepository.findById(REVIEWER_ID)).thenReturn(Optional.of(user(REVIEWER_ID)));
        when(userRepository.findById(TARGET_ID)).thenReturn(Optional.of(user(TARGET_ID)));
        when(userReviewRepository.findTopByReviewReviewerUserIdAndTargetUserIdAndReviewIsDeletedFalseOrderByReviewCreatedAtDesc(REVIEWER_ID, TARGET_ID))
            .thenReturn(Optional.empty());
        when(reviewRepository.saveAndFlush(any(Review.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThatThrownBy(() -> reviewService.createReview(REVIEWER_ID, TARGET_ID, request(5)))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Failed to save review - ID is null");
    }

    @Test
    void createReview_noExistingReview_thenSuccess() {
        when(userRepository.findById(REVIEWER_ID)).thenReturn(Optional.of(user(REVIEWER_ID)));
        when(userRepository.findById(TARGET_ID)).thenReturn(Optional.of(user(TARGET_ID)));
        when(userReviewRepository.findTopByReviewReviewerUserIdAndTargetUserIdAndReviewIsDeletedFalseOrderByReviewCreatedAtDesc(REVIEWER_ID, TARGET_ID))
            .thenReturn(Optional.empty());
        when(reviewRepository.saveAndFlush(any(Review.class))).thenAnswer(inv -> {
            Review r = inv.getArgument(0);
            r.setReviewId(303L);
            return r;
        });
        when(userReviewRepository.saveAndFlush(any(UserReview.class))).thenAnswer(inv -> inv.getArgument(0));

        ReviewDTO result = reviewService.createReview(REVIEWER_ID, TARGET_ID, request(5));

        assertThat(result.getReviewId()).isEqualTo(303L);
        assertThat(result.getReviewerId()).isEqualTo(REVIEWER_ID);
    }

    @Test
    void createClinicReview_ratingInvalid() {
        assertThatThrownBy(() -> reviewService.createClinicReview(REVIEWER_ID, CLINIC_ID, request(0)))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Rating must be between 1 and 5");
    }

    @Test
    void createClinicReview_reviewerNotFound() {
        when(userRepository.findById(REVIEWER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.createClinicReview(REVIEWER_ID, CLINIC_ID, request(5)))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Reviewer not found");
    }

    @Test
    void createClinicReview_clinicNotFound() {
        when(userRepository.findById(REVIEWER_ID)).thenReturn(Optional.of(user(REVIEWER_ID)));
        when(vetClinicRepository.existsById(CLINIC_ID)).thenReturn(false);

        assertThatThrownBy(() -> reviewService.createClinicReview(REVIEWER_ID, CLINIC_ID, request(5)))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Clinic not found");
    }

    @Test
    void createClinicReview_noCompletedAppointment() {
        when(userRepository.findById(REVIEWER_ID)).thenReturn(Optional.of(user(REVIEWER_ID)));
        when(vetClinicRepository.existsById(CLINIC_ID)).thenReturn(true);
        when(appointmentRepository.existsByResponsibleOwnerUserIdAndClinicIdAndStatus(REVIEWER_ID, CLINIC_ID, "DONE"))
            .thenReturn(false);

        assertThatThrownBy(() -> reviewService.createClinicReview(REVIEWER_ID, CLINIC_ID, request(5)))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("You can review this clinic only after a completed appointment");
    }

    @Test
    void createClinicReview_alreadyReviewed() {
        when(userRepository.findById(REVIEWER_ID)).thenReturn(Optional.of(user(REVIEWER_ID)));
        when(vetClinicRepository.existsById(CLINIC_ID)).thenReturn(true);
        when(appointmentRepository.existsByResponsibleOwnerUserIdAndClinicIdAndStatus(REVIEWER_ID, CLINIC_ID, "DONE"))
            .thenReturn(true);

        Review existing = review(REVIEW_ID, user(REVIEWER_ID), 4, false);
        when(clinicReviewRepository.findTopByReviewReviewerUserIdAndTargetClinicIdAndReviewIsDeletedFalseOrderByReviewCreatedAtDesc(REVIEWER_ID, CLINIC_ID))
            .thenReturn(Optional.of(new ClinicReview(existing, CLINIC_ID)));

        assertThatThrownBy(() -> reviewService.createClinicReview(REVIEWER_ID, CLINIC_ID, request(5)))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("You have already reviewed this clinic");

        verify(reviewRepository, never()).saveAndFlush(any());
    }

    @Test
    void createClinicReview_success() {
        when(userRepository.findById(REVIEWER_ID)).thenReturn(Optional.of(user(REVIEWER_ID)));
        when(vetClinicRepository.existsById(CLINIC_ID)).thenReturn(true);
        when(appointmentRepository.existsByResponsibleOwnerUserIdAndClinicIdAndStatus(REVIEWER_ID, CLINIC_ID, "DONE"))
            .thenReturn(true);
        when(clinicReviewRepository.findTopByReviewReviewerUserIdAndTargetClinicIdAndReviewIsDeletedFalseOrderByReviewCreatedAtDesc(REVIEWER_ID, CLINIC_ID))
            .thenReturn(Optional.empty());
        when(reviewRepository.saveAndFlush(any(Review.class))).thenAnswer(inv -> {
            Review r = inv.getArgument(0);
            r.setReviewId(404L);
            return r;
        });

        ReviewDTO result = reviewService.createClinicReview(REVIEWER_ID, CLINIC_ID, request(5));

        assertThat(result.getReviewId()).isEqualTo(404L);
        verify(clinicReviewRepository).saveAndFlush(any(ClinicReview.class));
    }

    @Test
    void updateReview_ratingInvalid() {
        assertThatThrownBy(() -> reviewService.updateReview(REVIEW_ID, REVIEWER_ID, request(6)))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Rating must be between 1 and 5");

        verify(reviewRepository, never()).findById(any());
    }

    @Test
    void updateReview_notFound() {
        when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.updateReview(REVIEW_ID, REVIEWER_ID, request(5)))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Review not found");
    }

    @Test
    void updateReview_alreadyDeleted() {
        Review deleted = review(REVIEW_ID, user(REVIEWER_ID), 4, true);
        when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.of(deleted));

        assertThatThrownBy(() -> reviewService.updateReview(REVIEW_ID, REVIEWER_ID, request(5)))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Review has been deleted");
    }

    @Test
    void updateReview_notOwner() {
        Review existing = review(REVIEW_ID, user(999L), 4, false);
        when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> reviewService.updateReview(REVIEW_ID, REVIEWER_ID, request(5)))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("You can only edit your own reviews");

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void updateReview_success() {
        Review existing = review(REVIEW_ID, user(REVIEWER_ID), 4, false);
        when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.of(existing));
        when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> inv.getArgument(0));

        ReviewDTO result = reviewService.updateReview(REVIEW_ID, REVIEWER_ID, request(2));

        assertThat(result.getRating()).isEqualTo(2);
        assertThat(result.getComment()).isEqualTo("Great service");
    }

    @Test
    void deleteReview_notFound() {
        when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.deleteReview(REVIEW_ID, REVIEWER_ID))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Review not found");
    }

    @Test
    void deleteReview_notOwner() {
        Review existing = review(REVIEW_ID, user(999L), 4, false);
        when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> reviewService.deleteReview(REVIEW_ID, REVIEWER_ID))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("You can only delete your own reviews");

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void deleteReview_success() {
        Review existing = review(REVIEW_ID, user(REVIEWER_ID), 4, false);
        when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.of(existing));

        reviewService.deleteReview(REVIEW_ID, REVIEWER_ID);

        assertThat(existing.getIsDeleted()).isTrue();
        verify(reviewRepository).save(existing);
    }

    @Test
    void getReviewsByUser_userNotFound() {
        when(userRepository.findById(TARGET_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.getReviewsByUser(TARGET_ID))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("User not found");

        verify(userReviewRepository, never()).findReviewsForTargetUser(any());
    }

    @Test
    void getReviewsByUser_noReviews() {
        when(userRepository.findById(TARGET_ID)).thenReturn(Optional.of(user(TARGET_ID)));
        when(userReviewRepository.findReviewsForTargetUser(TARGET_ID)).thenReturn(List.of());

        assertThat(reviewService.getReviewsByUser(TARGET_ID)).isEmpty();
    }

    @Test
    void getReviewsByUser_mapsTwoReviews() {
        when(userRepository.findById(TARGET_ID)).thenReturn(Optional.of(user(TARGET_ID)));
        Review r1 = review(1L, user(REVIEWER_ID), 5, false);
        Review r2 = review(2L, user(999L), 3, false);
        when(userReviewRepository.findReviewsForTargetUser(TARGET_ID)).thenReturn(List.of(r1, r2));

        List<ReviewDTO> result = reviewService.getReviewsByUser(TARGET_ID);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getReviewId()).isEqualTo(1L);
        assertThat(result.get(1).getReviewId()).isEqualTo(2L);
    }

    @Test
    void getReviewsByClinic_clinicNotFound() {
        when(vetClinicRepository.existsById(CLINIC_ID)).thenReturn(false);

        assertThatThrownBy(() -> reviewService.getReviewsByClinic(CLINIC_ID))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Clinic not found");
    }

    @Test
    void getReviewsByClinic_noReviews() {
        when(vetClinicRepository.existsById(CLINIC_ID)).thenReturn(true);
        when(clinicReviewRepository.findReviewsForClinic(CLINIC_ID)).thenReturn(List.of());

        assertThat(reviewService.getReviewsByClinic(CLINIC_ID)).isEmpty();
    }

    @Test
    void getReviewsByClinic_nullClinicId() {
        when(vetClinicRepository.existsById(null)).thenReturn(false);

        assertThatThrownBy(() -> reviewService.getReviewsByClinic(null))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Clinic not found");
    }

    @Test
    void getReviewsByClinic_someReviews() {
        when(vetClinicRepository.existsById(CLINIC_ID)).thenReturn(true);
        Review r = review(1L, user(REVIEWER_ID), 5, false);
        when(clinicReviewRepository.findReviewsForClinic(CLINIC_ID)).thenReturn(List.of(r));

        assertThat(reviewService.getReviewsByClinic(CLINIC_ID)).hasSize(1);
    }

    @Test
    void getReviewsByClinic_negativeClinicIdStillResolves() {
        when(vetClinicRepository.existsById(-3L)).thenReturn(true);
        when(clinicReviewRepository.findReviewsForClinic(-3L)).thenReturn(List.of(review(1L, user(REVIEWER_ID), 5, false)));

        assertThat(reviewService.getReviewsByClinic(-3L)).hasSize(1);
    }

    @Test
    void getReviewsByClinic_zeroClinicIdStillResolves() {
        when(vetClinicRepository.existsById(0L)).thenReturn(true);
        when(clinicReviewRepository.findReviewsForClinic(0L)).thenReturn(List.of(review(1L, user(REVIEWER_ID), 5, false)));

        assertThat(reviewService.getReviewsByClinic(0L)).hasSize(1);
    }

    @Test
    void getMyClinicReview_clinicNotFound() {
        when(vetClinicRepository.existsById(CLINIC_ID)).thenReturn(false);

        assertThatThrownBy(() -> reviewService.getMyClinicReview(REVIEWER_ID, CLINIC_ID))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Clinic not found");
    }

    @Test
    void getMyClinicReview_noOwnReview() {
        when(vetClinicRepository.existsById(CLINIC_ID)).thenReturn(true);
        when(clinicReviewRepository.findTopByReviewReviewerUserIdAndTargetClinicIdAndReviewIsDeletedFalseOrderByReviewCreatedAtDesc(REVIEWER_ID, CLINIC_ID))
            .thenReturn(Optional.empty());

        assertThat(reviewService.getMyClinicReview(REVIEWER_ID, CLINIC_ID)).isNull();
    }

    @Test
    void getMyClinicReview_nullReviewerId() {
        when(vetClinicRepository.existsById(CLINIC_ID)).thenReturn(true);
        when(clinicReviewRepository.findTopByReviewReviewerUserIdAndTargetClinicIdAndReviewIsDeletedFalseOrderByReviewCreatedAtDesc(null, CLINIC_ID))
            .thenReturn(Optional.empty());

        assertThat(reviewService.getMyClinicReview(null, CLINIC_ID)).isNull();
    }

    @Test
    void getMyClinicReview_nullClinicId() {
        when(vetClinicRepository.existsById(null)).thenReturn(false);

        assertThatThrownBy(() -> reviewService.getMyClinicReview(REVIEWER_ID, null))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Clinic not found");
    }

    @Test
    void getMyClinicReview_hasOwnReview() {
        when(vetClinicRepository.existsById(CLINIC_ID)).thenReturn(true);
        Review existing = review(REVIEW_ID, user(REVIEWER_ID), 4, false);
        when(clinicReviewRepository.findTopByReviewReviewerUserIdAndTargetClinicIdAndReviewIsDeletedFalseOrderByReviewCreatedAtDesc(REVIEWER_ID, CLINIC_ID))
            .thenReturn(Optional.of(new ClinicReview(existing, CLINIC_ID)));

        ReviewDTO result = reviewService.getMyClinicReview(REVIEWER_ID, CLINIC_ID);

        assertThat(result).isNotNull();
        assertThat(result.getReviewId()).isEqualTo(REVIEW_ID);
    }

    @Test
    void getMyClinicReview_negativeReviewerIdStillResolves() {
        when(vetClinicRepository.existsById(CLINIC_ID)).thenReturn(true);
        Review existing = review(REVIEW_ID, user(-7L), 4, false);
        when(clinicReviewRepository.findTopByReviewReviewerUserIdAndTargetClinicIdAndReviewIsDeletedFalseOrderByReviewCreatedAtDesc(-7L, CLINIC_ID))
            .thenReturn(Optional.of(new ClinicReview(existing, CLINIC_ID)));

        assertThat(reviewService.getMyClinicReview(-7L, CLINIC_ID)).isNotNull();
    }

    @Test
    void getMyClinicReview_zeroReviewerIdStillResolves() {
        when(vetClinicRepository.existsById(CLINIC_ID)).thenReturn(true);
        Review existing = review(REVIEW_ID, user(0L), 4, false);
        when(clinicReviewRepository.findTopByReviewReviewerUserIdAndTargetClinicIdAndReviewIsDeletedFalseOrderByReviewCreatedAtDesc(0L, CLINIC_ID))
            .thenReturn(Optional.of(new ClinicReview(existing, CLINIC_ID)));

        assertThat(reviewService.getMyClinicReview(0L, CLINIC_ID)).isNotNull();
    }

    @Test
    void getMyClinicReview_negativeClinicIdStillResolves() {
        when(vetClinicRepository.existsById(-3L)).thenReturn(true);
        Review existing = review(REVIEW_ID, user(REVIEWER_ID), 4, false);
        when(clinicReviewRepository.findTopByReviewReviewerUserIdAndTargetClinicIdAndReviewIsDeletedFalseOrderByReviewCreatedAtDesc(REVIEWER_ID, -3L))
            .thenReturn(Optional.of(new ClinicReview(existing, -3L)));

        assertThat(reviewService.getMyClinicReview(REVIEWER_ID, -3L)).isNotNull();
    }

    @Test
    void getMyClinicReview_zeroClinicIdStillResolves() {
        when(vetClinicRepository.existsById(0L)).thenReturn(true);
        Review existing = review(REVIEW_ID, user(REVIEWER_ID), 4, false);
        when(clinicReviewRepository.findTopByReviewReviewerUserIdAndTargetClinicIdAndReviewIsDeletedFalseOrderByReviewCreatedAtDesc(REVIEWER_ID, 0L))
            .thenReturn(Optional.of(new ClinicReview(existing, 0L)));

        assertThat(reviewService.getMyClinicReview(REVIEWER_ID, 0L)).isNotNull();
    }

    @Test
    void getReviewsLeftByUser_userNotFound() {
        when(userRepository.findById(REVIEWER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.getReviewsLeftByUser(REVIEWER_ID))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("User not found");
    }

    @Test
    void getReviewsLeftByUser_noReviews() {
        when(userRepository.findById(REVIEWER_ID)).thenReturn(Optional.of(user(REVIEWER_ID)));
        when(reviewRepository.findByReviewerUserIdAndIsDeletedFalseOrderByCreatedAtDesc(REVIEWER_ID)).thenReturn(List.of());

        assertThat(reviewService.getReviewsLeftByUser(REVIEWER_ID)).isEmpty();
    }

    @Test
    void getReviewsLeftByUser_negativeReviewerIdStillResolves() {
        when(userRepository.findById(-7L)).thenReturn(Optional.of(user(-7L)));
        when(reviewRepository.findByReviewerUserIdAndIsDeletedFalseOrderByCreatedAtDesc(-7L))
            .thenReturn(List.of(review(1L, user(-7L), 5, false)));

        assertThat(reviewService.getReviewsLeftByUser(-7L)).hasSize(1);
    }

    @Test
    void getReviewsLeftByUser_zeroReviewerIdStillResolves() {
        when(userRepository.findById(0L)).thenReturn(Optional.of(user(0L)));
        when(reviewRepository.findByReviewerUserIdAndIsDeletedFalseOrderByCreatedAtDesc(0L))
            .thenReturn(List.of(review(1L, user(0L), 5, false)));

        assertThat(reviewService.getReviewsLeftByUser(0L)).hasSize(1);
    }

    @Test
    void getReviewsLeftByUser_nullReviewerId() {
        when(userRepository.findById(null)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.getReviewsLeftByUser(null))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("User not found");
    }

    @Test
    void getReviewsLeftByUser_someReviews() {
        when(userRepository.findById(REVIEWER_ID)).thenReturn(Optional.of(user(REVIEWER_ID)));
        Review r = review(1L, user(REVIEWER_ID), 5, false);
        when(reviewRepository.findByReviewerUserIdAndIsDeletedFalseOrderByCreatedAtDesc(REVIEWER_ID)).thenReturn(List.of(r));

        assertThat(reviewService.getReviewsLeftByUser(REVIEWER_ID)).hasSize(1);
    }
}
