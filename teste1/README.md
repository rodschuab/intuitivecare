# Teste 1 – Processamento de Dados da ANS

## Objetivo
Realizar o download automático dos arquivos públicos da ANS referentes às demonstrações contábeis, processar os **3 últimos trimestres disponíveis**, cruzar os dados com o **CADOP** e gerar um arquivo consolidado de despesas.

---

## Entradas
- Demonstrações contábeis da ANS (download automático)
- `Relatorio_cadop.csv`

---

## Processamento
- Identificação automática dos 3 últimos trimestres
- Download e extração dos arquivos ZIP
- Leitura dos arquivos CSV
- Cruzamento por **REG_ANS** com o CADOP
- Consolidação das despesas

---

## Saídas
Arquivos gerados em `teste2/data/input/`:
- `consolidado_despesas.csv`
- `consolidado_despesas.zip`

Formato do CSV:
CNPJ,RazaoSocial,Ano,Trimestre,ValorDespesas


---

## Como Executar
- Requisitos: Java 17, Maven
- Executar a classe:
  br.com.intuitivecare.Main

