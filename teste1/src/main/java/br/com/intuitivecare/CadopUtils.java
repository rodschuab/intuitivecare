package br.com.intuitivecare;

import com.opencsv.*;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReaderBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;

/*
 * Lê Relatorio_cadop.csv (separador ';') e cria Map por REGISTRO_OPERADORA (REG_ANS).
 * Robusto contra BOM e aspas.
 */
public class CadopUtils {

    public static class CadopBasico {
        public String cnpj;
        public String razaoSocial;

        public CadopBasico(String cnpj, String razaoSocial) {
            this.cnpj = cnpj;
            this.razaoSocial = razaoSocial;
        }
    }

    public static Map<String, CadopBasico> carregarPorRegistroAns(Path cadopFile) {

        Map<String, CadopBasico> map = new HashMap<>();

        try (Reader fr = new InputStreamReader(new FileInputStream(cadopFile.toFile()), StandardCharsets.UTF_8);
             CSVReader reader = new CSVReaderBuilder(fr)
                     .withCSVParser(new CSVParserBuilder().withSeparator(';').build())
                     .build()) {

            String[] header = reader.readNext();
            if (header == null) return map;

            int idxReg = index(header, "registro_operadora");
            int idxCnpj = index(header, "cnpj");
            int idxRazao = index(header, "razao_social", "razao social", "razao");

            if (idxReg < 0 || idxCnpj < 0 || idxRazao < 0) return map;

            String[] line;
            while ((line = reader.readNext()) != null) {

                String regAns = clean(safe(line, idxReg));
                if (regAns.isEmpty()) continue;

                String cnpj = CnpjUtils.normalize(clean(safe(line, idxCnpj)));
                String razao = clean(safe(line, idxRazao));

                if (cnpj.isEmpty() || razao.isEmpty()) continue;

                map.putIfAbsent(regAns, new CadopBasico(cnpj, razao));
            }

        } catch (Exception e) {
            System.out.println("Erro lendo CADOP: " + e.getMessage());
        }

        return map;
    }

    private static int index(String[] header, String... nomes) {
        for (int i = 0; i < header.length; i++) {
            String h = norm(header[i]);
            for (String n : nomes) {
                if (h.contains(norm(n))) return i;
            }
        }
        return -1;
    }

    private static String norm(String s) {
        if (s == null) return "";
        s = s.replace("\uFEFF", ""); // BOM
        return s.toLowerCase().trim()
                .replace("_", "")
                .replace(" ", "")
                .replace("-", "")
                .replace("\"", "");
    }

    private static String clean(String s) {
        if (s == null) return "";
        return s.replace("\uFEFF", "").replace("\"", "").trim();
    }

    private static String safe(String[] arr, int idx) {
        if (idx < 0 || idx >= arr.length || arr[idx] == null) return "";
        return arr[idx];
    }
}
