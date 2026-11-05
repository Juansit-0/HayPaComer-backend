-- Texts for choosing who can use a food, checking a new household, and spending over the budget.
INSERT INTO translations (locale, key, text) VALUES
    ('es-CO', 'add.visibility', '¿Quién puede usarla?'),
    ('en', 'add.visibility', 'Who can use it?'),
    ('es-CO', 'add.visibility.SHARED', 'Todos'),
    ('en', 'add.visibility.SHARED', 'Everyone'),
    ('es-CO', 'add.visibility.ASK_FIRST', 'Que me pregunten'),
    ('en', 'add.visibility.ASK_FIRST', 'Ask me first'),
    ('es-CO', 'add.visibility.PRIVATE', 'Solo yo'),
    ('en', 'add.visibility.PRIVATE', 'Only me'),
    ('es-CO', 'home.check', 'Escribe el nombre del hogar y una moneda de 3 letras, como COP.'),
    ('en', 'home.check', 'Enter the household name and a 3-letter currency, like COP.'),
    ('es-CO', 'market.over', 'De más este mes'),
    ('en', 'market.over', 'Over this month')
ON CONFLICT (locale, key) DO UPDATE SET text = EXCLUDED.text, updated_at = now();
