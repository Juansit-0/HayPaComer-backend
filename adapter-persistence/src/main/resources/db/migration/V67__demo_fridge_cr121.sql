-- The demo fridge is now the Challenger CR 121.
INSERT INTO translations (locale, key, text) VALUES
    ('es-CO', 'fridge-example.challenger-cr121', 'Minibar Challenger CR 121'),
    ('en', 'fridge-example.challenger-cr121', 'Challenger CR 121 minibar')
ON CONFLICT (locale, key) DO UPDATE SET text = EXCLUDED.text, updated_at = now();

DELETE FROM translations WHERE key = 'fridge-example.challenger-cr050';
