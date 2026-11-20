-- Drinks named by what they come in, so a can of beer and a bottle of beer are two foods the fridge draws differently: a can and a bottle.
INSERT INTO food_catalog (id, name, name_key, category, default_unit, grams_per_ml, grams_per_piece, perishable, shelf_days,
                          fridge_days, door_days, freezer_days, opened_days, reference_price_cop_per_kg)
VALUES
    (gen_random_uuid(), 'Beer can', 'beer can', 'BEVERAGE', 'PIECE', 1.01, 360, FALSE, 180, 180, 180, 0, 1, 9700),
    (gen_random_uuid(), 'Beer bottle', 'beer bottle', 'BEVERAGE', 'PIECE', 1.01, 333, FALSE, 180, 180, 180, 0, 1, 9000),
    (gen_random_uuid(), 'Soda can', 'soda can', 'BEVERAGE', 'PIECE', 1.04, 370, FALSE, 270, 270, 270, 0, 2, 8100),
    (gen_random_uuid(), 'Soda bottle', 'soda bottle', 'BEVERAGE', 'MILLILITER', 1.04, NULL, FALSE, 180, 180, 180, 0, 5, 3200),
    (gen_random_uuid(), 'Water bottle', 'water bottle', 'BEVERAGE', 'MILLILITER', 1.00, NULL, FALSE, 365, 365, 365, 0, 7, 3300),
    (gen_random_uuid(), 'Wine bottle', 'wine bottle', 'BEVERAGE', 'PIECE', 0.99, 742, FALSE, 730, 730, 730, 0, 5, 54000)
ON CONFLICT (name_key) DO NOTHING;

INSERT INTO food_allergens (food_id, allergen_id)
SELECT f.id, a.id FROM food_catalog f JOIN allergens a ON a.code = 'GLUTEN' AND f.name_key IN ('beer can', 'beer bottle')
ON CONFLICT DO NOTHING;

INSERT INTO food_allergens (food_id, allergen_id)
SELECT f.id, a.id FROM food_catalog f JOIN allergens a ON a.code = 'SULPHITES' AND f.name_key = 'wine bottle'
ON CONFLICT DO NOTHING;

INSERT INTO translations (locale, key, text) VALUES
    ('es-CO', 'food.beer can', 'Cerveza en lata'),
    ('en', 'food.beer can', 'Beer can'),
    ('es-CO', 'food.beer bottle', 'Cerveza en botella'),
    ('en', 'food.beer bottle', 'Beer bottle'),
    ('es-CO', 'food.soda can', 'Gaseosa en lata'),
    ('en', 'food.soda can', 'Soda can'),
    ('es-CO', 'food.soda bottle', 'Gaseosa en botella'),
    ('en', 'food.soda bottle', 'Soda bottle'),
    ('es-CO', 'food.water bottle', 'Agua en botella'),
    ('en', 'food.water bottle', 'Water bottle'),
    ('es-CO', 'food.wine bottle', 'Botella de vino'),
    ('en', 'food.wine bottle', 'Wine bottle'),
    ('es-CO', 'food-gender.beer can', 'f'),
    ('en', 'food-gender.beer can', 'm'),
    ('es-CO', 'food-gender.beer bottle', 'f'),
    ('en', 'food-gender.beer bottle', 'm'),
    ('es-CO', 'food-gender.soda can', 'f'),
    ('en', 'food-gender.soda can', 'm'),
    ('es-CO', 'food-gender.soda bottle', 'f'),
    ('en', 'food-gender.soda bottle', 'm'),
    ('es-CO', 'food-gender.water bottle', 'm'),
    ('en', 'food-gender.water bottle', 'm'),
    ('es-CO', 'food-gender.wine bottle', 'f'),
    ('en', 'food-gender.wine bottle', 'm')
ON CONFLICT (locale, key) DO UPDATE SET text = EXCLUDED.text, updated_at = now();
