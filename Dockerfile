#FROM --platform=linux/amd64 openjdk:17-jdk-slim
#COPY target/shopoclock-1.0.0-SNAPSHOT.jar shopoclock.jar
#EXPOSE 8080
#ENTRYPOINT ["java","-jar","shopoclock.jar"]

FROM maven:3.8.5-openjdk-17 AS build
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:17.0.1-jdk-slim
COPY --from=build /target/shopoclock-0.0.1-SNAPSHOT.jar shopoclock.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","shopoclock.jar"]
