-- PostgreSQL > 10
-- Teste 3.3 - Importação CSV (UTF-8) + staging + tratamento

BEGIN;

-- 0) STAGING: tudo texto para importar sem erro
DROP TABLE IF EXISTS stg_operadoras;
CREATE TEMP TABLE stg_operadoras (
  registro_operadora TEXT,
  cnpj TEXT,
  razao_social TEXT,
  nome_fantasia TEXT,
  modalidade TEXT,
  logradouro TEXT,
  numero TEXT,
  complemento TEXT,
  bairro TEXT,
  cidade TEXT,
  uf TEXT,
  cep TEXT,
  ddd TEXT,
  telefone TEXT,
  fax TEXT,
  endereco_eletronico TEXT,
  representante TEXT,
  cargo_representante TEXT,
  regiao_de_comercializacao TEXT,
  data_registro_ans TEXT
);

DROP TABLE IF EXISTS stg_consolidadas;
CREATE TEMP TABLE stg_consolidadas (
  cnpj TEXT,
  razao_social TEXT,
  ano TEXT,
  trimestre TEXT,
  valor_despesas TEXT
);

DROP TABLE IF EXISTS stg_agregadas;
CREATE TEMP TABLE stg_agregadas (
  cnpj TEXT,
  razao_social TEXT,
  uf TEXT,
  total_despesas TEXT,
  qtd_registros TEXT
);

-- 1) COPY (ajuste os paths conforme onde você rodar o sql)
-- Exemplo de path: 'C:/.../teste2/data/input/Relatorio_cadop.csv'

-- CADOP (separador ;)
-- \copy stg_operadoras FROM 'teste2/data/input/Relatorio_cadop.csv' WITH (FORMAT csv, HEADER true, DELIMITER ';', ENCODING 'UTF8');

-- consolidado (separador ,)
-- \copy stg_consolidadas FROM 'teste2/data/input/consolidado_despesas.csv' WITH (FORMAT csv, HEADER true, DELIMITER ',', ENCODING 'UTF8');

-- agregadas (separador ,)
-- \copy stg_agregadas FROM 'teste2/data/output/despesas_agregadas.csv' WITH (FORMAT csv, HEADER true, DELIMITER ',', ENCODING 'UTF8');


-- 2) Inserção em operadoras (tratamento de inconsistências)
-- Estratégia:
-- - cnpj: só dígitos, precisa ter 14
-- - razao_social: obrigatório (descarta se vazio)
-- - uf: pega 2 chars se existir
-- - data_registro_ans: tenta converter (YYYY-MM-DD); se falhar fica NULL
INSERT INTO operadoras (cnpj, registro_ans, razao_social, nome_fantasia, modalidade, uf, data_registro_ans)
SELECT
  cnpj_digits.cnpj14 AS cnpj,
  NULLIF(trim(stg.registro_operadora), '') AS registro_ans,
  trim(stg.razao_social) AS razao_social,
  NULLIF(trim(stg.nome_fantasia), '') AS nome_fantasia,
  NULLIF(trim(stg.modalidade), '') AS modalidade,
  CASE WHEN length(trim(stg.uf)) = 2 THEN upper(trim(stg.uf)) ELSE NULL END AS uf,
  CASE
    WHEN trim(stg.data_registro_ans) ~ '^\d{4}-\d{2}-\d{2}$' THEN trim(stg.data_registro_ans)::date
    ELSE NULL
  END AS data_registro_ans
FROM stg_operadoras stg
CROSS JOIN LATERAL (
  SELECT regexp_replace(coalesce(stg.cnpj,''), '\D', '', 'g') AS cnpj14
) cnpj_digits
WHERE length(cnpj_digits.cnpj14) = 14
  AND NULLIF(trim(stg.razao_social), '') IS NOT NULL
ON CONFLICT (cnpj) DO UPDATE
SET
  registro_ans = EXCLUDED.registro_ans,
  razao_social = EXCLUDED.razao_social,
  nome_fantasia = EXCLUDED.nome_fantasia,
  modalidade = EXCLUDED.modalidade,
  uf = EXCLUDED.uf,
  data_registro_ans = EXCLUDED.data_registro_ans;

-- 3) Inserção em despesas_consolidadas
-- Estratégia:
-- - descarta linhas com null/invalidos em campos obrigatórios
-- - converte valor para numeric
INSERT INTO despesas_consolidadas (cnpj, razao_social_csv, ano, trimestre, valor_despesas)
SELECT
  cnpj_digits.cnpj14 AS cnpj,
  trim(stg.razao_social) AS razao_social_csv,
  (trim(stg.ano))::smallint AS ano,
  (trim(stg.trimestre))::smallint AS trimestre,
  (trim(stg.valor_despesas))::numeric(18,2) AS valor_despesas
FROM stg_consolidadas stg
CROSS JOIN LATERAL (
  SELECT regexp_replace(coalesce(stg.cnpj,''), '\D', '', 'g') AS cnpj14
) cnpj_digits
WHERE length(cnpj_digits.cnpj14) = 14
  AND NULLIF(trim(stg.razao_social), '') IS NOT NULL
  AND trim(stg.ano) ~ '^\d{4}$'
  AND trim(stg.trimestre) ~ '^[1-4]$'
  AND trim(stg.valor_despesas) ~ '^\d+(\.\d+)?$'
  AND (trim(stg.valor_despesas))::numeric > 0
  -- garante que CNPJ existe em operadoras (join do FK)
  AND EXISTS (SELECT 1 FROM operadoras o WHERE o.cnpj = cnpj_digits.cnpj14);

-- 4) Inserção em despesas_agregadas
INSERT INTO despesas_agregadas (cnpj, uf, razao_social_csv, total_despesas, qtd_registros)
SELECT
  cnpj_digits.cnpj14 AS cnpj,
  CASE WHEN length(trim(stg.uf)) = 2 THEN upper(trim(stg.uf)) ELSE NULL END AS uf,
  trim(stg.razao_social) AS razao_social_csv,
  (trim(stg.total_despesas))::numeric(18,2) AS total_despesas,
  (trim(stg.qtd_registros))::int AS qtd_registros
FROM stg_agregadas stg
CROSS JOIN LATERAL (
  SELECT regexp_replace(coalesce(stg.cnpj,''), '\D', '', 'g') AS cnpj14
) cnpj_digits
WHERE length(cnpj_digits.cnpj14) = 14
  AND NULLIF(trim(stg.razao_social), '') IS NOT NULL
  AND trim(stg.total_despesas) ~ '^\d+(\.\d+)?$'
  AND trim(stg.qtd_registros) ~ '^\d+$'
  AND EXISTS (SELECT 1 FROM operadoras o WHERE o.cnpj = cnpj_digits.cnpj14)
ON CONFLICT (cnpj, uf) DO UPDATE
SET total_despesas = EXCLUDED.total_despesas,
    qtd_registros = EXCLUDED.qtd_registros,
    razao_social_csv = EXCLUDED.razao_social_csv;

COMMIT;
