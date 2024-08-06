FROM gradle:8.8-jdk17 as build

WORKDIR /app

COPY gradlew .
COPY gradle/ gradle/
COPY build.gradle .

RUN chmod +x gradlew

RUN apt-get update && apt-get install -y dos2unix

RUN dos2unix gradlew gradle/wrapper/gradle-wrapper.properties build.gradle

ENV JAVA_OPTS="-Dhttps.protocols=TLSv1.2,TLSv1.3"

RUN ./gradlew dependencies --configuration runtimeClasspath

COPY src src

RUN ./gradlew clean build -x test --info --stacktrace

FROM mcr.microsoft.com/openjdk/jdk:17-mariner

WORKDIR /app

COPY build/libs/translator-0.0.1-SNAPSHOT.jar /app/app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
