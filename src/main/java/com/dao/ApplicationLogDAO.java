package com.dao;

import com.config.MongoDBConnection;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

public class ApplicationLogDAO {
    
    private MongoCollection<Document> logsCollection;
    
    public ApplicationLogDAO() {
        this.logsCollection = MongoDBConnection.getInstance()
                .getCollection("application_logs");
    }
    
    public void createLog(String logLevel, String message, String source) {
        try {
            Document logDoc = new Document()
                    .append("_id", new ObjectId())
                    .append("logLevel", logLevel)
                    .append("message", message)
                    .append("source", source)
                    .append("timestamp", System.currentTimeMillis());
            
            logsCollection.insertOne(logDoc);
            
        } catch (Exception e) {
            System.err.println("Error creating log: " + e.getMessage());
        }
    }
    
    public List<Document> getLogsByLevel(String logLevel) {
        List<Document> logs = new ArrayList<>();
        
        try {
            FindIterable<Document> documents = logsCollection
                    .find(Filters.eq("logLevel", logLevel))
                    .sort(Sorts.descending("timestamp"));
            
            for (Document doc : documents) {
                logs.add(doc);
            }
            
        } catch (Exception e) {
            System.err.println("Error retrieving logs by level: " + e.getMessage());
        }
        
        return logs;
    }
    
    public List<Document> getRecentLogs(int limit) {
        List<Document> logs = new ArrayList<>();
        
        try {
            FindIterable<Document> documents = logsCollection
                    .find()
                    .sort(Sorts.descending("timestamp"))
                    .limit(limit);
            
            for (Document doc : documents) {
                logs.add(doc);
            }
        } catch (Exception e) {
            System.err.println("Error retrieving recent logs: " + e.getMessage());
        }
        
        return logs;
    }
}