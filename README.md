Skip to content
peprah12git
Smart-E-commerce-System
Repository navigation
Code
Issues
Pull requests
Actions
Projects
Wiki
Security
Insights
Settings
Owner avatar
Smart-E-commerce-System
Public
peprah12git/Smart-E-commerce-System
Go to file
t
This branch is 5 commits ahead of and 1 commit behind main.
Name		
peprah12git
peprah12git
Update client and login controllers
6d5f4f4
·
19 hours ago
.idea
Update controllers, services, DAO, and cleanup SQL
19 hours ago
docs
implementing performance and creating admin dashboard
2 days ago
src/main
Update client and login controllers
19 hours ago
.gitignore
Initial project setup with DAO, entities, and UI structure
2 weeks ago
FIXES_APPLIED.md
Update controllers, services, DAO, and cleanup SQL
19 hours ago
PERFORMANCE_BENCHMARK_GUIDE.md
implementing performance and creating admin dashboard
2 days ago
README.md
implementing performance and creating admin dashboard
2 days ago
install-maven.ps1
Initial project setup with DAO, entities, and UI structure
2 weeks ago
pom.xml
Feature: database configuration and schema for MongoDB
last week
run_javafx.bat
Initial project setup with DAO, entities, and UI structure
2 weeks ago
Repository files navigation
README
E-Commerce Desktop Application
Project Overview
A full-featured e-commerce desktop application built with JavaFX, featuring a complete shopping platform with product catalog, shopping cart, order management, and customer reviews.

Technology Stack
Component	Technology
Frontend	JavaFX 21 (FXML + CSS)
Backend	Java 21
Relational Database	MySQL 8.0
NoSQL Database	MongoDB 6.0
Build Tool	Maven 3.9+
Architecture	MVC Pattern
Features
Customer Features
✅ Browse products with search and category filters
✅ View detailed product information with reviews
✅ Shopping cart management (add, update, remove items)
✅ Secure checkout process
✅ Order history viewing
✅ Submit and view product reviews
✅ Guest browsing (no login required)
Admin Features
✅ Product management (CRUD operations)
✅ Category management
✅ Inventory tracking
✅ Order management and status updates
✅ User management
Prerequisites
Before running this application, ensure you have the following installed:

Java Development Kit (JDK) 21+

Download: https://adoptium.net/
Verify: java --version
Apache Maven 3.9+

Download: https://maven.apache.org/download.cgi
Verify: mvn --version
MySQL Server 8.0+

Download: https://dev.mysql.com/downloads/mysql/
Create database: ecommerce_db
MongoDB 6.0+ (Optional - for NoSQL features)

Download: https://www.mongodb.com/try/download/community
Start MongoDB service
Installation & Setup
Step 1: Clone the Repository
cd "c:\Users\EmmanuelPeprahMensah\Desktop\New folder\e-c0mmerce"
Step 2: Configure Database Connection
Edit src/main/java/com/ecommerce/config/DatabaseConnection.java:

private static final String URL = "jdbc:mysql://localhost:3306/ecommerce_db";
private static final String USERNAME = "your_username";
private static final String PASSWORD = "your_password";
Step 3: Create Database Schema
Run the schema script in MySQL:

source src/main/resources/com/ecommerce/schema.sql
Or execute from command line:

mysql -u root -p < src/main/resources/com/ecommerce/schema.sql
Step 4: Install Dependencies
mvn clean install
Step 5: Run the Application
mvn javafx:run
Or use the provided batch file:

run_javafx.bat
Project Structure
src/main/java/com/ecommerce/
├── Main.java                 # Application entry point
├── config/
│   ├── DatabaseConnection.java    # MySQL connection singleton
│   └── MongoDBConnection.java     # MongoDB connection singleton
├── controllers/
│   ├── LoginController.java       # User authentication
│   ├── AdminLoginController.java  # Admin authentication
│   ├── ClientViewController.java  # Main client interface
│   ├── ProductBrowserController.java  # Product catalog
│   ├── CartViewController.java    # Shopping cart
│   ├── CheckoutController.java    # Order checkout
│   └── OrderHistoryController.java # Order history
├── dao/
│   ├── UserDAO.java              # User database operations
│   ├── ProductDAO.java           # Product database operations
│   ├── CategoryDAO.java          # Category database operations
│   ├── OrderDAO.java             # Order database operations
│   ├── OrderItemDAO.java         # Order items operations
│   ├── ReviewDAO.java            # Reviews (MySQL)
│   ├── ReviewMongoDAO.java       # Reviews (MongoDB)
│   ├── InventoryDAO.java         # Inventory operations
│   └── ApplicationLogDAO.java    # Logging (MongoDB)
├── models/
│   ├── User.java
│   ├── Product.java
│   ├── Category.java
│   ├── Order.java
│   ├── OrderItem.java
│   ├── CartItem.java
│   ├── Review.java
│   └── Inventory.java
├── service/
│   ├── UserService.java          # User authentication & management
│   ├── ProductService.java       # Product catalog & search
│   ├── CartService.java          # Shopping cart management
│   ├── OrderService.java         # Order processing & history
│   ├── CategoryService.java      # Category management with caching
│   ├── InventoryService.java     # Stock tracking & updates
│   ├── ReviewService.java        # Product reviews with caching
│   └── PerformanceReportService.java  # Performance benchmarking

