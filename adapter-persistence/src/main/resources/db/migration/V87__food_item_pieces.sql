-- What one unit of a stocked food weighs when it was added by units (4 cans), so the fridge draws exactly that many.
CREATE TABLE food_item_pieces (
    food_item_id UUID PRIMARY KEY REFERENCES food_items (id) ON DELETE CASCADE,
    piece_grams NUMERIC(10, 2) NOT NULL CHECK (piece_grams > 0)
);

INSERT INTO translations (locale, key, text) VALUES
    ('es-CO', 'fridge.units', '{count} u.'),
    ('en', 'fridge.units', '{count} pcs')
ON CONFLICT (locale, key) DO UPDATE SET text = EXCLUDED.text, updated_at = now();
