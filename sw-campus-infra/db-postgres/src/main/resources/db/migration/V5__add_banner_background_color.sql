-- V5: Add background_color column to banners table

ALTER TABLE banners
ADD COLUMN background_color VARCHAR(7);

COMMENT ON COLUMN banners.background_color IS 'HEX color code (e.g., #FF5733) for banner background';
