INSERT INTO food_catalog (id, name, name_key, category, default_unit, grams_per_ml, grams_per_piece, perishable, shelf_days,
                          fridge_days, door_days, freezer_days, opened_days, reference_price_cop_per_kg)
VALUES
    (gen_random_uuid(), 'Creole potato', 'creole potato', 'VEGETABLE', 'PIECE', NULL, 40, TRUE, 20, 20, 14, 300, 5, 4500),
    (gen_random_uuid(), 'Green onion', 'green onion', 'VEGETABLE', 'GRAM', NULL, NULL, TRUE, 10, 10, 7, 180, 5, 4000),
    (gen_random_uuid(), 'Cilantro', 'cilantro', 'VEGETABLE', 'GRAM', NULL, NULL, TRUE, 7, 7, 5, 120, 3, 12000),
    (gen_random_uuid(), 'Lime', 'lime', 'FRUIT', 'PIECE', NULL, 60, TRUE, 21, 21, 14, 120, 3, 4000),
    (gen_random_uuid(), 'Orange', 'orange', 'FRUIT', 'PIECE', NULL, 200, TRUE, 21, 21, 14, 180, 3, 3500),
    (gen_random_uuid(), 'Double cream cheese', 'double cream cheese', 'DAIRY', 'GRAM', NULL, NULL, TRUE, 21, 21, 14, 90, 7, 30000),
    (gen_random_uuid(), 'Beef steak', 'beef steak', 'MEAT', 'GRAM', NULL, NULL, TRUE, 3, 3, 2, 180, 2, 34000),
    (gen_random_uuid(), 'Chicken thighs', 'chicken thighs', 'POULTRY', 'GRAM', NULL, NULL, TRUE, 2, 2, 1, 270, 2, 15000),
    (gen_random_uuid(), 'Blackberries', 'blackberries', 'FRUIT', 'GRAM', NULL, NULL, TRUE, 4, 4, 3, 240, 2, 9000),
    (gen_random_uuid(), 'Mango', 'mango', 'FRUIT', 'PIECE', NULL, 300, TRUE, 7, 7, 5, 240, 2, 5000),
    (gen_random_uuid(), 'Green peas', 'green peas', 'VEGETABLE', 'GRAM', NULL, NULL, TRUE, 5, 5, 4, 240, 2, 9000),
    (gen_random_uuid(), 'Corn', 'corn', 'VEGETABLE', 'PIECE', NULL, 250, TRUE, 4, 4, 3, 240, 2, 3000),
    (gen_random_uuid(), 'Tree tomato', 'tree tomato', 'FRUIT', 'PIECE', NULL, 90, TRUE, 14, 14, 10, 240, 3, 4500),
    (gen_random_uuid(), 'Oca', 'oca', 'VEGETABLE', 'PIECE', NULL, 40, TRUE, 21, 21, 14, 240, 4, 5000),
    (gen_random_uuid(), 'Lulo', 'lulo', 'FRUIT', 'PIECE', NULL, 100, TRUE, 10, 10, 7, 240, 2, 6000)
ON CONFLICT (name_key) DO NOTHING;

INSERT INTO food_allergens (food_id, allergen_id)
SELECT f.id, a.id FROM food_catalog f JOIN allergens a ON f.name_key = 'double cream cheese' AND a.code = 'MILK'
ON CONFLICT DO NOTHING;

INSERT INTO translations (locale, key, text) VALUES
    ('es-CO', 'food.creole potato', 'Papa criolla'),
    ('en', 'food.creole potato', 'Creole potato'),
    ('es-CO', 'food.green onion', 'Cebolla larga'),
    ('en', 'food.green onion', 'Green onion'),
    ('es-CO', 'food.cilantro', 'Cilantro'),
    ('en', 'food.cilantro', 'Cilantro'),
    ('es-CO', 'food.lime', 'Limón'),
    ('en', 'food.lime', 'Lime'),
    ('es-CO', 'food.orange', 'Naranja'),
    ('en', 'food.orange', 'Orange'),
    ('es-CO', 'food.double cream cheese', 'Queso doble crema'),
    ('en', 'food.double cream cheese', 'Double cream cheese'),
    ('es-CO', 'food.beef steak', 'Carne de res'),
    ('en', 'food.beef steak', 'Beef steak'),
    ('es-CO', 'food.chicken thighs', 'Pernil de pollo'),
    ('en', 'food.chicken thighs', 'Chicken thighs'),
    ('es-CO', 'food.blackberries', 'Mora'),
    ('en', 'food.blackberries', 'Blackberries'),
    ('es-CO', 'food.mango', 'Mango'),
    ('en', 'food.mango', 'Mango'),
    ('es-CO', 'food.green peas', 'Arveja'),
    ('en', 'food.green peas', 'Green peas'),
    ('es-CO', 'food.corn', 'Mazorca'),
    ('en', 'food.corn', 'Corn'),
    ('es-CO', 'food.tree tomato', 'Tomate de árbol'),
    ('en', 'food.tree tomato', 'Tree tomato'),
    ('es-CO', 'food.oca', 'Oca'),
    ('en', 'food.oca', 'Oca'),
    ('es-CO', 'food.lulo', 'Lulo'),
    ('en', 'food.lulo', 'Lulo')
ON CONFLICT (locale, key) DO UPDATE SET text = EXCLUDED.text, updated_at = now();
