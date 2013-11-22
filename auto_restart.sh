#!/bin/sh
while true
do
    isAlive=`ps ax | grep server.py | grep -v grep | wc -l`
    if [ $isAlive = 0 ]; then
        ~/mushapp/server.py
        echo "server.py start!"
    else
        echo "actived!"
    fi
    sleep 60
done
