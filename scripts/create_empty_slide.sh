#!/bin/bash
DIR=$1
COUNT=$2

mkdir -p $DIR
rm -f $DIR/*

for i in `seq 0 $COUNT`
do
   cp bos.png $DIR/x-$i.png
done

./create_empty_draw.sh $DIR
