package br.com.intuitivecare.teste2;

import com.opencsv.*;
import java.io.*;
import java.math.BigDecimal;
import java.nio.file.*;
import java.util.*;

public class Teste2Main {

    public static void main(String[] args) throws Exception {

        Path base = Path.of("teste2/data");
        Path input = base.resolve("input");
        Path output = base.resolve("output");

        Files.createDirectories(output);

        Path consolidado = input.resolve("consolidado_despesas.csv");
        Path cadop = input.resolve("Relatorio_cadop.csv");

        Map<String, CadopLoader.Info> cadopMap = CadopLoader.load(cadop);
        Map<String, Agregado> grupos = new HashMap<>();

        List<String[]> semMatch = new ArrayList<>();
        List<String[]> invalidos = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new FileReader(consolidado.toFile()))) {

            String[] header = reader.readNext();
            String[] l;

            while ((l = reader.readNext()) != null) {

                String cnpj = l[0].replaceAll("\\D", "");
                String razao = l[1];
                BigDecimal valor;

                try {
                    valor = new BigDecimal(l[4]);
                } catch (Exception e) {
                    invalidos.add(l);
                    continue;
                }

                if (cnpj.length() != 14 || valor.compareTo(BigDecimal.ZERO) <= 0) {
                    invalidos.add(l);
                    continue;
                }

                CadopLoader.Info info = cadopMap.get(cnpj);
                if (info == null) {
                    semMatch.add(l);
                    continue;
                }

                String key = cnpj + "|" + info.uf;
                Agregado ag = grupos.computeIfAbsent(key, k -> {
                    Agregado a = new Agregado();
                    a.cnpj = cnpj;
                    a.razao = razao;
                    a.uf = info.uf;
                    return a;
                });

                ag.add(valor);
            }
        }

        // 🔹 despesas_agregadas.csv
        try (CSVWriter w = new CSVWriter(new FileWriter(output.resolve("despesas_agregadas.csv").toFile()))) {
            w.writeNext(new String[]{"CNPJ", "RazaoSocial", "UF", "TotalDespesas", "QtdRegistros"});
            grupos.values().stream()
                    .sorted((a,b) -> b.total.compareTo(a.total))
                    .forEach(a -> w.writeNext(new String[]{
                            a.cnpj, a.razao, a.uf,
                            a.total.toPlainString(),
                            String.valueOf(a.registros)
                    }));
        }

        // 🔹 sem_match.csv
        writeSimple(output.resolve("sem_match.csv"), header(consolidado), semMatch);

        // 🔹 invalidos.csv
        writeSimple(output.resolve("invalidos.csv"), header(consolidado), invalidos);

        System.out.println("Teste 2 finalizado com sucesso.");
    }

    private static void writeSimple(Path file, String[] header, List<String[]> data) throws Exception {
        try (CSVWriter w = new CSVWriter(new FileWriter(file.toFile()))) {
            w.writeNext(header);
            for (String[] l : data) w.writeNext(l);
        }
    }

    private static String[] header(Path csv) throws Exception {
        try (CSVReader r = new CSVReader(new FileReader(csv.toFile()))) {
            return r.readNext();
        }
    }
}
