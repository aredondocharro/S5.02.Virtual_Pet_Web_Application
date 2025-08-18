
ALTER TABLE pets
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ;

-- Rellena valores para filas existentes
UPDATE pets SET created_at = COALESCE(created_at, now());
UPDATE pets SET updated_at = COALESCE(updated_at, now());

-- Marca NOT NULL y defaults para nuevas filas
ALTER TABLE pets
    ALTER COLUMN created_at SET NOT NULL,
    ALTER COLUMN created_at SET DEFAULT now(),
    ALTER COLUMN updated_at SET NOT NULL,
    ALTER COLUMN updated_at SET DEFAULT now();
