#!/usr/bin/env bash
# Cap. 20 - vhost, usuarios y permisos. Ejecutar en Git Bash con el contenedor "rabbit" corriendo.
# (En Git Bash, si las rutas se "traducen" raro, anteponé MSYS_NO_PATHCONV=1 a cada docker exec.)
docker exec rabbit rabbitmqctl add_vhost pedidos

docker exec rabbit rabbitmqctl add_user admin 'Cambiame-0'
docker exec rabbit rabbitmqctl set_user_tags admin administrator
docker exec rabbit rabbitmqctl set_permissions -p pedidos admin '.*' '.*' '.*'

docker exec rabbit rabbitmqctl add_user app_productor 'Cambiame-1'
docker exec rabbit rabbitmqctl add_user app_consumidor 'Cambiame-2'
docker exec rabbit rabbitmqctl set_permissions -p pedidos app_productor '^$' '^exchange_pedidos$' '^$'
docker exec rabbit rabbitmqctl set_permissions -p pedidos app_consumidor '^$' '^$' '^queue_pedidos$'

# docker exec rabbit rabbitmqctl delete_user guest   # OJO: la app con el yaml original deja de conectar
