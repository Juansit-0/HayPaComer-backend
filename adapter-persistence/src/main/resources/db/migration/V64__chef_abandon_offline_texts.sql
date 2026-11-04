-- Texts for leaving a cooking session and for answers given without AI.
INSERT INTO translations (locale, key, text) VALUES
    ('es-CO', 'chef.abandon', 'Dejar de cocinar'),
    ('en', 'chef.abandon', 'Stop cooking'),
    ('es-CO', 'chef.offline-answer', 'Sin IA solo puedo mirar lo que hay en la cocina, y esto es lo que vi. Para cambiar algo, usa Mercado o Nevera.'),
    ('en', 'chef.offline-answer', 'Without AI I can only look at what is in the kitchen, and this is what I saw. To change something, use Market or Fridge.'),
    ('es-CO', 'chef.offline-none', 'Ahora no pude leer la cocina y no cambié nada. Inténtalo de nuevo en un momento.'),
    ('en', 'chef.offline-none', 'I could not read the kitchen just now and changed nothing. Try again in a moment.')
ON CONFLICT (locale, key) DO UPDATE SET text = EXCLUDED.text, updated_at = now();
