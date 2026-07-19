-- A coluna pode ter sido criada nullable por um ddl-auto=update antigo (antes da V3),
-- ficando com valores nulos que a V3 (ADD COLUMN IF NOT EXISTS) nao corrigiu.
-- Backfill + reforco do NOT NULL/DEFAULT. Idempotente: em banco onde a V3 ja criou
-- a coluna corretamente, estas instrucoes sao no-op.

UPDATE perfil_candidato SET aceita_email_followup = TRUE WHERE aceita_email_followup IS NULL;

ALTER TABLE perfil_candidato ALTER COLUMN aceita_email_followup SET DEFAULT TRUE;
ALTER TABLE perfil_candidato ALTER COLUMN aceita_email_followup SET NOT NULL;
