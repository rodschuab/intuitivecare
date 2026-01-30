package br.com.intuitivecare;

import java.math.BigDecimal;

/*
 * Classe que representa uma despesa normalizada.
 * Independentemente do formato do arquivo original,
 * todos os dados são convertidos para esse modelo.
 *
 * Isso facilita a consolidação e o processamento.
 */
public class Despesa {

    // CNPJ da operadora
    public String cnpj;

    // Razão social da operadora
    public String razaoSocial;

    // Ano de referência do dado
    public int ano;

    // Trimestre de referência
    public int trimestre;

    // Valor da despesa (BigDecimal para evitar erro de precisão)
    public BigDecimal valor;
}