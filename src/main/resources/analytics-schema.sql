CREATE TABLE IF NOT EXISTS report_order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id UUID NOT NULL,
    location_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(255),
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL
    );

CREATE INDEX IF NOT EXISTS idx_roi_location ON report_order_items(location_id);
CREATE INDEX IF NOT EXISTS idx_roi_created ON report_order_items(created_at);
CREATE INDEX IF NOT EXISTS idx_roi_product ON report_order_items(product_id);
CREATE INDEX IF NOT EXISTS idx_roi_location_date ON report_order_items(location_id, created_at);
CREATE INDEX IF NOT EXISTS idx_roi_date_location ON report_order_items(created_at, location_id);

CREATE TABLE IF NOT EXISTS report_jobs (
    id UUID PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    location_id BIGINT,
    from_time TIMESTAMP NOT NULL,
    to_time TIMESTAMP NOT NULL,
    file_path TEXT,
    file_content TEXT,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMP
);