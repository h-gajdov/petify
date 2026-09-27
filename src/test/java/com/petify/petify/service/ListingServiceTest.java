package com.petify.petify.service;

import com.petify.petify.domain.Listing;
import com.petify.petify.domain.Owner;
import com.petify.petify.domain.User;
import com.petify.petify.dto.CreateListingRequest;
import com.petify.petify.dto.ListingDTO;
import com.petify.petify.repo.ListingRepository;
import com.petify.petify.repo.OwnerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ListingServiceTest {

    private static final Long OWNER_ID = 7L;
    private static final Long LISTING_ID = 55L;
    private static final Pageable PAGEABLE = PageRequest.of(0, 20);

    @Mock private ListingRepository listingRepository;
    @Mock private OwnerRepository ownerRepository;

    @InjectMocks private ListingService listingService;

    private static Owner owner(Long id) {
        User user = new User("owner" + id, "owner" + id + "@petify.test", "secret", "Ana", "Ilieva");
        user.setUserId(id);
        Owner owner = new Owner(user);
        owner.setUserId(id);
        return owner;
    }

    private static Listing listing(Long id, Owner owner, String status) {
        Listing listing = new Listing(owner, 42L, new BigDecimal("100"), "A good pet");
        listing.setListingId(id);
        listing.setStatus(status);
        return listing;
    }

    @Test
    void createListing_userNotOwner() {
        when(ownerRepository.findByUserId(OWNER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> listingService.createListing(OWNER_ID, new CreateListingRequest(42L, "desc", BigDecimal.TEN)))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("User is not an owner. Only owners can create listings.");

        verify(listingRepository, never()).save(any());
    }

    @Test
    void createListing_success() {
        Owner owner = owner(OWNER_ID);
        when(ownerRepository.findByUserId(OWNER_ID)).thenReturn(Optional.of(owner));
        when(listingRepository.save(any(Listing.class))).thenAnswer(inv -> {
            Listing l = inv.getArgument(0);
            l.setListingId(LISTING_ID);
            return l;
        });

        ListingDTO result = listingService.createListing(OWNER_ID, new CreateListingRequest(42L, "desc", BigDecimal.TEN));

        assertThat(result.getListingId()).isEqualTo(LISTING_ID);
        assertThat(result.getOwnerId()).isEqualTo(OWNER_ID);
        assertThat(result.getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    void createListing_nullUserId() {
        when(ownerRepository.findByUserId(null)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> listingService.createListing(null, new CreateListingRequest(42L, "desc", BigDecimal.TEN)))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("User is not an owner. Only owners can create listings.");
    }

    private static Stream<Arguments> nonPositiveUserIds() {
        return Stream.of(
            Arguments.of(-5L),  // C2.1: negative
            Arguments.of(0L)    // C2.2: zero
        );
    }

    @ParameterizedTest(name = "BCC row: userId = {0} still resolves an owner if the repository has one")
    @MethodSource("nonPositiveUserIds")
    void createListing_nonPositiveUserIdStillResolvesOwner(Long userId) {
        Owner owner = owner(userId);
        when(ownerRepository.findByUserId(userId)).thenReturn(Optional.of(owner));
        when(listingRepository.save(any(Listing.class))).thenAnswer(inv -> inv.getArgument(0));

        ListingDTO result = listingService.createListing(userId, new CreateListingRequest(42L, "desc", BigDecimal.TEN));

        assertThat(result.getOwnerId()).isEqualTo(userId);
    }

    private static Stream<Arguments> unvalidatedRequestFields() {
        return Stream.of(
            Arguments.of("row5: animalId null", null, "desc", BigDecimal.TEN),
            Arguments.of("row6: animalId negative", -3L, "desc", BigDecimal.TEN),
            Arguments.of("row7: animalId zero", 0L, "desc", BigDecimal.TEN),
            Arguments.of("row8: description null", 42L, null, BigDecimal.TEN),
            Arguments.of("row9: description blank", 42L, "   ", BigDecimal.TEN),
            Arguments.of("row10: price null", 42L, "desc", null),
            Arguments.of("row11: price negative", 42L, "desc", new BigDecimal("-10")),
            Arguments.of("row12: price zero", 42L, "desc", BigDecimal.ZERO)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("unvalidatedRequestFields")
    void createListing_doesNotValidateAnimalIdDescriptionOrPrice(String rowLabel, Long animalId, String description, BigDecimal price) {
        Owner owner = owner(OWNER_ID);
        when(ownerRepository.findByUserId(OWNER_ID)).thenReturn(Optional.of(owner));
        when(listingRepository.save(any(Listing.class))).thenAnswer(inv -> inv.getArgument(0));

        listingService.createListing(OWNER_ID, new CreateListingRequest(animalId, description, price));

        ArgumentCaptor<Listing> captor = ArgumentCaptor.forClass(Listing.class);
        verify(listingRepository).save(captor.capture());
        assertThat(captor.getValue().getAnimalId()).isEqualTo(animalId);
        assertThat(captor.getValue().getDescription()).isEqualTo(description);
        assertThat(captor.getValue().getPrice()).isEqualTo(price);
    }

    @Test
    void getListingsByOwner_notOwner() {
        when(ownerRepository.findByUserId(OWNER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> listingService.getListingsByOwner(OWNER_ID))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Owner not found");

        verify(listingRepository, never()).findByOwner(any());
    }

    @Test
    void getListingsByOwner_none() {
        Owner owner = owner(OWNER_ID);
        when(ownerRepository.findByUserId(OWNER_ID)).thenReturn(Optional.of(owner));
        when(listingRepository.findByOwner(owner)).thenReturn(List.of());

        assertThat(listingService.getListingsByOwner(OWNER_ID)).isEmpty();
    }

    @Test
    void getListingsByOwner_mapsTwoListings() {
        Owner owner = owner(OWNER_ID);
        when(ownerRepository.findByUserId(OWNER_ID)).thenReturn(Optional.of(owner));
        when(listingRepository.findByOwner(owner)).thenReturn(List.of(
            listing(1L, owner, "ACTIVE"),
            listing(2L, owner, "DRAFT")
        ));

        List<ListingDTO> result = listingService.getListingsByOwner(OWNER_ID);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getListingId()).isEqualTo(1L);
        assertThat(result.get(1).getListingId()).isEqualTo(2L);
    }

    @Test
    void getListingsByStatus_empty() {
        when(listingRepository.findByStatusIgnoreCase("draft", PAGEABLE)).thenReturn(Page.empty(PAGEABLE));

        Page<ListingDTO> result = listingService.getListingsByStatus("draft", PAGEABLE);

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void getListingsByStatus_nonEmpty_delegatesStatusAndPageable() {
        Owner owner = owner(OWNER_ID);
        Page<Listing> page = new PageImpl<>(List.of(listing(1L, owner, "DRAFT")));
        when(listingRepository.findByStatusIgnoreCase("draft", PAGEABLE)).thenReturn(page);

        Page<ListingDTO> result = listingService.getListingsByStatus("draft", PAGEABLE);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getStatus()).isEqualTo("DRAFT");
    }

    @Test
    void getListingsByStatus_nullStatus() {
        when(listingRepository.findByStatusIgnoreCase(null, PAGEABLE)).thenReturn(Page.empty(PAGEABLE));

        Page<ListingDTO> result = listingService.getListingsByStatus(null, PAGEABLE);

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void getListingsByStatus_blankStatus() {
        when(listingRepository.findByStatusIgnoreCase("   ", PAGEABLE)).thenReturn(Page.empty(PAGEABLE));

        Page<ListingDTO> result = listingService.getListingsByStatus("   ", PAGEABLE);

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void countListingsByStatus_delegatesToRepository() {
        when(listingRepository.countByStatus("ACTIVE")).thenReturn(5L);
        assertThat(listingService.countListingsByStatus("ACTIVE")).isEqualTo(5L);
    }

    @Test
    void getRecommendedListings_repositoryThrows_returnsEmptyList() {
        when(listingRepository.findRecommendedListings(OWNER_ID)).thenThrow(new RuntimeException("db error"));

        assertThat(listingService.getRecommendedListings(OWNER_ID)).isEmpty();
    }

    @Test
    void getRecommendedListings_recommendedListingStillExists() {
        Owner owner = owner(OWNER_ID);
        Object[] row = new Object[]{LISTING_ID};
        when(listingRepository.findRecommendedListings(OWNER_ID)).thenReturn(List.<Object[]>of(row));
        when(listingRepository.findById(LISTING_ID)).thenReturn(Optional.of(listing(LISTING_ID, owner, "ACTIVE")));

        List<ListingDTO> result = listingService.getRecommendedListings(OWNER_ID);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getListingId()).isEqualTo(LISTING_ID);
    }

    @Test
    void getRecommendedListings_recommendedListingWasDeleted() {
        Object[] row = new Object[]{LISTING_ID};
        when(listingRepository.findRecommendedListings(OWNER_ID)).thenReturn(List.<Object[]>of(row));
        when(listingRepository.findById(LISTING_ID)).thenReturn(Optional.empty());

        List<ListingDTO> result = listingService.getRecommendedListings(OWNER_ID);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isNull();
    }

    @Test
    void getRecommendedListings_nullUserId() {
        when(listingRepository.findRecommendedListings(null)).thenReturn(List.of());

        assertThat(listingService.getRecommendedListings(null)).isEmpty();
    }

    @Test
    void getRecommendedListings_negativeUserId() {
        Owner owner = owner(-7L);
        Object[] row = new Object[]{LISTING_ID};
        when(listingRepository.findRecommendedListings(-7L)).thenReturn(List.<Object[]>of(row));
        when(listingRepository.findById(LISTING_ID)).thenReturn(Optional.of(listing(LISTING_ID, owner, "ACTIVE")));

        assertThat(listingService.getRecommendedListings(-7L)).hasSize(1);
    }

    @Test
    void getRecommendedListings_zeroUserId() {
        Owner owner = owner(0L);
        Object[] row = new Object[]{LISTING_ID};
        when(listingRepository.findRecommendedListings(0L)).thenReturn(List.<Object[]>of(row));
        when(listingRepository.findById(LISTING_ID)).thenReturn(Optional.of(listing(LISTING_ID, owner, "ACTIVE")));

        assertThat(listingService.getRecommendedListings(0L)).hasSize(1);
    }

    @Test
    void getListingById_notFound() {
        when(listingRepository.findById(LISTING_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> listingService.getListingById(LISTING_ID))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Listing not found");
    }

    @Test
    void getListingById_found() {
        Owner owner = owner(OWNER_ID);
        when(listingRepository.findById(LISTING_ID)).thenReturn(Optional.of(listing(LISTING_ID, owner, "ACTIVE")));

        ListingDTO result = listingService.getListingById(LISTING_ID);

        assertThat(result.getListingId()).isEqualTo(LISTING_ID);
    }

    @Test
    void getListingById_nullListingId() {
        when(listingRepository.findById(null)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> listingService.getListingById(null))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Listing not found");
    }

    @Test
    void getListingById_negativeListingIdStillResolves() {
        Owner owner = owner(OWNER_ID);
        when(listingRepository.findById(-9L)).thenReturn(Optional.of(listing(-9L, owner, "ACTIVE")));

        assertThat(listingService.getListingById(-9L).getListingId()).isEqualTo(-9L);
    }

    @Test
    void getListingById_zeroListingIdStillResolves() {
        Owner owner = owner(OWNER_ID);
        when(listingRepository.findById(0L)).thenReturn(Optional.of(listing(0L, owner, "ACTIVE")));

        assertThat(listingService.getListingById(0L).getListingId()).isEqualTo(0L);
    }

    @Test
    void updateListingStatus_notFound() {
        when(listingRepository.findById(LISTING_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> listingService.updateListingStatus(LISTING_ID, "SOLD", OWNER_ID))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Listing not found");
    }

    @Test
    void updateListingStatus_notOwner() {
        Owner owner = owner(999L);
        when(listingRepository.findById(LISTING_ID)).thenReturn(Optional.of(listing(LISTING_ID, owner, "ACTIVE")));

        assertThatThrownBy(() -> listingService.updateListingStatus(LISTING_ID, "SOLD", OWNER_ID))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("You can only update your own listings");

        verify(listingRepository, never()).save(any());
    }

    @Test
    void updateListingStatus_success() {
        Owner owner = owner(OWNER_ID);
        Listing listing = listing(LISTING_ID, owner, "ACTIVE");
        when(listingRepository.findById(LISTING_ID)).thenReturn(Optional.of(listing));
        when(listingRepository.save(any(Listing.class))).thenAnswer(inv -> inv.getArgument(0));

        ListingDTO result = listingService.updateListingStatus(LISTING_ID, "SOLD", OWNER_ID);

        assertThat(result.getStatus()).isEqualTo("SOLD");
    }

    @Test
    void deleteListing_notFound() {
        when(listingRepository.findById(LISTING_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> listingService.deleteListing(LISTING_ID, OWNER_ID))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Listing not found");
    }

    @Test
    void deleteListing_notOwner() {
        Owner owner = owner(999L);
        when(listingRepository.findById(LISTING_ID)).thenReturn(Optional.of(listing(LISTING_ID, owner, "ACTIVE")));

        assertThatThrownBy(() -> listingService.deleteListing(LISTING_ID, OWNER_ID))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("You can only delete your own listings");

        verify(listingRepository, never()).delete(any());
    }

    @Test
    void deleteListing_success() {
        Owner owner = owner(OWNER_ID);
        Listing listing = listing(LISTING_ID, owner, "ACTIVE");
        when(listingRepository.findById(LISTING_ID)).thenReturn(Optional.of(listing));

        listingService.deleteListing(LISTING_ID, OWNER_ID);

        verify(listingRepository).delete(listing);
    }
}
