# Build stage
FROM eclipse-temurin:25-jdk as builder
WORKDIR /app
COPY . .
RUN chmod +x gradlew && ./gradlew clean build -x test

# Runtime stage
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENV TELEGRAM_BOT_TOKEN=${TELEGRAM_BOT_TOKEN}
ENV ALLOWED_USER_IDS=${ALLOWED_USER_IDS}
ENV CARD_ENCRYPTION_KEY=${CARD_ENCRYPTION_KEY}
ENV DB_USERNAME=${DB_USERNAME}
ENV DB_PASSWORD=${DB_PASSWORD}
CMD ["java", "-jar", "app.jar"]
