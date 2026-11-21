-- Texts for deleting a saved recipe, and the reason a recipe still in a weekly plan stays.
INSERT INTO translations (locale, key, text) VALUES
    ('es-CO', 'recipes.delete', 'Borrar'),
    ('en', 'recipes.delete', 'Delete'),
    ('es-CO', 'recipes.delete-title', 'Borrar receta'),
    ('en', 'recipes.delete-title', 'Delete recipe'),
    ('es-CO', 'recipes.delete-ask', '¿Borrar {recipe}? Esto no se puede deshacer.'),
    ('en', 'recipes.delete-ask', 'Delete {recipe}? This cannot be undone.'),
    ('es-CO', 'recipes.toast-deleted', 'Borraste {recipe}.'),
    ('en', 'recipes.toast-deleted', '{recipe} was deleted.')
ON CONFLICT (locale, key) DO UPDATE SET text = EXCLUDED.text, updated_at = now();

INSERT INTO message_templates (locale, source, target) VALUES
    ('es-CO', 'This recipe is in a weekly plan; change those meals before deleting it',
     'Esta receta está en un plan semanal; cambia esas comidas antes de borrarla')
ON CONFLICT (locale, source) DO UPDATE SET target = EXCLUDED.target;
