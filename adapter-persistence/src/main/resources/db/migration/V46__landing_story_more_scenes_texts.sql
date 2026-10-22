INSERT INTO translations (locale, key, text) VALUES
    ('es-CO', 'landing.story.chapter.streets', 'Las calles'),
    ('en', 'landing.story.chapter.streets', 'The streets'),
    ('es-CO', 'landing.story.chapter.home', 'Camino a casa'),
    ('en', 'landing.story.chapter.home', 'The way home'),
    ('es-CO', 'landing.story.chapter.table', 'La mesa'),
    ('en', 'landing.story.chapter.table', 'The table'),
    ('es-CO', 'landing.story.people', 'Se vende por montones y a ojo: nadie anota cuánto lleva cada casa.'),
    ('en', 'landing.story.people', 'Food is sold by the heap and by eye: nobody writes down how much each home takes.'),
    ('es-CO', 'landing.story.table', 'Lo que entró a la nevera llega a la mesa, no a la basura.'),
    ('en', 'landing.story.table', 'What went into the fridge reaches the table, not the bin.')
ON CONFLICT (locale, key) DO UPDATE SET text = EXCLUDED.text, updated_at = now();
