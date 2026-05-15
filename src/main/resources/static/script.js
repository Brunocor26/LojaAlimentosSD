let cartCount = 0;
let cartItems = []; // Array de objetos {produtoId, nome, preco, quantidade}
let allProducts = [];
let currentProduct = null;

// Elementos do DOM
const cartModal = document.getElementById('cart-modal');
const productModal = document.getElementById('product-modal');
const cartButton = document.getElementById('cart-button');
const closeCart = document.getElementById('close-cart');
const closeProduct = document.getElementById('close-product');
const cartItemsList = document.getElementById('cart-items-list');
const cartTotalValue = document.getElementById('cart-total-value');
const checkoutBtn = document.getElementById('checkout-btn');

// Elementos do Modal de Produto
const modalProductImg = document.getElementById('modal-product-img');
const modalProductName = document.getElementById('modal-product-name');
const modalProductPrice = document.getElementById('modal-product-price');
const modalProductStock = document.getElementById('modal-product-stock');
const productQuantityInput = document.getElementById('product-quantity');
const qtyMinus = document.getElementById('qty-minus');
const qtyPlus = document.getElementById('qty-plus');
const addToCartBtn = document.getElementById('add-to-cart-btn');

// Função para carregar produtos
async function carregarProdutos() {
    console.log("Iniciando carregamento de produtos...");
    try {
        const resposta = await fetch('/api/produtos');
        console.log("Resposta da API recebida:", resposta.status);
        allProducts = await resposta.json();
        console.log("Produtos carregados:", allProducts.length);

        const grid = document.getElementById('grid-produtos');
        if (!grid) {
            console.error("ERRO: Elemento 'grid-produtos' não encontrado!");
            return;
        }
        grid.innerHTML = '';
allProducts.forEach(produto => {
    console.log("Renderizando produto:", produto.nome);
    const artigo = document.createElement('article');
    artigo.className = 'product';
            if (produto.stock <= 0) {
                artigo.classList.add('out-of-stock');
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
    } catch (erro) {
        console.error("Erro ao carregar produtos:", erro);
    }
}

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
    productQuantityInput.value = 1;
    productModal.style.display = "block";
}

qtyMinus.onclick = () => {
    let val = parseInt(productQuantityInput.value);
    if (val > 1) productQuantityInput.value = val - 1;
};

qtyPlus.onclick = () => {
    let val = parseInt(productQuantityInput.value);
    if (val < currentProduct.stock) productQuantityInput.value = val + 1;
};

addToCartBtn.onclick = () => {
    const qtd = parseInt(productQuantityInput.value);
    
    // Verificar se já existe no carrinho para validar stock total
    const itemExistente = cartItems.find(item => item.produtoId === currentProduct.id);
    const qtdNoCarrinho = itemExistente ? itemExistente.quantidade : 0;

    if (qtd + qtdNoCarrinho > currentProduct.stock) {
        alert("Não há stock suficiente!");
        return;
    }

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
    updateCartUI();
    productModal.style.display = "none";
};

function updateCartUI() {
    document.getElementById('cart-count').innerText = cartCount;
    
    // Garantir que mostramos o conteúdo normal do carrinho (caso estivesse em modo sucesso)
    const modalContent = cartModal.querySelector('.modal-content');
    modalContent.innerHTML = `
        <span class="close" id="close-cart">&times;</span>
        <h2>O Teu Carrinho</h2>
        <div id="cart-items-list"></div>
        <div class="cart-total">
            Total: <span id="cart-total-value">0.00</span>€
        </div>
        <button id="checkout-btn" class="checkout-button">Finalizar Compra</button>
    `;

    // Re-atribuir eventos e referências pois o HTML foi resetado
    document.getElementById('close-cart').onclick = () => cartModal.style.display = "none";
    const newCheckoutBtn = document.getElementById('checkout-btn');
    newCheckoutBtn.onclick = finalizarCompra;

    const listContainer = document.getElementById('cart-items-list');
    const totalSpan = document.getElementById('cart-total-value');
    
    let total = 0;
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
                    <button onclick="changeQuantityInCart(${item.produtoId}, -1)">-</button>
                    <span>${item.quantidade}</span>
                    <button onclick="changeQuantityInCart(${item.produtoId}, 1)">+</button>
                </div>
                <button class="remove-item" onclick="removeFromCart(${item.produtoId})">Remover</button>
            </div>
        `;
        listContainer.appendChild(itemDiv);
        total += item.preco * item.quantidade;
    });

    totalSpan.innerText = total.toFixed(2);
}

function changeQuantityInCart(produtoId, delta) {
    const item = cartItems.find(i => i.produtoId === produtoId);
    if (!item) return;

    const produtoOriginal = allProducts.find(p => p.id === produtoId);
    
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

function removeFromCart(produtoId) {
    const itemIndex = cartItems.findIndex(i => i.produtoId === produtoId);
    if (itemIndex > -1) {
        cartCount -= cartItems[itemIndex].quantidade;
        cartItems.splice(itemIndex, 1);
        updateCartUI();
    }
}

// Abrir/Fechar Modais
cartButton.onclick = () => {
    updateCartUI();
    cartModal.style.display = "block";
};
closeProduct.onclick = () => productModal.style.display = "none";

window.onclick = (event) => {
    if (event.target == cartModal) cartModal.style.display = "none";
    if (event.target == productModal) productModal.style.display = "none";
};

// Checkout
async function finalizarCompra() {
    if (cartItems.length === 0) {
        alert("O teu carrinho está vazio!");
        return;
    }

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

        if (resposta.ok) {
            showSuccessState();
            cartItems = [];
            cartCount = 0;
            document.getElementById('cart-count').innerText = "0";
            carregarProdutos(); // Recarregar para atualizar stocks
        } else {
            const erroMsg = await resposta.text();
            alert("Erro na compra: " + erroMsg);
        }
    } catch (erro) {
        console.error("Erro no checkout:", erro);
        alert("Erro ao processar a compra.");
    }
}

function showSuccessState() {
    const modalContent = cartModal.querySelector('.modal-content');
    modalContent.innerHTML = `
        <div class="success-message">
            <div class="success-icon">✓</div>
            <h2>OBRIGADO!</h2>
            <p>A tua compra foi processada com sucesso.</p>
            <button class="checkout-button" onclick="document.getElementById('cart-modal').style.display='none'">CONTINUAR A COMPRAR</button>
        </div>
    `;
}

// Inicializar
carregarProdutos();
