-- PRODUCTION-READY INDEXES FOR E-COMMERCE PERFORMANCE

-- Primary covering index for getAllProducts
CREATE INDEX idx_products_covering_main ON Products(
    product_id, name, description, price, category_id, created_at
);

-- Optimized index for ORDER BY product_id (ascending for better performance)
CREATE INDEX idx_products_id_asc ON Products(product_id ASC);

-- Search optimization - prefix matching
CREATE INDEX idx_products_name_prefix ON Products(name, category_id, price);

-- Category join optimization
CREATE INDEX idx_categories_lookup ON Categories(category_id, category_name);

-- Inventory join optimization  
CREATE INDEX idx_inventory_lookup ON Inventory(product_id, quantity_available);

-- Composite index for filtered searches
CREATE INDEX idx_products_search_composite ON Products(category_id, name, price);

-- Foreign key optimization
CREATE INDEX idx_products_category_fk ON Products(category_id);

-- Update statistics for query planner
ANALYZE TABLE Products;
ANALYZE TABLE Categories; 
ANALYZE TABLE Inventory;