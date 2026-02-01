package br.com.intuitivecare;

import java.net.URI;
import java.net.http.*;
import java.util.*;
import java.util.regex.*;

/*
 * Busca e baixa as demonstrações contábeis da ANS.
 * Estrutura:
 * https://dadosabertos.ans.gov.br/FTP/PDA/demonstracoes_contabeis/
 * 2025/  -> 1T2025.zip, 2T2025.zip, 3T2025.zip, 4T2025.zip
 */
public class AnsService {

    private static final String BASE_URL =
            "https://dadosabertos.ans.gov.br/FTP/PDA/demonstracoes_contabeis/";

    private final HttpClient client = HttpClient.newHttpClient();

    public static class Periodo {
        public final int ano;
        public final int trimestre;
        public final String zipName;

        public Periodo(int ano, int trimestre, String zipName) {
            this.ano = ano;
            this.trimestre = trimestre;
            this.zipName = zipName;
        }

        @Override
        public String toString() {
            return trimestre + "T" + ano + " (" + zipName + ")";
        }
    }

    /*
     * Descobre os 3 últimos zips disponíveis (ano desc, trimestre desc).
     */
    public List<Periodo> ultimosTresTrimestres() throws Exception {

        String htmlBase = getHtml(BASE_URL);

        // anos: href="2025/"
        Pattern pAno = Pattern.compile("href=\"(20\\d{2})/\"");
        Matcher mAno = pAno.matcher(htmlBase);

        Set<Integer> anos = new HashSet<>();
        while (mAno.find()) {
            anos.add(Integer.parseInt(mAno.group(1)));
        }

        List<Integer> anosOrdenados = anos.stream()
                .sorted(Comparator.reverseOrder())
                .toList();

        List<Periodo> periodos = new ArrayList<>();

        // pega zips dos anos mais recentes até ter bastante para ordenar e pegar 3
        for (int ano : anosOrdenados) {
            String urlAno = BASE_URL + ano + "/";
            String htmlAno = getHtml(urlAno);

            Pattern pZip = Pattern.compile("href=\"([1-4]T" + ano + "\\.zip)\"");
            Matcher mZip = pZip.matcher(htmlAno);

            while (mZip.find()) {
                String zipName = mZip.group(1);
                int trimestre = Character.getNumericValue(zipName.charAt(0));
                periodos.add(new Periodo(ano, trimestre, zipName));
            }

            if (periodos.size() >= 8) break;
        }

        return periodos.stream()
                .sorted((a, b) -> {
                    if (a.ano != b.ano) return Integer.compare(b.ano, a.ano);
                    return Integer.compare(b.trimestre, a.trimestre);
                })
                .limit(3)
                .toList();
    }

    /*
     * Baixa o zip do período.
     */
    public byte[] baixarZip(Periodo p) throws Exception {
        String url = BASE_URL + p.ano + "/" + p.zipName;

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();

        return client.send(req, HttpResponse.BodyHandlers.ofByteArray()).body();
    }

    private String getHtml(String url) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();

        return client.send(req, HttpResponse.BodyHandlers.ofString()).body();
    }
}
