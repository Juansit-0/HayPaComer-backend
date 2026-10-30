INSERT INTO translations (locale, key, text) VALUES
    ('es-CO', 'net.slow', 'Esperando al servidor…'),
    ('en', 'net.slow', 'Waiting for the server…'),
    ('es-CO', 'net.waking', 'El servidor se está despertando. Puede tardar hasta un minuto.'),
    ('en', 'net.waking', 'The server is waking up. It can take up to a minute.'),
    ('es-CO', 'net.failed', 'No pudimos conectar con el servidor. Revisa tu conexión e inténtalo de nuevo.'),
    ('en', 'net.failed', 'We could not reach the server. Check your connection and try again.'),
    ('es-CO', 'net.retry', 'Reintentar'),
    ('en', 'net.retry', 'Try again')
ON CONFLICT (locale, key) DO UPDATE SET text = EXCLUDED.text, updated_at = now();