└── ui/
└── App.java                  # JavaFX Application class
Database Schema
Entity Relationship Overview
Users - Customer and admin accounts
Products - Product catalog with categories
Categories - Product categorization
Inventory - Stock tracking per product
Orders - Customer orders
OrderItems - Individual items within orders
Reviews - Customer product reviews
Performance Indexes
-- Optimized indexes for common queries
CREATE INDEX idx_users_email ON Users(email);
CREATE INDEX idx_products_category ON Products(category_id);
CREATE INDEX idx_products_name ON Products(name);
CREATE INDEX idx_orders_user ON Orders(user_id);
CREATE INDEX idx_orders_date ON Orders(order_date);
CREATE INDEX idx_reviews_product ON Reviews(product_id);
Usage Guide
Logging In
Launch the application
Click "Continue as Guest" for customer access
Or click "Admin Login" for administrative access
Default admin: admin@admin.com / admin123
Shopping
Browse products on the main catalog page
Use search bar and category filters to find items
Click "Add to Cart" on any product
Navigate to Cart to review and modify items
Proceed to Checkout to complete purchase
Admin Panel
Manage products (add, edit, delete)
Update inventory quantities
View and update order statuses
Manage user accounts
Testing
Test Credentials
Role	Email	Password
Admin	admin@admin.com	admin123
User	john.doe@email.com	hashed_password_123
Sample Data
The application includes pre-populated sample data:

5 categories (Electronics, Clothing, Home & Garden, Sports, Books)
12 sample products
Sample inventory records
Sample orders and reviews
Troubleshooting
Common Issues
1. "Cannot connect to MySQL"

Ensure MySQL service is running
Verify credentials in DatabaseConnection.java
Check if database ecommerce_db exists
2. "MongoDB connection failed"

MongoDB is optional for core functionality
Start MongoDB service if using NoSQL features
Reviews will fall back to MySQL if MongoDB unavailable
3. "JavaFX runtime components are missing"

Use mvn javafx:run instead of java -jar
Ensure JavaFX dependencies in pom.xml
4. Application won't start

Run mvn clean install to rebuild
Check for compilation errors
Performance Benchmarking
The application includes a comprehensive Performance Report Generation System (User Story 4.1) that measures and analyzes query execution times before and after optimization techniques.

Features
✅ 8 comprehensive benchmark tests covering key operations
✅ Measures pre-optimization vs post-optimization performance
✅ Documents improvements from indexes, caching, and query optimization
✅ Generates detailed markdown reports with methodology
✅ Command-line tool for analysts
✅ Optional UI dashboard for performance analysis
Running Performance Benchmarks
Method 1: Command Line (Recommended)
# Compile the project
mvn clean compile

# Run benchmarks
mvn exec:java -Dexec.mainClass="com.ecommerce.util.PerformanceBenchmarkRunner"

# Save to custom file
mvn exec:java -Dexec.mainClass="com.ecommerce.util.PerformanceBenchmarkRunner" -Dexec.args="reports/my_report.md"
Method 2: From UI Dashboard
Launch the application: mvn javafx:run
Navigate to the Performance Report section
Click "Run Benchmarks" button
Save the generated report
What Gets Tested
The benchmark suite measures performance improvements in:

User Authentication: Email index optimization (~96% improvement)
Product Catalog: Caching strategies (~90% improvement)
Category Search: Database indexing (~85% improvement)
Order History: JOIN optimization & multiple indexes (~85% improvement)
Product Reviews: Indexed queries (~80% improvement)
Cart Operations: In-memory processing (~100% improvement)
Category Loading: Caching and indexing
Connection Pooling: Singleton pattern reuse
Benchmark Components
PerformanceReportService.java - Core benchmarking engine
PerformanceBenchmarkRunner.java - Standalone CLI tool
PerformanceReportController.java - JavaFX UI controller
performance-report.fxml - Dashboard interface
Documentation
Architecture Overview
Database Design Document
Performance Report
NoSQL Comparison
Performance Benchmark Implementation
Performance Benchmark Guide
License
This project is for educational purposes.

Contributors
Emmanuel Peprah Mensah
About
No description, website, or topics provided.
Resources
Readme
Activity
Stars
0 stars
Watchers
0 watching
Forks
0 forks
Releases
No releases published
Create a new release
Packages
No packages published
Publish your first package
Languages
Java
92.4%

CSS
6.4%

Other
1.2%
Suggested workflows
Based on your tech stack
Java with Ant logo
Java with Ant
Build and test a Java project with Apache Ant.
Java with Gradle logo
Java with Gradle
Build and test a Java project using a Gradle wrapper script.
Publish Java Package with Gradle logo
Publish Java Package with maven
Build a Java Package using Gradle and publish to GitHub Packages.
More workflows
Footer
© 2026 GitHub, Inc.
Footer navigation
Terms
Privacy
Security
Status
Community
Docs
Contact
