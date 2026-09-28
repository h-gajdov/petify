package com.petify.petify.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class MaintenanceSchedulerTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private MaintenanceScheduler maintenanceScheduler;

    @Test
    void markNoShowAppointmentsCallsTheNoShowProcedure() {
        maintenanceScheduler.markNoShowAppointments();

        verify(jdbcTemplate).execute("CALL job_mark_no_show()");
        verifyNoMoreInteractions(jdbcTemplate);
    }

    @Test
    void archiveStaleDraftListingsCallsTheArchiveProcedure() {
        maintenanceScheduler.archiveStaleDraftListings();

        verify(jdbcTemplate).execute("CALL job_archive_stale_drafts()");
        verifyNoMoreInteractions(jdbcTemplate);
    }
}
