INSERT INTO tipos_usuario (id, tipo)
SELECT gen_random_uuid(), 'USUARIO'
    WHERE NOT EXISTS (SELECT 1 FROM tipos_usuario WHERE tipo = 'USUARIO');

INSERT INTO tipos_usuario (id, tipo)
SELECT gen_random_uuid(), 'ADMIN'
    WHERE NOT EXISTS (SELECT 1 FROM tipos_usuario WHERE tipo = 'ADMIN');