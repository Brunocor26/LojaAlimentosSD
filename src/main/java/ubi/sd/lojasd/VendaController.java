package ubi.sd.lojasd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    public ResponseEntity<?> checkout(@RequestBody CheckoutRequest request, java.security.Principal principal) {
        // Verifica se o carrinho está vazio
        if (request.getItens() == null || request.getItens().isEmpty()) {
            return ResponseEntity.badRequest().body("Carrinho vazio!");
        }

        // Verifica se o utilizador tem sessão iniciada
        if (principal == null) {
            return ResponseEntity.status(401).body("Utilizador não autenticado.");
        }

        try {
            // Obtém o cliente autenticado usando o email
            Cliente cliente = clienteRepository.findByEmail(principal.getName())
                    .orElseThrow(() -> new RuntimeException("Cliente não encontrado."));

            // Inicializa a venda
            Venda venda = new Venda();
            venda.setDataVenda(LocalDateTime.now());
            venda.setCliente(cliente);
            venda.setValorTotal(BigDecimal.ZERO);
            
            // Guarda a venda para gerar um ID que os itens possam referenciar
            final Venda vendaSalva = vendaRepository.save(venda);
            
            List<ItemVenda> itensVenda = new ArrayList<>();
            BigDecimal valorTotal = BigDecimal.ZERO;

            // Processa cada item do carrinho
            for (CheckoutItem itemReq : request.getItens()) {
                Produto produto = produtoRepository.findById(itemReq.getProdutoId())
                        .orElseThrow(() -> new RuntimeException("Produto não encontrado ID: " + itemReq.getProdutoId()));

                // Garante que existe stock suficiente
                if (produto.getStock() < itemReq.getQuantidade()) {
                    throw new RuntimeException("Stock insuficiente para: " + produto.getNome());
                }

                // Atualiza e reduz o stock disponível
                produto.setStock(produto.getStock() - itemReq.getQuantidade());
                produtoRepository.save(produto);

                // Cria a relação entre Produto, Venda e Quantidade
                ItemVenda itemVenda = new ItemVenda();
                itemVenda.setVenda(vendaSalva);
                itemVenda.setProduto(produto);
                itemVenda.setQuantidade(itemReq.getQuantidade());
                
                // Calcula o custo do item e soma ao total
                BigDecimal subtotal = BigDecimal.valueOf(produto.getPreco()).multiply(BigDecimal.valueOf(itemReq.getQuantidade()));
                valorTotal = valorTotal.add(subtotal);
                
                itensVenda.add(itemVenda);
            }

            // Atualiza os dados finais da venda
            vendaSalva.setValorTotal(valorTotal);
            vendaSalva.setItens(itensVenda);
            
            // Guarda os itens e o valor total na base de dados
            itemVendaRepository.saveAll(itensVenda);
            vendaRepository.save(vendaSalva);

            return ResponseEntity.ok(vendaSalva);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}/fatura")
    public ResponseEntity<String> emitirFatura(@PathVariable Long id, java.security.Principal principal) {
        // Acesso restrito a utilizadores autenticados
        if (principal == null) {
            return ResponseEntity.status(401).body("Utilizador não autenticado.");
        }

        try {
            Venda venda = vendaRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Venda não encontrada."));

            // Impede que um utilizador veja faturas de outros (segurança)
            if (!venda.getCliente().getEmail().equals(principal.getName())) {
                return ResponseEntity.status(403).body("Acesso negado.");
            }

            // Constrói a estrutura HTML da fatura
            StringBuilder fatura = new StringBuilder();
            fatura.append("<!DOCTYPE html><html lang='pt'><head><meta charset='UTF-8'><title>Fatura #").append(venda.getId()).append("</title>");
            fatura.append("<link rel='stylesheet' href='/style.css'><style>");
            fatura.append(".fatura-container { max-width: 600px; margin: 50px auto; border: 1px solid #000; padding: 40px; }");
            fatura.append(".fatura-header { text-align: center; margin-bottom: 40px; border-bottom: 2px solid #000; padding-bottom: 20px; }");
            fatura.append(".fatura-header h1 { font-size: 24px; text-transform: uppercase; letter-spacing: 2px; }");
            fatura.append(".fatura-info { margin-bottom: 30px; font-size: 12px; text-transform: uppercase; line-height: 1.6; }");
            fatura.append(".fatura-table { width: 100%; border-collapse: collapse; margin-bottom: 30px; font-size: 12px; }");
            fatura.append(".fatura-table th, .fatura-table td { text-align: left; padding: 10px 0; border-bottom: 1px solid #eee; }");
            fatura.append(".fatura-table th { text-transform: uppercase; color: #666; }");
            fatura.append(".fatura-total { text-align: right; font-size: 16px; font-weight: bold; text-transform: uppercase; margin-top: 20px; border-top: 2px solid #000; padding-top: 20px; }");
            fatura.append("</style></head><body>");
            
            fatura.append("<div class='fatura-container'>");
            fatura.append("<div class='fatura-header'><h1>Fatura / Recibo</h1><p>LOJA SD</p></div>");
            fatura.append("<div class='fatura-info'>");
            fatura.append("<strong>Fatura nº:</strong> ").append(venda.getId()).append("<br>");
            fatura.append("<strong>Data:</strong> ").append(venda.getDataVenda()).append("<br>");
            fatura.append("<strong>Cliente:</strong> ").append(venda.getCliente().getNome()).append("<br>");
            fatura.append("<strong>Email:</strong> ").append(venda.getCliente().getEmail()).append("</div>");
            
            fatura.append("<table class='fatura-table'>");
            fatura.append("<thead><tr><th>Produto</th><th>Qtd</th><th style='text-align:right'>Subtotal</th></tr></thead><tbody>");
            
            // Preenche a tabela com os produtos comprados
            for (ItemVenda item : venda.getItens()) {
                BigDecimal subtotal = BigDecimal.valueOf(item.getProduto().getPreco())
                        .multiply(BigDecimal.valueOf(item.getQuantidade()));
                fatura.append("<tr>");
                fatura.append("<td>").append(item.getProduto().getNome()).append("</td>");
                fatura.append("<td>").append(item.getQuantidade()).append("</td>");
                fatura.append("<td style='text-align:right'>").append(String.format("%.2f€", subtotal)).append("</td>");
                fatura.append("</tr>");
            }
            
            fatura.append("</tbody></table>");
            fatura.append("<div class='fatura-total'>Total a Pagar: ").append(String.format("%.2f€", venda.getValorTotal())).append("</div>");
            fatura.append("</div></body></html>");

            return ResponseEntity.ok()
                    .header("Content-Type", "text/html; charset=UTF-8")
                    .body(fatura.toString());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
