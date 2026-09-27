package com.petify.petify.service;

import com.petify.petify.dto.UserActivityRankingProjection;
import com.petify.petify.repo.AnalyticsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private AnalyticsRepository analyticsRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    private static final LocalDateTime START = LocalDateTime.of(2026, 1, 8, 0, 0);
    private static final LocalDateTime END = LocalDateTime.of(2026, 1, 15, 0, 0);

    private static Stream<Arguments> bccRows() {
        return Stream.of(
            Arguments.of(1, START, END),
            Arguments.of(2, START, START),
            Arguments.of(3, END, START)
        );
    }

    @ParameterizedTest(name = "Row 1, variant {0}")
    @MethodSource("bccRows")
    void getTopActiveUsersBcc(int variant, LocalDateTime startTs, LocalDateTime endTs) {
        List<UserActivityRankingProjection> expected = List.of(mock(UserActivityRankingProjection.class));

        when(analyticsRepository.getTopActiveUsers(startTs, endTs)).thenReturn(expected);

        List<UserActivityRankingProjection> result = analyticsService.getTopActiveUsers(startTs, endTs);

        assertThat(result).isEqualTo(expected);
        verify(analyticsRepository).getTopActiveUsers(startTs, endTs);
    }

    @Test
    void getTopActiveUsersNullStartTs() {
        when(analyticsRepository.getTopActiveUsers(null, END)).thenReturn(List.of());

        List<UserActivityRankingProjection> result = analyticsService.getTopActiveUsers(null, END);

        assertThat(result).isEmpty();
        verify(analyticsRepository).getTopActiveUsers(null, END);
    }

    @Test
    void getTopActiveUsersNullEndTs() {
        when(analyticsRepository.getTopActiveUsers(START, null)).thenReturn(List.of());

        List<UserActivityRankingProjection> result = analyticsService.getTopActiveUsers(START, null);

        assertThat(result).isEmpty();
        verify(analyticsRepository).getTopActiveUsers(START, null);
    }

    @Test
    void getTopActiveUsersBothNull() {
        when(analyticsRepository.getTopActiveUsers(null, null)).thenReturn(List.of());

        List<UserActivityRankingProjection> result = analyticsService.getTopActiveUsers(null, null);

        assertThat(result).isEmpty();
        verify(analyticsRepository).getTopActiveUsers(null, null);
    }

    @Test
    void getTopActiveUsersReturnsEmptyListWhenRepositoryHasNoRows() {
        when(analyticsRepository.getTopActiveUsers(START, END)).thenReturn(List.of());

        List<UserActivityRankingProjection> result = analyticsService.getTopActiveUsers(START, END);

        assertThat(result).isEmpty();
    }
}
