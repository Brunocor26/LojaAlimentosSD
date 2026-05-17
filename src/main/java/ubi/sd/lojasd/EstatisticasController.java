package ubi.sd.lojasd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/estatisticas")
public class EstatisticasController {

    @Autowired
    private VendaRepository vendaRepository;

    @Autowired
    private ItemVendaRepository itemVendaRepository;

    @GetMapping
    public Map<String, Object> getEstatisticas() {
        Map<String, Object> stats = new HashMap<>();

        List<Venda> todasVendas = vendaRepository.findAll();
        List<ItemVenda> todosItens = itemVendaRepository.findAll();

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

        // Produtos Mais e Menos Vendidos
        Map<String, Integer> vendasPorProduto = new HashMap<>();
        for (ItemVenda item : todosItens) {
            String nomeProduto = item.getProduto().getNome();
            vendasPorProduto.put(nomeProduto, vendasPorProduto.getOrDefault(nomeProduto, 0) + item.getQuantidade());
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

        // Melhores Clientes
        Map<String, BigDecimal> valorPorCliente = new HashMap<>();
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
        stats.put("produtosMaisVendidos", topProdutos);
        stats.put("produtosMenosVendidos", bottomProdutos);
        stats.put("melhoresClientes", melhoresClientes);

        return stats;
    }
}
