# Teste 3 – Banco de Dados e Análise (PostgreSQL)

## Objetivo
Criar scripts SQL para:
1) Estruturar tabelas (DDL)
2) Importar CSVs gerados nos testes 1 e 2
3) Executar queries analíticas

## Arquivos
- `01_ddl.sql` → criação de tabelas e índices
- `02_import.sql` → staging + importação via COPY + validações
- `03_analytics.sql` → queries analíticas (Q1, Q2, Q3)

## Abordagem (trade-offs)
- **Normalização (Opção B):** separa `operadoras` de `despesas_consolidadas` para reduzir repetição e melhorar manutenção.
- **Valores monetários:** `NUMERIC(18,2)` para evitar erro de precisão.
- **Datas:** `DATE` quando disponível; se formato inválido, grava como `NULL`.

## Importação (UTF-8)
A importação usa tabelas temporárias (staging) como texto para evitar falhas de carga.
Linhas inválidas são descartadas por regras simples (`WHERE` + regex), priorizando consistência dos dados.

## Execução (exemplo)
1) Rode `01_ddl.sql`
2) Ajuste os caminhos dos arquivos no `02_import.sql` e execute os comandos `\copy`
3) Rode `03_analytics.sql`
