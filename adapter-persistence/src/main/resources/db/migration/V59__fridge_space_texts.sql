-- Texts for the room in each part of the fridge, moving food between parts, and the freezer on the 3D fridge.
INSERT INTO translations (locale, key, text) VALUES
    ('es-CO', 'stage.full', 'Esta parte de la nevera está llena ({place}). Prueba en otra.'),
    ('en', 'stage.full', 'This part of the fridge is full ({place}). Try another one.'),
    ('es-CO', 'stage.this-part', 'esta parte'),
    ('en', 'stage.this-part', 'this part'),
    ('es-CO', 'stage.drop-on-shelf', 'Suelta la comida sobre una repisa, un cajón o la puerta'),
    ('en', 'stage.drop-on-shelf', 'Drop the food on a shelf, a drawer or the door'),
    ('es-CO', 'stage.freezer', 'Congelador'),
    ('en', 'stage.freezer', 'Freezer'),
    ('es-CO', 'fridge.move-to', 'Mover a'),
    ('en', 'fridge.move-to', 'Move to'),
    ('es-CO', 'fridge.move', 'Mover'),
    ('en', 'fridge.move', 'Move'),
    ('es-CO', 'fridge.space', 'Quedan {free} de {total} litros'),
    ('en', 'fridge.space', '{free} of {total} litres left'),
    ('es-CO', 'fridge.space-full', 'Esta parte de la nevera está llena'),
    ('en', 'fridge.space-full', 'This part of the fridge is full')
ON CONFLICT (locale, key) DO UPDATE SET text = EXCLUDED.text, updated_at = now();
