-- Insert test users for authentication
INSERT INTO Users (name, email, password, phone, address, created_at) VALUES
('Admin User', 'admin@test.com', 'admin123', '+1234567890', '123 Admin Street', NOW()),
('John Doe', 'john@test.com', 'john123', '+1987654321', '456 Main Street', NOW()),
('Jane Smith', 'jane@test.com', 'jane123', '+1555555555', '789 Oak Avenue', NOW()),
('Test User', 'test@test.com', 'test123', '+1666666666', '321 Pine Road', NOW());

-- Verify insertion
SELECT * FROM Users;
