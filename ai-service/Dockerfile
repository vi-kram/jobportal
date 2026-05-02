FROM public.ecr.aws/amazoncorretto/amazoncorretto:17-al2023 AS build
RUN yum install -y tar gzip
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -q
COPY src/ src/
RUN ./mvnw package -DskipTests -q

FROM public.ecr.aws/amazoncorretto/amazoncorretto:17-al2023
WORKDIR /app
COPY --from=build /app/target/ai-service-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
