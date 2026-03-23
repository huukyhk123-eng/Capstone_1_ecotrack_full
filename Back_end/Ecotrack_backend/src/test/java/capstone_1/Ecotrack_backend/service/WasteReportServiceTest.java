package capstone_1.Ecotrack_backend.service;

import capstone_1.Ecotrack_backend.model.NotificationType;
import capstone_1.Ecotrack_backend.model.PointTransaction;
import capstone_1.Ecotrack_backend.model.WasteReport;
import capstone_1.Ecotrack_backend.repository.PointTransactionRepository;
import capstone_1.Ecotrack_backend.repository.UserPointsRepository;
import capstone_1.Ecotrack_backend.repository.UserRepository;
import capstone_1.Ecotrack_backend.repository.WasteReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WasteReportServiceTest {

    @Mock
    private WasteReportRepository reportRepository;

    @Mock
    private PointTransactionRepository pointTransactionRepository;

    @Mock
    private UserPointsRepository userPointsRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    private WasteReportService wasteReportService;

    @BeforeEach
    void setUp() {
        wasteReportService = new WasteReportService(reportRepository);
        ReflectionTestUtils.setField(wasteReportService, "pointTransactionRepository", pointTransactionRepository);
        ReflectionTestUtils.setField(wasteReportService, "userPointsRepository", userPointsRepository);
        ReflectionTestUtils.setField(wasteReportService, "userRepository", userRepository);
        ReflectionTestUtils.setField(wasteReportService, "notificationService", notificationService);
    }

    @Test
    void saveReportShouldRejectWhenDailyLimitReached() {
        WasteReport report = buildReport();
        when(reportRepository.countByUserIdAndCreatedAtAfter(eq(5L), any())).thenReturn(10L);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> wasteReportService.saveReport(report, null));

        assertFalse(ex.getMessage().isBlank());
        verify(reportRepository, never()).save(any());
    }

    @Test
    void saveReportShouldRejectWhenCooldownHasNotElapsed() {
        WasteReport report = buildReport();
        WasteReport latest = buildReport();
        latest.setCreatedAt(LocalDateTime.now().minusMinutes(2));

        when(reportRepository.countByUserIdAndCreatedAtAfter(eq(5L), any())).thenReturn(0L);
        when(reportRepository.findTopByUserIdOrderByCreatedAtDesc(5L)).thenReturn(Optional.of(latest));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> wasteReportService.saveReport(report, null));

        assertFalse(ex.getMessage().isBlank());
        verify(reportRepository, never()).save(any());
    }

    @Test
    void saveReportShouldPersistPendingReportAndNotifyWhenNoImageProvided() {
        WasteReport report = buildReport();
        WasteReport saved = buildReport();
        saved.setReportId(55L);
        saved.setStatus(WasteReport.Status.PENDING);

        when(reportRepository.countByUserIdAndCreatedAtAfter(eq(5L), any())).thenReturn(0L);
        when(reportRepository.findTopByUserIdOrderByCreatedAtDesc(5L)).thenReturn(Optional.empty());
        when(reportRepository.save(any(WasteReport.class))).thenReturn(saved);

        WasteReport result = wasteReportService.saveReport(report, null);

        assertSame(saved, result);
        ArgumentCaptor<WasteReport> reportCaptor = ArgumentCaptor.forClass(WasteReport.class);
        verify(reportRepository).save(reportCaptor.capture());
        assertEquals(WasteReport.Status.PENDING, reportCaptor.getValue().getStatus());
        verify(pointTransactionRepository, never()).save(any(PointTransaction.class));
        verify(userRepository, never()).findById(any());
        verify(userPointsRepository, never()).findById(any());
        verify(notificationService).createNotification(
                eq(5L),
                eq(NotificationType.SYSTEM),
                any(String.class),
                any(String.class),
                eq("REPORT"),
                eq(55L)
        );
    }

    @Test
    void getReportsByUserShouldDelegateToRepository() {
        WasteReport report = buildReport();
        when(reportRepository.findByUserIdOrderByCreatedAtDesc(5L)).thenReturn(List.of(report));

        List<WasteReport> reports = wasteReportService.getReportsByUser(5L);

        assertEquals(1, reports.size());
        assertSame(report, reports.get(0));
    }

    private WasteReport buildReport() {
        WasteReport report = new WasteReport();
        report.setReportId(1L);
        report.setUserId(5L);
        report.setTitle("Bao cao rac");
        report.setDescription("Mo ta");
        report.setCategory("Vo co");
        report.setGpsLat(BigDecimal.valueOf(10.75));
        report.setGpsLong(BigDecimal.valueOf(106.67));
        report.setStatus(WasteReport.Status.PENDING);
        report.setCreatedAt(LocalDateTime.now());
        return report;
    }
}
