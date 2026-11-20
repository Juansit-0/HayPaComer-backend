-- Sealed packages keep their printed date, so the margin now only limits opened food.
INSERT INTO message_templates (locale, source, target) VALUES
    ('es-CO', 'The expiry date is more than {} years away; check the label',
     'La fecha de vencimiento está a más de {} años; revisa la etiqueta')
ON CONFLICT (locale, source) DO UPDATE SET target = EXCLUDED.target;

UPDATE settings
SET description = 'Extra days accepted beyond the shelf life of opened food before a typed expiry date is rejected'
WHERE key = 'food.expiry-margin-days';

INSERT INTO translations (locale, key, text) VALUES
    ('es-CO', 'setting.food.expiry-margin-days',
     'Días extra que se aceptan sobre la vida útil de un alimento abierto antes de rechazar una fecha escrita')
ON CONFLICT (locale, key) DO UPDATE SET text = EXCLUDED.text, updated_at = now();
