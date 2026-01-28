-- Insert test categories
INSERT INTO Categories (category_name, description) VALUES
('Electronics', 'Electronic devices and gadgets'),
('Clothing', 'Apparel and fashion items'),
('Books', 'Books and educational materials'),
('Home & Garden', 'Home decor and garden supplies'),
('Sports', 'Sports equipment and accessories');

-- Insert test products
INSERT INTO Products (name, description, price, category_id, stock_quantity, image_url, created_at) VALUES
('Wireless Bluetooth Headphones', 'Premium noise-cancelling headphones with 30-hour battery life', 89.99, 1, 50, 'headphones.jpg', NOW()),
('Smart Watch Pro', 'Fitness tracker with heart rate monitor and GPS', 199.99, 1, 30, 'smartwatch.jpg', NOW()),
('Laptop Stand Aluminum', 'Ergonomic adjustable laptop stand for desk', 45.99, 1, 75, 'laptop-stand.jpg', NOW()),
('USB-C Hub 7-in-1', 'Multi-port adapter with HDMI, USB 3.0, and card readers', 34.99, 1, 100, 'usb-hub.jpg', NOW()),

('Men''s Cotton T-Shirt', 'Comfortable 100% cotton t-shirt, available in multiple colors', 19.99, 2, 200, 'tshirt.jpg', NOW()),
('Women''s Running Shoes', 'Lightweight athletic shoes with cushioned sole', 79.99, 2, 80, 'running-shoes.jpg', NOW()),
('Denim Jeans Classic Fit', 'Premium denim jeans with classic straight fit', 59.99, 2, 120, 'jeans.jpg', NOW()),
('Winter Jacket Waterproof', 'Insulated jacket perfect for cold weather', 149.99, 2, 45, 'jacket.jpg', NOW()),

('The Art of Programming', 'Comprehensive guide to software development best practices', 49.99, 3, 150, 'programming-book.jpg', NOW()),
('Science Fiction Collection', 'Box set of 5 award-winning sci-fi novels', 39.99, 3, 60, 'scifi-books.jpg', NOW()),
('Cooking Masterclass Book', 'Professional cooking techniques and 200+ recipes', 34.99, 3, 90, 'cooking-book.jpg', NOW()),

('LED Desk Lamp Smart', 'Adjustable brightness desk lamp with USB charging', 29.99, 4, 110, 'desk-lamp.jpg', NOW()),
('Indoor Plant Monstera', 'Easy-care tropical plant in decorative pot', 24.99, 4, 40, 'plant.jpg', NOW()),
('Wall Clock Modern Design', 'Silent sweep movement wall clock, 12-inch', 19.99, 4, 85, 'wall-clock.jpg', NOW()),
('Throw Pillows Set of 4', 'Decorative cushion covers with inserts', 44.99, 4, 70, 'pillows.jpg', NOW()),

('Yoga Mat Premium', 'Non-slip exercise mat with carrying strap', 29.99, 5, 100, 'yoga-mat.jpg', NOW()),
('Dumbbell Set Adjustable', 'Space-saving adjustable weights 5-50 lbs', 199.99, 5, 25, 'dumbbells.jpg', NOW()),
('Tennis Racket Professional', 'Lightweight carbon fiber racket', 129.99, 5, 35, 'tennis-racket.jpg', NOW()),
('Cycling Helmet Safety', 'CPSC-certified helmet with LED light', 39.99, 5, 60, 'helmet.jpg', NOW());

-- Insert inventory records for all products
INSERT INTO Inventory (product_id, quantity_available, last_updated)
SELECT product_id, stock_quantity, NOW()
FROM Products;

-- Verify insertion
SELECT p.product_id, p.name, p.price, c.category_name, p.stock_quantity
FROM Products p
JOIN Categories c ON p.category_id = c.category_id
ORDER BY c.category_name, p.name;
