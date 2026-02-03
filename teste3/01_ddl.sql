-- PostgreSQL > 10
-- Teste 3.2 - DDL (modelo normalizado)

BEGIN;

-- 1) Cadastro de operadoras (CADOP)
CREATE TABLE IF NOT EXISTS operadoras (
  cnpj              CHAR(14) PRIMARY KEY,          -- apenas dígitos
  registro_ans      VARCHAR(20),                   -- REGISTRO_OPERADORA (REG_ANS)
  razao_social      TEXT NOT NULL,
  nome_fantasia     TEXT,
  modalidade        TEXT,
  uf                CHAR(2),
  data_registro_ans DATE
);

CREATE INDEX IF NOT EXISTS idx_operadoras_uf ON operadoras (uf);
CREATE INDEX IF NOT EXISTS idx_operadoras_registro_ans ON operadoras (registro_ans);

-- 2) Despesas consolidadas (geradas no Teste 1)
-- Observação: o CSV pode ter muitas linhas, então índice por (cnpj, ano, trimestre)
CREATE TABLE IF NOT EXISTS despesas_consolidadas (
  id               BIGSERIAL PRIMARY KEY,
  cnpj             CHAR(14) NOT NULL REFERENCES operadoras(cnpj),
  razao_social_csv TEXT NOT NULL,  -- mantido do CSV (controle/auditoria)
  ano              SMALLINT NOT NULL,
  trimestre        SMALLINT NOT NULL CHECK (trimestre BETWEEN 1 AND 4),
  valor_despesas   NUMERIC(18,2) NOT NULL CHECK (valor_despesas > 0)
);

CREATE INDEX IF NOT EXISTS idx_dc_cnpj_ano_tri ON despesas_consolidadas (cnpj, ano, trimestre);
CREATE INDEX IF NOT EXISTS idx_dc_ano_tri ON despesas_consolidadas (ano, trimestre);

-- 3) Despesas agregadas (resultado do Teste 2)
CREATE TABLE IF NOT EXISTS despesas_agregadas (
  cnpj              CHAR(14) NOT NULL REFERENCES operadoras(cnpj),
  uf                CHAR(2),
  razao_social_csv  TEXT NOT NULL,
  total_despesas    NUMERIC(18,2) NOT NULL CHECK (total_despesas >= 0),
  qtd_registros     INT NOT NULL CHECK (qtd_registros >= 0),
  PRIMARY KEY (cnpj, uf)
);

CREATE INDEX IF NOT EXISTS idx_da_total ON despesas_agregadas (total_despesas DESC);
CREATE INDEX IF NOT EXISTS idx_da_uf ON despesas_agregadas (uf);

COMMIT;
