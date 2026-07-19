-- Tokens de redefinição de senha: uso único, expiração curta (30 min).
-- Mesmo desenho da tabela tokens_verificacao_email.
CREATE TABLE IF NOT EXISTS tokens_redefinicao_senha (
    id          UUID PRIMARY KEY,
    usuario_id  UUID         NOT NULL REFERENCES usuarios (id),
    token       VARCHAR(255) NOT NULL UNIQUE,
    expira_em   TIMESTAMP    NOT NULL,
    usado_em    TIMESTAMP,
    criado_em   TIMESTAMP    NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_tokens_redefinicao_senha_usuario
    ON tokens_redefinicao_senha (usuario_id);
