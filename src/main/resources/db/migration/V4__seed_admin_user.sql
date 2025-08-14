-- Creates an admin user if it does not exist
INSERT INTO users (email, password, is_enabled, account_non_expired, credentials_non_expired, account_non_locked, username)
SELECT '${adminEmail}', '${adminPasswordHash}', true, true, true, true, 'admin'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = '${adminEmail}');

-- Asign ADMIN to admin user if not already assigned
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.role_name = 'ADMIN'
WHERE u.email = '${adminEmail}'
  AND NOT EXISTS (
      SELECT 1 FROM user_roles ur
      WHERE ur.user_id = u.id AND ur.role_id = r.id
  );
