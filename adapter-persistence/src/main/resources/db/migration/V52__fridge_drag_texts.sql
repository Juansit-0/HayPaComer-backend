INSERT INTO translations (locale, key, text) VALUES
    ('es-CO', 'landing.story.drag', 'Arrastra la manija para abrir la nevera'),
    ('en', 'landing.story.drag', 'Drag the handle to open the fridge'),
    ('es-CO', 'landing.story.drag-hint', 'Arrastra la manija hacia la derecha hasta la marca. Con teclado, usa las flechas.'),
    ('en', 'landing.story.drag-hint', 'Drag the handle to the right, past the mark. On a keyboard, use the arrow keys.')
ON CONFLICT (locale, key) DO UPDATE SET text = EXCLUDED.text, updated_at = now();
