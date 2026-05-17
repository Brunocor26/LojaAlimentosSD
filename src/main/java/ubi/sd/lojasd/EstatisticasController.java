package ubi.sd.lojasd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@RestController
@RequestMapping("/api/estatisticas")
public class EstatisticasController {

    @Autowired
    private VendaRepository vendaRepository;

    @Autowired
    private ItemVendaRepository itemVendaRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @GetMapping
    public Map<String, Object> getEstatisticas() {
        Map<String, Object> stats = new HashMap<>();

        List<Venda> todasVendas = vendaRepository.findAll();
        List<ItemVenda> todosItens = itemVendaRepository.findAll();
        List<Produto> todosProdutos = produtoRepository.findAll();
        List<Cliente> todosClientes = clienteRepository.findAll();
        List<Categoria> todasCategorias = categoriaRepository.findAll();

        Map<Long, String> mapCategorias = new HashMap<>();
        for (Categoria c : todasCategorias) {
            mapCategorias.put(c.getId(), c.getNome());
        }

        // Valor Faturado
        BigDecimal faturadoDia = BigDecimal.ZERO;
        BigDecimal faturadoSemana = BigDecimal.ZERO;
        BigDecimal faturadoMes = BigDecimal.ZERO;
        BigDecimal faturadoTotal = BigDecimal.ZERO;

        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime inicioDia = agora.toLocalDate().atStartOfDay();
        LocalDateTime inicioSemana = agora.minusDays(7);
        LocalDateTime inicioMes = agora.minusDays(30);

        for (Venda v : todasVendas) {
            BigDecimal valor = v.getValorTotal() != null ? v.getValorTotal() : BigDecimal.ZERO;
            faturadoTotal = faturadoTotal.add(valor);

            if (v.getDataVenda().isAfter(inicioDia)) {
                faturadoDia = faturadoDia.add(valor);
            }
            if (v.getDataVenda().isAfter(inicioSemana)) {
                faturadoSemana = faturadoSemana.add(valor);
            }
            if (v.getDataVenda().isAfter(inicioMes)) {
                faturadoMes = faturadoMes.add(valor);
            }
        }

        // TICKET MEDIO
        BigDecimal ticketMedio = todasVendas.isEmpty() ? BigDecimal.ZERO : faturadoTotal.divide(new BigDecimal(todasVendas.size()), 2, RoundingMode.HALF_UP);

        // VENDAS POR CATEGORIA & Produtos Mais/Menos Vendidos
        Map<String, Integer> vendasPorProduto = new HashMap<>();
        Map<String, BigDecimal> vendasPorCategoria = new HashMap<>();

        for (ItemVenda item : todosItens) {
            String nomeProduto = item.getProduto().getNome();
            vendasPorProduto.put(nomeProduto, vendasPorProduto.getOrDefault(nomeProduto, 0) + item.getQuantidade());

            Long catId = item.getProduto().getId_categoria();
            String nomeCategoria = catId != null ? mapCategorias.getOrDefault(catId, "Outros") : "Outros";

            BigDecimal subtotal = BigDecimal.valueOf(item.getProduto().getPreco()).multiply(BigDecimal.valueOf(item.getQuantidade()));
            vendasPorCategoria.put(nomeCategoria, vendasPorCategoria.getOrDefault(nomeCategoria, BigDecimal.ZERO).add(subtotal));
        }

        List<Map.Entry<String, BigDecimal>> catOrdenadas = new ArrayList<>(vendasPorCategoria.entrySet());
        catOrdenadas.sort(Map.Entry.<String, BigDecimal>comparingByValue().reversed());
        List<Map<String, Object>> vendasCategoriaList = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : catOrdenadas) {
            Map<String, Object> map = new HashMap<>();
            map.put("categoria", entry.getKey());
            map.put("totalVendido", entry.getValue());
            vendasCategoriaList.add(map);
        }

        List<Map.Entry<String, Integer>> produtosOrdenados = new ArrayList<>(vendasPorProduto.entrySet());
        produtosOrdenados.sort(Map.Entry.<String, Integer>comparingByValue().reversed());

