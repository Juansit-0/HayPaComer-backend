-- Texts for saving leftovers when a cooking session ends.
INSERT INTO translations (locale, key, text) VALUES
    ('es-CO', 'chef.leftover.title', '¿Sobró algo de {recipe}?'),
    ('en', 'chef.leftover.title', 'Any {recipe} left over?'),
    ('es-CO', 'chef.leftover.lead', 'Guárdalo como sobras y quedará de primero para que no se pierda.'),
    ('en', 'chef.leftover.lead', 'Save it as leftovers and it will come first so it does not go to waste.'),
    ('es-CO', 'chef.leftover.save', 'Guardar sobras'),
    ('en', 'chef.leftover.save', 'Save leftovers'),
    ('es-CO', 'chef.leftover.none', 'No sobró nada'),
    ('en', 'chef.leftover.none', 'Nothing left')
ON CONFLICT (locale, key) DO UPDATE SET text = EXCLUDED.text, updated_at = now();
