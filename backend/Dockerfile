# 1. 빌드 스테이지: 그레이들(Gradle) 환경에서 자바 소스코드를 .jar 파일로 컴포넌트화
FROM gradle:8-jdk17 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
# 테스트 코드로 인해 빌드가 터지는 것을 방지하기 위해 -x test 추가
RUN ./gradlew build -x test --no-daemon

# 2. 실행 스테이지: 빌드된 가벼운 jar 파일만 추출해서 가동
FROM eclipse-temurin:17-jdk-alpine
EXPOSE 8080
COPY --from=build /home/gradle/src/build/libs/*-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]