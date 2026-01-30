package br.com.intuitivecare;

import java.net.URI;
import java.net.http.*;
import java.util.*;
import java.util.regex.*;

/*
 * Classe responsável por acessar o site da ANS,
 * identificar os trimestres disponíveis
 * e baixar os arquivos ZIP.
 */
public class AnsService {

    // URL base do repositório de dados da ANS
    private static final String BASE_URL =
            "https://dadosabertos.ans.gov.br/FTP/PDA/";

    // HttpClient nativo do Java (Java 11+)
    private final HttpClient client = HttpClient.newHttpClient();

    /*
     * Busca os últimos 3 trimestres disponíveis no site.
     * Como não existe uma API REST estruturada,
     * é feito parsing simples do HTML usando regex.
     */
    public List<String> ultimosTresTrimestres() throws Exception {

        // Requisição HTTP para a página principal
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .build();

        // Conteúdo HTML da página
        String html = client.send(req,
                HttpResponse.BodyHandlers.ofString()).body();

        // Regex para capturar padrões como 2023/01/
        Pattern p = Pattern.compile("(20\\d{2}/\\d{2}/)");
        Matcher m = p.matcher(html);

        // Set evita duplicação de trimestres
        Set<String> encontrados = new HashSet<>();
        while (m.find()) {
            encontrados.add(m.group(1));
        }

        // Ordena do mais recente para o mais antigo
        // e retorna apenas os 3 últimos
        return encontrados.stream()
                .sorted(Comparator.reverseOrder())
                .limit(3)
                .toList();
    }

    /*
     * Para um trimestre específico, baixa todos os arquivos ZIP.
     * Alguns trimestres possuem mais de um arquivo,
     * então todos são considerados.
     */
    public List<byte[]> baixarZips(String trimestre) throws Exception {

        String url = BASE_URL + trimestre;

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();

        String html = client.send(req,
                HttpResponse.BodyHandlers.ofString()).body();

        // Regex para encontrar links .zip
        Pattern p = Pattern.compile("href=\"([^\"]+\\.zip)\"");
        Matcher m = p.matcher(html);

        List<byte[]> zips = new ArrayList<>();

        while (m.find()) {
            String nomeZip = m.group(1);

            // Requisição para baixar o ZIP
            HttpRequest zipReq = HttpRequest.newBuilder()
                    .uri(URI.create(url + nomeZip))
                    .build();

            // ZIP é tratado como array de bytes
            byte[] bytes = client.send(zipReq,
                    HttpResponse.BodyHandlers.ofByteArray()).body();

            zips.add(bytes);
        }

        return zips;
    }
}