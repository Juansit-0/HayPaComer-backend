-- Texts for a photo that cannot be read, a weak password, and the new quantity, invitation and setting messages.
INSERT INTO translations (locale, key, text) VALUES
    ('es-CO', 'add.photo-check', 'Usa una foto JPG, PNG o WebP de hasta 4 MB.'),
    ('en', 'add.photo-check', 'Use a JPG, PNG or WebP photo of up to 4 MB.'),
    ('es-CO', 'auth.password-weak', 'Usa una contraseña que no sea un solo carácter repetido.'),
    ('en', 'auth.password-weak', 'Use a password that is not one repeated character.')
ON CONFLICT (locale, key) DO UPDATE SET text = EXCLUDED.text, updated_at = now();

INSERT INTO message_templates (locale, source, target) VALUES
    ('es-CO', 'A quantity is more than zero', 'Una cantidad debe ser mayor que cero'),
    ('es-CO', 'A quantity holds up to 100 kg', 'Una cantidad llega hasta 100 kg'),
    ('es-CO', 'Invitation not found', 'Invitación no encontrada'),
    ('es-CO', '{} allows one decimal', '{} admite un decimal'),
    ('es-CO', 'Password cannot be one repeated character', 'La contraseña no puede ser un solo carácter repetido')
ON CONFLICT (locale, source) DO UPDATE SET target = EXCLUDED.target;
