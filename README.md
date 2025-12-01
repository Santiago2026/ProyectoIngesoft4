# Arquitectura de Software IV
### Estimación de Velocidad Promedio por Tramos en Rutas con Datos del SITM-MIO

Este proyecto implementa un sistema distribuido para procesar grandes volúmenes de datagramas generados por los buses del sistema de transporte SITM-MIO, con el objetivo de calcular la velocidad promedio de desplazamiento entre tramos de la ciudad.

La solución incluye un coordinador (maestro), múltiples nodos de procesamiento (workers) y un cliente responsable de enviar las solicitudes.
Se utiliza comunicación distribuida basada en ICE (Internet Communications Engine).

## Integrantes
- Santiago Grajales
- Melissa Hurtado
- Valentina Tobar

## Funcionalidad Principal
- Procesamiento distribuido de grandes archivos de datagramas (1M, 10M, 100M).
- Cálculo de velocidades promedio por tramo.
- Capacidad de ejecución con múltiples nodos.
- Medición de tiempos.
- Determinación del punto de corte a partir del cual conviene distribuir el procesamiento.

## Instrucciones de Ejecución

### 1. Construir el proyecto con Gradle

En la raíz del proyecto, ejecuta:

```bash
./gradlew build
```

Esto compilará todos los módulos del proyecto.

Asegúrate de tener ICE correctamente instalado y configurado en tu sistema.

### 2. Ejecución de las clases

Para la ejecución de las clases hay que tener en cuenta el order de la conexion entre las clases. Primero se debera ejecutar el server que estara esperando que algun worker o que el cliente le mande una petición.

```bash
# Ejecutar el server
java -jar server/build/libs/server.jar
```

Después se deberá ejecutar al menos un worker con la siguiente linea de comando.

```bash
# Ejecutar el worker
java -jar worker/build/libs/worker.jar
```

Por último, se deberá ejecutar el cliente con la linea de comando.

```bash
# Ejecutar el cliente
java -jar client/build/libs/client.jar
```

Como resultado en términos de las necesidades expresadas en el proyecto, vas a recibir el tiempo de respuesta que tomo hacer el proceso de calcular la velocidad promedio por arco de las rutas del sistema SITM-MIO con los datos historicos, entre los archivos seleccionados (1M, 10M, 100M). 
