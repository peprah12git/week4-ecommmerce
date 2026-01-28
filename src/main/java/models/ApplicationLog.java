package models;

import java.util.HashMap;
import java.util.Map;

/**
 * ApplicationLog Model - Represents unstructured application logs
 * Designed for MongoDB storage where schema flexibility is beneficial
 * 
 * Why unstructured logs work well with MongoDB:
 * - Different log types have different fields
 * - Easy to store additional context/metadata
 * - Flexible serialization/deserialization
 * - No need for schema migrations
 */
public class ApplicationLog {
    
    private String logId;
    private String logLevel; // DEBUG, INFO, WARNING, ERROR, CRITICAL
    private String message;
    private String source; // Class or component name
    private String methodName;
    private long timestamp;
    
    // Flexible fields for different log types
    private String userId; // For user action logs
    private String productId; // For product-related logs
    private String orderId; // For order-related logs
    private String errorStackTrace; // For error logs
    private String userAgent; // For web requests
    private String ipAddress; // For security audit
    
    // Dynamic metadata (can vary per log entry)
    private Map<String, Object> metadata = new HashMap<>();
    
    // Performance metrics
    private long executionTimeMs;
    private String databaseQuery; // For slow query logs
    private int recordsAffected;
    
    // Audit information
    private String action; // CREATE, READ, UPDATE, DELETE
    private String entityType; // USER, PRODUCT, ORDER
    private String changedFields; // JSON of what changed
    
    public ApplicationLog() {
    }
    
    public ApplicationLog(String logLevel, String message, String source) {
        this.logLevel = logLevel;
        this.message = message;
        this.source = source;
        this.timestamp = System.currentTimeMillis();
    }
    
    // Getters and Setters
    public String getLogId() {
        return logId;
    }
    
    public void setLogId(String logId) {
        this.logId = logId;
    }
    
    public String getLogLevel() {
        return logLevel;
    }
    
    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getSource() {
        return source;
    }
    
    public void setSource(String source) {
        this.source = source;
    }
    
    public String getMethodName() {
        return methodName;
    }
    
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getProductId() {
        return productId;
    }
    
    public void setProductId(String productId) {
        this.productId = productId;
    }
    
    public String getOrderId() {
        return orderId;
    }
    
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    
    public String getErrorStackTrace() {
        return errorStackTrace;
    }
    
    public void setErrorStackTrace(String errorStackTrace) {
        this.errorStackTrace = errorStackTrace;
    }
    
    public String getUserAgent() {
        return userAgent;
    }
    
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
    
    public void addMetadata(String key, Object value) {
        this.metadata.put(key, value);
    }
    
    public long getExecutionTimeMs() {
        return executionTimeMs;
    }
    
    public void setExecutionTimeMs(long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }
    
    public String getDatabaseQuery() {
        return databaseQuery;
    }
    
    public void setDatabaseQuery(String databaseQuery) {
        this.databaseQuery = databaseQuery;
    }
    
    public int getRecordsAffected() {
        return recordsAffected;
    }
    
    public void setRecordsAffected(int recordsAffected) {
        this.recordsAffected = recordsAffected;
    }
    
    public String getAction() {
        return action;
    }
    
    public void setAction(String action) {
        this.action = action;
    }
    
    public String getEntityType() {
        return entityType;
    }
    
    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }
    
    public String getChangedFields() {
        return changedFields;
    }
    
    public void setChangedFields(String changedFields) {
        this.changedFields = changedFields;
    }
    
    @Override
    public String toString() {
        return "[" + logLevel + "] " + source + "." + methodName + " - " + message + " (Time: " + timestamp + ")";
    }
}