        List<Map<String, Object>> topProdutos = new ArrayList<>();
        List<Map<String, Object>> bottomProdutos = new ArrayList<>();
        
        for (int i = 0; i < Math.min(5, produtosOrdenados.size()); i++) {
            Map<String, Object> p = new HashMap<>();
            p.put("nome", produtosOrdenados.get(i).getKey());
            p.put("quantidade", produtosOrdenados.get(i).getValue());
            topProdutos.add(p);
        }

        for (int i = produtosOrdenados.size() - 1; i >= Math.max(0, produtosOrdenados.size() - 5); i--) {
            Map<String, Object> p = new HashMap<>();
            p.put("nome", produtosOrdenados.get(i).getKey());
            p.put("quantidade", produtosOrdenados.get(i).getValue());
            bottomProdutos.add(p);
        }

        // ALERTA DE STOCK CRÍTICO (<= 5)
        List<Map<String, Object>> stockCritico = new ArrayList<>();
        for (Produto p : todosProdutos) {
            if (p.getStock() <= 5) {
                Map<String, Object> map = new HashMap<>();
                map.put("nome", p.getNome());
                map.put("stock", p.getStock());
                stockCritico.add(map);
            }
        }
        stockCritico.sort((a, b) -> Integer.compare((int)a.get("stock"), (int)b.get("stock")));

        // CLIENTES INATIVOS (> 30 dias) & Melhores Clientes
        Map<String, BigDecimal> valorPorCliente = new HashMap<>();
        List<Map<String, Object>> clientesInativos = new ArrayList<>();

        for (Cliente c : todosClientes) {
            Optional<Venda> ultimaVenda = todasVendas.stream()
                .filter(v -> v.getCliente().getId().equals(c.getId()))
                .max(Comparator.comparing(Venda::getDataVenda));
            
            if (ultimaVenda.isEmpty() || ultimaVenda.get().getDataVenda().isBefore(inicioMes)) {
                Map<String, Object> map = new HashMap<>();
                map.put("email", c.getEmail());
                map.put("diasInativo", ultimaVenda.isEmpty() ? "Nunca comprou" : ChronoUnit.DAYS.between(ultimaVenda.get().getDataVenda(), agora));
                clientesInativos.add(map);
            }
        }

        for (Venda v : todasVendas) {
            String emailCliente = v.getCliente().getEmail();
            BigDecimal valor = v.getValorTotal() != null ? v.getValorTotal() : BigDecimal.ZERO;
            valorPorCliente.put(emailCliente, valorPorCliente.getOrDefault(emailCliente, BigDecimal.ZERO).add(valor));
        }

        List<Map.Entry<String, BigDecimal>> clientesOrdenados = new ArrayList<>(valorPorCliente.entrySet());
        clientesOrdenados.sort(Map.Entry.<String, BigDecimal>comparingByValue().reversed());

        List<Map<String, Object>> melhoresClientes = new ArrayList<>();
        for (int i = 0; i < Math.min(5, clientesOrdenados.size()); i++) {
            Map<String, Object> c = new HashMap<>();
            c.put("email", clientesOrdenados.get(i).getKey());
            c.put("totalComprado", clientesOrdenados.get(i).getValue());
            melhoresClientes.add(c);
        }

        stats.put("faturadoDia", faturadoDia);
        stats.put("faturadoSemana", faturadoSemana);
        stats.put("faturadoMes", faturadoMes);
        stats.put("faturadoTotal", faturadoTotal);
        stats.put("ticketMedio", ticketMedio);
        stats.put("produtosMaisVendidos", topProdutos);
        stats.put("produtosMenosVendidos", bottomProdutos);
        stats.put("melhoresClientes", melhoresClientes);
        stats.put("vendasPorCategoria", vendasCategoriaList);
        stats.put("stockCritico", stockCritico);
        stats.put("clientesInativos", clientesInativos);

        return stats;
    }
}
