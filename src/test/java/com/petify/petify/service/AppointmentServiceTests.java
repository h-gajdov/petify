package com.petify.petify.service;

import com.petify.petify.domain.Appointment;
import com.petify.petify.domain.ClinicUnavailableSlot;
import com.petify.petify.domain.Owner;
import com.petify.petify.domain.Pet;
import com.petify.petify.domain.User;
import com.petify.petify.domain.VetClinic;
import com.petify.petify.dto.AppointmentSlotDTO;
import com.petify.petify.dto.ClinicAppointmentDTO;
import com.petify.petify.dto.ClinicUnavailableSlotDTO;
import com.petify.petify.dto.CreateAppointmentRequest;
import com.petify.petify.dto.CreateUnavailableSlotRequest;
import com.petify.petify.dto.OwnerAppointmentDTO;
import com.petify.petify.repo.AppointmentRepository;
import com.petify.petify.repo.ClinicUnavailableSlotRepository;
import com.petify.petify.repo.NotificationRepository;
import com.petify.petify.repo.OwnerRepository;
import com.petify.petify.repo.PetRepository;
import com.petify.petify.repo.VetClinicRepository;
import com.petify.petify.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AppointmentServiceTests {

    @Mock AppointmentRepository appointmentRepository;
    @Mock VetClinicRepository vetClinicRepository;
    @Mock UserRepository userRepository;
    @Mock NotificationRepository notificationRepository;
    @Mock ClinicUnavailableSlotRepository unavailableSlotRepository;
    @Mock OwnerRepository ownerRepository;
    @Mock PetRepository petRepository;

    @InjectMocks
    AppointmentService appointmentService;

    @BeforeEach
    void setUp() {
        org.mockito.Mockito.lenient().when(appointmentRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        org.mockito.Mockito.lenient().when(unavailableSlotRepository.save(any())).thenAnswer(i -> {
            ClinicUnavailableSlot slot = i.getArgument(0);
            slot.setSlotId(999L);
            return slot;
        });

        VetClinic clinic = new VetClinic();
        clinic.setClinicId(10L);
        org.mockito.Mockito.lenient().when(vetClinicRepository.findByUserId(1L)).thenReturn(Optional.of(clinic));
    }

    // ==========================================
    // TESTS FOR cancelAppointmentForOwner
    // ==========================================

    @ParameterizedTest
    @MethodSource("cancelAppointmentValues")
    public void cancelAppointmentTest(Long userId, Long appointmentId, Appointment mockAppointment, String expectedOutput) {
        if (mockAppointment != null) {
            when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(mockAppointment));
        } else if (appointmentId != null) {
            when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.empty());
        }

        if ("SUCCESS".equals(expectedOutput)) {
            var result = appointmentService.cancelAppointmentForOwner(userId, appointmentId);
            assertEquals("CANCELLED", result.getStatus());
        } else {
            RuntimeException ex = assertThrows(RuntimeException.class, () ->
                    appointmentService.cancelAppointmentForOwner(userId, appointmentId)
            );
            assertEquals(expectedOutput, ex.getMessage());
        }
    }

    public static Stream<Arguments> cancelAppointmentValues() {
        return Stream.of(
                Arguments.of(null, null, null, "User and appointment are required"),
                Arguments.of(1L, 100L, null, "Appointment not found"),
                Arguments.of(1L, 100L, createMockOwnerAppointment(2L, LocalDateTime.now().plusDays(1), "CONFIRMED"), "You can only cancel your own appointments"),
                Arguments.of(1L, 100L, createMockOwnerAppointment(1L, LocalDateTime.now().minusDays(1), "CONFIRMED"), "Only future appointments can be cancelled"),
                Arguments.of(1L, 100L, createMockOwnerAppointment(1L, LocalDateTime.now().plusDays(1), "CANCELLED"), "Appointment is already cancelled"),
                Arguments.of(1L, 100L, createMockOwnerAppointment(1L, LocalDateTime.now().plusDays(1), "DONE"), "This appointment can no longer be cancelled"),
                Arguments.of(1L, 100L, createMockOwnerAppointment(1L, LocalDateTime.now().plusDays(1), "CONFIRMED"), "SUCCESS")
        );
    }

    private static Appointment createMockOwnerAppointment(Long ownerUserId, LocalDateTime dateTime, String status) {
        Appointment appointment = new Appointment();
        Owner owner = new Owner();
        owner.setUserId(ownerUserId);
        User user = new User();
        user.setUserId(ownerUserId);
        owner.setUser(user);
        appointment.setResponsibleOwner(owner);
        appointment.setDateTime(dateTime);
        appointment.setStatus(status);
        appointment.setClinicId(1L);
        return appointment;
    }

    // ==========================================
    // TESTS FOR markAppointmentNoShowForClinicUser
    // ==========================================

    @ParameterizedTest
    @MethodSource("markNoShowValues")
    public void markNoShowTest(Long userId, Long appointmentId, Appointment mockAppointment, String expectedOutput) {
        if (mockAppointment != null) {
            when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(mockAppointment));
        } else if (appointmentId != null) {
            when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.empty());
        }

        if ("SUCCESS".equals(expectedOutput)) {
            var result = appointmentService.markAppointmentNoShowForClinicUser(userId, appointmentId);
            assertEquals("NO_SHOW", result.getStatus());
        } else {
            RuntimeException ex = assertThrows(RuntimeException.class, () ->
                    appointmentService.markAppointmentNoShowForClinicUser(userId, appointmentId)
            );
            assertEquals(expectedOutput, ex.getMessage());
        }
    }

    public static Stream<Arguments> markNoShowValues() {
        return Stream.of(
                Arguments.of(1L, 100L, null, "Appointment not found"),
                Arguments.of(1L, 100L, createMockClinicAppointment(99L, "CONFIRMED", LocalDateTime.now().minusDays(1)), "You can only update appointments for your own clinic"),
                Arguments.of(1L, 100L, createMockClinicAppointment(10L, "CANCELLED", LocalDateTime.now().minusDays(1)), "Only confirmed or done appointments can be marked as no-show"),
                Arguments.of(1L, 100L, createMockClinicAppointment(10L, "CONFIRMED", LocalDateTime.now().plusDays(1)), "An appointment can be marked as no-show only after its scheduled time"),
                Arguments.of(1L, 100L, createMockClinicAppointment(10L, "CONFIRMED", LocalDateTime.now().minusDays(1)), "SUCCESS")
        );
    }

    private static Appointment createMockClinicAppointment(Long clinicId, String status, LocalDateTime dateTime) {
        Appointment appointment = new Appointment();
        appointment.setClinicId(clinicId);
        appointment.setStatus(status);
        appointment.setDateTime(dateTime);
        return appointment;
    }

    // ==========================================
    // TESTS FOR createUnavailableSlot (All-DU-Paths)
    // ==========================================

    @ParameterizedTest
    @MethodSource("createUnavailableSlotValues")
    public void createUnavailableSlotTest(Long clinicId, String requestDateTime, boolean clinicExists, boolean appointmentExists, boolean slotExists, String expectedOutput) {
        CreateUnavailableSlotRequest request = new CreateUnavailableSlotRequest();
        request.setDateTime(requestDateTime);
        request.setReason("Doctor unavailable");

        if (clinicId != null && requestDateTime != null) {
            when(vetClinicRepository.existsById(clinicId)).thenReturn(clinicExists);

            if (clinicExists) {
                LocalDateTime slotTime = LocalDateTime.parse(requestDateTime);

                org.mockito.Mockito.lenient()
                        .when(appointmentRepository.existsByClinicIdAndDateTimeAndStatusNotIn(
                                eq(clinicId), eq(slotTime), any(List.class)))
                        .thenReturn(appointmentExists);

                org.mockito.Mockito.lenient()
                        .when(unavailableSlotRepository.existsByClinicIdAndDateTime(clinicId, slotTime))
                        .thenReturn(slotExists);
            }
        }

        if ("SUCCESS".equals(expectedOutput)) {
            var result = appointmentService.createUnavailableSlot(clinicId, request);
            assertNotNull(result);
            assertEquals(clinicId, result.getClinicId());
        } else {
            RuntimeException ex = assertThrows(RuntimeException.class, () ->
                    appointmentService.createUnavailableSlot(clinicId, request)
            );
            assertEquals(expectedOutput, ex.getMessage());
        }
    }

    public static Stream<Arguments> createUnavailableSlotValues() {
        // Valid slot (Future, lands on a 30-min boundary between 09:00 and 17:00)
        String validFutureTime = LocalDateTime.now().plusDays(1).withHour(10).withMinute(30).withSecond(0).withNano(0).toString();

        // Invalid slot (Past time) to purposefully fail the isValidWorkingSlot constraint
        String invalidPastTime = LocalDateTime.now().minusDays(1).withHour(10).withMinute(30).withSecond(0).withNano(0).toString();

        return Stream.of(
                // Path [1, 2]: Missing clinic ID
                Arguments.of(null, validFutureTime, false, false, false, "Clinic and date/time are required"),

                // Path [1, 3, 4]: Vet clinic does not exist
                Arguments.of(1L, validFutureTime, false, false, false, "Vet clinic not found"),

                // Path [1, 3, 5, 6]: Invalid working slot
                Arguments.of(1L, invalidPastTime, true, false, false, "Unavailable slot must be a future 30-minute slot between 09:00 and 17:00"),

                // Path [1, 3, 5, 7, 8]: Overlaps with existing appointment
                Arguments.of(1L, validFutureTime, true, true, false, "Cannot block a slot that already has an appointment"),

                // Path [1, 3, 5, 7, 9, 10]: Slot already marked unavailable
                Arguments.of(1L, validFutureTime, true, false, true, "This slot is already marked unavailable"),

                // Path [1, 3, 5, 7, 9, 11]: SUCCESS
                Arguments.of(1L, validFutureTime, true, false, false, "SUCCESS")
        );
    }

    @Test
    void createAppointment_petNotFound() {
        Owner owner = new Owner();
        owner.setUserId(1L);
        when(ownerRepository.findByUserId(1L)).thenReturn(Optional.of(owner));
        when(petRepository.findById(50L)).thenReturn(Optional.empty());

        CreateAppointmentRequest request = new CreateAppointmentRequest(
            1L, 50L, LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0).toString(), null);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> appointmentService.createAppointment(1L, request));
        assertEquals("Pet not found", ex.getMessage());
    }

    @Test
    void getAppointmentsForOwner_notOwner() {
        when(ownerRepository.findByUserId(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> appointmentService.getAppointmentsForOwner(1L));
        assertEquals("User is not an owner. Only owners can view appointments.", ex.getMessage());
    }

    @Test
    void getAppointmentsForOwner_ignoresNonConfirmedAppointments() {
        Owner owner = new Owner();
        owner.setUserId(1L);
        when(ownerRepository.findByUserId(1L)).thenReturn(Optional.of(owner));
        Appointment cancelled = createMockOwnerAppointment(1L, LocalDateTime.now().minusDays(1), "CANCELLED");
        when(appointmentRepository.findByResponsibleOwnerUserIdOrderByDateTimeAsc(1L)).thenReturn(List.of(cancelled));

        List<OwnerAppointmentDTO> result = appointmentService.getAppointmentsForOwner(1L);

        assertEquals(1, result.size());
        assertEquals("CANCELLED", cancelled.getStatus());
        verify(appointmentRepository, never()).saveAll(anyList());
    }

    @Test
    void getAppointmentsForOwner_noPastDueAppointments() {
        Owner owner = new Owner();
        owner.setUserId(1L);
        when(ownerRepository.findByUserId(1L)).thenReturn(Optional.of(owner));
        Appointment future = createMockOwnerAppointment(1L, LocalDateTime.now().plusDays(1), "CONFIRMED");
        when(appointmentRepository.findByResponsibleOwnerUserIdOrderByDateTimeAsc(1L)).thenReturn(List.of(future));

        List<OwnerAppointmentDTO> result = appointmentService.getAppointmentsForOwner(1L);

        assertEquals(1, result.size());
        verify(appointmentRepository, never()).saveAll(anyList());
    }

    @Test
    void getAppointmentsForOwner_marksPastConfirmedAppointmentsDone() {
        Owner owner = new Owner();
        owner.setUserId(1L);
        when(ownerRepository.findByUserId(1L)).thenReturn(Optional.of(owner));
        Appointment past = createMockOwnerAppointment(1L, LocalDateTime.now().minusDays(1), "CONFIRMED");
        when(appointmentRepository.findByResponsibleOwnerUserIdOrderByDateTimeAsc(1L)).thenReturn(List.of(past));

        appointmentService.getAppointmentsForOwner(1L);

        verify(appointmentRepository).saveAll(anyList());
        assertEquals("DONE", past.getStatus());
    }

    @Test
    void getAppointmentsForClinic_missingParams() {
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> appointmentService.getAppointmentsForClinic(null, LocalDate.now()));
        assertEquals("Clinic and date are required", ex.getMessage());
    }

    @Test
    void getAppointmentsForClinic_clinicNotFound() {
        when(vetClinicRepository.existsById(10L)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> appointmentService.getAppointmentsForClinic(10L, LocalDate.now()));
        assertEquals("Vet clinic not found", ex.getMessage());
    }

    @Test
    void getAppointmentsForClinic_noPastDueAppointments() {
        when(vetClinicRepository.existsById(10L)).thenReturn(true);
        when(appointmentRepository.findByClinicIdAndStatusAndDateTimeLessThanEqual(eq(10L), eq("CONFIRMED"), any()))
            .thenReturn(List.of());
        when(appointmentRepository.findByClinicIdAndDateTimeBetweenOrderByDateTimeAsc(eq(10L), any(), any()))
            .thenReturn(List.of());

        List<ClinicAppointmentDTO> result = appointmentService.getAppointmentsForClinic(10L, LocalDate.now());

        assertEquals(0, result.size());
        verify(appointmentRepository, never()).saveAll(anyList());
    }

    @Test
    void getAppointmentsForClinic_marksPastConfirmedAppointmentsDone() {
        when(vetClinicRepository.existsById(10L)).thenReturn(true);
        Appointment past = createMockClinicAppointment(10L, "CONFIRMED", LocalDateTime.now().minusHours(1));
        when(appointmentRepository.findByClinicIdAndStatusAndDateTimeLessThanEqual(eq(10L), eq("CONFIRMED"), any()))
            .thenReturn(List.of(past));
        when(appointmentRepository.findByClinicIdAndDateTimeBetweenOrderByDateTimeAsc(eq(10L), any(), any()))
            .thenReturn(List.of(past));

        List<ClinicAppointmentDTO> result = appointmentService.getAppointmentsForClinic(10L, LocalDate.now());

        assertEquals(1, result.size());
        verify(appointmentRepository).saveAll(anyList());
    }

    @Test
    void getAppointmentsForClinicUser_userIdNull() {
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> appointmentService.getAppointmentsForClinicUser(null, LocalDate.now()));
        assertEquals("User is required", ex.getMessage());
    }

    @Test
    void getAppointmentsForClinicUser_notLinkedToClinic() {
        when(vetClinicRepository.findByUserId(2L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> appointmentService.getAppointmentsForClinicUser(2L, LocalDate.now()));
        assertEquals("User is not linked to a clinic", ex.getMessage());
    }

    @Test
    void getAppointmentsForClinicUser_negativeUserIdStillResolvesClinic() {
        VetClinic clinic = new VetClinic();
        clinic.setClinicId(20L);
        when(vetClinicRepository.findByUserId(-2L)).thenReturn(Optional.of(clinic));
        when(vetClinicRepository.existsById(20L)).thenReturn(true);
        when(appointmentRepository.findByClinicIdAndStatusAndDateTimeLessThanEqual(eq(20L), eq("CONFIRMED"), any()))
            .thenReturn(List.of());
        when(appointmentRepository.findByClinicIdAndDateTimeBetweenOrderByDateTimeAsc(eq(20L), any(), any()))
            .thenReturn(List.of());

        assertNotNull(appointmentService.getAppointmentsForClinicUser(-2L, LocalDate.now()));
    }

    @Test
    void getAppointmentsForClinicUser_zeroUserIdStillResolvesClinic() {
        VetClinic clinic = new VetClinic();
        clinic.setClinicId(20L);
        when(vetClinicRepository.findByUserId(0L)).thenReturn(Optional.of(clinic));
        when(vetClinicRepository.existsById(20L)).thenReturn(true);
        when(appointmentRepository.findByClinicIdAndStatusAndDateTimeLessThanEqual(eq(20L), eq("CONFIRMED"), any()))
            .thenReturn(List.of());
        when(appointmentRepository.findByClinicIdAndDateTimeBetweenOrderByDateTimeAsc(eq(20L), any(), any()))
            .thenReturn(List.of());

        assertNotNull(appointmentService.getAppointmentsForClinicUser(0L, LocalDate.now()));
    }

    @Test
    void getAppointmentsForClinicUser_delegatesToResolvedClinic() {
        when(vetClinicRepository.existsById(10L)).thenReturn(true);
        when(appointmentRepository.findByClinicIdAndStatusAndDateTimeLessThanEqual(eq(10L), eq("CONFIRMED"), any()))
            .thenReturn(List.of());
        when(appointmentRepository.findByClinicIdAndDateTimeBetweenOrderByDateTimeAsc(eq(10L), any(), any()))
            .thenReturn(List.of());

        List<ClinicAppointmentDTO> result = appointmentService.getAppointmentsForClinicUser(1L, LocalDate.now());

        assertNotNull(result);
    }

    @Test
    void getAvailableSlots_missingParams() {
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> appointmentService.getAvailableSlots(null, LocalDate.now()));
        assertEquals("Clinic and date are required", ex.getMessage());
    }

    @Test
    void getAvailableSlots_clinicNotFound() {
        when(vetClinicRepository.existsById(10L)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> appointmentService.getAvailableSlots(10L, LocalDate.now()));
        assertEquals("Vet clinic not found", ex.getMessage());
    }

    @Test
    void getAvailableSlots_excludesBookedAndUnavailableSlots() {
        LocalDate date = LocalDate.now().plusDays(2);
        when(vetClinicRepository.existsById(10L)).thenReturn(true);

        LocalDateTime booked = date.atTime(9, 30);
        LocalDateTime unavailable = date.atTime(10, 0);
        LocalDateTime free = date.atTime(11, 0);

        Appointment bookedAppointment = new Appointment();
        bookedAppointment.setDateTime(booked);
        when(appointmentRepository.findByClinicIdAndDateTimeBetweenAndStatusNotInOrderByDateTimeAsc(eq(10L), any(), any(), anyList()))
            .thenReturn(List.of(bookedAppointment));
        when(unavailableSlotRepository.findByClinicIdAndDateTimeBetweenOrderByDateTimeAsc(eq(10L), any(), any()))
            .thenReturn(List.of(new ClinicUnavailableSlot(10L, unavailable, "closed")));

        List<AppointmentSlotDTO> result = appointmentService.getAvailableSlots(10L, date);

        assertEquals(14, result.size());
        assertTrue(result.stream().noneMatch(s -> s.getDateTime().equals(booked)));
        assertTrue(result.stream().noneMatch(s -> s.getDateTime().equals(unavailable)));
        assertTrue(result.stream().anyMatch(s -> s.getDateTime().equals(free)));
    }

    @Test
    void getAvailableSlotsForClinicUser_delegatesToResolvedClinic() {
        when(vetClinicRepository.existsById(10L)).thenReturn(true);
        when(appointmentRepository.findByClinicIdAndDateTimeBetweenAndStatusNotInOrderByDateTimeAsc(eq(10L), any(), any(), anyList()))
            .thenReturn(List.of());
        when(unavailableSlotRepository.findByClinicIdAndDateTimeBetweenOrderByDateTimeAsc(eq(10L), any(), any()))
            .thenReturn(List.of());

        List<AppointmentSlotDTO> result = appointmentService.getAvailableSlotsForClinicUser(1L, LocalDate.now().plusDays(1));

        assertNotNull(result);
    }

    @Test
    void getUnavailableSlots_missingParams() {
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> appointmentService.getUnavailableSlots(null, LocalDate.now()));
        assertEquals("Clinic and date are required", ex.getMessage());
    }

    @Test
    void getUnavailableSlots_clinicNotFound() {
        when(vetClinicRepository.existsById(10L)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> appointmentService.getUnavailableSlots(10L, LocalDate.now()));
        assertEquals("Vet clinic not found", ex.getMessage());
    }

    @Test
    void getUnavailableSlots_none() {
        when(vetClinicRepository.existsById(10L)).thenReturn(true);
        when(unavailableSlotRepository.findByClinicIdAndDateTimeBetweenOrderByDateTimeAsc(eq(10L), any(), any()))
            .thenReturn(List.of());

        assertEquals(0, appointmentService.getUnavailableSlots(10L, LocalDate.now()).size());
    }

    @Test
    void getUnavailableSlots_some() {
        when(vetClinicRepository.existsById(10L)).thenReturn(true);
        ClinicUnavailableSlot slot = new ClinicUnavailableSlot(10L, LocalDate.now().atTime(9, 0), "closed");
        slot.setSlotId(5L);
        when(unavailableSlotRepository.findByClinicIdAndDateTimeBetweenOrderByDateTimeAsc(eq(10L), any(), any()))
            .thenReturn(List.of(slot));

        List<ClinicUnavailableSlotDTO> result = appointmentService.getUnavailableSlots(10L, LocalDate.now());

        assertEquals(1, result.size());
        assertEquals("closed", result.get(0).getReason());
    }

    @Test
    void getUnavailableSlotsForClinicUser_delegatesToResolvedClinic() {
        when(vetClinicRepository.existsById(10L)).thenReturn(true);
        when(unavailableSlotRepository.findByClinicIdAndDateTimeBetweenOrderByDateTimeAsc(eq(10L), any(), any()))
            .thenReturn(List.of());

        assertNotNull(appointmentService.getUnavailableSlotsForClinicUser(1L, LocalDate.now()));
    }

    @Test
    void createUnavailableSlotForClinicUser_delegatesToResolvedClinic() {
        when(vetClinicRepository.existsById(10L)).thenReturn(true);
        String futureTime = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0).toString();
        CreateUnavailableSlotRequest request = new CreateUnavailableSlotRequest();
        request.setDateTime(futureTime);
        request.setReason("Vet on leave");

        ClinicUnavailableSlotDTO result = appointmentService.createUnavailableSlotForClinicUser(1L, request);

        assertEquals(10L, result.getClinicId());
    }

    @Test
    void deleteUnavailableSlot_notFound() {
        when(unavailableSlotRepository.findBySlotIdAndClinicId(5L, 10L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> appointmentService.deleteUnavailableSlot(10L, 5L));
        assertEquals("Unavailable slot not found", ex.getMessage());
    }

    @Test
    void deleteUnavailableSlot_success() {
        ClinicUnavailableSlot slot = new ClinicUnavailableSlot(10L, LocalDateTime.now().plusDays(1), "closed");
        slot.setSlotId(5L);
        when(unavailableSlotRepository.findBySlotIdAndClinicId(5L, 10L)).thenReturn(Optional.of(slot));

        appointmentService.deleteUnavailableSlot(10L, 5L);

        verify(unavailableSlotRepository).delete(slot);
    }

    @Test
    void deleteUnavailableSlot_nullClinicId() {
        when(unavailableSlotRepository.findBySlotIdAndClinicId(5L, null)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> appointmentService.deleteUnavailableSlot(null, 5L));
        assertEquals("Unavailable slot not found", ex.getMessage());
    }

    @Test
    void deleteUnavailableSlot_negativeClinicIdStillResolves() {
        ClinicUnavailableSlot slot = new ClinicUnavailableSlot(-10L, LocalDateTime.now().plusDays(1), "closed");
        slot.setSlotId(5L);
        when(unavailableSlotRepository.findBySlotIdAndClinicId(5L, -10L)).thenReturn(Optional.of(slot));

        appointmentService.deleteUnavailableSlot(-10L, 5L);

        verify(unavailableSlotRepository).delete(slot);
    }

    @Test
    void deleteUnavailableSlot_zeroClinicIdStillResolves() {
        ClinicUnavailableSlot slot = new ClinicUnavailableSlot(0L, LocalDateTime.now().plusDays(1), "closed");
        slot.setSlotId(5L);
        when(unavailableSlotRepository.findBySlotIdAndClinicId(5L, 0L)).thenReturn(Optional.of(slot));

        appointmentService.deleteUnavailableSlot(0L, 5L);

        verify(unavailableSlotRepository).delete(slot);
    }

    @Test
    void deleteUnavailableSlot_nullSlotId() {
        when(unavailableSlotRepository.findBySlotIdAndClinicId(null, 10L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> appointmentService.deleteUnavailableSlot(10L, null));
        assertEquals("Unavailable slot not found", ex.getMessage());
    }

    @Test
    void deleteUnavailableSlot_negativeSlotIdStillResolves() {
        ClinicUnavailableSlot slot = new ClinicUnavailableSlot(10L, LocalDateTime.now().plusDays(1), "closed");
        slot.setSlotId(-5L);
        when(unavailableSlotRepository.findBySlotIdAndClinicId(-5L, 10L)).thenReturn(Optional.of(slot));

        appointmentService.deleteUnavailableSlot(10L, -5L);

        verify(unavailableSlotRepository).delete(slot);
    }

    @Test
    void deleteUnavailableSlot_zeroSlotIdStillResolves() {
        ClinicUnavailableSlot slot = new ClinicUnavailableSlot(10L, LocalDateTime.now().plusDays(1), "closed");
        slot.setSlotId(0L);
        when(unavailableSlotRepository.findBySlotIdAndClinicId(0L, 10L)).thenReturn(Optional.of(slot));

        appointmentService.deleteUnavailableSlot(10L, 0L);

        verify(unavailableSlotRepository).delete(slot);
    }

    @Test
    void deleteUnavailableSlotForClinicUser_delegatesToResolvedClinic() {
        ClinicUnavailableSlot slot = new ClinicUnavailableSlot(10L, LocalDateTime.now().plusDays(1), "closed");
        slot.setSlotId(5L);
        when(unavailableSlotRepository.findBySlotIdAndClinicId(5L, 10L)).thenReturn(Optional.of(slot));

        appointmentService.deleteUnavailableSlotForClinicUser(1L, 5L);

        verify(unavailableSlotRepository).delete(slot);
    }

    @Test
    void cancelAppointment_skipsNotificationWhenClinicHasNoLinkedUser() {
        Appointment appointment = createMockOwnerAppointment(1L, LocalDateTime.now().plusDays(1), "CONFIRMED");
        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(appointment));
        when(vetClinicRepository.findById(1L)).thenReturn(Optional.empty());

        OwnerAppointmentDTO result = appointmentService.cancelAppointmentForOwner(1L, 100L);

        assertEquals("CANCELLED", result.getStatus());
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void cancelAppointment_skipsNotificationWhenClinicUserNotFound() {
        Appointment appointment = createMockOwnerAppointment(1L, LocalDateTime.now().plusDays(1), "CONFIRMED");
        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(appointment));
        VetClinic clinic = new VetClinic();
        clinic.setClinicId(1L);
        clinic.setUserId(50L);
        when(vetClinicRepository.findById(1L)).thenReturn(Optional.of(clinic));
        when(userRepository.findById(50L)).thenReturn(Optional.empty());

        appointmentService.cancelAppointmentForOwner(1L, 100L);

        verify(notificationRepository, never()).save(any());
    }

    @Test
    void cancelAppointment_savesNotificationForClinicUser() {
        Appointment appointment = createMockOwnerAppointment(1L, LocalDateTime.now().plusDays(1), "CONFIRMED");
        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(appointment));
        VetClinic clinic = new VetClinic();
        clinic.setClinicId(1L);
        clinic.setUserId(50L);
        when(vetClinicRepository.findById(1L)).thenReturn(Optional.of(clinic));
        User clinicUser = new User("clinicuser", "clinic@petify.test", "x", "Clinic", "User");
        clinicUser.setUserId(50L);
        when(userRepository.findById(50L)).thenReturn(Optional.of(clinicUser));

        appointmentService.cancelAppointmentForOwner(1L, 100L);

        verify(notificationRepository).save(any());
    }
}