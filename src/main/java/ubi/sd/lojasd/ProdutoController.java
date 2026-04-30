package ubi.sd.lojasd;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicLong;


//The @GetMapping annotation ensures that HTTP GET requests to /produto are mapped to the produto() method.
@RestController
public class ProdutoController {
    
    private static final String prod = "Maçã";
    private final AtomicLong counter = new AtomicLong();

    @GetMapping("/produto")
public Produto produto(@RequestParam(defaultValue = "1") long idProduto) {
    //devolve o produto com o ID exato que foi solicitado na URL
    return new Produto(idProduto, prod, "Fruta", 0.50, 5);
  }
}
