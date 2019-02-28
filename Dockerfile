FROM hseeberger/scala-sbt

COPY build.sbt build.sbt
COPY project/ project/
COPY src/ src/

RUN sbt assembly

FROM java

COPY --from=0 /root/target/scala-2.12/spypartybot.jar /app.jar

CMD java -jar /app.jar
