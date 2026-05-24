# RDV Service - Application de Gestion de Rendez-vous

## Description

Application Spring Boot 3.x pour la gestion de rendez-vous avec support de PostgreSQL, validation des conflits, et gestion de concurrence.

## Technologies Utilisées

* Java 21 - Langage principal 
* Spring Boot 3.x - Framework 
* PostgreSQL - Base de données 
* Spring Data JPA - ORM 
* Lombok - Réduction du code boilerplate 
* Maven - Gestion des dépendances 
* Testcontainers - Tests d'intégration 
* JUnit 5 & Mockito - Tests unitaires

 # 1. Installation et lancement

git clone https://github.com/votre-compte/rdv-service.git 

cd rdv-service

# 2. Compiler et lancer

Vous devez avoir Docker installee sur votre ordinateur

mvn test

# 3. Test Technique

### Tests repository
mvn test -Dtest=*RepositoryTest

### Tests service
mvn test -Dtest=*ServiceImplTest

### Tests API
mvn test -Dtest=*ApiTest

### Test spécifique 
 #### (ex: UtilisateurRepositoryTest)
mvn test -Dtest=UtilisateurRepositoryTest

# 4. Accéder à l'application
   API REST : http://localhost:8080

Swagger UI : http://localhost:8080/swagger-ui.html

Health Check : http://localhost:8080/actuator/health