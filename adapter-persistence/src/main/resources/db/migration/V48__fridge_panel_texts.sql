INSERT INTO translations (locale, key, text) VALUES
    ('es-CO', 'action.back', 'Atrás'),
    ('en', 'action.back', 'Back'),
    ('es-CO', 'fridge.toast-discarded-value', 'Botaste {food}: {money} perdidos'),
    ('en', 'fridge.toast-discarded-value', 'You threw away {food}: {money} lost')
ON CONFLICT (locale, key) DO UPDATE SET text = EXCLUDED.text, updated_at = now();
