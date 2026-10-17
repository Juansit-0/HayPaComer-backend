INSERT INTO translations (locale, key, text) VALUES
    ('es-CO', 'stage.palette', 'Arrastra un alimento a una repisa y queda guardado; tócalo para elegir cantidad y fecha.'),
    ('en', 'stage.palette', 'Drag a food onto a shelf and it is stored; tap it to choose the amount and date.'),
    ('es-CO', 'stage.share.100', 'Todo'),
    ('en', 'stage.share.100', 'All of it'),
    ('es-CO', 'stage.share.25', 'Un cuarto'),
    ('en', 'stage.share.25', 'A quarter'),
    ('es-CO', 'stage.share.50', 'La mitad'),
    ('en', 'stage.share.50', 'Half'),
    ('es-CO', 'stage.undo', 'Deshacer'),
    ('en', 'stage.undo', 'Undo')
ON CONFLICT (locale, key) DO UPDATE SET text = EXCLUDED.text, updated_at = now();
