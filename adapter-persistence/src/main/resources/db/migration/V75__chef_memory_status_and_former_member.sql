-- Texts for the notice when the Chef cannot reach its memory, and the name used for someone who left the household.
INSERT INTO translations (locale, key, text) VALUES
    ('es-CO', 'status.chef-memory', 'El Chef no puede usar su memoria en este momento. La nevera y el mercado siguen funcionando.'),
    ('en', 'status.chef-memory', 'The Chef cannot use its memory right now. The fridge and the market keep working.')
ON CONFLICT (locale, key) DO UPDATE SET text = EXCLUDED.text, updated_at = now();

INSERT INTO message_templates (locale, source, target) VALUES
    ('es-CO', 'A former member', 'Alguien que ya no está en el hogar')
ON CONFLICT (locale, source) DO UPDATE SET target = EXCLUDED.target;
