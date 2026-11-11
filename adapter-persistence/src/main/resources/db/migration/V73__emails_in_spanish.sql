INSERT INTO message_templates (locale, source, target) VALUES
    ('es-CO', 'Reset your HayPaComer password', 'Restablece tu contraseña de HayPaComer'),
    ('es-CO', 'Use this link within one hour to choose a new password: {}', 'Usa este enlace en la próxima hora para elegir una contraseña nueva: {}'),
    ('es-CO', 'Confirm your email for HayPaComer', 'Confirma tu correo en HayPaComer'),
    ('es-CO', 'Hi {}, confirm your email within 24 hours: {}', 'Hola, {}. Confirma tu correo en las próximas 24 horas: {}'),
    ('es-CO', 'You are invited to {} on HayPaComer', 'Te invitaron a {} en HayPaComer'),
    ('es-CO', 'Join {} within 7 days: {}', 'Únete a {} en los próximos 7 días: {}')
ON CONFLICT (locale, source) DO UPDATE SET target = EXCLUDED.target;
