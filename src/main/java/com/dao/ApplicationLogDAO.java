package com.dao;

import com.ecommerce.config.MongoDBConnection;
import com.ecommerce.models.ApplicationLog;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

/**
 * MongoDB DAO for Application Logs - Handles unstructured application log data
 * 
 * Why MongoDB for Application Logs:

 */
public class ApplicationLogDAO {
    
    private MongoCollection<Document> logsCollection;
    
    public ApplicationLogDAO() {
        this.logsCollection = MongoDBConnection.getInstance()
                .getCollection("application_logs");
        
        // Create TTL index to auto-delete logs older than 30 days
        try {
            logsCollection.createIndex(new Document("timestamp", 1)
                    .append("expireAfterSeconds", 30 * 24 * 60 * 60)); // 30 days in seconds
            System.out.println("✓ TTL index created for automatic log cleanup");
        } catch (Exception e) {
            System.out.println("ℹ TTL index may already exist: " + e.getMessage());
        }
    }
    
    /**
     * Create a new application log in MongoDB
     * Demonstrates how unstructured data is flexibly stored
     */
    public void createLog(ApplicationLog log) {
        try {
            Document logDoc = new Document()
                    .append("_id", new ObjectId())
                    .append("logLevel", log.getLogLevel())
                    .append("message", log.getMessage())
                    .append("source", log.getSource())
                    .append("methodName", log.getMethodName())
                    .append("timestamp", log.getTimestamp());
            
            // Only add optional fields if they're set (flexible schema)
            if (log.getUserId() != null) logDoc.append("userId", log.getUserId());
            if (log.getProductId() != null) logDoc.append("productId", log.getProductId());
            if (log.getOrderId() != null) logDoc.append("orderId", log.getOrderId());
            if (log.getErrorStackTrace() != null) logDoc.append("errorStackTrace", log.getErrorStackTrace());
            if (log.getUserAgent() != null) logDoc.append("userAgent", log.getUserAgent());
            if (log.getIpAddress() != null) logDoc.append("ipAddress", log.getIpAddress());
            if (log.getExecutionTimeMs() > 0) logDoc.append("executionTimeMs", log.getExecutionTimeMs());
            if (log.getDatabaseQuery() != null) logDoc.append("databaseQuery", log.getDatabaseQuery());
            if (log.getRecordsAffected() > 0) logDoc.append("recordsAffected", log.getRecordsAffected());
            if (log.getAction() != null) logDoc.append("action", log.getAction());
            if (log.getEntityType() != null) logDoc.append("entityType", log.getEntityType());
            if (log.getChangedFields() != null) logDoc.append("changedFields", log.getChangedFields());
            
            // Add dynamic metadata
            if (!log.getMetadata().isEmpty()) {
                logDoc.append("metadata", new Document(log.getMetadata()));
            }
            
            logsCollection.insertOne(logDoc);
            
        } catch (Exception e) {
            System.err.println("✗ Error creating log: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Get logs by level (INFO, ERROR, WARNING, etc.)
     */
    public List<ApplicationLog> getLogsByLevel(String logLevel) {
        List<ApplicationLog> logs = new ArrayList<>();
        
        try {
            FindIterable<Document> documents = logsCollection
                    .find(Filters.eq("logLevel", logLevel))
                    .sort(Sorts.descending("timestamp")); // Most recent first
            
            for (Document doc : documents) {
                logs.add(convertDocumentToLog(doc));
            }
            
            System.out.println("✓ Retrieved " + logs.size() + " logs with level: " + logLevel);
            
        } catch (Exception e) {
            System.err.println("✗ Error retrieving logs by level: " + e.getMessage());
        }
        
        return logs;
    }
    
    /**
     * Get logs from a specific source (class/component)
     */
    public List<ApplicationLog> getLogsBySource(String source) {
        List<ApplicationLog> logs = new ArrayList<>();
        
        try {
            FindIterable<Document> documents = logsCollection
                    .find(Filters.eq("source", source))
                    .sort(Sorts.descending("timestamp"));
            
            for (Document doc : documents) {
                logs.add(convertDocumentToLog(doc));
            }
        } catch (Exception e) {
            System.err.println("✗ Error retrieving logs by source: " + e.getMessage());
        }
        
        return logs;
    }
    
    /**
     * Get error logs (ERROR and CRITICAL levels)
     * Demonstrates filtering on specific field values
     */
    public List<ApplicationLog> getErrorLogs() {
        List<ApplicationLog> logs = new ArrayList<>();
        
        try {
            FindIterable<Document> documents = logsCollection
                    .find(Filters.in("logLevel", "ERROR", "CRITICAL"))
                    .sort(Sorts.descending("timestamp"));
            
            for (Document doc : documents) {
                logs.add(convertDocumentToLog(doc));
            }
            
            System.out.println("✓ Retrieved " + logs.size() + " error logs");
            
        } catch (Exception e) {
            System.err.println("✗ Error retrieving error logs: " + e.getMessage());
        }
        
        return logs;
    }
    
    /**
     * Get logs for a specific user (audit trail)
     * Demonstrates querying on user-specific data
     */
    public List<ApplicationLog> getLogsByUserId(String userId) {
        List<ApplicationLog> logs = new ArrayList<>();
        
        try {
            FindIterable<Document> documents = logsCollection
                    .find(Filters.eq("userId", userId))
                    .sort(Sorts.descending("timestamp"));
            
            for (Document doc : documents) {
                logs.add(convertDocumentToLog(doc));
            }
        } catch (Exception e) {
            System.err.println("✗ Error retrieving user logs: " + e.getMessage());
        }
        
        return logs;
    }
    
    /**
     * Get logs for a specific time range
     */
    public List<ApplicationLog> getLogsByTimeRange(long startTime, long endTime) {
        List<ApplicationLog> logs = new ArrayList<>();
        
        try {
            FindIterable<Document> documents = logsCollection
                    .find(Filters.and(
                            Filters.gte("timestamp", startTime),
                            Filters.lte("timestamp", endTime)
                    ))
                    .sort(Sorts.descending("timestamp"));
            
            for (Document doc : documents) {
                logs.add(convertDocumentToLog(doc));
            }
            
            System.out.println("✓ Retrieved " + logs.size() + " logs in time range");
            
        } catch (Exception e) {
            System.err.println("✗ Error retrieving logs by time range: " + e.getMessage());
        }
        
        return logs;
    }
    
    /**
     * Get slow queries (execution time > threshold)
     * Example of querying unstructured performance data
     */
    public List<ApplicationLog> getSlowQueries(long thresholdMs) {
        List<ApplicationLog> logs = new ArrayList<>();
        
        try {
            FindIterable<Document> documents = logsCollection
                    .find(Filters.and(
                            Filters.exists("databaseQuery"),
                            Filters.gte("executionTimeMs", thresholdMs)
                    ))
                    .sort(Sorts.descending("executionTimeMs"));
            
            for (Document doc : documents) {
                logs.add(convertDocumentToLog(doc));
            }
            
            System.out.println("✓ Found " + logs.size() + " slow queries (> " + thresholdMs + "ms)");
            
        } catch (Exception e) {
            System.err.println("✗ Error retrieving slow queries: " + e.getMessage());
        }
        
        return logs;
    }
    
    /**
     * Get audit logs for specific entity type and action
     * Demonstrates flexible schema for audit trails
     */
    public List<ApplicationLog> getAuditLogs(String entityType, String action) {
        List<ApplicationLog> logs = new ArrayList<>();
        
        try {
            FindIterable<Document> documents = logsCollection
                    .find(Filters.and(
                            Filters.eq("entityType", entityType),
                            Filters.eq("action", action)
                    ))
                    .sort(Sorts.descending("timestamp"));
            
            for (Document doc : documents) {
                logs.add(convertDocumentToLog(doc));
            }
        } catch (Exception e) {
            System.err.println("✗ Error retrieving audit logs: " + e.getMessage());
        }
        
        return logs;
    }
    
    /**
     * Search logs by message content (text search)
     * Demonstrates unstructured data search capability
     */
    public List<ApplicationLog> searchLogsByMessage(String keyword) {
        List<ApplicationLog> logs = new ArrayList<>();
        
        try {
            FindIterable<Document> documents = logsCollection
                    .find(Filters.regex("message", keyword, "i"))
                    .sort(Sorts.descending("timestamp"));
            
            for (Document doc : documents) {
                logs.add(convertDocumentToLog(doc));
            }
            
            System.out.println("✓ Found " + logs.size() + " logs matching: " + keyword);
            
        } catch (Exception e) {
            System.err.println("✗ Error searching logs: " + e.getMessage());
        }
        
        return logs;
    }
    
    /**
     * Get logs with specific metadata key-value pairs
     * Demonstrates querying nested flexible data
     */
    public List<ApplicationLog> getLogsByMetadata(String key, Object value) {
        List<ApplicationLog> logs = new ArrayList<>();
        
        try {
            FindIterable<Document> documents = logsCollection
                    .find(Filters.eq("metadata." + key, value))
                    .sort(Sorts.descending("timestamp"));
            
            for (Document doc : documents) {
                logs.add(convertDocumentToLog(doc));
            }
        } catch (Exception e) {
            System.err.println("✗ Error retrieving logs by metadata: " + e.getMessage());
        }
        
        return logs;
    }
    
    /**
     * Get most recent N logs
     */
    public List<ApplicationLog> getRecentLogs(int limit) {
        List<ApplicationLog> logs = new ArrayList<>();
        
        try {
            FindIterable<Document> documents = logsCollection
                    .find()
                    .sort(Sorts.descending("timestamp"))
                    .limit(limit);
            
            for (Document doc : documents) {
                logs.add(convertDocumentToLog(doc));
            }
        } catch (Exception e) {
            System.err.println("✗ Error retrieving recent logs: " + e.getMessage());
        }
        
        return logs;
    }
    
    /**
     * Delete old logs (manual cleanup, in addition to TTL)
     */
    public void deleteOldLogs(long olderThanTimestamp) {
        try {
            long deletedCount = logsCollection.deleteMany(
                    Filters.lt("timestamp", olderThanTimestamp)
            ).getDeletedCount();
            
            System.out.println("✓ Deleted " + deletedCount + " old logs");
        } catch (Exception e) {
            System.err.println("✗ Error deleting old logs: " + e.getMessage());
        }
    }
    
    /**
     * Get log statistics (aggregate data)
     * Demonstrates MongoDB aggregation pipeline
     */
    public void getLogStatistics() {
        try {
            // Count logs by level
            long totalLogs = logsCollection.countDocuments();
            long errorCount = logsCollection.countDocuments(Filters.eq("logLevel", "ERROR"));
            long warningCount = logsCollection.countDocuments(Filters.eq("logLevel", "WARNING"));
            long infoCount = logsCollection.countDocuments(Filters.eq("logLevel", "INFO"));
            
            System.out.println("\n=== LOG STATISTICS ===");
            System.out.println("Total logs: " + totalLogs);
            System.out.println("ERROR logs: " + errorCount);
            System.out.println("WARNING logs: " + warningCount);
            System.out.println("INFO logs: " + infoCount);
            System.out.println("====================\n");
            
        } catch (Exception e) {
            System.err.println("✗ Error getting log statistics: " + e.getMessage());
        }
    }
    
    /**
     * Convert MongoDB Document to ApplicationLog object
     */
    private ApplicationLog convertDocumentToLog(Document doc) {
        ApplicationLog log = new ApplicationLog();
        log.setLogId(doc.getObjectId("_id").toString());
        log.setLogLevel(doc.getString("logLevel"));
        log.setMessage(doc.getString("message"));
        log.setSource(doc.getString("source"));
        log.setMethodName(doc.getString("methodName"));
        log.setTimestamp(doc.getLong("timestamp"));
        
        // Set optional fields if present
        if (doc.containsKey("userId")) log.setUserId(doc.getString("userId"));
        if (doc.containsKey("productId")) log.setProductId(doc.getString("productId"));
        if (doc.containsKey("orderId")) log.setOrderId(doc.getString("orderId"));
        if (doc.containsKey("errorStackTrace")) log.setErrorStackTrace(doc.getString("errorStackTrace"));
        if (doc.containsKey("executionTimeMs")) log.setExecutionTimeMs(doc.getLong("executionTimeMs"));
        if (doc.containsKey("recordsAffected")) log.setRecordsAffected(doc.getInteger("recordsAffected"));
        if (doc.containsKey("action")) log.setAction(doc.getString("action"));
        if (doc.containsKey("entityType")) log.setEntityType(doc.getString("entityType"));
        if (doc.containsKey("changedFields")) log.setChangedFields(doc.getString("changedFields"));
        
        // Set metadata if present
        if (doc.containsKey("metadata")) {
            Document metadataDoc = doc.get("metadata", Document.class);
            log.setMetadata(new java.util.HashMap<>(metadataDoc));
        }
        
        return log;
    }
}
