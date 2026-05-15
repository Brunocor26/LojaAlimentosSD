package ubi.sd.lojasd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/vendas")
public class VendaController {

    @Autowired
    private VendaRepository vendaRepository;

    @Autowired
    private ItemVendaRepository itemVendaRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @PostMapping("/checkout")
    @Transactional
    public ResponseEntity<?> checkout(@RequestBody CheckoutRequest request) {
        if (request.getItens() == null || request.getItens().isEmpty()) {
            return ResponseEntity.badRequest().body("Carrinho vazio!");
        }

        // Para este exemplo, vamos buscar o primeiro cliente ou criar um dummy
        Cliente cliente = clienteRepository.findAll().stream().findFirst().orElseGet(() -> {
            Cliente novoCliente = new Cliente("Cliente Padrão", "cliente@exemplo.com");
            return clienteRepository.save(novoCliente);
        });

        Venda venda = new Venda();
        venda.setDataVenda(LocalDateTime.now());
        venda.setCliente(cliente);
        venda.setValorTotal(BigDecimal.ZERO);
        
        // Guardamos primeiro a venda para ter o ID
        final Venda vendaSalva = vendaRepository.save(venda);
        
        List<ItemVenda> itensVenda = new ArrayList<>();
        BigDecimal valorTotal = BigDecimal.ZERO;

        for (CheckoutItem itemReq : request.getItens()) {
            Produto produto = produtoRepository.findById(itemReq.getProdutoId())
                    .orElseThrow(() -> new RuntimeException("Produto não encontrado: " + itemReq.getProdutoId()));

            if (produto.getStock() < itemReq.getQuantidade()) {
                throw new RuntimeException("Stock insuficiente para o produto: " + produto.getNome());
            }

            // Atualiza Stock
            produto.setStock(produto.getStock() - itemReq.getQuantidade());
            produtoRepository.save(produto);

            // Cria ItemVenda
            ItemVenda itemVenda = new ItemVenda();
            itemVenda.setVenda(vendaSalva);
            itemVenda.setProduto(produto);
            itemVenda.setQuantidade(itemReq.getQuantidade());
            
            // Aqui poderíamos guardar o preço unitário se tivéssemos o campo, 
            // mas como foi pedido para remover, não o fazemos.
            
            BigDecimal subtotal = BigDecimal.valueOf(produto.getPreco()).multiply(BigDecimal.valueOf(itemReq.getQuantidade()));
            valorTotal = valorTotal.add(subtotal);
            
            itensVenda.add(itemVenda);
        }

        vendaSalva.setValorTotal(valorTotal);
        vendaSalva.setItens(itensVenda);
        
        itemVendaRepository.saveAll(itensVenda);
        vendaRepository.save(vendaSalva);

        return ResponseEntity.ok(vendaSalva);
    }
}
