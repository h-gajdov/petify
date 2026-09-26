package com.petify.petify.service;

import com.petify.petify.domain.Owner;
import com.petify.petify.domain.Pet;
import com.petify.petify.domain.User;
import com.petify.petify.dto.AnimalResponseDTO;
import com.petify.petify.dto.CreatePetRequest;
import com.petify.petify.repo.OwnerRepository;
import com.petify.petify.repo.PetRepository;
import com.petify.petify.repo.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PetServiceTest {

    @Mock
    private PetRepository petRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OwnerRepository ownerRepository;

    @InjectMocks
    private PetService petService;

    @Test
    void addPetCreatesOwnerWhenUserIsNotAlreadyOwner() {
        User user = user(7L);
        Owner savedOwner = new Owner(user);
        savedOwner.setUserId(7L);
        CreatePetRequest request = validPetRequest();

        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(ownerRepository.findByUserId(7L)).thenReturn(Optional.empty());
        when(ownerRepository.save(any(Owner.class))).thenReturn(savedOwner);
        when(petRepository.save(any(Pet.class))).thenAnswer(invocation -> {
            Pet pet = invocation.getArgument(0);
            pet.setAnimalId(42L);
            return pet;
        });

        AnimalResponseDTO result = petService.addPet(7L, request);

        assertThat(result.getAnimalId()).isEqualTo(42L);
        assertThat(result.getName()).isEqualTo("Mila");
        assertThat(result.getOwnerUserId()).isEqualTo(7L);

        ArgumentCaptor<Owner> ownerCaptor = ArgumentCaptor.forClass(Owner.class);
        verify(ownerRepository).save(ownerCaptor.capture());
        assertThat(ownerCaptor.getValue().getUser()).isSameAs(user);

        ArgumentCaptor<Pet> petCaptor = ArgumentCaptor.forClass(Pet.class);
        verify(petRepository).save(petCaptor.capture());
        assertThat(petCaptor.getValue().getOwner()).isSameAs(savedOwner);
        assertThat(petCaptor.getValue().getSpecies()).isEqualTo("Dog");
    }

    @Test
    void addPetUsesExistingOwnerWithoutSavingANewOne() {
        User user = user(8L);
        Owner existingOwner = new Owner(user);
        existingOwner.setUserId(8L);
        CreatePetRequest request = validPetRequest();

        when(userRepository.findById(8L)).thenReturn(Optional.of(user));
        when(ownerRepository.findByUserId(8L)).thenReturn(Optional.of(existingOwner));
        when(petRepository.save(any(Pet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AnimalResponseDTO result = petService.addPet(8L, request);

        assertThat(result.getOwnerUserId()).isEqualTo(8L);
        verify(ownerRepository, never()).save(any(Owner.class));
        verify(petRepository).save(any(Pet.class));
    }

    @Test
    void addPetRejectsBlankNameBeforeCallingRepositories() {
        CreatePetRequest request = validPetRequest();
        request.setName(" ");

        assertThatThrownBy(() -> petService.addPet(7L, request))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Pet name is required");

        verify(userRepository, never()).findById(any());
        verify(ownerRepository, never()).findByUserId(any());
        verify(petRepository, never()).save(any());
    }

    @Test
    void addPetRejectsNullName() {
        CreatePetRequest request = validPetRequest();
        request.setName(null);

        assertThatThrownBy(() -> petService.addPet(7L, request))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Pet name is required");

        verify(userRepository, never()).findById(any());
    }

    @Test
    void addPetRejectsBlankSex() {
        CreatePetRequest request = validPetRequest();
        request.setSex(" ");

        assertThatThrownBy(() -> petService.addPet(7L, request))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Pet sex is required");

        verify(userRepository, never()).findById(any());
    }

    @Test
    void addPetRejectsNullSex() {
        CreatePetRequest request = validPetRequest();
        request.setSex(null);

        assertThatThrownBy(() -> petService.addPet(7L, request))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Pet sex is required");

        verify(userRepository, never()).findById(any());
    }

    @Test
    void addPetRejectsBlankType() {
        CreatePetRequest request = validPetRequest();
        request.setType(" ");

        assertThatThrownBy(() -> petService.addPet(7L, request))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Pet type is required");

        verify(userRepository, never()).findById(any());
    }

    @Test
    void addPetRejectsNullType() {
        CreatePetRequest request = validPetRequest();
        request.setType(null);

        assertThatThrownBy(() -> petService.addPet(7L, request))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Pet type is required");

        verify(userRepository, never()).findById(any());
    }

    @Test
    void addPetRejectsBlankSpecies() {
        CreatePetRequest request = validPetRequest();
        request.setSpecies(" ");

        assertThatThrownBy(() -> petService.addPet(7L, request))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Pet species is required");

        verify(userRepository, never()).findById(any());
    }

    @Test
    void addPetRejectsNullSpecies() {
        CreatePetRequest request = validPetRequest();
        request.setSpecies(null);

        assertThatThrownBy(() -> petService.addPet(7L, request))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Pet species is required");

        verify(userRepository, never()).findById(any());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("unvalidatedOptionalFields")
    void addPetDoesNotValidateDateOfBirthBreedOrLocatedName(String rowLabel, LocalDate dateOfBirth, String breed, String locatedName) {
        User user = user(7L);
        Owner owner = new Owner(user);
        owner.setUserId(7L);
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(ownerRepository.findByUserId(7L)).thenReturn(Optional.of(owner));
        when(petRepository.save(any(Pet.class))).thenAnswer(inv -> inv.getArgument(0));

        CreatePetRequest request = new CreatePetRequest("Mila", "F", dateOfBirth, null, "Mammal", "Dog", breed, locatedName);

        ArgumentCaptor<Pet> captor = ArgumentCaptor.forClass(Pet.class);
        AnimalResponseDTO result = petService.addPet(7L, request);
        verify(petRepository).save(captor.capture());

        assertThat(result).isNotNull();
        assertThat(captor.getValue().getDateOfBirth()).isEqualTo(dateOfBirth);
        assertThat(captor.getValue().getBreed()).isEqualTo(breed);
        assertThat(captor.getValue().getLocatedName()).isEqualTo(locatedName);
    }

    private static java.util.stream.Stream<org.junit.jupiter.params.provider.Arguments> unvalidatedOptionalFields() {
        LocalDate validDob = LocalDate.of(2021, 2, 3);
        return java.util.stream.Stream.of(
            org.junit.jupiter.params.provider.Arguments.of("row13: dateOfBirth null", null, "Labrador", "Skopje"),
            org.junit.jupiter.params.provider.Arguments.of("row14: breed null", validDob, null, "Skopje"),
            org.junit.jupiter.params.provider.Arguments.of("row15: breed blank", validDob, "   ", "Skopje"),
            org.junit.jupiter.params.provider.Arguments.of("row16: locatedName null", validDob, "Labrador", null),
            org.junit.jupiter.params.provider.Arguments.of("row17: locatedName blank", validDob, "Labrador", "   ")
        );
    }

    @Test
    void addPet_negativeUserIdStillResolvesOwner() {
        User user = user(-7L);
        Owner owner = new Owner(user);
        owner.setUserId(-7L);
        when(userRepository.findById(-7L)).thenReturn(Optional.of(user));
        when(ownerRepository.findByUserId(-7L)).thenReturn(Optional.of(owner));
        when(petRepository.save(any(Pet.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThat(petService.addPet(-7L, validPetRequest()).getOwnerUserId()).isEqualTo(-7L);
    }

    @Test
    void addPet_zeroUserIdStillResolvesOwner() {
        User user = user(0L);
        Owner owner = new Owner(user);
        owner.setUserId(0L);
        when(userRepository.findById(0L)).thenReturn(Optional.of(user));
        when(ownerRepository.findByUserId(0L)).thenReturn(Optional.of(owner));
        when(petRepository.save(any(Pet.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThat(petService.addPet(0L, validPetRequest()).getOwnerUserId()).isEqualTo(0L);
    }

    @Test
    void addPetThrowsWhenUserNotFound() {
        CreatePetRequest request = validPetRequest();
        when(userRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> petService.addPet(9L, request))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("User not found");

        verify(ownerRepository, never()).findByUserId(any());
        verify(petRepository, never()).save(any());
    }

    @Test
    void addPet_nullUserId() {
        CreatePetRequest request = validPetRequest();
        when(userRepository.findById(null)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> petService.addPet(null, request))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("User not found");
    }

    @Test
    void addPetWithPhoto_rejectsDisallowedContentType() {
        User user = user(7L);
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        MockMultipartFile photo = new MockMultipartFile("photo", "pet.txt", "text/plain", "not an image".getBytes());

        assertThatThrownBy(() -> petService.addPet(7L, validPetRequest(), photo))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Pet photo must be a JPG, PNG, WEBP, or GIF image");

        verify(petRepository, never()).save(any());
    }

    @Test
    void addPetWithPhoto_rejectsPhotoOverFiveMegabytes() {
        User user = user(7L);
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        byte[] tooLarge = new byte[6 * 1024 * 1024];
        MockMultipartFile photo = new MockMultipartFile("photo", "pet.png", "image/png", tooLarge);

        assertThatThrownBy(() -> petService.addPet(7L, validPetRequest(), photo))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Pet photo must be 5MB or smaller");

        verify(petRepository, never()).save(any());
    }

    @ParameterizedTest(name = "{0} saves with extension {1}")
    @CsvSource({
        "image/jpeg, .jpg",
        "image/png, .png",
        "image/webp, .webp",
        "image/gif, .gif"
    })
    void addPetWithPhoto_savesPhotoAndSetsUrl(String contentType, String expectedExtension) throws IOException {
        User user = user(7L);
        Owner owner = new Owner(user);
        owner.setUserId(7L);
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(ownerRepository.findByUserId(7L)).thenReturn(Optional.of(owner));
        when(petRepository.save(any(Pet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MockMultipartFile photo = new MockMultipartFile("photo", "pet" + expectedExtension, contentType, "fake-bytes".getBytes());

        AnimalResponseDTO result = petService.addPet(7L, validPetRequest(), photo);

        try {
            assertThat(result.getPhotoUrl()).isNotNull();
            assertThat(result.getPhotoUrl()).startsWith("/uploads/pets/").endsWith(expectedExtension);
        } finally {
            String filename = result.getPhotoUrl().substring("/uploads/pets/".length());
            Files.deleteIfExists(Path.of("uploads", "pets", filename));
        }
    }

   @Test
    void addPetWithPhoto_wrapsIOExceptionFromCopy() {
        User user = user(7L);
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));

        MultipartFile photo = new MockMultipartFile("photo", "pet.png", "image/png", "bytes".getBytes()) {
            @Override
            public java.io.InputStream getInputStream() throws IOException {
                throw new IOException("disk full");
            }
        };

        assertThatThrownBy(() -> petService.addPet(7L, validPetRequest(), photo))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Failed to save pet photo");

        verify(petRepository, never()).save(any());
    }

    @Test
    void getPetByIdThrowsWhenNotFound() {
        when(petRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> petService.getPetById(404L))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Pet not found");
    }

    @Test
    void getPetById_nullPetId() {
        when(petRepository.findById(null)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> petService.getPetById(null))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Pet not found");
    }

    @Test
    void getPetByIdReturnsPetDetails() {
        User user = user(3L);
        Owner owner = new Owner(user);
        owner.setUserId(3L);
        Pet pet = new Pet("Luna", "F", LocalDate.of(2022, 4, 5), null, "Cat", "Cat", "Siamese", "Skopje", owner);
        pet.setAnimalId(99L);

        when(petRepository.findById(99L)).thenReturn(Optional.of(pet));

        AnimalResponseDTO result = petService.getPetById(99L);

        assertThat(result.getAnimalId()).isEqualTo(99L);
        assertThat(result.getName()).isEqualTo("Luna");
        assertThat(result.getOwnerUserId()).isEqualTo(3L);
    }

    @Test
    void getPetById_negativePetIdStillResolves() {
        User user = user(3L);
        Owner owner = new Owner(user);
        owner.setUserId(3L);
        Pet pet = new Pet("Luna", "F", LocalDate.of(2022, 4, 5), null, "Cat", "Cat", "Siamese", "Skopje", owner);
        pet.setAnimalId(-99L);
        when(petRepository.findById(-99L)).thenReturn(Optional.of(pet));

        assertThat(petService.getPetById(-99L).getAnimalId()).isEqualTo(-99L);
    }

    @Test
    void getPetById_zeroPetIdStillResolves() {
        User user = user(3L);
        Owner owner = new Owner(user);
        owner.setUserId(3L);
        Pet pet = new Pet("Luna", "F", LocalDate.of(2022, 4, 5), null, "Cat", "Cat", "Siamese", "Skopje", owner);
        pet.setAnimalId(0L);
        when(petRepository.findById(0L)).thenReturn(Optional.of(pet));

        assertThat(petService.getPetById(0L).getAnimalId()).isEqualTo(0L);
    }

    private static CreatePetRequest validPetRequest() {
        return new CreatePetRequest(
            "Mila",
            "F",
            LocalDate.of(2021, 2, 3),
            null,
            "Mammal",
            "Dog",
            "Labrador",
            "Skopje"
        );
    }

    private static User user(Long id) {
        User user = new User("user" + id, "user" + id + "@petify.test", "secret", "Test", "User");
        user.setUserId(id);
        return user;
    }
}
