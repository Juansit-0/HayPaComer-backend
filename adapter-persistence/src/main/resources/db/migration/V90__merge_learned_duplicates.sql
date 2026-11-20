-- Foods learned by the AI under the translated name of a catalog food ("Tomate" next to Tomato) are the same food:
-- everything that used the copy moves to the catalog food, which has its drawing, and the copy goes.
CREATE TEMPORARY TABLE learned_duplicates ON COMMIT DROP AS
SELECT copy.id AS copy_id, copy.name_key AS copy_key, original.id AS original_id, original.name_key AS original_key
FROM food_catalog copy
JOIN translations t ON t.key LIKE 'food.%' AND lower(t.text) = copy.name_key
JOIN food_catalog original ON 'food.' || original.name_key = t.key
WHERE copy.id <> original.id
  AND 'food.' || copy.name_key NOT IN (SELECT key FROM translations WHERE key LIKE 'food.%');

UPDATE food_items i SET food_id = d.original_id FROM learned_duplicates d WHERE i.food_id = d.copy_id;
UPDATE market_items i SET food_id = d.original_id FROM learned_duplicates d WHERE i.food_id = d.copy_id;
UPDATE carried_food i SET food_id = d.original_id FROM learned_duplicates d WHERE i.food_id = d.copy_id;
UPDATE recipe_requirements r SET food_id = d.original_id FROM learned_duplicates d WHERE r.food_id = d.copy_id;
UPDATE recipe_steps s SET weigh_food_id = d.original_id FROM learned_duplicates d WHERE s.weigh_food_id = d.copy_id;
UPDATE inventory_movements m SET food_key = d.original_key FROM learned_duplicates d WHERE m.food_key = d.copy_key;
DELETE FROM food_prices p USING learned_duplicates d
WHERE p.food_key = d.copy_key
  AND EXISTS (SELECT 1 FROM food_prices kept WHERE kept.household_id = p.household_id AND kept.food_key = d.original_key);
UPDATE food_prices p SET food_key = d.original_key FROM learned_duplicates d WHERE p.food_key = d.copy_key;
DELETE FROM food_catalog c USING learned_duplicates d WHERE c.id = d.copy_id;
