let cartCount = 0; // Quantidade total no carrinho
let cartItems = []; // Array de itens {produtoId, nome, preco, quantidade}
let allProducts = []; // Guarda todos os produtos
let currentProduct = null; // Produto selecionado atualmente

// Referências aos elementos da interface (DOM)
const cartModal = document.getElementById('cart-modal');
const productModal = document.getElementById('product-modal');
const cartButton = document.getElementById('cart-button');
const closeCart = document.getElementById('close-cart');
const closeProduct = document.getElementById('close-product');
const cartItemsList = document.getElementById('cart-items-list');
const cartTotalValue = document.getElementById('cart-total-value');
const checkoutBtn = document.getElementById('checkout-btn');

// Elementos do modal de detalhes do produto
const modalProductImg = document.getElementById('modal-product-img');
const modalProductName = document.getElementById('modal-product-name');
const modalProductPrice = document.getElementById('modal-product-price');
const modalProductStock = document.getElementById('modal-product-stock');
const productQuantityInput = document.getElementById('product-quantity');
const qtyMinus = document.getElementById('qty-minus');
const qtyPlus = document.getElementById('qty-plus');
const addToCartBtn = document.getElementById('add-to-cart-btn');

// Guarda o layout original do carrinho para repor após uma compra
const originalCartHTML = cartModal.querySelector('.modal-content').innerHTML;

let allCategories = []; // Guarda todas as categorias

