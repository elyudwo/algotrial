# Step 1: Spring Boot 실행 환경 및 C++ 컴파일러 설치
FROM ubuntu:latest

# JDK 설치
RUN apt-get update && \
    apt-get install -y openjdk-17-jdk && \
    apt-get clean

# C++ 컴파일러 설치
RUN apt-get update && \
    apt-get install -y \
    build-essential \
    g++ \
    cmake \
    git && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Spring Boot JAR 복사
ARG JAR_FILE=build/libs/algotrial-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} algotrial-0.0.1-SNAPSHOT.jar

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "/algotrial-0.0.1-SNAPSHOT.jar"]
