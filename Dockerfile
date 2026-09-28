FROM eclipse-temurin:25-jdk AS build
WORKDIR /build

COPY gradlew ./
COPY gradle gradle
COPY build.gradle settings.gradle ./
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon --quiet || true

COPY src src
RUN ./gradlew bootJar --no-daemon -x test

FROM eclipse-temurin:25-jre
WORKDIR /app

RUN useradd --system --create-home --uid 10001 spring
USER spring

COPY --from=build --chown=spring:spring /build/build/libs/*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
