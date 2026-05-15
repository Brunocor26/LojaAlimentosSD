package ubi.sd.lojasd;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ProdutoRequest(
        @NotBlank String nome,
        String descricao,
        @Positive double preco,
        @Min(0) int stock,
        String imagemUrl,
        @NotNull Long categoriaId
) {}
