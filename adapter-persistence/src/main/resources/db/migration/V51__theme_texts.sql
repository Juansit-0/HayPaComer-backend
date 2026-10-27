INSERT INTO translations (locale, key, text) VALUES
    ('es-CO', 'nav.dark-mode', 'Modo oscuro'),
    ('en', 'nav.dark-mode', 'Dark mode')
ON CONFLICT (locale, key) DO UPDATE SET text = EXCLUDED.text, updated_at = now();
