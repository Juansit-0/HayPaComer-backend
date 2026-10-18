INSERT INTO translations (locale, key, text) VALUES
    ('es-CO', 'hold.hint', 'Mantén presionado para confirmar'),
    ('en', 'hold.hint', 'Press and hold to confirm')
ON CONFLICT (locale, key) DO UPDATE SET text = EXCLUDED.text, updated_at = now();
