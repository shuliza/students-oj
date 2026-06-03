FROM maven:3.9.9-eclipse-temurin-21 AS build
ARG SERVICE_NAME
WORKDIR /workspace
COPY . .
RUN mvn -B -pl ${SERVICE_NAME} -am -DskipTests package

FROM eclipse-temurin:21-jre
ARG SERVICE_NAME
ENV SERVICE_NAME=${SERVICE_NAME}
WORKDIR /app
COPY --from=build /workspace/${SERVICE_NAME}/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
