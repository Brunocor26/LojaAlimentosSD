let cartCount = 0;
let cartItems = [];
let allProducts = [];

// Função para carregar produtos da Base de Dados (PostgreSQL via Spring Boot)
async function carregarProdutos() {
    try {
        // Vai ao teu backend buscar os dados
        const resposta = await fetch('/api/produtos');
        allProducts = await resposta.json();
        
        const grid = document.getElementById('grid-produtos');
        grid.innerHTML = ''; // Limpar a grid antes de recarregar

        // Para cada produto que veio do Postgres, cria o HTML
        allProducts.forEach(produto => {
            const artigo = document.createElement('article');
            artigo.className = 'product';
            // Se não houver stock, adicionar uma classe visual ou desativar o clique
            if (produto.stock <= 0) {
                artigo.classList.add('out-of-stock');
            }

            artigo.onclick = () => addToCart(produto.id, produto.stock); // Clicar adiciona ao carrinho

            artigo.innerHTML = `
                <div class="product-image">
                    <img src="${produto.imagemUrl}" alt="${produto.nome}">
                </div>
                <div class="product-info">
                    <div class="product-main-info">
                        <span class="product-name">${produto.nome}</span>
                        <span class="product-price">${produto.preco.toFixed(2)}€</span>
                    </div>
                    <span class="product-stock">${produto.stock > 0 ? `Stock: ${produto.stock} unidades` : 'Sem Stock'}</span>
                </div>
            `;
            
            grid.appendChild(artigo);
        });
    } catch (erro) {
        console.error("Erro ao carregar produtos:", erro);
    }
}

async function addToCart(id, stockAtual) {
    if (stockAtual <= 0) {
        alert("Desculpe, este produto está esgotado!");
        return;
    }

    try {
        const resposta = await fetch(`/api/produtos/${id}/diminuir-stock`, {
            method: 'POST'
        });

        if (resposta.ok) {
            cartCount++;
            document.getElementById('cart-count').innerText = cartCount;
            
            // Adicionar o produto à lista do carrinho (localmente)
            const produto = allProducts.find(p => p.id === id);
            if (produto) {
                cartItems.push(produto);
            }

            // Recarregar os produtos para atualizar o stock na UI
            carregarProdutos();
        } else {
            const erroMsg = await resposta.text();
            alert(erroMsg);
        }
    } catch (erro) {
        console.error("Erro ao adicionar ao carrinho:", erro);
    }
}

// Evento ao clicar no botão do carrinho
document.querySelector('.cart').addEventListener('click', () => {
    if (cartItems.length === 0) {
        alert("O teu carrinho está vazio!");
    } else {
        const listaProdutos = cartItems.map(p => `- ${p.nome}: ${p.preco.toFixed(2)}€`).join('\n');
        const total = cartItems.reduce((sum, p) => sum + p.preco, 0);
        alert(`Produtos no Carrinho:\n${listaProdutos}\n\nTotal: ${total.toFixed(2)}€`);
    }
});

// Corre a função mal a página abre
carregarProdutos();