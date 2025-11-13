# MedCloud  
Plataforma de Gestión Clínica – Backend en Java 21 + Spring Boot

MedCloud es un backend desarrollado en Java 21 + Spring Boot, diseñado para gestionar usuarios, doctores, pacientes y documentos clínicos dentro de un ecosistema digital de salud. Su arquitectura modular facilita la escalabilidad, mantenimiento y extensibilidad del proyecto, integrando buenas prácticas como DTOs, separación de capas, seguridad JWT y manejo global de excepciones.

--------------------------------------------------------------------
Características principales
--------------------------------------------------------------------

- Autenticación JWT segura.
- Gestión de pacientes, doctores y roles.
- Subida y consulta de documentos clínicos (PDF, imágenes, reportes, escaneos).
- Arquitectura por capas: dominio, persistencia, seguridad, controladores.
- Validación de datos y excepciones centralizadas.
- Integración con JPA/Hibernate y base de datos relacional.

--------------------------------------------------------------------
Stack Tecnológico
--------------------------------------------------------------------

Lenguaje: Java 21  
Framework: Spring Boot 3.x  
Seguridad: Spring Security + JWT  
Build Tool: Gradle  
Persistencia: JPA/Hibernate, PostgreSQL o H2  
Controladores: Spring Web MVC

--------------------------------------------------------------------
Arquitectura del Proyecto
--------------------------------------------------------------------

com.medcloud.app  
 ├── domain/                # Lógica de negocio y reglas del dominio  
 │    ├── dto/              # Requests y Responses  
 │    ├── enums/            # Enumeraciones  
 │    ├── exceptions/       # Excepciones personalizadas  
 │    ├── repository/       # Interfaces del dominio (puertos)  
 │    └── service/          # Casos de uso  
 │  
 ├── persistence/           # Infraestructura de datos  
 │    ├── entity/           # Entidades JPA  
 │    ├── jpa/              # Repositorios Spring Data JPA  
 │    ├── mapper/           # Mappers Entity <-> DTO  
 │    └── repositoryimp/    # Adaptadores del dominio  
 │  
 ├── security/              # Configuración Spring Security + JWT  
 │    └── SecurityConfig  
 │  
 ├── web.controller/        # Capa de exposición REST  
 │    ├── ClinicalDocumentController  
 │    ├── UserController  
 │    └── GlobalExceptionHandler  
 │  
 └── MedCloudApplication    # Clase principal  

Descripción de capas:

**domain/**  
Contiene toda la lógica del negocio: DTOs, servicios, validaciones, excepciones y puertos.

**persistence/**  
Implementaciones concretas de repositorios, entidades y mappers. Conecta el dominio con la base de datos.

**web.controller/**  
Controladores REST que exponen la API de la aplicación.

**security/**  
Configuración de autenticación, autorización y filtros JWT.

--------------------------------------------------------------------
Endpoints principales
--------------------------------------------------------------------

**Autenticación:**  
POST /auth/login  

**Usuarios:**  
POST /auth/register/patient  
POST /auth/register/doctor  
GET /users/search?email=<correo>  

**Documentos clínicos:**  
POST /documents/upload  
GET /documents/{id}

--------------------------------------------------------------------
Modelos principales (DTOs)
--------------------------------------------------------------------

**LoginRequest:**  
{  
  "email": "string",  
  "password": "string"  
}

**CreateUserRequest incluye:**  
- username  
- email  
- password  
- roles  
- documentType (CC, TI, CE)  
- documentNumber  
- fullName  
- birthDate  

**DocumentResponse:**  
- id  
- name  
- kind (PDF, IMAGE, LAB_REPORT, SCAN, OTHER)  
- patientId  
- uploadedAt  
- description  
- size  

--------------------------------------------------------------------
Ejecución del proyecto
--------------------------------------------------------------------

**Requisitos:**  
- Java 21  
- Gradle 8+  
- PostgreSQL (opcional si usas H2)  

**Ejecutar:**  
./gradlew bootRun  

**API disponible en:**  
http://localhost:8080  

**Ejecutar pruebas:**  
./gradlew test  

--------------------------------------------------------------------
Seguridad
--------------------------------------------------------------------

- JWT para autenticación  
- Filtros personalizados de seguridad  
- Roles: PACIENTE, EPS  
- Configuración centralizada en SecurityConfig  

--------------------------------------------------------------------
Contribuciones
--------------------------------------------------------------------

Las contribuciones, mejoras y reportes son bienvenidos.

--------------------------------------------------------------------
Licencia
--------------------------------------------------------------------

Pendiente por definir (MIT recomendado).
