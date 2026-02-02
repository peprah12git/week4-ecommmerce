package services;

import com.util.CorrectedPerformanceReport;
import org.junit.jupiter.api.*;
import java.io.File;
import static org.junit.jupiter.api.Assertions.*;

public class PerformanceReportServiceTest {

    @Test
    void testGenerateCorrectedReport() {
        assertDoesNotThrow(() -> CorrectedPerformanceReport.generateCorrectedReport());
    }

    @Test
    void testReportFileCreation() {
        CorrectedPerformanceReport.generateCorrectedReport();
        File dir = new File(".");
        File[] files = dir.listFiles((d, name) -> name.startsWith("corrected_performance_report_"));
        assertNotNull(files);
        assertTrue(files.length > 0);
    }

    @Test
    void testReportNotNull() {
        assertDoesNotThrow(() -> {
            CorrectedPerformanceReport.generateCorrectedReport();
        });
    }

    @Test
    void testMultipleReportGeneration() {
        assertDoesNotThrow(() -> {
            CorrectedPerformanceReport.generateCorrectedReport();
            CorrectedPerformanceReport.generateCorrectedReport();
        });
    }
}
