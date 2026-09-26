package com.petify.petify.service;

import com.petify.petify.dto.RecommendedListingProjection;
import com.petify.petify.repo.RecommendationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private RecommendationRepository recommendationRepository;

    @InjectMocks
    private RecommendationService recommendationService;

    private static Stream<Arguments> acocRows() {
        return Stream.of(
            Arguments.of(1, -3L),  // C1.1, C2.1 : negative
            Arguments.of(2, 0L),   // C1.1, C2.2 : zero
            Arguments.of(3, 7L)    // C1.1, C2.3 : positive
        );
    }

    @ParameterizedTest(name = "ACoC row {0}: userId = {1}")
    @MethodSource("acocRows")
    void getRecommendedListingsAcoc(int row, Long userId) {
        RecommendedListingProjection listing = mock(RecommendedListingProjection.class);
        List<RecommendedListingProjection> expected = List.of(listing);

        when(recommendationRepository.getRecommendedListings(userId)).thenReturn(expected);

        List<RecommendedListingProjection> result = recommendationService.getRecommendedListings(userId);

        assertThat(result).isSameAs(expected);
        verify(recommendationRepository).getRecommendedListings(userId);
    }

    @Test
    void getRecommendedListingsReturnsEmptyListWhenRepositoryHasNoRows() {
        Long userId = 12L;

        when(recommendationRepository.getRecommendedListings(userId)).thenReturn(List.of());

        List<RecommendedListingProjection> result = recommendationService.getRecommendedListings(userId);

        assertThat(result).isEmpty();
    }

    @Test
    void getRecommendedListingsNullUserId() {
        when(recommendationRepository.getRecommendedListings(null)).thenReturn(List.of());

        List<RecommendedListingProjection> result = recommendationService.getRecommendedListings(null);

        assertThat(result).isEmpty();
        verify(recommendationRepository).getRecommendedListings(null);
    }
}
