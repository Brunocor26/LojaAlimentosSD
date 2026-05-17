-- Inserir categorias se não existirem
INSERT IGNORE INTO categoria (nome, descricao) VALUES ('Frutas', 'Frutas frescas e deliciosas');
INSERT IGNORE INTO categoria (nome, descricao) VALUES ('Vegetais', 'Vegetais frescos da horta');

-- Obter ID da categoria Frutas
SET @categoria_frutas_id = (SELECT id FROM categoria WHERE nome = 'Frutas' LIMIT 1);

-- Inserir produtos se não existirem
-- Nota: Como o produto não tem uma chave única clara no schema JPA fornecido, 
-- vamos verificar pelo nome antes de inserir para evitar duplicados se o script for corrido várias vezes.

INSERT INTO produto (nome, descricao, preco, stock, imagem_url, categoria_id)
SELECT 'Maçã', 'Maçã vermelha sumarenta', 0.50, 100, 'img/apple.png', @categoria_frutas_id
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM produto WHERE nome = 'Maçã');

INSERT INTO produto (nome, descricao, preco, stock, imagem_url, categoria_id)
SELECT 'Banana', 'Banana da Madeira', 0.30, 150, 'img/banana.png', @categoria_frutas_id
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM produto WHERE nome = 'Banana');

INSERT INTO produto (nome, descricao, preco, stock, imagem_url, categoria_id)
SELECT 'Morango', 'Morangos doces', 2.50, 50, 'img/strawberry.png', @categoria_frutas_id
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM produto WHERE nome = 'Morango');
