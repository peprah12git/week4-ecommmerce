package com.controllers;

import com.service.OrderService;
import com.service.ProductService;
import com.service.UserService;
import com.util.PerformanceTimer;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class PerformanceReportController {

    @FXML private Label lblCacheHitRate;
    @FXML private Label lblCachedItems;
    @FXML private Label lblAvgQueryTime;
    @FXML private Label lblTotalQueries;
    @FXML private Label lblDbStatus;
    @FXML private Label lblDbRecords;
    @FXML private TextArea txtTestResults;
    @FXML private TextArea txtPerformanceLog;
    @FXML private CheckBox chkAutoRefresh;
    @FXML private TableView<PerformanceMetric> tblPerformanceMetrics;
    @FXML private TableColumn<PerformanceMetric, String> colService;
    @FXML private TableColumn<PerformanceMetric, String> colCacheSize;
    @FXML private TableColumn<PerformanceMetric, String> colCacheAge;
    @FXML private TableColumn<PerformanceMetric, String> colCacheValid;
    @FXML private TableColumn<PerformanceMetric, String> colLastQuery;

    private ProductService productService;
    private OrderService orderService;
    private UserService userService;
    private Timer autoRefreshTimer;
    private int totalQueries = 0;
    private double totalQueryTime = 0;

    @FXML
    public void initialize() {
        productService = ProductService.getInstance();
        orderService = OrderService.getInstance();
        userService = UserService.getInstance();

        setupTable();
        refreshStats();
        logMessage("Performance monitoring initialized");
    }

    private void setupTable() {
        colService.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().serviceName));
        colCacheSize.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().cacheSize));
        colCacheAge.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().cacheAge));
        colCacheValid.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().cacheValid));
        colLastQuery.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().lastQuery));
    }

    @FXML
    private void testCachePerformance() {
        txtTestResults.clear();
        appendResult("=== Cache Performance Test ===\n");

        PerformanceTimer.comparePerformance(
            "Product Query Performance",
            "Without Cache", () -> {
                productService.measureQueryTime(false);
                return null;
            },
            "With Cache", () -> {
                productService.measureQueryTime(true);
                return null;
            }
        );

        long withoutCache = productService.measureQueryTime(false);
        long withCache = productService.measureQueryTime(true);
        
        appendResult(String.format("Without Cache: %d ms\n", withoutCache));
        appendResult(String.format("With Cache: %d ms\n", withCache));
        appendResult(String.format("Improvement: %.1f%%\n", ((withoutCache - withCache) * 100.0 / withoutCache)));
        
        logMessage("Cache performance test completed");
        refreshStats();
    }

    @FXML
    private void testQuerySpeed() {
        txtTestResults.clear();
        appendResult("=== Query Speed Test ===\n");

        PerformanceTimer.TimedResult<Integer> productResult = PerformanceTimer.timeWithResult(
            "Product Query",
            () -> productService.getAllProducts().size()
        );

        PerformanceTimer.TimedResult<Integer> orderResult = PerformanceTimer.timeWithResult(
            "Order Query",
            () -> orderService.getAllOrders().size()
        );

        PerformanceTimer.TimedResult<Integer> userResult = PerformanceTimer.timeWithResult(
            "User Query",
            () -> userService.getAllUsers().size()
        );

        appendResult(String.format("Products: %d records in %.2f ms\n", 
            productResult.getResult(), productResult.getDurationMs()));
        appendResult(String.format("Orders: %d records in %.2f ms\n", 
            orderResult.getResult(), orderResult.getDurationMs()));
        appendResult(String.format("Users: %d records in %.2f ms\n", 
            userResult.getResult(), userResult.getDurationMs()));

        totalQueries += 3;
        totalQueryTime += productResult.getDurationMs() + orderResult.getDurationMs() + userResult.getDurationMs();

        logMessage("Query speed test completed");
        refreshStats();
    }

    @FXML
    private void refreshStats() {
        Map<String, Object> productStats = productService.getCacheStats();
        
        int cachedProducts = (int) productStats.get("cachedProducts");
        int allProductsSize = (int) productStats.get("allProductsSize");
        long cacheAge = (long) productStats.get("cacheAge");
        boolean cacheValid = (boolean) productStats.get("cacheValid");

        lblCachedItems.setText("Cached Items: " + cachedProducts);
        lblCacheHitRate.setText(cacheValid ? "Hit Rate: Active" : "Hit Rate: Expired");
        
        if (totalQueries > 0) {
            lblAvgQueryTime.setText(String.format("Avg Time: %.2f ms", totalQueryTime / totalQueries));
            lblTotalQueries.setText("Total Queries: " + totalQueries);
        }

        lblDbStatus.setText("Status: Connected");
        lblDbRecords.setText("Total Records: " + (allProductsSize + orderService.getAllOrders().size() + userService.getAllUsers().size()));

        updateMetricsTable();
        logMessage("Statistics refreshed");
    }

    private void updateMetricsTable() {
        ObservableList<PerformanceMetric> metrics = FXCollections.observableArrayList();

        Map<String, Object> productStats = productService.getCacheStats();
        metrics.add(new PerformanceMetric(
            "ProductService",
            String.valueOf(productStats.get("cachedProducts")),
            String.format("%.1f", (long)productStats.get("cacheAge") / 1000.0),
            String.valueOf(productStats.get("cacheValid")),
            "N/A"
        ));

        metrics.add(new PerformanceMetric(
            "OrderService",
            String.valueOf(orderService.getAllOrders().size()),
            "N/A",
            "Active",
            "N/A"
        ));

        metrics.add(new PerformanceMetric(
            "UserService",
            String.valueOf(userService.getAllUsers().size()),
            "N/A",
            "Active",
            "N/A"
        ));

        tblPerformanceMetrics.setItems(metrics);
    }

    @FXML
    private void clearCache() {
        productService.getAllProducts();
        appendResult("Cache cleared and reloaded\n");
        logMessage("Cache cleared");
        refreshStats();
    }

    @FXML
    private void toggleAutoRefresh() {
        if (chkAutoRefresh.isSelected()) {
            autoRefreshTimer = new Timer(true);
            autoRefreshTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> {
                        refreshStats();
                        logMessage("Auto-refresh triggered");
                    });
                }
            }, 5000, 5000);
            logMessage("Auto-refresh enabled (5s interval)");
        } else {
            if (autoRefreshTimer != null) {
                autoRefreshTimer.cancel();
                autoRefreshTimer = null;
            }
            logMessage("Auto-refresh disabled");
        }
    }

    private void appendResult(String text) {
        txtTestResults.appendText(text);
    }

    private void logMessage(String message) {
        String timestamp = new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date());
        Platform.runLater(() -> {
            txtPerformanceLog.appendText(String.format("[%s] %s\n", timestamp, message));
        });
    }

    public static class PerformanceMetric {
        private final String serviceName;
        private final String cacheSize;
        private final String cacheAge;
        private final String cacheValid;
        private final String lastQuery;

        public PerformanceMetric(String serviceName, String cacheSize, String cacheAge, 
                                String cacheValid, String lastQuery) {
            this.serviceName = serviceName;
            this.cacheSize = cacheSize;
            this.cacheAge = cacheAge;
            this.cacheValid = cacheValid;
            this.lastQuery = lastQuery;
        }
    }
}
