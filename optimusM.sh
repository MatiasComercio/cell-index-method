#!/bin/bash

MINPARAMS=6

STATIC_PATH="./output/static.dat"
DYNAMIC_PATH="./output/dynamic.dat"
OUTPUT_DATA_PATH="./output/output.dat"
OUTPUT_TABLE_PREFIX_PATH="./output/optimus_graph_"
OUTPUT_TABLE_TYPE=".csv"

SIMULATION="java -jar core/target/core-1.0-SNAPSHOT-jar-with-dependencies.jar"

### Start of Script ###

if [ $# -ne "$MINPARAMS" ]; then
	echo "This script requires 4 parameters (N, L, r, rc, M min, M max)"
	exit 1
fi

N=$1
L=$2
R=$3
RC=$4
M_MIN=$5
M_MAX=$6
OUTPUT_TABLE_PATH="$OUTPUT_TABLE_PREFIX_PATH$N$OUTPUT_TABLE_TYPE"

# Generate dynamic and static files
$SIMULATION gen staticdat $N $L $R
$SIMULATION gen dynamicdat $STATIC_PATH

# Delete / Create a new output file and set the (M, nanosec) columns
rm -f $OUTPUT_TABLE_PATH
touch $OUTPUT_TABLE_PATH
echo "M, Nanosec"$'\r' >> $OUTPUT_TABLE_PATH


#let "M = 1"
#let "UPPER_BOUND = $L / $M"

#while [ "$UPPER_BOUND" -gt "$RC" ]; do
#	$SIMULATION cim $STATIC_PATH $DYNAMIC_PATH $M $RC false # let it be a variable
#	COLUMN_M_TIME="$M, `head -n 1 $OUTPUT_DATA_PATH`" # Get the M value and the time in nano seconds at the first line
#	echo $COLUMN_M_TIME$'\r' >> $OUTPUT_TABLE_PATH
#	echo $UPPER_BOUND
#	let "M = $M + 1"
#	let "UPPER_BOUND = $L / $M"
#done

for M in `seq $M_MIN $M_MAX`; do
	$SIMULATION cim $STATIC_PATH $DYNAMIC_PATH $M $RC false # let it be a variable
	COLUMN_M_TIME="$M, `head -n 1 $OUTPUT_DATA_PATH`" # Get the M value and the time in mili seconds at the first line
	echo $COLUMN_M_TIME$'\r' >> $OUTPUT_TABLE_PATH
done

less $OUTPUT_TABLE_PATH