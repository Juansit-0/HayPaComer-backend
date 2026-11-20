-- Requests for food marked "ask first" reach the notifications: the owner when someone asks, the asker when it is answered.
INSERT INTO translations (locale, key, text) VALUES
    ('es-CO', 'Food request', 'Solicitud de comida'),
    ('es-CO', '{} asked to use your {}', '{} pidió permiso para usar tu {}'),
    ('es-CO', 'Answer to your request', 'Respuesta a tu solicitud'),
    ('es-CO', '{} let you use {}', '{} te dejó usar {}'),
    ('es-CO', '{} would rather keep {}', '{} prefirió no compartir {}'),
    ('es-CO', 'inbox.action.answer', 'Responder'),
    ('en', 'inbox.action.answer', 'Answer')
ON CONFLICT (locale, key) DO UPDATE SET text = EXCLUDED.text, updated_at = now();
