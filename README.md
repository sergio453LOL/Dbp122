## Spring Boot Plantilla

Este repositorio contiene una plantilla base para el desarrollo de aplicaciones API REST con Spring Boot, con PostgreSQL como base de datos y Docker para el despliegue. Se utiliza Maven como gestor de dependencias.


### Tecnologías

- [Spring Boot](https://spring.io/projects/spring-boot)
- [PostgreSQL](https://www.postgresql.org/)
- [Docker](https://www.docker.com/)
- [Maven](https://maven.apache.org/)

### Requisitos

- Java 21
- Docker
- Maven

### Tabla de Dependencias

Se utiliza Maven como gestor de dependencias, la versión de Java es 21 y las dependencias son las siguientes

| Dependencia                    | Versión | Descripción                                            |
| ------------------------------ | :-----: | ------------------------------------------------------ |
| Spring Boot Starter WebMVC     |  4.0.7  | Desarrollo de API REST y manejo de solicitudes HTTP.   |
| Spring Boot Starter Security   |  4.0.7  | Autenticación, autorización y protección de endpoints. |
| Spring Boot Starter Data JPA   |  4.0.7  | Persistencia de datos mediante JPA/Hibernate.          |
| SpringDoc OpenAPI (Swagger UI) |  3.0.2  | Documentación interactiva de la API basada en OpenAPI. |
| Spring Boot Starter Mail       |  4.0.7  | Envío de correos electrónicos mediante SMTP.           |
| Spring Boot Starter Validation |  4.0.7  | Validación de datos con Bean Validation (Jakarta).     |
| jjwt (API + Impl + Jackson)    |  0.12.6 | Generación y validación de tokens JWT firmados con HS256. |
| Google ZXing Core + JavaSE     |  3.5.3  | Generación de imágenes QR (PNG en Base64) para login por QR. |
| TOTP (dev.samstevens.totp)     |  1.7.1  | Autenticación de doble factor (2FA) compatible con Google Authenticator. |

Esta tabla contiene las dependencias del proyecto, la versión de cada dependencia y una breve descripción de cada dependencia. Esta información se encuentra en el archivo pom.xml.


## Estructura del Proyecto

Como implementacion basica se tiene una API REST con operaciones CRUD (Create, Read, Update, Delete) sobre una entidad 'User' con campos 'id', 'username', 'email' y 'password'.

La estructura por entidad deberia seguir el siguiente patron:

```text
/entity-name
├── controller
├── dto
├── domain
├── repository
└── service
```

Adicionalmente se ha implementado seguridad mediante JWT, inicio de sesion mediante QR,autenticacion en doble factor (2FA) y envio de correos electronicos para el registro y verificacion de los usuarios .



## Ejecucion y Testeo 

### Despliegue con Docker 
Se recomienda el uso de Docker para la ejecucion del proyecto, para ello se debe seguir los siguientes pasos:

En una terminal se debe ejecutar el comando:

```bash
docker-compose up -d --build
```
Con esto se habra construido y levantado los contenedores de la aplicacion.


### Despliegue Manual 
En caso de no usar Docker, se debe ejecutar el proyecto de la siguiente manera:
```bash
mvn clean install
mvn spring-boot:run
```


### Testing
Para testear la aplicacion se puede hacer uso de la documentacion OpenAPI que se encuentra en la siguiente direccion:
```
http://localhost:8080/swagger-ui.html
```

De igual forma se pueden correr los test unitarios con el comando:

```bash
mvn test
```
Los test se encuentran en la carpeta `src/test/java/com/example/demo`


## Adicionales

Este repositorio cuenta con un archivo .env.example que contiene la configuracion basica para la aplicacion. Se puede copiar y pegar en un archivo .env y modificar los valores segun la necesidad.

Asi mismo se cuenta con un worflow de GitHub que se encarga de ejecutar los test unitarios cada vez que se hace un push al repositorio. De igual forma cuenta con un worflow de GitHub que se encarga de construir y pushear la imagen a un registro de Docker. 


Este proyecto es parte de un ejercicio realizado por Lazheart con el fin de apoyar el desarrollo de API REST con Spring Boot.