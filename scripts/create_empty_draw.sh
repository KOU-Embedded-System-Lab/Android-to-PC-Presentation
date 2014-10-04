#!/bin/bash
DIR=$1

for i in $DIR/*.png
do
   cp transparent.png $i.draw
done
