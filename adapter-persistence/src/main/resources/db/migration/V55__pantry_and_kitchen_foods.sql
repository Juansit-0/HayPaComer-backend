INSERT INTO food_catalog (id, name, name_key, category, default_unit, grams_per_ml, grams_per_piece, perishable, shelf_days,
                          fridge_days, door_days, freezer_days, opened_days, reference_price_cop_per_kg)
VALUES
    (gen_random_uuid(), 'Red beans', 'red beans', 'LEGUME', 'GRAM', NULL, NULL, FALSE, 365, 365, 365, 365, 365, 9000),
    (gen_random_uuid(), 'Mote', 'mote', 'GRAIN', 'GRAM', NULL, NULL, FALSE, 365, 365, 365, 365, 180, 6000),
    (gen_random_uuid(), 'Oats', 'oats', 'GRAIN', 'GRAM', NULL, NULL, FALSE, 365, 365, 365, 365, 120, 9000),
    (gen_random_uuid(), 'Oil', 'oil', 'CONDIMENT', 'MILLILITER', 0.92, NULL, FALSE, 365, 365, 365, 365, 180, 9000),
    (gen_random_uuid(), 'Coffee', 'coffee', 'BEVERAGE', 'GRAM', NULL, NULL, FALSE, 365, 365, 365, 365, 30, 30000),
    (gen_random_uuid(), 'Aji', 'aji', 'CONDIMENT', 'GRAM', NULL, NULL, TRUE, 14, 14, 14, 90, 7, 15000),
    (gen_random_uuid(), 'Cabbage', 'cabbage', 'VEGETABLE', 'GRAM', NULL, NULL, TRUE, 21, 21, 14, 240, 5, 2500),
    (gen_random_uuid(), 'Lettuce', 'lettuce', 'VEGETABLE', 'GRAM', NULL, NULL, TRUE, 7, 7, 5, 0, 3, 5000),
    (gen_random_uuid(), 'Apple', 'apple', 'FRUIT', 'PIECE', NULL, 180, TRUE, 30, 30, 21, 240, 1, 9000),
    (gen_random_uuid(), 'Pineapple', 'pineapple', 'FRUIT', 'GRAM', NULL, NULL, TRUE, 5, 5, 4, 180, 3, 3000),
    (gen_random_uuid(), 'Pork chop', 'pork chop', 'MEAT', 'GRAM', NULL, NULL, TRUE, 3, 3, 2, 180, 2, 24000),
    (gen_random_uuid(), 'Cuy', 'cuy', 'PREPARED', 'GRAM', NULL, NULL, TRUE, 3, 3, 2, 90, 2, 60000),
    (gen_random_uuid(), 'Leftovers', 'leftovers', 'PREPARED', 'GRAM', NULL, NULL, TRUE, 3, 3, 3, 60, 3, 12000)
ON CONFLICT (name_key) DO NOTHING;

INSERT INTO food_allergens (food_id, allergen_id)
SELECT f.id, a.id FROM food_catalog f JOIN allergens a ON f.name_key = 'oats' AND a.code = 'GLUTEN'
ON CONFLICT DO NOTHING;

INSERT INTO translations (locale, key, text) VALUES
    ('es-CO', 'food.red beans', 'Fríjol rojo'),
    ('en', 'food.red beans', 'Red beans'),
    ('es-CO', 'food.mote', 'Mote'),
    ('en', 'food.mote', 'Mote'),
    ('es-CO', 'food.oats', 'Avena'),
    ('en', 'food.oats', 'Oats'),
    ('es-CO', 'food.oil', 'Aceite'),
    ('en', 'food.oil', 'Oil'),
    ('es-CO', 'food.coffee', 'Café'),
    ('en', 'food.coffee', 'Coffee'),
    ('es-CO', 'food.aji', 'Ají'),
    ('en', 'food.aji', 'Aji'),
    ('es-CO', 'food.cabbage', 'Repollo'),
    ('en', 'food.cabbage', 'Cabbage'),
    ('es-CO', 'food.lettuce', 'Lechuga'),
    ('en', 'food.lettuce', 'Lettuce'),
    ('es-CO', 'food.apple', 'Manzana'),
    ('en', 'food.apple', 'Apple'),
    ('es-CO', 'food.pineapple', 'Piña'),
    ('en', 'food.pineapple', 'Pineapple'),
    ('es-CO', 'food.pork chop', 'Chuleta de cerdo'),
    ('en', 'food.pork chop', 'Pork chop'),
    ('es-CO', 'food.cuy', 'Cuy'),
    ('en', 'food.cuy', 'Cuy'),
    ('es-CO', 'food.leftovers', 'Sobras'),
    ('en', 'food.leftovers', 'Leftovers')
ON CONFLICT (locale, key) DO UPDATE SET text = EXCLUDED.text, updated_at = now();
