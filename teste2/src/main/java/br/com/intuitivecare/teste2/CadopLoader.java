package br.com.intuitivecare.teste2;

import com.opencsv.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;

public class CadopLoader {

    public static class Info {
        public String razao;
        public String uf;

        public Info(String razao, String uf) {
            this.razao = razao;
            this.uf = uf;
        }
    }

    public static Map<String, Info> load(Path file) throws Exception {

        Map<String, Info> map = new HashMap<>();

        try (CSVReader reader = new CSVReaderBuilder(
                new InputStreamReader(new FileInputStream(file.toFile()), StandardCharsets.UTF_8))
                .withCSVParser(new CSVParserBuilder().withSeparator(';').build())
                .build()) {

            String[] header = reader.readNext();
            if (header == null) return map;

            int idxCnpj = index(header, "cnpj");
            int idxRazao = index(header, "razao_social");
            int idxUf = index(header, "uf");

            String[] line;
            while ((line = reader.readNext()) != null) {
                String cnpj = digits(line[idxCnpj]);
                if (cnpj.length() != 14) continue;

                map.putIfAbsent(cnpj,
                        new Info(clean(line[idxRazao]), clean(line[idxUf])));
            }
        }
        return map;
    }

    private static int index(String[] h, String k) {
        for (int i = 0; i < h.length; i++)
            if (h[i].toLowerCase().contains(k)) return i;
        return -1;
    }

    private static String clean(String s) {
        return s == null ? "" : s.replace("\"", "").trim();
    }

    private static String digits(String s) {
        return s == null ? "" : s.replaceAll("\\D", "");
    }
}
