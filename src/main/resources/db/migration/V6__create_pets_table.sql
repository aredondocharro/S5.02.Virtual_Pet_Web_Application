
CREATE TABLE IF NOT EXISTS pets (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  color VARCHAR(100) NOT NULL,
  hunger INTEGER NOT NULL DEFAULT 50,
  happiness INTEGER NOT NULL DEFAULT 50,
  owner_id BIGINT NOT NULL,

  CONSTRAINT fk_pets_owner
    FOREIGN KEY (owner_id) REFERENCES users (id) ON DELETE CASCADE,

  CONSTRAINT chk_pets_hunger CHECK (hunger BETWEEN 0 AND 100),
  CONSTRAINT chk_pets_happiness CHECK (happiness BETWEEN 0 AND 100)
);

CREATE INDEX IF NOT EXISTS idx_pets_owner_id ON pets(owner_id);
CREATE INDEX IF NOT EXISTS idx_pets_name ON pets(name);
