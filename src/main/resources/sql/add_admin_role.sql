-- Add role column to Users table if it doesn't exist
ALTER TABLE Users ADD COLUMN IF NOT EXISTS role VARCHAR(20) DEFAULT 'user';

-- Update existing users to have 'user' role
UPDATE Users SET role = 'user' WHERE role IS NULL;

-- Insert default admin user
INSERT INTO Users (name, email, password, phone, address, role) 
VALUES ('Admin User', 'admin@ecommerce.com', 'admin123', '555-0000', 'Admin Office', 'admin')
ON DUPLICATE KEY UPDATE role = 'admin';

-- You can also make an existing user an admin
-- UPDATE Users SET role = 'admin' WHERE email = 'your-email@example.com';
