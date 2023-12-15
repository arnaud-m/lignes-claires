#!/bin/sh

#--------------------------------------------------------------------
# Setup Global Variables
#--------------------------------------------------------------------

PROG=`basename $0`
JAR=../../../target/lignes-claires-0.1.0-SNAPSHOT-with-dependencies.jar

NB_INSTANCES=2
NB_NODES="25 50 100 250 500"
DENSITY_FROM=100
DENSITY_STEP=100
DENSITY_TO=400

#--------------------------------------------------------------------
# Version and help messages
#--------------------------------------------------------------------


version() {
cat <<EOF
$PROG 0.1

This file is part of lignes-claires, https://github.com/arnaud-m/lignes-claires

Copyright (c) 2023, Université Côte d'Azur. All rights reserved.

Licensed under the BSD 3-clause license.
See LICENSE file in the project root for full license information.
EOF
}

help() {
cat <<EOF
$PROG generates random bipartite graph of uniform density.

Usage: $PROG [OPTION] FILES...

Options:
 -h        display this help and exit
 -v        output version information and exit

Example:
$PROG

Report bugs to <arnaud (dot) malapert (at) univ-cotedazur (dot) fr>."
EOF
}

#--------------------------------------------------------------------
# Test for prerequisites
#--------------------------------------------------------------------

while getopts ":hvf" opt; do
    case $opt in
        f)
            OVERWRITE=1
            ;;
        h)
            help
            exit 0
            ;;
        v)
            version;
            exit 0
            ;;
        \?)
            echo "Invalid option: -$OPTARG" >&2
            exit 1
            ;;
        :)
            echo "Option -$OPTARG requires an argument." >&2
            exit 1
            ;;
    esac
done

shift $((OPTIND-1))
#--------------------------------------------------------------------
# Do something
#--------------------------------------------------------------------

CMD="java -cp $JAR lignesclaires.bigraph.BipartiteGraph"



    for NODES in $NB_NODES; do
        for DENSITY in `seq $DENSITY_FROM $DENSITY_STEP $DENSITY_TO`; do
            for IDX in `seq $NB_INSTANCES`; do
                echo $NODES $DENSITY $IDX
                DRATE=$(echo "$DENSITY / 1000" | bc -l)
                FILENAME=`printf "N%03dD%03d-%d.gr" $NODES $DENSITY $IDX`
                $CMD $NODES $NODES $DRATE $IDX > $FILENAME
        done
    done


done
