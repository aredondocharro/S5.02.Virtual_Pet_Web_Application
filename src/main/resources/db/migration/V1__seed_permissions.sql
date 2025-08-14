INSERT INTO permissions (name)
SELECT 'CREATE'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'CREATE');

INSERT INTO permissions (name)
SELECT 'READ'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'READ');

INSERT INTO permissions (name)
SELECT 'UPDATE'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'UPDATE');

INSERT INTO permissions (name)
SELECT 'DELETE'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'DELETE');
