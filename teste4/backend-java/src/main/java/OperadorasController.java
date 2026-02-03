import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class OperadorasController {

    private final DataService service;

    public OperadorasController(DataService service) {
        this.service = service;
    }

    @GetMapping("/operadoras")
    public Map<String, Object> listar(
            @RequestParam(defaultValue="1") int page,
            @RequestParam(defaultValue="10") int limit,
            @RequestParam(required = false) String search
    ) {
        return service.listarOperadoras(page, limit, search);
    }

    @GetMapping("/operadoras/{cnpj}")
    public Operadora detalhes(@PathVariable String cnpj) {
        Operadora o = service.getOperadora(cnpj);
        if (o == null) throw new RuntimeException("Operadora não encontrada");
        return o;
    }

    @GetMapping("/operadoras/{cnpj}/despesas")
    public List<Despesa> despesas(@PathVariable String cnpj) {
        return service.getDespesas(cnpj);
    }

    @GetMapping("/estatisticas")
    public Map<String, Object> estatisticas() {
        return service.getEstatisticas();
    }
}
