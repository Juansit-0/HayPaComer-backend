-- Adding countable food by units: 4 cans instead of typing their grams.
INSERT INTO translations (locale, key, text) VALUES
    ('es-CO', 'add.unit.u', 'Unidades'),
    ('en', 'add.unit.u', 'Units'),
    ('es-CO', 'add.unit.u-short', 'u'),
    ('en', 'add.unit.u-short', 'pcs'),
    ('es-CO', 'add.piece-grams', 'Peso de cada una (opcional si ya aparece)'),
    ('en', 'add.piece-grams', 'Weight of each one (optional when filled in)'),
    ('es-CO', 'add.piece-needed', 'Escribe cuánto pesa cada unidad'),
    ('en', 'add.piece-needed', 'Type how much each unit weighs')
ON CONFLICT (locale, key) DO UPDATE SET text = EXCLUDED.text, updated_at = now();
