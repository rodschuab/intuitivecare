package br.com.intuitivecare;

import com.opencsv.CSVReader;

import java.io.FileReader;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.*;

/*
 * Classe responsável por ler arquivos CSV
 * e transformar os dados em objetos Despesa.
 */
public class CsvUtils {

    /*
     * Lê um CSV e retorna apenas os registros
     * que representam despesas válidas.
     */
    public static List<Despesa> lerCsv(
            Path arquivo, int ano, int trimestre) {

        List<Despesa> lista = new ArrayList<>();

        try (CSVReader reader =
                     new CSVReader(
                             new FileReader(arquivo.toFile()))) {

            // Lê o cabeçalho
            String[] header = reader.readNext();
            if (header == null) return lista;

            // Identifica dinamicamente as colunas
            int cnpj = index(header, "cnpj");
            int razao = index(header, "razao");
            int valor = index(header, "valor");

            // Se faltar alguma coluna, ignora o arquivo
            if (cnpj < 0 || razao < 0 || valor < 0) return lista;

            String[] linha;
            while ((linha = reader.readNext()) != null) {

                BigDecimal v = new BigDecimal(linha[valor]);

                // Valores negativos são descartados
                if (v.compareTo(BigDecimal.ZERO) < 0) continue;

                Despesa d = new Despesa();
                d.cnpj = linha[cnpj];
                d.razaoSocial = linha[razao];
                d.valor = v;
                d.ano = ano;
                d.trimestre = trimestre;

                lista.add(d);
            }
        } catch (Exception e) {
            // Arquivos inválidos são ignorados
        }

        return lista;
    }

    /*
     * Busca o índice de uma coluna pelo nome,
     * permitindo variações no cabeçalho.
     */
    private static int index(String[] header, String nome) {
        for (int i = 0; i < header.length; i++) {
            if (header[i].toLowerCase().contains(nome)) {
                return i;
            }
        }
        return -1;
    }
}