#!/bin/bash

RC=1
while [ $RC -ne 0 ]; do
    java -jar spypartybot.jar
    RC=$?
    sleep 1
done
