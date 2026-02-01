package br.com.intuitivecare;

import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.nio.file.*;
import java.util.*;

/*
 * TESTE 1 FINAL:
 * - usa demonstracoes_contabeis (REG_ANS)
 * - join com CADOP por REG_ANS (REGISTRO_OPERADORA)
 * - gera consolidado em teste2/data/input para o Teste 2 consumir
 */
public class Main {

    public static void main(String[] args) throws Exception {

        AnsService ans = new AnsService();

        Path base = Path.of("teste2/data");
        Path temp = base.resolve("temp");
        Path cadopFile = base.resolve("input/Relatorio_cadop.csv");

        // ✅ consolidado vai para input (para o Teste 2)
        Path consolidadoOut = base.resolve("input/consolidado_despesas.csv");
        Path zipOut = base.resolve("input/consolidado_despesas.zip");

        Files.createDirectories(temp);
        Files.createDirectories(consolidadoOut.getParent());

        // Carrega CADOP uma vez
        Map<String, CadopUtils.CadopBasico> cadopPorRegAns =
                CadopUtils.carregarPorRegistroAns(cadopFile);

        System.out.println("CADOP carregado (por REG_ANS): " + cadopPorRegAns.size());

        List<Despesa> despesas = new ArrayList<>();

        List<AnsService.Periodo> periodos = ans.ultimosTresTrimestres();
        System.out.println("Períodos encontrados: " + periodos.size());
        for (AnsService.Periodo p : periodos) System.out.println(" - " + p);

        for (AnsService.Periodo p : periodos) {

            System.out.println("\n=== PROCESSANDO: " + p + " ===");

            limparDiretorio(temp);

            byte[] zipBytes = ans.baixarZip(p);
            ZipUtils.extrair(zipBytes, temp);

            List<Path> arquivos = Files.walk(temp)
                    .filter(Files::isRegularFile)
                    .toList();

            System.out.println("Arquivos extraídos: " + arquivos.size());

            for (Path arq : arquivos) {
                String name = arq.getFileName().toString().toLowerCase();
                if (name.endsWith(".csv") || name.endsWith(".txt")) {
                    List<Despesa> lidas = CsvUtils.lerDemonstracaoContabil(arq, p.ano, p.trimestre, cadopPorRegAns);
                    System.out.println(" + " + arq.getFileName() + " -> " + lidas.size());
                    despesas.addAll(lidas);
                }
            }

            System.out.println("TOTAL acumulado até agora: " + despesas.size());
        }

        System.out.println("\nDespesas consolidadas: " + despesas.size());

        // escreve consolidado
        try (CSVWriter writer = new CSVWriter(new FileWriter(consolidadoOut.toFile()))) {
            writer.writeNext(new String[]{"CNPJ", "RazaoSocial", "Ano", "Trimestre", "ValorDespesas"});

            for (Despesa d : despesas) {
                writer.writeNext(new String[]{
                        d.cnpj,
                        d.razaoSocial,
                        String.valueOf(d.ano),
                        String.valueOf(d.trimestre),
                        d.valor.toPlainString()
                });
            }
        }

        ZipUtils.compactar(consolidadoOut, zipOut);

        System.out.println("Consolidado gerado em: " + consolidadoOut.toAbsolutePath());
        System.out.println("ZIP gerado em: " + zipOut.toAbsolutePath());
        System.out.println("Teste 1 finalizado com sucesso.");
    }

    private static void limparDiretorio(Path dir) {
        try {
            if (!Files.exists(dir)) return;

            Files.walk(dir)
                    .sorted(Comparator.reverseOrder())
                    .filter(p -> !p.equals(dir))
                    .forEach(p -> {
                        try { Files.deleteIfExists(p); } catch (Exception ignored) {}
                    });

        } catch (Exception ignored) {}
    }
}
