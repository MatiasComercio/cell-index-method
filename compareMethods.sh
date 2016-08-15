#!/bin/bash

SIMULATION="java -jar core/target/core-1.0-SNAPSHOT-jar-with-dependencies.jar"

PROJECT=$HOME/ideaProjects/cell-index-method
OUTPUT_DATA_PATH=$PROJECT/data/statistics

mValues=( 4 6 8 10 12 13)

r=0.25
rc=1
L=20

rm $OUTPUT_DATA_PATH/*.out
rm $OUTPUT_DATA_PATH/*.dat

# Generate all .dat files.
# Each file contains the output of the corresponding algorithm.
# The files are named as follows:
# file_name:= <algorithm>-<N>-<M>.dat
# <algorithm> := cell | cellPeriodic | force
# <N> := An integer indicating the amount of particles
# <M> := An integer indicating the amount of cells, for the cell index algorithm

cd $PROJECT

for N in `seq 10 10 100` ; do

    $SIMULATION gen staticdat $N $L $r
    $SIMULATION gen dynamicdat output/static.dat

    for M in "${mValues[@]}" ; do
        
        $SIMULATION cim output/static.dat output/dynamic.dat $M $rc false
        mv output/output.dat $OUTPUT_DATA_PATH/cell-N$N-M$M.dat 

        $SIMULATION cim output/static.dat output/dynamic.dat $M $rc true
        mv output/output.dat $OUTPUT_DATA_PATH/cellPeriodic-N$N-M$M.dat

        #TODO: Add an entry in main for BruteForceMethod and calculate time
        $SIMULATION force output/static.dat output/dynamic.dat $rc false
        mv output/output.dat $OUTPUT_DATA_PATH/force-N$N-M$M.dat

        #DELETE:
        #force data/static.dat data/dynamic.dat $rc false
        #mv data/output.dat ../examples/forceN$(N)M($M).dat
        #java -jar core/target/core-1.0-SNAPSHOT-jar-with-dependencies.jar force output/static.dat output/dynamic.dat 1 false
    done

done

# Create one file per "M" value. 
# The files are named as follows:
# fileName := M<M>.out
# Each file has 4 columns, each separated by a space: 
# First column is N (amount of particles), second, third and forth column are the times measured
# for the CellIndex, CellIndexPeriodic and bruteForceMethod respectively.
# e.g: 
# Filename : M8.out
# N CellIndex CellIndexPeriodic BruteForceMethod
# 2 16726481 12450097 12452481
# 3 23617402 15555581 16135481


cd $OUTPUT_DATA_PATH

for M in "${mValues[@]}" ; do
    #CELL_OUT=cell-M$M.out
    #CELL_PERIODIC_OUT=cellPeriodic-M$M.out
    #FORCE_OUT=force-M$M.out
    OUT_FILE=M$M.out

    echo "M=$M" >> $OUT_FILE
    echo "N CellIndex CellIndexPeriodic BruteForceMethod" >> $OUT_FILE

    #DELETE
    #touch $CELL_OUT
    #for i in $(ls | grep cell-*.-$M.dat) ; do
    
    for N in `seq 10 10 100` ; do
        
        #COLUMN_TIME="`head -n 1 cell-N$N-M$M.dat`"
        echo -n "$N " >> $OUT_FILE
        #echo -n $COLUMN_TIME >> $OUT_FILE
        echo -n "`head -n 1 cell-N$N-M$M.dat` " >> $OUT_FILE

        #COLUMN_TIME="`head -n 1 cellPeriodic-N$N-M$M.dat` "
        #echo -n "$N " >> $OUT_FILE
        #echo -n $COLUMN_TIME >> $OUT_FILE
        echo -n "`head -n 1 cellPeriodic-N$N-M$M.dat` " >> $OUT_FILE

        #COLUMN_TIME="`head -n 1 force-N$N-M$M.dat` "
        #echo -n "$N " >> $OUT_FILE
        #echo $COLUMN_TIME$'\r' >> $OUT_FILE
        echo "`head -n 1 force-N$N-M$M.dat`" >> $OUT_FILE

   done

done
