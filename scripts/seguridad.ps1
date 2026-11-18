# Cap. 20 - vhost, usuarios y permisos. Ejecutar en PowerShell con el contenedor "rabbit" corriendo.
docker exec rabbit rabbitmqctl add_vhost pedidos

# un administrador para humanos
docker exec rabbit rabbitmqctl add_user admin 'Cambiame-0'
docker exec rabbit rabbitmqctl set_user_tags admin administrator
docker exec rabbit rabbitmqctl set_permissions -p pedidos admin '.*' '.*' '.*'

# un usuario por rol: permisos configure / write / read
docker exec rabbit rabbitmqctl add_user app_productor 'Cambiame-1'
docker exec rabbit rabbitmqctl add_user app_consumidor 'Cambiame-2'
docker exec rabbit rabbitmqctl set_permissions -p pedidos app_productor '^$' '^exchange_pedidos$' '^$'
docker exec rabbit rabbitmqctl set_permissions -p pedidos app_consumidor '^$' '^$' '^queue_pedidos$'

# El usuario por defecto afuera. OJO: si lo borrás, la app con el application.yaml original deja de conectar.
# docker exec rabbit rabbitmqctl delete_user guest
