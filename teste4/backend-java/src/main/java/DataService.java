import com.opencsv.CSVReader;
import org.springframework.stereotype.Service;

import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DataService {

    private final Map<String, Operadora> operadorasByCnpj = new HashMap<>();
    private final Map<String, List<Despesa>> despesasByCnpj = new HashMap<>();

    public DataService() {
        carregarOperadoras();
        carregarDespesasConsolidadas();
    }

    public Map<String, Object> listarOperadoras(int page, int limit, String search) {
        List<Operadora> lista = new ArrayList<>(operadorasByCnpj.values());

        if (search != null && !search.isBlank()) {
            String s = search.toLowerCase();
            lista = lista.stream()
                    .filter(o -> o.cnpj.contains(s) || o.razaoSocial.toLowerCase().contains(s))
                    .collect(Collectors.toList());
        }

        int total = lista.size();
        int start = Math.max(0, (page - 1) * limit);
        int end = Math.min(total, start + limit);

        List<Operadora> data = start >= total ? List.of() : lista.subList(start, end);

        return Map.of(
                "data", data,
                "page", page,
                "limit", limit,
                "total", total
        );
    }

    public Operadora getOperadora(String cnpj) {
        return operadorasByCnpj.get(apenasDigitos(cnpj));
    }

    public List<Despesa> getDespesas(String cnpj) {
        return despesasByCnpj.getOrDefault(apenasDigitos(cnpj), List.of());
    }

    public Map<String, Object> getEstatisticas() {
        BigDecimal total = BigDecimal.ZERO;
        long count = 0;

        Map<String, BigDecimal> totalPorOperadora = new HashMap<>();

        for (var entry : despesasByCnpj.entrySet()) {
            BigDecimal somaOp = entry.getValue().stream()
                    .map(d -> d.valor)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            totalPorOperadora.put(entry.getKey(), somaOp);
            total = total.add(somaOp);
            count += entry.getValue().size();
        }

        BigDecimal media = (count == 0) ? BigDecimal.ZERO : total.divide(BigDecimal.valueOf(count), 2, BigDecimal.ROUND_HALF_UP);

        // Top 5 operadoras por total
        List<Map<String, Object>> top5 = totalPorOperadora.entrySet().stream()
                .sorted((a,b) -> b.getValue().compareTo(a.getValue()))
                .limit(5)
                .map(e -> {
                    Operadora o = operadorasByCnpj.get(e.getKey());
                    return Map.of(
                            "cnpj", e.getKey(),
                            "razaoSocial", o != null ? o.razaoSocial : "",
                            "total", e.getValue()
                    );
                })
                .toList();

        return Map.of(
                "totalDespesas", total,
                "mediaDespesas", media,
                "top5", top5
        );
    }

    private void carregarOperadoras() {
        try (CSVReader reader = new CSVReader(
                new InputStreamReader(getClass().getResourceAsStream("/data/operadoras.csv")))) {

            String[] header = reader.readNext(); // ignora
            String[] line;

            while ((line = reader.readNext()) != null) {
                String cnpj = apenasDigitos(line[1]); // CADOP: coluna CNPJ
                String razao = line[2];
                String uf = line[10];

                if (cnpj.length() != 14) continue;

                operadorasByCnpj.putIfAbsent(cnpj, new Operadora(cnpj, razao, uf));
            }
        } catch (Exception e) {
            System.out.println("Erro ao carregar operadoras: " + e.getMessage());
        }
    }

    private void carregarDespesasConsolidadas() {
        try (CSVReader reader = new CSVReader(
                new InputStreamReader(getClass().getResourceAsStream("/data/consolidado_despesas.csv")))) {

            reader.readNext(); // header
            String[] line;

            while ((line = reader.readNext()) != null) {
                String cnpj = apenasDigitos(line[0]);
                int ano = Integer.parseInt(line[2]);
                int tri = Integer.parseInt(line[3]);
                BigDecimal valor = new BigDecimal(line[4]);

                if (cnpj.length() != 14) continue;

                despesasByCnpj.computeIfAbsent(cnpj, k -> new ArrayList<>())
                        .add(new Despesa(cnpj, ano, tri, valor));
            }
        } catch (Exception e) {
            System.out.println("Erro ao carregar despesas: " + e.getMessage());
        }
    }

    private String apenasDigitos(String s) {
        return s == null ? "" : s.replaceAll("\\D", "");
    }
}
