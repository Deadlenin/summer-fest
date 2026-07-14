INSERT INTO events (
    id,
    title,
    description,
    event_date,
    company_name,
    location,
    registration_enabled,
    created_at,
    updated_at
)
VALUES
    (
        UUID '45d82aa2-0c48-4311-b70b-ed6451f518d1',
        'Summer Fest — День 1',
        'Первый день фестиваля',
        DATE '2026-07-19',
        'SberTech',
        NULL,
        TRUE,
        NOW(),
        NOW()
    ),
    (
        UUID '1d8d38db-4e30-4c13-a6bf-0f54fac47292',
        'Summer Fest — День 2',
        'Второй день фестиваля',
        DATE '2026-08-16',
        'SberTech',
        NULL,
        TRUE,
        NOW(),
        NOW()
    )
ON CONFLICT (id) DO NOTHING;
