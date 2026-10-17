UPDATE settings
SET value = '10', updated_at = now()
WHERE key = 'auth.max-failed-logins' AND value = '5';
