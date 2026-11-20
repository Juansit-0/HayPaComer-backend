-- The Casa tab: who shares the fridge, the household code, joining with a code, and the "ask first" requests.
INSERT INTO translations (locale, key, text) VALUES
    ('es-CO', 'nav.home', 'Casa'),
    ('en', 'nav.home', 'Home'),
    ('es-CO', 'loading.home.1', 'Buscando a los de la casa'),
    ('en', 'loading.home.1', 'Finding everyone at home'),
    ('es-CO', 'loading.home.2', 'Contando quién comparte la nevera'),
    ('en', 'loading.home.2', 'Counting who shares the fridge'),
    ('es-CO', 'loading.home.3', 'Revisando los permisos'),
    ('en', 'loading.home.3', 'Checking the permissions'),
    ('es-CO', 'home-tab.lead', 'Quiénes comparten esta nevera, el código para que alguien más se una, y los permisos sobre la comida.'),
    ('en', 'home-tab.lead', 'Who shares this fridge, the code for someone else to join, and the permissions on food.'),
    ('es-CO', 'home-tab.members-hint', 'Todos ven la misma nevera. La comida privada solo la ve quien la guardó.'),
    ('en', 'home-tab.members-hint', 'Everyone sees the same fridge. Private food is seen only by whoever stocked it.'),
    ('es-CO', 'home-tab.code-ask-owner', 'Si alguien más quiere unirse, pídele el código a {owner}.'),
    ('en', 'home-tab.code-ask-owner', 'If someone else wants to join, ask {owner} for the code.'),
    ('es-CO', 'home-tab.requests-hint', 'Lo marcado como "preguntar antes" se usa solo con permiso de quien lo guardó.'),
    ('en', 'home-tab.requests-hint', 'Food marked "ask first" is used only with the permission of whoever stocked it.'),
    ('es-CO', 'home-tab.requests-none', 'No hay solicitudes por ahora.'),
    ('en', 'home-tab.requests-none', 'No requests for now.')
ON CONFLICT (locale, key) DO UPDATE SET text = EXCLUDED.text, updated_at = now();
