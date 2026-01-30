package br.com.intuitivecare;

import br.com.intuitivecare.AnsService;

import java.nio.file.*;
import java.util.*;
import java.io.FileWriter;

/*
 * Classe principal que orquestra todo o fluxo:
 * - Busca trimestres
 * - Baixa ZIPs
 * - Extrai arquivos
 * - Processa CSVs
 * - Gera o CSV consolidado
 */
public class Main {

    public static void main(String[] args) throws Exception {

        AnsService ans = new AnsService();

        // Diretórios temporários e de saída
        Path temp = Path.of("data/temp");
        Path output = Path.of("data/output");

        Files.createDirectories(temp);
        Files.createDirectories(output);

        // Lista final de despesas consolidadas
        List<Despesa> despesas = new ArrayList<>();

        // Para cada um dos 3 últimos trimestres
        for (String t : ans.ultimosTresTrimestres()) {

            int ano = Integer.parseInt(t.substring(0, 4));
            int trimestre = Integer.parseInt(t.substring(5, 7));

            // Baixa e extrai todos os ZIPs
            for (byte[] zip : ans.baixarZips(t)) {
                ZipUtils.extrair(zip, temp);
            }

            // Processa apenas arquivos CSV
            Files.walk(temp)
                    .filter(p -> p.toString().endsWith(".csv"))
                    .forEach(p ->
                            despesas.addAll(
                                    CsvUtils.lerCsv(
                                            p, ano, trimestre)));
        }

        // Gera o CSV consolidado
        Path csvFinal =
                output.resolve("consolidado_despesas.csv");

        try (FileWriter fw =
                     new FileWriter(csvFinal.toFile())) {

            fw.write("CNPJ,RazaoSocial,Ano,Trimestre,ValorDespesas\n");

            for (Despesa d : despesas) {
                fw.write(String.format(
                        "%s,%s,%d,%d,%s\n",
                        d.cnpj,
                        d.razaoSocial,
                        d.ano,
                        d.trimestre,
                        d.valor));
            }
        }

        // Compacta o resultado final
        ZipUtils.compactar(
                csvFinal,
                output.resolve("consolidado_despesas.zip"));

        System.out.println("Teste 1 finalizado com sucesso.");
    }
}