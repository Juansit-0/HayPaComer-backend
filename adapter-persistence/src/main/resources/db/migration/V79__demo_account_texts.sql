-- Texts for the button that fills in the shared test account on the sign-in form.
INSERT INTO translations (locale, key, text) VALUES
    ('es-CO', 'auth.demo-fill', 'Datos de prueba'),
    ('en', 'auth.demo-fill', 'Test account'),
    ('es-CO', 'auth.demo-filled', 'Listo, se llenaron el correo y la contraseña de prueba. Toca Iniciar sesión.'),
    ('en', 'auth.demo-filled', 'The test email and password are filled in. Tap Sign in.')
ON CONFLICT (locale, key) DO UPDATE SET text = EXCLUDED.text, updated_at = now();
