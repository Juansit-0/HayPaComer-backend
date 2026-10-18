INSERT INTO translations (locale, key, text) VALUES
    ('es-CO', 'landing.story.scroll', 'Desliza para entrar al mercado.'),
    ('en', 'landing.story.scroll', 'Scroll to walk into the market.'),
    ('es-CO', 'landing.story.skip', 'Saltar la historia'),
    ('en', 'landing.story.skip', 'Skip the story')
ON CONFLICT (locale, key) DO UPDATE SET text = EXCLUDED.text, updated_at = now();
