package br.com.intuitivecare;

/*
 * Normaliza e valida CNPJ.
 */
public class CnpjUtils {

    public static String normalize(String cnpj) {
        if (cnpj == null) return "";
        String digits = cnpj.replaceAll("\\D", "");
        return (digits.length() == 14) ? digits : "";
    }

    public static boolean isValid(String cnpj) {
        cnpj = normalize(cnpj);
        if (cnpj.isEmpty()) return false;

        // evita 00000000000000
        if (cnpj.chars().distinct().count() == 1) return false;

        int d1 = dv(cnpj.substring(0, 12), new int[]{5,4,3,2,9,8,7,6,5,4,3,2});
        int d2 = dv(cnpj.substring(0, 12) + d1, new int[]{6,5,4,3,2,9,8,7,6,5,4,3,2});

        return cnpj.equals(cnpj.substring(0, 12) + d1 + d2);
    }

    private static int dv(String base, int[] pesos) {
        int soma = 0;
        for (int i = 0; i < pesos.length; i++) {
            soma += (base.charAt(i) - '0') * pesos[i];
        }
        int mod = soma % 11;
        return (mod < 2) ? 0 : (11 - mod);
    }
}
