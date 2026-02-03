-- PostgreSQL > 10
-- Teste 3.4 - Queries Analíticas

-- Helper: identifica primeiro e último período no dataset
WITH periodos AS (
  SELECT
    MIN(ano * 10 + trimestre) AS p_min,
    MAX(ano * 10 + trimestre) AS p_max
  FROM despesas_consolidadas
)

-- =========================
-- Query 1
-- 5 operadoras com maior crescimento percentual entre primeiro e último trimestre
-- Tratamento: considera apenas operadoras que possuem valores em ambos períodos (p_min e p_max)
-- =========================
, base AS (
  SELECT
    dc.cnpj,
    SUM(CASE WHEN (dc.ano * 10 + dc.trimestre) = (SELECT p_min FROM periodos) THEN dc.valor_despesas ELSE 0 END) AS v_min,
    SUM(CASE WHEN (dc.ano * 10 + dc.trimestre) = (SELECT p_max FROM periodos) THEN dc.valor_despesas ELSE 0 END) AS v_max
  FROM despesas_consolidadas dc
  GROUP BY dc.cnpj
)
SELECT
  b.cnpj,
  o.razao_social,
  b.v_min,
  b.v_max,
  ROUND(((b.v_max - b.v_min) / NULLIF(b.v_min, 0)) * 100, 2) AS crescimento_percentual
FROM base b
JOIN operadoras o ON o.cnpj = b.cnpj
WHERE b.v_min > 0 AND b.v_max > 0
ORDER BY crescimento_percentual DESC
LIMIT 5;


-- =========================
-- Query 2
-- Distribuição por UF: top 5 UFs com maior total.
-- E média por operadora em cada UF (média do total por CNPJ dentro da UF).
-- =========================
WITH totais_por_operadora AS (
  SELECT
    o.uf,
    dc.cnpj,
    SUM(dc.valor_despesas) AS total_operadora
  FROM despesas_consolidadas dc
  JOIN operadoras o ON o.cnpj = dc.cnpj
  WHERE o.uf IS NOT NULL
  GROUP BY o.uf, dc.cnpj
),
totais_por_uf AS (
  SELECT
    uf,
    SUM(total_operadora) AS total_uf,
    AVG(total_operadora) AS media_por_operadora
  FROM totais_por_operadora
  GROUP BY uf
)
SELECT
  uf,
  total_uf,
  ROUND(media_por_operadora, 2) AS media_por_operadora
FROM totais_por_uf
ORDER BY total_uf DESC
LIMIT 5;


-- =========================
-- Query 3
-- Quantas operadoras ficaram acima da média geral em pelo menos 2 dos 3 trimestres analisados?
--
-- Abordagem escolhida (trade-off):
-- 1) calcula média geral por trimestre (1 linha por trimestre)
-- 2) calcula total por operadora por trimestre
-- 3) conta em quantos trimestres a operadora ficou acima da média do trimestre
-- Justificativa: boa performance (agregações simples + join pequeno), legível e fácil de manter.
-- =========================
WITH periodos_3 AS (
  -- garante os 3 trimestres analisados: pega os 3 maiores períodos do dataset
  SELECT DISTINCT (ano * 10 + trimestre) AS p
  FROM despesas_consolidadas
  ORDER BY p DESC
  LIMIT 3
),
media_por_periodo AS (
  SELECT
    (dc.ano * 10 + dc.trimestre) AS p,
    AVG(dc.valor_despesas) AS media_geral_periodo
  FROM despesas_consolidadas dc
  WHERE (dc.ano * 10 + dc.trimestre) IN (SELECT p FROM periodos_3)
  GROUP BY p
),
total_operadora_periodo AS (
  SELECT
    dc.cnpj,
    (dc.ano * 10 + dc.trimestre) AS p,
    SUM(dc.valor_despesas) AS total_operadora
  FROM despesas_consolidadas dc
  WHERE (dc.ano * 10 + dc.trimestre) IN (SELECT p FROM periodos_3)
  GROUP BY dc.cnpj, p
),
acima_media AS (
  SELECT
    t.cnpj,
    COUNT(*) AS trimestres_acima
  FROM total_operadora_periodo t
  JOIN media_por_periodo m ON m.p = t.p
  WHERE t.total_operadora > m.media_geral_periodo
  GROUP BY t.cnpj
)
SELECT COUNT(*) AS operadoras_acima_media_em_2_ou_mais
FROM acima_media
WHERE trimestres_acima >= 2;
