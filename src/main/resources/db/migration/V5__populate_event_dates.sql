UPDATE events
SET event_date = DATE '2026-07-19',
    registration_enabled = TRUE,
    updated_at = NOW()
WHERE id = UUID '45d82aa2-0c48-4311-b70b-ed6451f518d1'
  AND event_date IS NULL;

UPDATE events
SET event_date = DATE '2026-08-16',
    registration_enabled = TRUE,
    updated_at = NOW()
WHERE id = UUID '1d8d38db-4e30-4c13-a6bf-0f54fac47292'
  AND event_date IS NULL;

UPDATE events
SET registration_enabled = TRUE,
    updated_at = NOW()
WHERE registration_enabled IS DISTINCT FROM TRUE;