// Carrega os produtos e categorias da API
async function carregarProdutos() {
    try {
        // Pedidos paralelos para maior rapidez
        const [resCategorias, resProdutos] = await Promise.all([
            fetch('/api/categorias'),
            fetch('/api/produtos')
        ]);
        
        allCategories = await resCategorias.json();
        allProducts = await resProdutos.json();
        
        const container = document.getElementById('categories-container');
        if (!container) return;
        container.innerHTML = '';

        // Cria categoria padrão se não existirem
        if (allCategories.length === 0) {
            allCategories = [{ id: null, nome: 'Todos os Produtos' }];
        }

        allCategories.forEach(categoria => {
            // Agrupa produtos por categoria
            let produtosDaCategoria;
            if (categoria.id === null) {
                produtosDaCategoria = allProducts;
            } else {
                produtosDaCategoria = allProducts.filter(p => p.id_categoria === categoria.id);
            }

            // Ignora categorias vazias
            if (produtosDaCategoria.length === 0) return;

            // Cria secção visual para a categoria
            const section = document.createElement('section');
            section.className = 'category-section';

            const title = document.createElement('h2');
            title.className = 'category-title';
            title.innerText = categoria.nome;
            section.appendChild(title);

            const grid = document.createElement('div');
            grid.className = 'product-grid';

            // Adiciona cada produto à grelha
            produtosDaCategoria.forEach(produto => {
                const artigo = document.createElement('article');
                artigo.className = 'product';
                if (produto.stock <= 0) {
                    artigo.classList.add('out-of-stock'); // Estilo visual sem stock
                }

                artigo.onclick = () => openProductModal(produto);

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

            section.appendChild(grid);
            container.appendChild(section);
        });

    } catch (erro) {
        console.error("Erro ao carregar dados:", erro);
    }
}

// Abre o modal de detalhes de um produto
function openProductModal(produto) {
    if (produto.stock <= 0) {
        alert("Desculpe, este produto está esgotado!");
        return;
    }
    currentProduct = produto;
    modalProductImg.src = produto.imagemUrl;
    modalProductName.innerText = produto.nome;
    modalProductPrice.innerText = produto.preco.toFixed(2) + "€";
    modalProductStock.innerText = "STOCK DISPONÍVEL: " + produto.stock;
    productQuantityInput.value = 1; // Reseta a quantidade
    productModal.style.display = "block";
}

// Lógica de diminuir quantidade no modal
if (qtyMinus) {
    qtyMinus.onclick = () => {
        let val = parseInt(productQuantityInput.value);
        if (val > 1) productQuantityInput.value = val - 1;
    };
}

// Lógica de aumentar quantidade no modal
if (qtyPlus) {
    qtyPlus.onclick = () => {
        let val = parseInt(productQuantityInput.value);
        if (val < currentProduct.stock) productQuantityInput.value = val + 1;
    };
}

// Adiciona o produto selecionado ao carrinho
if (addToCartBtn) {
    addToCartBtn.onclick = () => {
        const qtd = parseInt(productQuantityInput.value);
        
        const itemExistente = cartItems.find(item => item.produtoId === currentProduct.id);
        const qtdNoCarrinho = itemExistente ? itemExistente.quantidade : 0;

        // Valida se o stock total é suficiente
        if (qtd + qtdNoCarrinho > currentProduct.stock) {
            alert("Não há stock suficiente!");
            return;
        }

        // Atualiza quantidade ou insere novo item
        if (itemExistente) {
            itemExistente.quantidade += qtd;
        } else {
            cartItems.push({
                produtoId: currentProduct.id,
                nome: currentProduct.nome,
                preco: currentProduct.preco,
                quantidade: qtd
            });
        }

        cartCount += qtd;
        updateCartCountUI();
        productModal.style.display = "none";
    };
}

// Atualiza o contador de itens visível no botão do carrinho
function updateCartCountUI() {
    const countSpan = document.getElementById('cart-count');
    if (countSpan) countSpan.innerText = cartCount;
}

// Refaz a interface gráfica do carrinho de compras
function updateCartUI() {
    updateCartCountUI();
    
    const modalContent = cartModal.querySelector('.modal-content');
    
    // Repõe o layout do carrinho caso esteja na mensagem de sucesso
    if (modalContent.querySelector('.success-message')) {
        modalContent.innerHTML = originalCartHTML;
        document.getElementById('close-cart').onclick = () => cartModal.style.display = "none";
        document.getElementById('checkout-btn').onclick = finalizarCompra;
    }

    const listContainer = document.getElementById('cart-items-list');
    const totalSpan = document.getElementById('cart-total-value');
    
    if (!listContainer || !totalSpan) return;

    listContainer.innerHTML = '';
    let total = 0;
    
    // Constrói o HTML para cada item do carrinho
    cartItems.forEach(item => {
        const itemDiv = document.createElement('div');
        itemDiv.className = 'cart-item';
        itemDiv.innerHTML = `
            <div class="cart-item-info">
                <span class="cart-item-name">${item.nome}</span>
                <span class="cart-item-price">${item.preco.toFixed(2)}€</span>
            </div>
            <div class="cart-item-actions">
                <div class="cart-qty-controls">
                    <button class="btn-minus" data-id="${item.produtoId}">-</button>
                    <span>${item.quantidade}</span>
                    <button class="btn-plus" data-id="${item.produtoId}">+</button>
                </div>
                <button class="remove-item" data-id="${item.produtoId}">Remover</button>
            </div>
        `;
        listContainer.appendChild(itemDiv);
        total += item.preco * item.quantidade;
    });

    // Liga os eventos aos botões recém-criados
    listContainer.querySelectorAll('.btn-minus').forEach(btn => {
        btn.onclick = () => changeQuantityInCart(parseInt(btn.dataset.id), -1);
    });
    listContainer.querySelectorAll('.btn-plus').forEach(btn => {
        btn.onclick = () => changeQuantityInCart(parseInt(btn.dataset.id), 1);
    });
    listContainer.querySelectorAll('.remove-item').forEach(btn => {
        btn.onclick = () => removeFromCart(parseInt(btn.dataset.id));
    });

    totalSpan.innerText = total.toFixed(2); // Atualiza o preço total
}

// Altera a quantidade de um item já no carrinho
function changeQuantityInCart(produtoId, delta) {
    const item = cartItems.find(i => i.produtoId === produtoId);
    if (!item) return;

    const produtoOriginal = allProducts.find(p => p.id === produtoId);
    
    // Impede ultrapassar o stock disponível
    if (delta > 0) {
        if (item.quantidade + delta > produtoOriginal.stock) {
            alert("Não há stock suficiente!");
            return;
        }
    }

    item.quantidade += delta;
    cartCount += delta;

    if (item.quantidade <= 0) {
        removeFromCart(produtoId);
    } else {
        updateCartUI();
    }
}

// Remove um item completamente do carrinho
function removeFromCart(produtoId) {
    const itemIndex = cartItems.findIndex(i => i.produtoId === produtoId);
    if (itemIndex > -1) {
        cartCount -= cartItems[itemIndex].quantidade;
        cartItems.splice(itemIndex, 1);
        updateCartUI();
    }
}

// Eventos de abrir e fechar os modais
if (cartButton) {
    cartButton.onclick = () => {
        updateCartUI();
        cartModal.style.display = "block";
    };
}

if (closeCart) closeCart.onclick = () => cartModal.style.display = "none";
if (closeProduct) closeProduct.onclick = () => productModal.style.display = "none";

if (checkoutBtn) checkoutBtn.onclick = finalizarCompra;

// Fecha os modais ao clicar fora da janela
window.onclick = (event) => {
    if (event.target == cartModal) cartModal.style.display = "none";
    if (event.target == productModal) productModal.style.display = "none";
};

// Envia o pedido de compra para a API (Checkout)
async function finalizarCompra() {
    if (cartItems.length === 0) {
        alert("O teu carrinho está vazio!");
        return;
    }

    // Valida se o utilizador tem sessão iniciada
    const utilizador = await verificarAutenticacao();
    if (!utilizador) {
        alert("Deves entrar na tua conta para finalizar a compra.");
        window.location.href = '/login.html';
        return;
    }

    // Prepara a carga (payload) para o backend
    const payload = {
        itens: cartItems.map(item => ({
            produtoId: item.produtoId,
            quantidade: item.quantidade
        }))
    };

    try {
        const resposta = await fetch('/api/vendas/checkout', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(payload)
        });

        // Caso a compra seja bem-sucedida
        if (resposta.ok) {
            const venda = await resposta.json();
            showSuccessState(venda.id);
            cartItems = [];
            cartCount = 0;
            updateCartCountUI();
            carregarProdutos(); // Atualiza os stocks na grelha
        } else {
            const erroMsg = await resposta.text();
            alert("Erro na compra: " + erroMsg);
        }
    } catch (erro) {
        console.error("Erro no checkout:", erro);
        alert("Erro ao processar a compra.");
    }
}

// Substitui o conteúdo do carrinho pela mensagem de sucesso com link para a fatura
function showSuccessState(vendaId) {
    const modalContent = cartModal.querySelector('.modal-content');
    modalContent.innerHTML = `
        <div class="success-message">
            <div class="success-icon">✓</div>
            <h2>OBRIGADO!</h2>
            <p>A tua compra foi processada com sucesso.</p>
            ${vendaId ? `
            <div style="margin: 20px 0;">
                <a href="/api/vendas/${vendaId}/fatura" target="_blank" style="display: inline-block; padding: 10px 20px; background-color: #2196F3; color: white; text-decoration: none; border-radius: 4px; font-weight: bold;">
                    📄 Ver/Imprimir Fatura
                </a>
            </div>` : ''}
            <button class="checkout-button" id="btn-continuar">CONTINUAR A COMPRAR</button>
        </div>
    `;
    document.getElementById('btn-continuar').onclick = () => cartModal.style.display = 'none';
}

// Inicia o programa carregando a loja
carregarProdutos();
