FROM hseeberger/scala-sbt:8u222_1.3.3_2.12.10

COPY build.sbt build.sbt
COPY project/ project/
COPY src/ src/

RUN sbt assembly

FROM java

COPY --from=0 /root/target/scala-2.12/spypartybot.jar /app.jar

CMD java -jar /app.jar
