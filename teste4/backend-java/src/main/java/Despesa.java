import java.math.BigDecimal;

public class Despesa {
    public String cnpj;
    public int ano;
    public int trimestre;
    public BigDecimal valor;

    public Despesa(String cnpj, int ano, int trimestre, BigDecimal valor) {
        this.cnpj = cnpj;
        this.ano = ano;
        this.trimestre = trimestre;
        this.valor = valor;
    }
}
