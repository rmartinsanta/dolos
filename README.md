# What
Este proyecto contiene diferentes técnicas para detectar fraude académico, utilizando técnicas lo más simples posibles y con una tasa de falsos positivos muy baja.

> Pero que haces haciendo esto público loco.

Después de ver en el Aula Virtual respuestas de gente tal que: `Compañero de Clase [09/05/2023 01:54]: La respuesta correcta es la C`,
creo que el diagrama de Venn entre la gente que entiende cómo funcionan las detecciones y los que hacen trampas son dos círculos en extremos opuestos de la pantalla.

# Requisitos
Una versión de Java reciente (recomendada la 21 LTS) y Maven si se desea compilar o contribuir al desarrollo.

# Instrucciones de uso
Los modos y técnicas de detección dependen de la versión exacta.
Para ver las opciones de uso y las técnicas implementadas lanzar la aplicación sin argumentos. Ejemplo:
```bash
> java -jar target/antiplagios-1.0-SNAPSHOT.jar
The following option is required: [-i]
Usage: <main class> [options]
  Options:
    -e
      Path containing excluded images that should be ignored if found inside
      the folder to analyze.
  * -i
      Folder containing one folder for each student. Each student folder
      contains either a PDF or a set of images, will be recursively enumerated
      if it contains directories.
    -pixelmatch
      Match image content using pixel data or match whole file. By default, if
      this parameter is not provided, matches whole files.
      Default: false
    -r
      Path containing PDFs from which images will be extracted and used to
      compare against, but they themselves should not compared against the
      rest. Useful for example to provide submissions from other years. Will
      be recursively enumerated.
```

# Compilar
Para compilar el proyecto necesitas Maven. Una vez instalado, simplemente ejecuta:
```
mvn clean package
```
Y en la carpeta `target` encontrarás el JAR.

# ¡Me han pillado! ¿Qué hago?
![](https://www.memecreator.org/static/images/memes/5454642.jpg)

# Contributing
¿Tienes alguna idea de detección? Abre una issue o PR :)
