#!/bin/bash

# Configurações (devem coincidir com o docker-compose.yml)
DB_CONTAINER="lojasd-db"
DB_NAME="lojasd_db"
DB_USER="lojasd_user"
DB_PASS="lojasd_password_local"

echo "A inserir dados iniciais na base de dados..."

# Verificar se o contentor está a correr
if ! docker ps | grep -q "$DB_CONTAINER"; then
    echo "Erro: O contentor $DB_CONTAINER não está a correr."
    echo "Certifique-se de que executou 'docker-compose up -d' primeiro."
    exit 1
fi

# Executar o script SQL dentro do contentor
docker exec -i "$DB_CONTAINER" mariadb -u"$DB_USER" -p"$DB_PASS" "$DB_NAME" < insert_data.sql

if [ $? -eq 0 ]; then
    echo "Dados inseridos com sucesso!"
else
    echo "Erro ao inserir dados."
fi
