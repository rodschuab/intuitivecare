package br.com.intuitivecare;

import com.opencsv.*;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReaderBuilder;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.*;

/*
 * Leitor para demonstracoes_contabeis.
 * Header: DATA;REG_ANS;...;VL_SALDO_INICIAL;VL_SALDO_FINAL
 * Faz join por REG_ANS com CADOP -> obtém CNPJ e Razão Social.
 */
public class CsvUtils {

    public static List<Despesa> lerDemonstracaoContabil(
            Path arquivo, int ano, int trimestre,
            Map<String, CadopUtils.CadopBasico> cadopPorRegAns
    ) {

        String first = readFirstLine(arquivo);
        if (first == null) return List.of();

        char sep = detectSeparator(first);

        List<Despesa> a = tryRead(arquivo, ano, trimestre, sep, Charset.forName("UTF-8"), cadopPorRegAns);
        if (!a.isEmpty()) return a;

        return tryRead(arquivo, ano, trimestre, sep, Charset.forName("ISO-8859-1"), cadopPorRegAns);
    }

    private static List<Despesa> tryRead(
            Path arquivo, int ano, int trimestre, char sep, Charset cs,
            Map<String, CadopUtils.CadopBasico> cadopPorRegAns
    ) {

        List<Despesa> lista = new ArrayList<>();

        try (Reader fr = new InputStreamReader(new FileInputStream(arquivo.toFile()), cs);
             CSVReader reader = new CSVReaderBuilder(fr)
                     .withCSVParser(new CSVParserBuilder().withSeparator(sep).build())
                     .build()) {

            String[] header = reader.readNext();
            if (header == null) return lista;

            int idxRegAns = find(header, "reg_ans");
            int idxSaldoFinal = find(header, "vl_saldo_final");

            if (idxRegAns < 0 || idxSaldoFinal < 0) return lista;

            String[] line;
            while ((line = reader.readNext()) != null) {

                String regAns = clean(safe(line, idxRegAns));
                if (regAns.isEmpty()) continue;

                CadopUtils.CadopBasico cad = cadopPorRegAns.get(regAns);
                if (cad == null) continue;

                BigDecimal valor = parseMoney(safe(line, idxSaldoFinal));
                if (valor == null) continue;

                // mantém só > 0 (ajuda no Teste 2)
                if (valor.compareTo(BigDecimal.ZERO) <= 0) continue;

                Despesa d = new Despesa();
                d.cnpj = cad.cnpj;
                d.razaoSocial = cad.razaoSocial;
                d.ano = ano;
                d.trimestre = trimestre;
                d.valor = valor;

                lista.add(d);
            }

        } catch (Exception ignored) {}

        return lista;
    }

    // -------- helpers --------

    private static int find(String[] header, String... keys) {
        for (int i = 0; i < header.length; i++) {
            String h = norm(header[i]);
            for (String k : keys) {
                if (h.contains(norm(k))) return i;
            }
        }
        return -1;
    }

    private static String norm(String s) {
        if (s == null) return "";
        return s.toLowerCase().trim().replace("_", "").replace(" ", "").replace("-", "").replace("\"", "");
    }

    private static String clean(String s) {
        if (s == null) return "";
        return s.replace("\uFEFF", "").replace("\"", "").trim();
    }

    private static String safe(String[] arr, int idx) {
        if (idx < 0 || idx >= arr.length || arr[idx] == null) return "";
        return arr[idx];
    }

    private static String readFirstLine(Path arquivo) {
        try (BufferedReader br = new BufferedReader(new FileReader(arquivo.toFile()))) {
            return br.readLine();
        } catch (Exception e) {
            return null;
        }
    }

    private static char detectSeparator(String line) {
        int commas = 0, semis = 0;
        for (char c : line.toCharArray()) {
            if (c == ',') commas++;
            if (c == ';') semis++;
        }
        return (semis > commas) ? ';' : ',';
    }

    private static BigDecimal parseMoney(String s) {
        if (s == null) return null;

        String t = s.trim().replace(" ", "").replace("\"", "");
        if (t.isEmpty()) return null;

        if (t.contains(",") && t.contains(".")) {
            if (t.lastIndexOf(",") > t.lastIndexOf(".")) {
                t = t.replace(".", "").replace(",", ".");
            } else {
                t = t.replace(",", "");
            }
        } else if (t.contains(",") && !t.contains(".")) {
            t = t.replace(",", ".");
        }

        try { return new BigDecimal(t); }
        catch (Exception e) { return null; }
    }
}
