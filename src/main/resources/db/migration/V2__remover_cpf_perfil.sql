-- LGPD (minimização): CPF não tem finalidade funcional num ATS pessoal.
-- Remoção definitiva do dado — irreversível por decisão de conformidade.
ALTER TABLE perfil_candidato DROP COLUMN IF EXISTS cpf;
