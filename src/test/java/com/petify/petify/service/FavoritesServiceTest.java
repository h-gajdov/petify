package com.petify.petify.service;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.petify.petify.domain.Client;
import com.petify.petify.domain.FavoriteListing;
import com.petify.petify.domain.Listing;
import com.petify.petify.domain.User;
import com.petify.petify.repo.ClientRepository;
import com.petify.petify.repo.FavoriteListingRepository;
import com.petify.petify.repo.ListingRepository;

@ExtendWith(MockitoExtension.class)
class FavoritesServiceTest {

    @Mock
    private FavoriteListingRepository favoriteRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ListingRepository listingRepository;

    @InjectMocks
    private FavoritesService favoritesService;

    @Test
    void addFavoriteSavesFavoriteForClientAndListing() {
        Client client = client(11L);
        Listing listing = listing(22L);

        when(clientRepository.findByUserId(11L)).thenReturn(Optional.of(client));
        when(listingRepository.findById(22L)).thenReturn(Optional.of(listing));

        favoritesService.addFavorite(11L, 22L);

        ArgumentCaptor<FavoriteListing> captor = ArgumentCaptor.forClass(FavoriteListing.class);
        verify(favoriteRepository).save(captor.capture());
        assertThat(captor.getValue().getClient()).isSameAs(client);
        assertThat(captor.getValue().getListing()).isSameAs(listing);
    }

    @Test
    void removeFavoriteDeletesExistingFavorite() {
        Client client = client(11L);
        Listing listing = listing(22L);
        FavoriteListing favorite = new FavoriteListing(client, listing);

        when(clientRepository.findByUserId(11L)).thenReturn(Optional.of(client));
        when(listingRepository.findById(22L)).thenReturn(Optional.of(listing));
        when(favoriteRepository.findByClientAndListing(client, listing)).thenReturn(Optional.of(favorite));

        favoritesService.removeFavorite(11L, 22L);

        verify(favoriteRepository).delete(favorite);
    }

    @Test
    void removeFavoriteWithNegativeUserIdStillResolvesClient() {
        Client client = client(-11L);
        Listing listing = listing(22L);
        FavoriteListing favorite = new FavoriteListing(client, listing);
        when(clientRepository.findByUserId(-11L)).thenReturn(Optional.of(client));
        when(listingRepository.findById(22L)).thenReturn(Optional.of(listing));
        when(favoriteRepository.findByClientAndListing(client, listing)).thenReturn(Optional.of(favorite));

        favoritesService.removeFavorite(-11L, 22L);

        verify(favoriteRepository).delete(favorite);
    }

    @Test
    void removeFavoriteWithZeroUserIdStillResolvesClient() {
        Client client = client(0L);
        Listing listing = listing(22L);
        FavoriteListing favorite = new FavoriteListing(client, listing);
        when(clientRepository.findByUserId(0L)).thenReturn(Optional.of(client));
        when(listingRepository.findById(22L)).thenReturn(Optional.of(listing));
        when(favoriteRepository.findByClientAndListing(client, listing)).thenReturn(Optional.of(favorite));

        favoritesService.removeFavorite(0L, 22L);

        verify(favoriteRepository).delete(favorite);
    }

    @Test
    void removeFavoriteWithNegativeListingIdStillResolvesListing() {
        Client client = client(11L);
        Listing listing = listing(-22L);
        FavoriteListing favorite = new FavoriteListing(client, listing);
        when(clientRepository.findByUserId(11L)).thenReturn(Optional.of(client));
        when(listingRepository.findById(-22L)).thenReturn(Optional.of(listing));
        when(favoriteRepository.findByClientAndListing(client, listing)).thenReturn(Optional.of(favorite));

        favoritesService.removeFavorite(11L, -22L);

        verify(favoriteRepository).delete(favorite);
    }

