# Ecommerce JavaFX Application

This project is a modern e-commerce desktop application built with Java, JavaFX, and MVC architecture. It features a multi-role system (admin and client), product browsing, cart management, order processing, and more. The application uses a service-DAO pattern and supports both SQL and MongoDB for data persistence.

## Features

- **User Roles:** Admin and Client interfaces
- **Product Catalog:** Browse, search, and view product details
- **Shopping Cart:** Add, update, and remove items
- **Checkout:** Place orders with address and contact info
- **Order History:** View past orders
- **Admin Dashboard:** Manage products, inventory, and view reports
- **Authentication:** Login and session management
- **Data Persistence:** Supports SQL (default) and MongoDB (optional)
- **MVC Pattern:** Clean separation of concerns (Controllers, Services, DAOs, Models)
- **JavaFX UI:** Modern, responsive interface with FXML views and CSS styling

## Project Structure

```
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/
│   │   │   ├── Main.java
│   │   │   ├── config/           # Database connection classes
│   │   │   ├── controllers/      # JavaFX controllers (UI logic)
│   │   │   ├── dao/              # Data access objects
│   │   │   ├── models/           # Data models (Product, User, Order, etc.)
│   │   │   ├── service/          # Business logic
│   │   │   ├── ui/               # JavaFX application entry
│   │   │   └── util/             # Utilities (e.g., DB initializer)
│   │   └── resources/
│   │       ├── design-application/
│   │       │   ├── styles/       # CSS files
│   │       │   └── views/        # FXML UI layouts
│   │       └── sql/              # SQL scripts
│   └── test/
└── target/
```

## Getting Started

### Prerequisites
- Java 11 or higher
- Maven
- (Optional) MongoDB for NoSQL support

### Setup
1. **Clone the repository:**
   ```sh
   git clone <repo-url>
   cd week4-Ecommerce
   ```
2. **Configure Database:**
   - By default, uses SQL (see `src/main/resources/sql/schema.sql`).
   - To use MongoDB, configure `MongoDBConnection.java` and update DAOs as needed.
3. **Build the project:**
   ```sh
   mvn clean install
   ```
4. **Run the application:**
   ```sh
   mvn javafx:run
   ```

### Database Initialization
- SQL scripts for schema and test data are in `src/main/resources/sql/`.
- Run `schema.sql` and insert scripts before first launch.

## Usage
- **Login:** Use test users from `insert_test_users.sql` or register a new account.
- **Admin:** Access dashboard for product and order management.
- **Client:** Browse products, add to cart, and checkout.

## Customization
- **UI:** Modify FXML files in `resources/design-application/views/` and CSS in `styles/`.
- **Business Logic:** Update services in `service/` and DAOs in `dao/`.

## Contributing
Pull requests are welcome! For major changes, please open an issue first to discuss what you would like to change.

## License
[MIT](LICENSE)

## Authors
- Emmanuel Peprah Mensah
- [Your Name Here]

---
*This project was developed as part of a week 4 assignment for an e-commerce system using JavaFX.*
