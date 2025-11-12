# Datalife Extensions

## 🛠️ Publicación

Antes de comenzar, **asegúrate de tener instalados Java 17 y Maven**.  
Puedes verificarlo con los siguientes comandos:

```shell
java -version
mvn -v
```

Ambos son necesarios para que Gradle pueda compilar y publicar los artefactos en tu repositorio local de Maven.

```shell
./gradlew build
./gradlew publishToMavenLocal
```

En este momento, los artefactos se están publicando utilizando el grupo (`group`) y la versión (`version`) definidos en el archivo `gradle.properties`.

---

# Estructura de directorios

## extensions
Contiene el código fuente de las extensiones.

## spi
Contiene el código fuente de las interfaces que deben implementarse o extenderse para ser utilizadas en las extensiones.

## launchers
Contiene dos lanzadores para probar las extensiones de impresión.

Después de ejecutar `./gradlew build`, ve a `launchers/terminal-printer`, ejecuta:

```shell
java -jar build/libs/terminal-printer.jar
```

y verifica el mensaje **“Hello, World!”** impreso en la terminal.

Para ejecutar la versión de archivo, ve a `launchers/file-printer`, crea una variable con:

```shell
export EDC_PRINTER_FILE_PATH=file.txt
```

Ejecuta:

```shell
java -jar build/libs/file-printer.jar
```
Y verifica el mensaje **“Hello, World!”** impreso en el archivo `file.txt`.