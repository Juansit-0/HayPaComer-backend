INSERT INTO translations (locale, key, text) VALUES
    ('es-CO', 'landing.story.cook', 'Y antes de que se pierda, te dice qué cocinar con lo que hay.'),
    ('en', 'landing.story.cook', 'And before it spoils, it tells you what to cook with what there is.'),
    ('es-CO', 'landing.story.knows', 'Así la nevera sabe qué tienes y qué vence primero.'),
    ('en', 'landing.story.knows', 'That is how the fridge knows what you have and what expires first.'),
    ('es-CO', 'landing.story.weigh', 'La báscula pesa lo que entra y lo que sale: 842 g de tomate de árbol.'),
    ('en', 'landing.story.weigh', 'The scale weighs what goes in and what comes out: 842 g of tree tomato.')
ON CONFLICT (locale, key) DO UPDATE SET text = EXCLUDED.text, updated_at = now();
