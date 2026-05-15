package ubi.sd.lojasd.controller;
import ubi.sd.lojasd.model.ItemVenda;
import ubi.sd.lojasd.model.Cliente;
import ubi.sd.lojasd.repository.ItemVendaRepository;
import ubi.sd.lojasd.model.Venda;
import ubi.sd.lojasd.repository.ProdutoRepository;
import ubi.sd.lojasd.dto.CheckoutItem;
import ubi.sd.lojasd.dto.CheckoutRequest;
import ubi.sd.lojasd.repository.ClienteRepository;
import ubi.sd.lojasd.repository.VendaRepository;
import ubi.sd.lojasd.model.Produto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<?> checkout(@RequestBody CheckoutRequest request, Principal principal) {
        if (request.getItens() == null || request.getItens().isEmpty()) {
            return ResponseEntity.badRequest().body("Carrinho vazio!");
        }

        if (principal == null) {
            return ResponseEntity.status(401).body("Deves iniciar sessão para finalizar a compra.");
        }

        String email = principal.getName();
        Cliente cliente = clienteRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado para o email: " + email));

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
