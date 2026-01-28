package com.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

/**
 * MongoDB Connection Manager - Singleton Pattern
 * Handles connection to MongoDB NoSQL database
 */
public class MongoDBConnection {
    
    // MongoDB Configuration
    private static final String MONGO_URI = "mongodb://localhost:27017";
    private static final String DATABASE_NAME = "ecommerce_db";
    
    // Collection Names
    public static final String USERS_COLLECTION = "users";
    public static final String PRODUCTS_COLLECTION = "products";
    public static final String ORDERS_COLLECTION = "orders";
    public static final String ORDER_ITEMS_COLLECTION = "order_items";
    public static final String CATEGORIES_COLLECTION = "categories";
    public static final String REVIEWS_COLLECTION = "reviews";
    public static final String INVENTORY_COLLECTION = "inventory";
    
    private static MongoDBConnection instance;
    private MongoClient mongoClient;
    private MongoDatabase database;
    
    /**
     * Private constructor - prevents instantiation
     */
    private MongoDBConnection() {
        try {
            // Create MongoDB client connection
            this.mongoClient = MongoClients.create(MONGO_URI);
            this.database = mongoClient.getDatabase(DATABASE_NAME);
            
            // Verify connection by executing a ping command
            Document pingResult = database.runCommand(new Document("ping", 1));
            System.out.println("✓ MongoDB connected! Response: " + pingResult);
            
        } catch (Exception e) {
            System.err.println("✗ MongoDB Connection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Get singleton instance of MongoDBConnection
     * @return MongoDBConnection instance
     */
    public static synchronized MongoDBConnection getInstance() {
        if (instance == null || !isConnectionValid()) {
            instance = new MongoDBConnection();
        }
        return instance;
    }
    
    /**
     * Verify if MongoDB connection is still valid
     * @return true if connection is valid, false otherwise
     */
    private static boolean isConnectionValid() {
        try {
            if (instance != null && instance.mongoClient != null && instance.database != null) {
                // Attempt a simple ping to verify connection
                instance.database.runCommand(new Document("ping", 1));
                return true;
            }
        } catch (Exception e) {
            System.err.println("⚠ Connection validation failed: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Get MongoDB database instance
     * @return MongoDatabase object
     */
    public MongoDatabase getDatabase() {
        if (database == null) {
            this.mongoClient = MongoClients.create(MONGO_URI);
            this.database = mongoClient.getDatabase(DATABASE_NAME);
        }
        return database;
    }
    
    /**
     * Get MongoDB client
     * @return MongoClient object
     */
    public MongoClient getMongoClient() {
        return mongoClient;
    }
    
    /**
     * Get specific collection from database
     * @param collectionName Name of the collection
     * @return MongoCollection object
     */
    public com.mongodb.client.MongoCollection<Document> getCollection(String collectionName) {
        return database.getCollection(collectionName);
    }
    
    /**
     * Close MongoDB connection
     */
    public void closeConnection() {
        try {
            if (mongoClient != null) {
                mongoClient.close();
                System.out.println("✓ MongoDB connection closed.");
            }
        } catch (Exception e) {
            System.err.println("✗ Error closing connection: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Initialize MongoDB collections (create if not exists)
     */
    public void initializeCollections() {
        try {
            // Get existing collection names
            var existingCollections = database.listCollectionNames().into(new java.util.ArrayList<>());
            
            // Create collections if they don't exist
            if (!existingCollections.contains(USERS_COLLECTION)) {
                database.createCollection(USERS_COLLECTION);
                System.out.println("✓ Created " + USERS_COLLECTION + " collection");
            }
            
            if (!existingCollections.contains(PRODUCTS_COLLECTION)) {
                database.createCollection(PRODUCTS_COLLECTION);
                System.out.println("✓ Created " + PRODUCTS_COLLECTION + " collection");
            }
            
            if (!existingCollections.contains(ORDERS_COLLECTION)) {
                database.createCollection(ORDERS_COLLECTION);
                System.out.println("✓ Created " + ORDERS_COLLECTION + " collection");
            }
            
            if (!existingCollections.contains(ORDER_ITEMS_COLLECTION)) {
                database.createCollection(ORDER_ITEMS_COLLECTION);
                System.out.println("✓ Created " + ORDER_ITEMS_COLLECTION + " collection");
            }
            
            if (!existingCollections.contains(CATEGORIES_COLLECTION)) {
                database.createCollection(CATEGORIES_COLLECTION);
                System.out.println("✓ Created " + CATEGORIES_COLLECTION + " collection");
            }
            
            if (!existingCollections.contains(REVIEWS_COLLECTION)) {
                database.createCollection(REVIEWS_COLLECTION);
                System.out.println("✓ Created " + REVIEWS_COLLECTION + " collection");
            }
            
            if (!existingCollections.contains(INVENTORY_COLLECTION)) {
                database.createCollection(INVENTORY_COLLECTION);
                System.out.println("✓ Created " + INVENTORY_COLLECTION + " collection");
            }
            
        } catch (Exception e) {
            System.err.println("✗ Error initializing collections: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
