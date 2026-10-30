INSERT INTO food_catalog (id, name, name_key, category, default_unit, grams_per_ml, grams_per_piece, perishable, shelf_days,
                          fridge_days, door_days, freezer_days, opened_days, reference_price_cop_per_kg)
VALUES
    (gen_random_uuid(), 'Arracacha', 'arracacha', 'VEGETABLE', 'GRAM', NULL, NULL, TRUE, 10, 10, 7, 240, 3, 5000),
    (gen_random_uuid(), 'Ulluco', 'ulluco', 'VEGETABLE', 'GRAM', NULL, NULL, TRUE, 14, 14, 10, 240, 3, 6000),
    (gen_random_uuid(), 'Yuca', 'yuca', 'VEGETABLE', 'PIECE', NULL, 500, TRUE, 7, 7, 5, 300, 2, 2500),
    (gen_random_uuid(), 'Garlic', 'garlic', 'VEGETABLE', 'PIECE', NULL, 40, TRUE, 60, 60, 45, 300, 14, 16000),
    (gen_random_uuid(), 'Pepper', 'pepper', 'VEGETABLE', 'PIECE', NULL, 150, TRUE, 10, 10, 7, 240, 4, 7000),
    (gen_random_uuid(), 'Squash', 'squash', 'VEGETABLE', 'GRAM', NULL, NULL, TRUE, 7, 7, 5, 240, 4, 3000),
    (gen_random_uuid(), 'Papaya', 'papaya', 'FRUIT', 'GRAM', NULL, NULL, TRUE, 5, 5, 4, 240, 2, 3500),
    (gen_random_uuid(), 'Granadilla', 'granadilla', 'FRUIT', 'PIECE', NULL, 90, TRUE, 14, 14, 10, 180, 2, 8000),
    (gen_random_uuid(), 'Kumis', 'kumis', 'DAIRY', 'MILLILITER', 1.04, NULL, TRUE, 10, 10, 7, 60, 3, 8000),
    (gen_random_uuid(), 'Chorizo', 'chorizo', 'MEAT', 'PIECE', NULL, 80, TRUE, 7, 7, 5, 120, 3, 22000),
    (gen_random_uuid(), 'Trout', 'trout', 'FISH', 'GRAM', NULL, NULL, TRUE, 2, 2, 1, 180, 1, 26000),
    (gen_random_uuid(), 'Panela', 'panela', 'CONDIMENT', 'PIECE', NULL, 500, FALSE, 180, 180, 180, 365, 90, 6000)
ON CONFLICT (name_key) DO NOTHING;

INSERT INTO food_allergens (food_id, allergen_id)
SELECT f.id, a.id FROM food_catalog f JOIN allergens a ON
    (f.name_key = 'kumis' AND a.code = 'MILK')
    OR (f.name_key = 'trout' AND a.code = 'FISH')
ON CONFLICT DO NOTHING;

INSERT INTO translations (locale, key, text) VALUES
    ('es-CO', 'food.arracacha', 'Arracacha'),
    ('en', 'food.arracacha', 'Arracacha'),
    ('es-CO', 'food.ulluco', 'Ulluco'),
    ('en', 'food.ulluco', 'Ulluco'),
    ('es-CO', 'food.yuca', 'Yuca'),
    ('en', 'food.yuca', 'Yuca'),
    ('es-CO', 'food.garlic', 'Ajo'),
    ('en', 'food.garlic', 'Garlic'),
    ('es-CO', 'food.pepper', 'Pimentón'),
    ('en', 'food.pepper', 'Pepper'),
    ('es-CO', 'food.squash', 'Ahuyama'),
    ('en', 'food.squash', 'Squash'),
    ('es-CO', 'food.papaya', 'Papaya'),
    ('en', 'food.papaya', 'Papaya'),
    ('es-CO', 'food.granadilla', 'Granadilla'),
    ('en', 'food.granadilla', 'Granadilla'),
    ('es-CO', 'food.kumis', 'Kumis'),
    ('en', 'food.kumis', 'Kumis'),
    ('es-CO', 'food.chorizo', 'Chorizo'),
    ('en', 'food.chorizo', 'Chorizo'),
    ('es-CO', 'food.trout', 'Trucha'),
    ('en', 'food.trout', 'Trout'),
    ('es-CO', 'food.panela', 'Panela'),
    ('en', 'food.panela', 'Panela')
ON CONFLICT (locale, key) DO UPDATE SET text = EXCLUDED.text, updated_at = now();
