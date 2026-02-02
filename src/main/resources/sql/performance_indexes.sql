-- Performance Optimization Indexes
-- Run these to optimize query performance

-- Index for product searches by name and description
CREATE INDEX IF NOT EXISTS idx_products_name ON products(name);
CREATE INDEX IF NOT EXISTS idx_products_category ON products(category);
CREATE INDEX IF NOT EXISTS idx_products_name_desc ON products(name, description);

-- Index for order queries
CREATE INDEX IF NOT EXISTS idx_orders_user_id ON orders(user_id);
CREATE INDEX IF NOT EXISTS idx_orders_date ON orders(order_date);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);

-- Index for order items
CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON order_items(order_id);
CREATE INDEX IF NOT EXISTS idx_order_items_product_id ON order_items(product_id);

-- Index for user authentication
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);

-- Composite indexes for common queries
CREATE INDEX IF NOT EXISTS idx_products_category_stock ON products(category, stock_quantity);
CREATE INDEX IF NOT EXISTS idx_orders_user_date ON orders(user_id, order_date);

-- Full-text search index for product search (if supported)
-- CREATE FULLTEXT INDEX idx_products_search ON products(name, description);