FROM openjdk:8-jre-slim
WORKDIR /
ENV TIME_ZONE=Asia/Shanghai 
RUN ln -snf /usr/share/zoneinfo/$TIME_ZONE /etc/localtime && echo $TIME_ZONE > /etc/timezone
ADD target/challenge.jar app.jar
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod"]
CMD ["app.jar"]
