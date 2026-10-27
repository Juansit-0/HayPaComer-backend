INSERT INTO translations (locale, key, text) VALUES
    ('es-CO', 'inbox.action.fridge', 'Ver la nevera'),
    ('en', 'inbox.action.fridge', 'See the fridge'),
    ('es-CO', 'inbox.action.check', 'Revisar la comida'),
    ('en', 'inbox.action.check', 'Check the food'),
    ('es-CO', 'inbox.action.cook', 'Qué cocinar ahora'),
    ('en', 'inbox.action.cook', 'What to cook now'),
    ('es-CO', 'inbox.failed', 'No pudimos traer tus alertas.'),
    ('en', 'inbox.failed', 'We could not load your alerts.'),
    ('es-CO', 'settings.saved', 'Guardado'),
    ('en', 'settings.saved', 'Saved')
ON CONFLICT (locale, key) DO UPDATE SET text = EXCLUDED.text, updated_at = now();
