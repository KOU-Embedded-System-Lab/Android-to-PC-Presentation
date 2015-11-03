#!/bin/bash
DIR=$1

for i in $DIR/*.png
do
   cp transparent.png $i.draw
done

COUNT=`ls $DIR/ | grep ".png.draw" | wc -l`
echo "$COUNT" > $DIR/info.txt
