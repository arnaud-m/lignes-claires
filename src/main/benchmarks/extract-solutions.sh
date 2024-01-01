#!/bin/sh

AWK='BEGIN{n=0}{
if ($1 == "c" && $2 == "FIXED") {n=$3+1}
if ($1 == "v") {for(i = 2; i < NF; i++) {print n+$i}; printf "%d" , n+$NF}
}'

for DIR in $* ;
do
    find $DIR -name '*.o' -print0 |
        while IFS= read -r -d '' OUTFILE; do
            SOLFILE="${OUTFILE%.*}.sol"
            awk  "$AWK" $OUTFILE > $SOLFILE
        done
done
