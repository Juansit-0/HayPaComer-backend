-- Texts for entering an amount in millilitres or litres when adding food.
INSERT INTO translations (locale, key, text) VALUES
    ('es-CO', 'add.unit', 'Unidad'),
    ('en', 'add.unit', 'Unit'),
    ('es-CO', 'add.unit.g', 'g'),
    ('en', 'add.unit.g', 'g'),
    ('es-CO', 'add.unit.ml', 'ml'),
    ('en', 'add.unit.ml', 'ml'),
    ('es-CO', 'add.unit.l', 'L'),
    ('en', 'add.unit.l', 'L'),
    ('es-CO', 'add.amount', 'Cantidad'),
    ('en', 'add.amount', 'Amount'),
    ('es-CO', 'add.volume-equals', '≈ {grams}'),
    ('en', 'add.volume-equals', '≈ {grams}'),
    ('es-CO', 'add.volume-ai', 'Estimado por el Chef según el alimento'),
    ('en', 'add.volume-ai', 'Estimated by the Chef from the food'),
    ('es-CO', 'add.volume-default', 'Estimado como agua'),
    ('en', 'add.volume-default', 'Estimated as water'),
    ('es-CO', 'add.volume-wait', 'Calculando el peso…'),
    ('en', 'add.volume-wait', 'Working out the weight…')
ON CONFLICT (locale, key) DO UPDATE SET text = EXCLUDED.text, updated_at = now();
