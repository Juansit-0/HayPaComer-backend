-- Texts for taring and calibrating a scale from the sensors list in Settings.
INSERT INTO translations (locale, key, text) VALUES
    ('es-CO', 'settings.scale-tare', 'Tara (plato vacío)'),
    ('en', 'settings.scale-tare', 'Tare (empty plate)'),
    ('es-CO', 'settings.scale-known', 'Gramos del objeto conocido'),
    ('en', 'settings.scale-known', 'Grams of the known object'),
    ('es-CO', 'settings.scale-calibrate', 'Calibrar'),
    ('en', 'settings.scale-calibrate', 'Calibrate'),
    ('es-CO', 'settings.scale-read', 'Ver peso'),
    ('en', 'settings.scale-read', 'Show weight'),
    ('es-CO', 'settings.scale-tared', 'Listo: la báscula quedó en cero. Ahora pon el objeto conocido y toca Calibrar.'),
    ('en', 'settings.scale-tared', 'Done: the scale is at zero. Now put the known object on it and tap Calibrate.'),
    ('es-CO', 'settings.scale-calibrated', 'Calibrada. Toca Ver peso para comprobar.'),
    ('en', 'settings.scale-calibrated', 'Calibrated. Tap Show weight to check.'),
    ('es-CO', 'settings.scale-now', 'Ahora pesa {grams}'),
    ('en', 'settings.scale-now', 'It weighs {grams} now'),
    ('es-CO', 'settings.scale-uncalibrated', 'Aún sin calibrar: haz la tara y luego calibra.'),
    ('en', 'settings.scale-uncalibrated', 'Not calibrated yet: tare it, then calibrate.')
ON CONFLICT (locale, key) DO UPDATE SET text = EXCLUDED.text, updated_at = now();
