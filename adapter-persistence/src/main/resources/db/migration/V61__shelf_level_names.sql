-- Shelves and freezer shelves are named by how many there are: two read Top and Bottom, three add
-- Middle, and more are numbered between Top and Bottom. Names a household chose are left alone.
WITH ranked AS (
    SELECT t.id,
           t.name,
           row_number() OVER (PARTITION BY z.fridge_id, z.kind ORDER BY z.position, t.position) - 1 AS level,
           count(*) OVER (PARTITION BY z.fridge_id, z.kind) AS levels
    FROM trays t
    JOIN zones z ON z.id = t.zone_id
    WHERE z.kind IN ('SHELF', 'FREEZER')
)
UPDATE trays
SET name = CASE
        WHEN ranked.level = 0 THEN 'Top'
        WHEN ranked.level = ranked.levels - 1 THEN 'Bottom'
        WHEN ranked.levels = 3 THEN 'Middle'
        ELSE 'Shelf ' || (ranked.level + 1)
    END
FROM ranked
WHERE trays.id = ranked.id
  AND ranked.name ~ '^(Top|Middle|Bottom|Shelf [0-9]+)$';

INSERT INTO translations (locale, key, text) VALUES
    ('es-CO', 'place.shelf 2', 'Repisa 2'),
    ('en', 'place.shelf 2', 'Shelf 2'),
    ('es-CO', 'place.shelf 3', 'Repisa 3'),
    ('en', 'place.shelf 3', 'Shelf 3')
ON CONFLICT (locale, key) DO UPDATE SET text = EXCLUDED.text, updated_at = now();
