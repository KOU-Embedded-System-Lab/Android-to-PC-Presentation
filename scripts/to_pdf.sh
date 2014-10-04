#!/bin/bash
DIR=$1

clean_tmp_files() {
	rm -f $DIR/*.merged
}

clean_tmp_files

for i in 0 1 2 3 4 5 6 7 8 9
do
	mv -f $DIR/x-${i}.png  $DIR/x-0${i}.png
    mv -f $DIR/x-${i}.png.draw  $DIR/x-0${i}.png.draw
done

for image in `ls $DIR/x-*.png`
do
	composite -gravity center $image.draw $image $image.merged
done

convert $DIR/x-*.merged $DIR/out.pdf

clean_tmp_files
