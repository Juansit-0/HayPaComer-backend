INSERT INTO translations (locale, key, text) VALUES
    ('es-CO', 'chef.how-to', '¿Cómo preparo {recipe}?'),
    ('en', 'chef.how-to', 'How do I make {recipe}?'),
    ('es-CO', 'chef.prompt.buy', '¿Qué compro esta semana?'),
    ('en', 'chef.prompt.buy', 'What should I buy this week?'),
    ('es-CO', 'chef.prompt.first', '¿Qué debo usar primero?'),
    ('en', 'chef.prompt.first', 'What should I use first?'),
    ('es-CO', 'chef.prompt.tonight', '¿Qué puedo cocinar esta noche?'),
    ('en', 'chef.prompt.tonight', 'What can I cook tonight?'),
    ('es-CO', 'chef.thinking', 'Pensando'),
    ('en', 'chef.thinking', 'Thinking')
ON CONFLICT (locale, key) DO UPDATE SET text = EXCLUDED.text, updated_at = now();
