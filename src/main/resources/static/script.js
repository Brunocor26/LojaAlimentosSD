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

// Guardar o HTML original do carrinho para poder restaurar após sucesso
const originalCartHTML = cartModal.querySelector('.modal-content').innerHTML;

let allCategories = [];

// Função para carregar produtos e categorias
async function carregarProdutos() {
    try {
        // Obter categorias e produtos em paralelo
        const [resCategorias, resProdutos] = await Promise.all([
            fetch('/api/categorias'),
            fetch('/api/produtos')
        ]);
        
        allCategories = await resCategorias.json();
        allProducts = await resProdutos.json();
        
        const container = document.getElementById('categories-container');
        if (!container) return;
        container.innerHTML = '';

        // Se não houver categorias, cria uma categoria "Outros" fictícia
        if (allCategories.length === 0) {
            allCategories = [{ id: null, nome: 'Todos os Produtos' }];
        }

        allCategories.forEach(categoria => {
            // Filtrar produtos que pertencem a esta categoria
            // Considerando que Produto tem id_categoria. Se for null, entra em "Outros"
            let produtosDaCategoria;
            if (categoria.id === null) {
                produtosDaCategoria = allProducts;
            } else {
                produtosDaCategoria = allProducts.filter(p => p.id_categoria === categoria.id);
            }

            if (produtosDaCategoria.length === 0) return; // Não mostrar categorias vazias

            // Criar secção da categoria
            const section = document.createElement('section');
            section.className = 'category-section';

            const title = document.createElement('h2');
            title.className = 'category-title';
            title.innerText = categoria.nome;
            section.appendChild(title);

            const grid = document.createElement('div');
            grid.className = 'product-grid';

            produtosDaCategoria.forEach(produto => {
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

            section.appendChild(grid);
            container.appendChild(section);
        });

    } catch (erro) {
        console.error("Erro ao carregar dados:", erro);
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

if (qtyMinus) {
    qtyMinus.onclick = () => {
        let val = parseInt(productQuantityInput.value);
        if (val > 1) productQuantityInput.value = val - 1;
    };
}

if (qtyPlus) {
    qtyPlus.onclick = () => {
        let val = parseInt(productQuantityInput.value);
        if (val < currentProduct.stock) productQuantityInput.value = val + 1;
    };
}

if (addToCartBtn) {
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
        updateCartCountUI();
        productModal.style.display = "none";
    };
}

function updateCartCountUI() {
    const countSpan = document.getElementById('cart-count');
    if (countSpan) countSpan.innerText = cartCount;
}

function updateCartUI() {
    updateCartCountUI();
    
    const modalContent = cartModal.querySelector('.modal-content');
    
    // Se estivermos na mensagem de sucesso, restauramos o layout
    if (modalContent.querySelector('.success-message')) {
        modalContent.innerHTML = originalCartHTML;
        // Re-atribuir eventos básicos que foram perdidos ao sobrescrever innerHTML
        document.getElementById('close-cart').onclick = () => cartModal.style.display = "none";
        document.getElementById('checkout-btn').onclick = finalizarCompra;
    }

    const listContainer = document.getElementById('cart-items-list');
    const totalSpan = document.getElementById('cart-total-value');
    
    if (!listContainer || !totalSpan) return;

    listContainer.innerHTML = '';
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

    // Re-atribuir eventos aos botões dos itens
    listContainer.querySelectorAll('.btn-minus').forEach(btn => {
        btn.onclick = () => changeQuantityInCart(parseInt(btn.dataset.id), -1);
    });
    listContainer.querySelectorAll('.btn-plus').forEach(btn => {
        btn.onclick = () => changeQuantityInCart(parseInt(btn.dataset.id), 1);
    });
    listContainer.querySelectorAll('.remove-item').forEach(btn => {
        btn.onclick = () => removeFromCart(parseInt(btn.dataset.id));
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
if (cartButton) {
    cartButton.onclick = () => {
        updateCartUI();
        cartModal.style.display = "block";
    };
}

if (closeCart) closeCart.onclick = () => cartModal.style.display = "none";
if (closeProduct) closeProduct.onclick = () => productModal.style.display = "none";

if (checkoutBtn) checkoutBtn.onclick = finalizarCompra;

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

    // Verificar se o utilizador está autenticado antes de finalizar
    const utilizador = await verificarAutenticacao();
    if (!utilizador) {
        alert("Deves entrar na tua conta para finalizar a compra.");
        window.location.href = '/login.html';
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
            updateCartCountUI();
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
            <button class="checkout-button" id="btn-continuar">CONTINUAR A COMPRAR</button>
        </div>
    `;
    document.getElementById('btn-continuar').onclick = () => cartModal.style.display = 'none';
}

// Inicializar
carregarProdutos();
