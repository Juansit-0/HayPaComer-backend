-- Food someone takes along when they leave a home (or the home is deleted): it waits here and goes into the fridge of the
-- next home they join, with its amount, date, visibility and weight per unit.
CREATE TABLE carried_food (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    food_id UUID NOT NULL REFERENCES food_catalog (id),
    quantity_g NUMERIC(10, 2) NOT NULL CHECK (quantity_g > 0),
    expires_on DATE,
    expiry_source TEXT CHECK (expiry_source IN ('USER', 'LABEL', 'AI_SUGGESTED', 'ESTIMATED')),
    opened BOOLEAN NOT NULL DEFAULT FALSE,
    visibility TEXT NOT NULL CHECK (visibility IN ('SHARED', 'ASK_FIRST', 'PRIVATE')),
    piece_grams NUMERIC(10, 2) CHECK (piece_grams > 0),
    packed_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX carried_food_user_idx ON carried_food (user_id, packed_at);
