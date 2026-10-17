INSERT INTO translations (locale, key, text) VALUES
    ('es-CO', 'inbox.empty', 'No tienes alertas.'),
    ('en', 'inbox.empty', 'You have no alerts.'),
    ('es-CO', 'inbox.mark-all', 'Marcar todas como leídas'),
    ('en', 'inbox.mark-all', 'Mark all as read'),
    ('es-CO', 'inbox.mark-read', 'Marcar como leída'),
    ('en', 'inbox.mark-read', 'Mark as read'),
    ('es-CO', 'inbox.title', 'Alertas'),
    ('en', 'inbox.title', 'Alerts')
ON CONFLICT (locale, key) DO UPDATE SET text = EXCLUDED.text, updated_at = now();
