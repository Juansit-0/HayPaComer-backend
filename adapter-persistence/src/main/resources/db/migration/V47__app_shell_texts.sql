INSERT INTO translations (locale, key, text) VALUES
    ('es-CO', 'nav.account', 'Tu cuenta'),
    ('en', 'nav.account', 'Your account')
ON CONFLICT (locale, key) DO UPDATE SET text = EXCLUDED.text, updated_at = now();
