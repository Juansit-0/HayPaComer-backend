-- A code to join a household and share its fridge without an email invitation, one per household; and the requests someone makes
-- to use a food its owner marked "ask first", which the owner answers once.
CREATE TABLE household_join_codes (
    household_id UUID PRIMARY KEY REFERENCES households (id) ON DELETE CASCADE,
    code TEXT NOT NULL UNIQUE CHECK (code ~ '^[A-Z2-9]{8}$'),
    role TEXT NOT NULL CHECK (role IN ('MEMBER', 'GUEST')),
    created_by UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE food_access_requests (
    id UUID PRIMARY KEY,
    household_id UUID NOT NULL REFERENCES households (id) ON DELETE CASCADE,
    food_item_id UUID NOT NULL REFERENCES food_items (id) ON DELETE CASCADE,
    requester_member_id UUID NOT NULL REFERENCES household_members (member_id) ON DELETE CASCADE,
    owner_member_id UUID NOT NULL REFERENCES household_members (member_id) ON DELETE CASCADE,
    status TEXT NOT NULL CHECK (status IN ('PENDING', 'APPROVED', 'DENIED')),
    created_at TIMESTAMPTZ NOT NULL,
    decided_at TIMESTAMPTZ,
    CHECK (requester_member_id <> owner_member_id)
);

CREATE UNIQUE INDEX food_access_requests_one_pending_idx
    ON food_access_requests (food_item_id, requester_member_id) WHERE status = 'PENDING';
CREATE INDEX food_access_requests_owner_idx ON food_access_requests (household_id, owner_member_id) WHERE status = 'PENDING';
CREATE INDEX food_access_requests_requester_idx ON food_access_requests (household_id, requester_member_id, created_at DESC);

INSERT INTO translations (locale, key, text) VALUES
    ('es-CO', 'That household code is not valid or has expired', 'Ese código de hogar no es válido o ya venció'),
    ('es-CO', 'You already belong to this household', 'Ya perteneces a este hogar'),
    ('es-CO', 'This food does not need asking', 'Este alimento no necesita permiso'),
    ('es-CO', 'You can already use this food', 'Ya puedes usar este alimento'),
    ('es-CO', 'Only the person who stocked this food can answer', 'Solo quien guardó este alimento puede responder'),
    ('es-CO', 'This request was already answered', 'Esta solicitud ya fue respondida'),
    ('es-CO', 'Food request not found', 'No encontramos esa solicitud'),
    ('es-CO', 'Someone asked to use a food', 'Alguien pidió permiso para usar un alimento'),
    ('es-CO', 'A food request was answered', 'Respondieron una solicitud de alimento'),
    ('es-CO', 'settings.code-title', 'Código del hogar'),
    ('en', 'settings.code-title', 'Household code'),
    ('es-CO', 'settings.code-hint', 'Compártelo con quien viva contigo: al escribirlo entra a este hogar y ve la misma nevera. Vence en 7 días.'),
    ('en', 'settings.code-hint', 'Share it with whoever lives with you: typing it joins this household and shows the same fridge. It expires in 7 days.'),
    ('es-CO', 'settings.code-none', 'Todavía no hay un código activo.'),
    ('en', 'settings.code-none', 'There is no active code yet.'),
    ('es-CO', 'settings.code-create', 'Generar código'),
    ('en', 'settings.code-create', 'Create code'),
    ('es-CO', 'settings.code-renew', 'Generar otro'),
    ('en', 'settings.code-renew', 'Make a new one'),
    ('es-CO', 'settings.code-revoke', 'Desactivar'),
    ('en', 'settings.code-revoke', 'Turn off'),
    ('es-CO', 'settings.code-expires', 'Vence {date}'),
    ('en', 'settings.code-expires', 'Expires {date}'),
    ('es-CO', 'settings.code-copied', 'Código copiado'),
    ('en', 'settings.code-copied', 'Code copied'),
    ('es-CO', 'settings.join-title', 'Unirme a un hogar'),
    ('en', 'settings.join-title', 'Join a household'),
    ('es-CO', 'settings.join-hint', 'Escribe el código que te compartieron para sincronizarte con la nevera de esa casa.'),
    ('en', 'settings.join-hint', 'Type the code you were given to sync with that home''s fridge.'),
    ('es-CO', 'settings.join-code', 'Código'),
    ('en', 'settings.join-code', 'Code'),
    ('es-CO', 'settings.join-send', 'Unirme'),
    ('en', 'settings.join-send', 'Join'),
    ('es-CO', 'settings.joined', 'Ahora compartes la nevera de {household}'),
    ('en', 'settings.joined', 'You now share the fridge of {household}'),
    ('es-CO', 'requests.title', 'Permisos'),
    ('en', 'requests.title', 'Permissions'),
    ('es-CO', 'requests.ask', 'Pedir permiso'),
    ('en', 'requests.ask', 'Ask to use it'),
    ('es-CO', 'requests.asked', 'Le pediste permiso a {owner}'),
    ('en', 'requests.asked', 'You asked {owner}'),
    ('es-CO', 'requests.sent', 'Solicitud enviada a {owner}'),
    ('en', 'requests.sent', 'Request sent to {owner}'),
    ('es-CO', 'requests.incoming', '{member} quiere usar {food}'),
    ('en', 'requests.incoming', '{member} wants to use {food}'),
    ('es-CO', 'requests.approve', 'Permitir'),
    ('en', 'requests.approve', 'Allow'),
    ('es-CO', 'requests.deny', 'No'),
    ('en', 'requests.deny', 'No'),
    ('es-CO', 'requests.approved', '{owner} te dejó usar {food}'),
    ('en', 'requests.approved', '{owner} let you use {food}'),
    ('es-CO', 'requests.denied', '{owner} prefirió no compartir {food}'),
    ('en', 'requests.denied', '{owner} would rather keep {food}'),
    ('es-CO', 'requests.pending', 'Esperando a {owner}'),
    ('en', 'requests.pending', 'Waiting for {owner}'),
    ('es-CO', 'requests.owner', 'Es de {owner}'),
    ('en', 'requests.owner', 'Belongs to {owner}'),
    ('es-CO', 'requests.need', '{owner} pidió que le preguntes antes de usarlo.'),
    ('en', 'requests.need', '{owner} asked to be asked before it is used.')
ON CONFLICT (locale, key) DO UPDATE SET text = EXCLUDED.text, updated_at = now();
