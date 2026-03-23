package capstone_1.Ecotrack_backend.controller.user;

import capstone_1.Ecotrack_backend.cloudinaryconfig.CloudinaryService;
import capstone_1.Ecotrack_backend.model.WasteReport;
import capstone_1.Ecotrack_backend.service.WasteReportService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WasteReportControllerTest {

    @Mock
    private WasteReportService reportService;

    @Mock
    private CloudinaryService cloudinaryService;

    private WasteReportController controller;

    @BeforeEach
    void setUp() {
        controller = new WasteReportController(reportService, cloudinaryService);
    }

    @Test
    void submitReportShouldReturnUnauthorizedWhenUserIdMissing() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        ResponseEntity<?> response = controller.submitReport(
                request,
                "Bao cao",
                "Vo co",
                "Mo ta",
                10.1,
                106.2,
                "PENDING",
                null
        );

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertInstanceOf(Map.class, response.getBody());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals(false, body.get("success"));
        assertEquals("Khong tim thay thong tin nguoi dung. Vui long dang nhap.", body.get("message"));
        verify(reportService, never()).saveReport(any(), any());
    }

    @Test
    void getMyReportsShouldReturnUnauthorizedWhenUserIdMissing() {
        HttpServletRequest request = new MockHttpServletRequest();

        assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> controller.getMyReports(request));
    }

    @Test
    void getMyReportsShouldReturnReportsForCurrentUser() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("userId", 7L);
        WasteReport report = new WasteReport();
        report.setReportId(99L);
        when(reportService.getReportsByUser(7L)).thenReturn(List.of(report));

        ResponseEntity<List<WasteReport>> response = controller.getMyReports(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(99L, response.getBody().get(0).getReportId());
    }

    @Test
    void submitReportShouldReturnSuccessPayloadWhenSaved() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("userId", 5L);
        MockMultipartFile image = new MockMultipartFile(
                "image", "report.jpg", "image/jpeg", "demo".getBytes()
        );

        WasteReport saved = new WasteReport();
        saved.setReportId(12L);
        saved.setStatus(WasteReport.Status.VERIFIED);
        saved.setCreatedAt(LocalDateTime.of(2026, 3, 23, 10, 30));

        when(cloudinaryService.uploadImage(eq(image), eq("ecotrack/reports"))).thenReturn("http://image.test/report.jpg");
        when(reportService.saveReport(any(WasteReport.class), eq(image))).thenReturn(saved);

        ResponseEntity<?> response = controller.submitReport(
                request,
                "Bao cao rac",
                "Vo co",
                "Mo ta chi tiet",
                10.75,
                106.67,
                "pending",
                image
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(Map.class, response.getBody());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertTrue((Boolean) body.get("success"));
        assertEquals("VERIFIED", body.get("status"));
        assertEquals(10, body.get("points"));
        assertEquals("TXN-12", body.get("transactionCode"));
        verify(reportService).saveReport(any(WasteReport.class), eq(image));
    }
}
