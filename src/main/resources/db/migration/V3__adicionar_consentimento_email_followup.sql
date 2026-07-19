-- Consentimento (opt-out) para o email diário de follow-up
ALTER TABLE perfil_candidato
    ADD COLUMN IF NOT EXISTS aceita_email_followup BOOLEAN NOT NULL DEFAULT TRUE;