    @Test
    void removeFavoriteWithZeroListingIdStillResolvesListing() {
        Client client = client(11L);
        Listing listing = listing(0L);
        FavoriteListing favorite = new FavoriteListing(client, listing);
        when(clientRepository.findByUserId(11L)).thenReturn(Optional.of(client));
        when(listingRepository.findById(0L)).thenReturn(Optional.of(listing));
        when(favoriteRepository.findByClientAndListing(client, listing)).thenReturn(Optional.of(favorite));

        favoritesService.removeFavorite(11L, 0L);

        verify(favoriteRepository).delete(favorite);
    }

    @Test
    void removeFavoriteThrowsWhenClientNotFound() {
        when(clientRepository.findByUserId(11L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> favoritesService.removeFavorite(11L, 22L))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Client not found");

        verify(listingRepository, never()).findById(any());
    }

    @Test
    void removeFavoriteWithNullUserId() {
        when(clientRepository.findByUserId(null)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> favoritesService.removeFavorite(null, 22L))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Client not found");
    }

    @Test
    void removeFavoriteWithNullListingId() {
        Client client = client(11L);
        when(clientRepository.findByUserId(11L)).thenReturn(Optional.of(client));
        when(listingRepository.findById(null)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> favoritesService.removeFavorite(11L, null))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Listing not found");
    }

    @Test
    void removeFavoriteThrowsWhenListingNotFound() {
        when(clientRepository.findByUserId(11L)).thenReturn(Optional.of(client(11L)));
        when(listingRepository.findById(22L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> favoritesService.removeFavorite(11L, 22L))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Listing not found");

        verify(favoriteRepository, never()).findByClientAndListing(any(), any());
    }

    @Test
    void removeFavoriteThrowsWhenFavoriteDoesNotExist() {
        Client client = client(11L);
        Listing listing = listing(22L);

        when(clientRepository.findByUserId(11L)).thenReturn(Optional.of(client));
        when(listingRepository.findById(22L)).thenReturn(Optional.of(listing));
        when(favoriteRepository.findByClientAndListing(client, listing)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> favoritesService.removeFavorite(11L, 22L))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Favorite not found");

        verify(favoriteRepository, never()).delete(any());
    }

    @Test
    void isFavoritedReturnsFalseWithoutListingLookupWhenClientDoesNotExist() {
        when(clientRepository.findByUserId(11L)).thenReturn(Optional.empty());

        boolean favorited = favoritesService.isFavorited(11L, 22L);

        assertThat(favorited).isFalse();
        verify(listingRepository, never()).findById(any());
        verify(favoriteRepository, never()).findByClientAndListing(any(), any());
    }

    @Test
    void isFavoritedReturnsFalseWhenListingDoesNotExist() {
        when(clientRepository.findByUserId(11L)).thenReturn(Optional.of(client(11L)));
        when(listingRepository.findById(22L)).thenReturn(Optional.empty());

        assertThat(favoritesService.isFavorited(11L, 22L)).isFalse();

        verify(favoriteRepository, never()).findByClientAndListing(any(), any());
    }

    @Test
    void isFavoritedReturnsTrueWhenFavoriteExists() {
        Client client = client(11L);
        Listing listing = listing(22L);

        when(clientRepository.findByUserId(11L)).thenReturn(Optional.of(client));
        when(listingRepository.findById(22L)).thenReturn(Optional.of(listing));
        when(favoriteRepository.findByClientAndListing(client, listing))
            .thenReturn(Optional.of(new FavoriteListing(client, listing)));

        assertThat(favoritesService.isFavorited(11L, 22L)).isTrue();
    }

    private static Client client(Long userId) {
        User user = new User("client" + userId, "client" + userId + "@petify.test", "secret", "Test", "Client");
        user.setUserId(userId);
        return new Client(user);
    }

    private static Listing listing(Long listingId) {
        Listing listing = new Listing();
        listing.setListingId(listingId);
        return listing;
    }
}
