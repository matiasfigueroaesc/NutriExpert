#!/bin/bash
# =====================================================================
# levantar.sh · Actualiza, compila y levanta los microservicios de NutriExpert
#
#   usuarios -> puerto 8080 (MariaDB: nutriexpert_usuarios)
#   predios  -> puerto 8081 (MariaDB: nutriexpert_predios)
#   planes   -> puerto 8082 (por ahora H2 en memoria: aún no tiene perfil prod)
#
# Se usa en la EC2. Instalación (una vez): cp ~/NutriExpert/backend/levantar.sh ~/ && chmod +x ~/levantar.sh
# Uso:  ~/levantar.sh            (git pull + compilar + levantar)
#       ~/levantar.sh --sin-pull (no descarga cambios de GitHub)
# Logs: ~/logs/<servicio>.log
# La clave de la base de datos se lee de ~/nutriexpert.env (NO está en GitHub).
# =====================================================================
set -e

REPO="$HOME/NutriExpert"
LOGS="$HOME/logs"
MVNW="$REPO/backend/predios/mvnw"   # planes no trae mvnw; se usa el de predios

# Memoria: la t2.micro tiene 1 GB, así que cada servicio usa poco
JAVA_OPTS="-Xms64m -Xmx192m -XX:MaxMetaspaceSize=160m -XX:+UseSerialGC"
export MAVEN_OPTS="-Xmx384m"

# Usuario y clave de MariaDB (DB_HOST, DB_USER, DB_PASSWORD)
if [ ! -f "$HOME/nutriexpert.env" ]; then
    echo "Falta ~/nutriexpert.env con DB_USER y DB_PASSWORD"; exit 1
fi
set -a; source "$HOME/nutriexpert.env"; set +a

mkdir -p "$LOGS"

echo "== 1/4 Deteniendo los servicios que estén corriendo"
for s in usuarios predios planes; do
    pkill -f "$s-1.0.jar" && echo "   $s detenido" || true
done
sleep 3

if [ "$1" != "--sin-pull" ]; then
    echo "== 2/4 Descargando cambios de GitHub"
    git -C "$REPO" pull
fi

echo "== 3/4 Compilando (unos minutos la primera vez)"
chmod +x "$MVNW"
for s in usuarios predios planes; do
    echo "   compilando $s..."
    (cd "$REPO/backend/$s" && "$MVNW" -q -DskipTests package)
done

echo "== 4/4 Levantando"
levantar() {   # $1 servicio  $2 puerto  $3 perfil  $4 base de datos
    local jar="$REPO/backend/$1/target/$1-1.0.jar"
    if [ "$3" = "prod" ]; then
        DB_NAME="$4" nohup java $JAVA_OPTS -jar "$jar" --spring.profiles.active=prod > "$LOGS/$1.log" 2>&1 &
    else
        nohup java $JAVA_OPTS -jar "$jar" > "$LOGS/$1.log" 2>&1 &
    fi
    # Espera hasta 3 minutos a que el puerto responda
    for i in $(seq 1 90); do
        if ss -ltn | grep -q ":$2 "; then echo "   $1 OK en el puerto $2"; return; fi
        sleep 2
    done
    echo "   $1 NO levantó: revisa $LOGS/$1.log"; tail -n 20 "$LOGS/$1.log"
}
# De a uno, para no llenar la memoria arrancando los tres a la vez
levantar usuarios 8080 prod nutriexpert_usuarios
levantar predios  8081 prod nutriexpert_predios
levantar planes   8082 dev  -

echo
echo "Listo. Prueba desde tu PC:  http://<ELASTIC_IP>:8080/api/usuarios/1"
