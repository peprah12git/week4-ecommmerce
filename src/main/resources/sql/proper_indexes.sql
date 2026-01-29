-- PROPER E-COMMERCE DATABASE OPTIMIZATION
-- Run these indexes BEFORE benchmarking

-- ============ PRIMARY INDEXES ============

-- Products table optimization
CREATE INDEX IF NOT EXISTS idx_products_name_text ON Products(name);
CREATE INDEX IF NOT EXISTS idx_products_description_text ON Products(description);
CREATE INDEX IF NOT EXISTS idx_products_category_id ON Products(category_id);
CREATE INDEX IF NOT EXISTS idx_products_price ON Products(price);
CREATE INDEX IF NOT EXISTS idx_products_created_at ON Products(created_at);

-- Composite index for common product queries
CREATE INDEX IF NOT EXISTS idx_products_category_price ON Products(category_id, price);
CREATE INDEX IF NOT EXISTS idx_products_name_category ON Products(name, category_id);

-- ============ JOIN OPTIMIZATION ============

-- Optimize LEFT JOIN with Categories
CREATE INDEX IF NOT EXISTS idx_categories_id_name ON Categories(category_id, category_name);

-- Optimize LEFT JOIN with Inventory
CREATE INDEX IF NOT EXISTS idx_inventory_product_qty ON Inventory(product_id, quantity_available);

-- ============ SEARCH OPTIMIZATION ============

-- Full-text search indexes (MySQL specific)
-- ALTER TABLE Products ADD FULLTEXT(name, description);

-- Case-insensitive search optimization (PostgreSQL specific)
-- CREATE INDEX IF NOT EXISTS idx_products_name_lower ON Products(LOWER(name));
-- CREATE INDEX IF NOT EXISTS idx_products_desc_lower ON Products(LOWER(description));

-- ============ ORDER BY OPTIMIZATION ============

-- Optimize ORDER BY product_id
CREATE INDEX IF NOT EXISTS idx_products_id_desc ON Products(product_id DESC);

-- Optimize ORDER BY name for search results
CREATE INDEX IF NOT EXISTS idx_products_name_asc ON Products(name ASC);

-- ============ COVERING INDEXES ============

-- Covering index for main product query (includes all needed columns)
CREATE INDEX IF NOT EXISTS idx_products_covering ON Products(
    product_id, name, description, price, category_id, created_at
);

-- ============ QUERY-SPECIFIC OPTIMIZATIONS ============

-- For getAllProducts query
CREATE INDEX IF NOT EXISTS idx_products_list_optimized ON Products(
    product_id, name, description, price, category_id, created_at
) INCLUDE (product_id);

-- For search queries with LIKE
CREATE INDEX IF NOT EXISTS idx_products_search_optimized ON Products(
    name, description, category_id, price
);

-- ============ MAINTENANCE ============

-- Update table statistics for query planner
ANALYZE TABLE Products;
ANALYZE TABLE Categories;
ANALYZE TABLE Inventory;

-- Check index usage (MySQL)
-- SHOW INDEX FROM Products;

-- Check query execution plans
-- EXPLAIN SELECT p.*, c.category_name, COALESCE(i.quantity_available, 0) as quantity 
-- FROM Products p 
-- LEFT JOIN Categories c ON p.category_id = c.category_id 
-- LEFT JOIN Inventory i ON p.product_id = i.product_id 
-- ORDER BY p.product_id;