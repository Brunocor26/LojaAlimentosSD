package ubi.sd.lojasd.repository;
import ubi.sd.lojasd.model.Produto;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {
}