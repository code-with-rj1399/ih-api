FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /src
COPY mvnw pom.xml ./
COPY .mvn .mvn
COPY src src
RUN chmod +x mvnw && ./mvnw -q -DskipTests package

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
RUN useradd --system --uid 1001 ihapi
COPY --from=build /src/target/ih-api-*.jar /app/app.jar
USER ihapi
EXPOSE 8080
ENV SERVER_PORT=8080
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-jar", "/app/app.jar"]
