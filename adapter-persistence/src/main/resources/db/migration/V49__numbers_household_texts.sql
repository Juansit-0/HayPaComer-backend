INSERT INTO translations (locale, key, text) VALUES
    ('es-CO', 'numbers.ranking', 'Lo que salvó la casa'),
    ('en', 'numbers.ranking', 'What the household saved'),
    ('es-CO', 'numbers.together', 'Entre {n} personas rescataron {grams} de comida que iba a vencer.'),
    ('en', 'numbers.together', 'Together, {n} people rescued {grams} of food that was about to go off.')
ON CONFLICT (locale, key) DO UPDATE SET text = EXCLUDED.text, updated_at = now();
