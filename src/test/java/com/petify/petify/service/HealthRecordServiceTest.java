package com.petify.petify.service;

import com.petify.petify.domain.Appointment;
import com.petify.petify.domain.Owner;
import com.petify.petify.domain.Pet;
import com.petify.petify.domain.User;
import com.petify.petify.domain.VetClinic;
import com.petify.petify.dto.CreateHealthRecordRequest;
import com.petify.petify.dto.HealthRecordDTO;
import com.petify.petify.repo.AppointmentRepository;
import com.petify.petify.repo.HealthRecordContextView;
import com.petify.petify.repo.HealthRecordRepository;
import com.petify.petify.repo.PetRepository;
import com.petify.petify.repo.VetClinicRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HealthRecordServiceTest {

    private static final Long OWNER_ID = 7L;
    private static final Long APPOINTMENT_ID = 21L;
    private static final Long PET_ID = 42L;
    private static final Long CLINIC_ID = 3L;

    @Mock private HealthRecordRepository healthRecordRepository;
    @Mock private AppointmentRepository appointmentRepository;
    @Mock private PetRepository petRepository;
    @Mock private VetClinicRepository vetClinicRepository;

    @InjectMocks private HealthRecordService healthRecordService;

    private static CreateHealthRecordRequest request(Long appointmentId, String type, String description) {
        CreateHealthRecordRequest request = new CreateHealthRecordRequest();
        request.setAppointmentId(appointmentId);
        request.setType(type);
        request.setDescription(description);
        return request;
    }

    private static Owner ownerWith(Long userId) {
        User user = new User();
        user.setUserId(userId);
        Owner owner = new Owner(user);
        owner.setUserId(userId);
        return owner;
    }

    private static Appointment doneAppointment(Long ownerId) {
        Pet pet = new Pet();
        pet.setAnimalId(PET_ID);
        pet.setName("Mila");

        Appointment appointment = new Appointment();
        appointment.setAppointmentId(APPOINTMENT_ID);
        appointment.setClinicId(CLINIC_ID);
        appointment.setPet(pet);
        appointment.setResponsibleOwner(ownerWith(ownerId));
        appointment.setStatus("DONE");
        appointment.setDateTime(LocalDateTime.of(2026, 3, 1, 10, 0));
        return appointment;
    }

    @Test
    void createHealthRecordRejectsNullOwner() {
        assertThatThrownBy(() -> healthRecordService.createHealthRecord(null, request(APPOINTMENT_ID, "Vaccination", null)))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Owner is required");

        verify(appointmentRepository, never()).findById(any());
    }

    @Test
    void createHealthRecordRejectsMissingAppointmentId() {
        assertThatThrownBy(() -> healthRecordService.createHealthRecord(OWNER_ID, request(null, "Vaccination", null)))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Appointment is required");

        verify(appointmentRepository, never()).findById(any());
    }

    @Test
    void createHealthRecordRejectsBlankType() {
        assertThatThrownBy(() -> healthRecordService.createHealthRecord(OWNER_ID, request(APPOINTMENT_ID, "  ", null)))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Health record type is required");

        verify(appointmentRepository, never()).findById(any());
    }

    @Test
    void createHealthRecordThrowsWhenAppointmentDoesNotExist() {
        when(appointmentRepository.findById(APPOINTMENT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> healthRecordService.createHealthRecord(OWNER_ID, request(APPOINTMENT_ID, "Vaccination", null)))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Appointment not found");
    }

    @Test
    void createHealthRecordRejectsAppointmentWithNoOwner() {
        Appointment appointment = doneAppointment(OWNER_ID);
        appointment.setResponsibleOwner(null);
        when(appointmentRepository.findById(APPOINTMENT_ID)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> healthRecordService.createHealthRecord(OWNER_ID, request(APPOINTMENT_ID, "Vaccination", null)))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("You can create health records only for your own appointments");

        verify(healthRecordRepository, never()).save(any());
    }

    @Test
    void createHealthRecordRejectsAppointmentWithOwnerMissingUserId() {
        Appointment appointment = doneAppointment(OWNER_ID);
        appointment.setResponsibleOwner(ownerWith(null));
        when(appointmentRepository.findById(APPOINTMENT_ID)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> healthRecordService.createHealthRecord(OWNER_ID, request(APPOINTMENT_ID, "Vaccination", null)))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("You can create health records only for your own appointments");

        verify(healthRecordRepository, never()).save(any());
    }

    @Test
    void createHealthRecordRejectsAppointmentBelongingToAnotherOwner() {
        Appointment appointment = doneAppointment(999L);
        when(appointmentRepository.findById(APPOINTMENT_ID)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> healthRecordService.createHealthRecord(OWNER_ID, request(APPOINTMENT_ID, "Vaccination", null)))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("You can create health records only for your own appointments");

        verify(healthRecordRepository, never()).save(any());
    }

    @Test
    void createHealthRecordRejectsAppointmentThatIsNotDone() {
        Appointment appointment = doneAppointment(OWNER_ID);
        appointment.setStatus("SCHEDULED");
        when(appointmentRepository.findById(APPOINTMENT_ID)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> healthRecordService.createHealthRecord(OWNER_ID, request(APPOINTMENT_ID, "Vaccination", null)))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Health records can be added only after a completed appointment");

        verify(healthRecordRepository, never()).save(any());
    }

    @Test
    void createHealthRecordRejectsDuplicateForSameAppointment() {
        Appointment appointment = doneAppointment(OWNER_ID);
        when(appointmentRepository.findById(APPOINTMENT_ID)).thenReturn(Optional.of(appointment));
        when(healthRecordRepository.existsByAppointmentAppointmentId(APPOINTMENT_ID)).thenReturn(true);

        assertThatThrownBy(() -> healthRecordService.createHealthRecord(OWNER_ID, request(APPOINTMENT_ID, "Vaccination", null)))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("A health record already exists for this appointment");

        verify(healthRecordRepository, never()).save(any());
    }

    @Test
    void createHealthRecordSavesTrimmedRecordAndReturnsDtoWithClinicName() {
        Appointment appointment = doneAppointment(OWNER_ID);
        when(appointmentRepository.findById(APPOINTMENT_ID)).thenReturn(Optional.of(appointment));
        when(healthRecordRepository.existsByAppointmentAppointmentId(APPOINTMENT_ID)).thenReturn(false);
        when(healthRecordRepository.save(any())).thenAnswer(invocation -> {
            var record = invocation.getArgument(0, com.petify.petify.domain.HealthRecord.class);
            record.setHealthRecordId(101L);
            record.setDate(appointment.getDateTime().toLocalDate());
            return record;
        });

        VetClinic clinic = new VetClinic();
        clinic.setClinicId(CLINIC_ID);
        clinic.setName("Central Vet");
        when(vetClinicRepository.findById(CLINIC_ID)).thenReturn(Optional.of(clinic));

        HealthRecordDTO result = healthRecordService.createHealthRecord(
            OWNER_ID, request(APPOINTMENT_ID, "  Vaccination  ", "annual shots"));

        assertThat(result.getHealthRecordId()).isEqualTo(101L);
        assertThat(result.getAnimalId()).isEqualTo(PET_ID);
        assertThat(result.getAnimalName()).isEqualTo("Mila");
        assertThat(result.getClinicId()).isEqualTo(CLINIC_ID);
        assertThat(result.getClinicName()).isEqualTo("Central Vet");
        assertThat(result.getType()).isEqualTo("Vaccination");
        assertThat(result.getDescription()).isEqualTo("annual shots");

        ArgumentCaptor<com.petify.petify.domain.HealthRecord> captor =
            ArgumentCaptor.forClass(com.petify.petify.domain.HealthRecord.class);
        verify(healthRecordRepository).save(captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo("Vaccination");
    }

    @Test
    void createHealthRecordLeavesClinicNameNullWhenClinicIsMissing() {
        Appointment appointment = doneAppointment(OWNER_ID);
        when(appointmentRepository.findById(APPOINTMENT_ID)).thenReturn(Optional.of(appointment));
        when(healthRecordRepository.existsByAppointmentAppointmentId(APPOINTMENT_ID)).thenReturn(false);
        when(healthRecordRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(vetClinicRepository.findById(CLINIC_ID)).thenReturn(Optional.empty());

        HealthRecordDTO result = healthRecordService.createHealthRecord(
            OWNER_ID, request(APPOINTMENT_ID, "Checkup", null));

        assertThat(result.getClinicName()).isNull();
    }

    @Test
    void getHealthRecordsForPetThrowsWhenPetDoesNotExist() {
        when(petRepository.findById(PET_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> healthRecordService.getHealthRecordsForPet(PET_ID))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Pet not found");

        verify(healthRecordRepository, never()).findContextByAnimalId(any());
    }

    @Test
    void getHealthRecordsForPetMapsContextRowsToDtos() {
        Pet pet = new Pet();
        pet.setAnimalId(PET_ID);
        when(petRepository.findById(PET_ID)).thenReturn(Optional.of(pet));

        HealthRecordContextView row1 = mock(HealthRecordContextView.class);
        when(row1.getHealthRecordId()).thenReturn(101L);
        when(row1.getAnimalId()).thenReturn(PET_ID);
        when(row1.getAnimalName()).thenReturn("Mila");
        when(row1.getAppointmentId()).thenReturn(APPOINTMENT_ID);
        when(row1.getClinicId()).thenReturn(CLINIC_ID);
        when(row1.getClinicName()).thenReturn("Central Vet");
        when(row1.getType()).thenReturn("Vaccination");
        when(row1.getDescription()).thenReturn("annual shots");
        when(row1.getDate()).thenReturn(LocalDate.of(2026, 3, 1));
        when(row1.getAppointmentDateTime()).thenReturn(LocalDateTime.of(2026, 3, 1, 10, 0));

        HealthRecordContextView row2 = mock(HealthRecordContextView.class);
        when(row2.getHealthRecordId()).thenReturn(102L);
        when(row2.getAnimalId()).thenReturn(PET_ID);
        when(row2.getAnimalName()).thenReturn("Mila");
        when(row2.getAppointmentId()).thenReturn(20L);
        when(row2.getClinicId()).thenReturn(CLINIC_ID);
        when(row2.getClinicName()).thenReturn("Central Vet");
        when(row2.getType()).thenReturn("Checkup");
        when(row2.getDescription()).thenReturn(null);
        when(row2.getDate()).thenReturn(LocalDate.of(2026, 1, 15));
        when(row2.getAppointmentDateTime()).thenReturn(LocalDateTime.of(2026, 1, 15, 9, 0));

        when(healthRecordRepository.findContextByAnimalId(PET_ID)).thenReturn(List.of(row1, row2));

        List<HealthRecordDTO> result = healthRecordService.getHealthRecordsForPet(PET_ID);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getHealthRecordId()).isEqualTo(101L);
        assertThat(result.get(0).getAnimalName()).isEqualTo("Mila");
        assertThat(result.get(0).getClinicName()).isEqualTo("Central Vet");
        assertThat(result.get(0).getType()).isEqualTo("Vaccination");
        assertThat(result.get(1).getHealthRecordId()).isEqualTo(102L);
        assertThat(result.get(1).getType()).isEqualTo("Checkup");
    }

    @Test
    void getHealthRecordsForPetReturnsEmptyListWhenThereAreNoRecords() {
        Pet pet = new Pet();
        pet.setAnimalId(PET_ID);
        when(petRepository.findById(PET_ID)).thenReturn(Optional.of(pet));
        when(healthRecordRepository.findContextByAnimalId(PET_ID)).thenReturn(List.of());

        List<HealthRecordDTO> result = healthRecordService.getHealthRecordsForPet(PET_ID);

        assertThat(result).isEmpty();
    }
}
