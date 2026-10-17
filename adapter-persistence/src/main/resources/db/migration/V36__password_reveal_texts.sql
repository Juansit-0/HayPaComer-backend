INSERT INTO translations (locale, key, text) VALUES
    ('es-CO', 'auth.hide-password', 'Ocultar'),
    ('en', 'auth.hide-password', 'Hide'),
    ('es-CO', 'auth.show-password', 'Mostrar'),
    ('en', 'auth.show-password', 'Show')
ON CONFLICT (locale, key) DO UPDATE SET text = EXCLUDED.text, updated_at = now();
