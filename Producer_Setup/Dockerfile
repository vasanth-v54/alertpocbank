FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY target/producer-setup-0.0.1-SNAPSHOT.jar app.jar

RUN mkdir /logs

ENTRYPOINT ["java","-jar","/app/app.jar"]