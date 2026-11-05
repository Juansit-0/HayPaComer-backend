-- Texts for someone who left a household while the app was open, and for inviting someone already in it.
INSERT INTO translations (locale, key, text) VALUES
    ('es-CO', 'home.gone', 'Ya no eres parte de ese hogar.'),
    ('en', 'home.gone', 'You are no longer part of that household.')
ON CONFLICT (locale, key) DO UPDATE SET text = EXCLUDED.text, updated_at = now();

INSERT INTO message_templates (locale, source, target) VALUES
    ('es-CO', 'That person is already in this household', 'Esa persona ya es parte de este hogar')
ON CONFLICT (locale, source) DO UPDATE SET target = EXCLUDED.target;
