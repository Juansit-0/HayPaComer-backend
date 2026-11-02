-- The tray tile and the add sheet name any part of the fridge, not only a shelf: a door rack, a drawer, or the freezer.
INSERT INTO translations (locale, key, text) VALUES
    ('es-CO', 'fridge.on-shelf', '{grams} en esta parte de la nevera.'),
    ('en', 'fridge.on-shelf', '{grams} in this part of the fridge.'),
    ('es-CO', 'fridge.empty-shelf', 'Esta parte de la nevera está vacía.'),
    ('en', 'fridge.empty-shelf', 'This part of the fridge is empty.'),
    ('es-CO', 'fridge.add-title', 'Pon comida en la nevera'),
    ('en', 'fridge.add-title', 'Put food in the fridge')
ON CONFLICT (locale, key) DO UPDATE SET text = EXCLUDED.text, updated_at = now();
