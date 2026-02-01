package br.com.intuitivecare.teste2;

import java.math.BigDecimal;

public class Agregado {
    public String cnpj;
    public String razao;
    public String uf;
    public BigDecimal total = BigDecimal.ZERO;
    public int registros = 0;

    public void add(BigDecimal v) {
        total = total.add(v);
        registros++;
    }
}
