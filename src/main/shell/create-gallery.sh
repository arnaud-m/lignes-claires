#!/bin/sh
#
# This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires
#
# Copyright (c) 2024, Université Côte d'Azur. All rights reserved.
#
# Licensed under the BSD 3-clause license.
# See LICENSE file in the project root for full license information.
#


AWK='BEGIN{print "strict graph G {\nnode [shape=plain]"}
{if(NF == 2) {print $1,"--",$2}}
END{print "}"}'

EXT="svg"
DOT="sfdp -x -Goverlap=scale"
for DIR in $* ;
do
    find $DIR -name '*.gr' -print0 |
        while IFS= read -r -d '' GRFILE; do
            OUTFILE="${GRFILE%.*}.$EXT"
            awk  "$AWK" $GRFILE | $DOT -T$EXT -o $OUTFILE
        done
done
