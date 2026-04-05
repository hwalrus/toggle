# Stage 1: build frontend
FROM node:22-alpine AS frontend
WORKDIR /web
COPY web/package.json web/package-lock.json ./
RUN npm ci
COPY web/ ./
RUN npm run build

# Stage 2: build backend
FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /build
COPY --from=frontend /web/dist /build/web/dist
COPY gradlew ./
COPY gradle/ gradle/
COPY settings.gradle.kts ./
COPY app/build.gradle.kts app/
COPY app/src/ app/src/
RUN ./gradlew :app:installDist -x buildFrontend --no-daemon

# Stage 3: runtime
FROM eclipse-temurin:25-jre-alpine
RUN addgroup -S toggle && adduser -S toggle -G toggle
WORKDIR /app
COPY --from=build /build/app/build/install/app .
USER toggle
EXPOSE 10800
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"
HEALTHCHECK --interval=30s --timeout=5s --start-period=15s --retries=3 \
  CMD wget -qO- http://localhost:10800/toggle || exit 1
ENTRYPOINT ["./bin/app"]
