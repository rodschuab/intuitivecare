# Teste 2 – Agregação e Análise das Despesas

## Objetivo
Utilizar o arquivo consolidado gerado no **Teste 1** para realizar a agregação das despesas por operadora e UF, além de validar e classificar os dados.

---

## Entradas
- `consolidado_despesas.csv`
- `Relatorio_cadop.csv`

---

## Processamento
- Leitura do arquivo consolidado
- Validação de CNPJ e valores
- Cruzamento com o CADOP para obtenção da UF
- Agregação por **CNPJ + UF**
- Classificação de registros inválidos e sem correspondência

---

## Saídas
Arquivos gerados em `teste2/data/output/`:
- `despesas_agregadas.csv`
- `cadop_duplicados.csv`
- `invalidos.csv`
- `sem_match.csv`

Formato principal:
CNPJ,RazaoSocial,UF,TotalDespesas,QtdRegistros


---

## Como Executar
- Requisitos: Java 17, Maven
- Executar a classe:
  br.com.intuitivecare.teste2.Teste2Main