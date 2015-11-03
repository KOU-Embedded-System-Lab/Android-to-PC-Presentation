#!/bin/bash

clean() {
    echo "clean"
    kill -9 $PID_HTTP_SERVER  &> /dev/null
    kill -9 $PID_SLIDE &> /dev/null
}

set -m
trap clean SIGHUP SIGINT SIGTERM

WD=`pwd`
DIR=$1

cd $DIR/
python -m SimpleHTTPServer &
PID_HTTP_SERVER=$!
echo "PID_HTTP_SERVER=$PID_HTTP_SERVER"

cd $WD
java -jar sunum.jar DIR/ DIR/draw.log &
PID_SLIDE=$!
echo "PID_SLIDE=$PID_SLIDE"

trap clean SIGCHLD
wait
