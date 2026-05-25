FROM eclipse-temurin:21
LABEL authors="ngandjui"
#COPY target/*.jar user-manager-0.0.1-SNAPSHOT.jar

#ENTRYPOINT ["java", "-jar", "/rdvservice-0.0.1-SNAPSHOT.jar"]
#EXPOSE 8081


RUN mkdir /opt/app
COPY target/rdvservice-0.0.1-SNAPSHOT.jar /opt/app
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/opt/app/rdvservice-0.0.1-SNAPSHOT.jar", "--spring.profiles.active=prod"]