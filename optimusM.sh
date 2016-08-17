#!/bin/bash

# Number of exact parameters required to run the script
NOPARAMS=7

# Paths to prerequired files
STATIC_PATH="./output/static.dat"
DYNAMIC_PATH="./output/dynamic.dat"
OUTPUT_DATA_PATH="./output/output.dat"

# Prefix identifier of the output table
OUTPUT_TABLE_PREFIX_PATH="./output/optimus_graph_"
OUTPUT_PERIODIC_TABLE_PREFIX_PATH="./output/optimus_graph_periodic_"

# Extension of the output table
OUTPUT_TABLE_TYPE=".csv"	

SIMULATION="java -jar core/target/core-1.0-SNAPSHOT-jar-with-dependencies.jar"

### Start of Script ###

if [ $# -ne "$NOPARAMS" ]; then
	echo "This script requires $NOPARAMS parameters (N, L, r, rc, M min, M max [true/false])" # [true/false] corresponds to the periodic bounds boolean
	exit 1
fi

N=$1
L=$2
R=$3
RC=$4
M_MIN=$5
M_MAX=$6
PERIODIC=$7

if [ $PERIODIC = true ]; then
	OUTPUT_TABLE_PREFIX=$OUTPUT_PERIODIC_TABLE_PREFIX_PATH
else
	OUTPUT_TABLE_PREFIX=$OUTPUT_TABLE_PREFIX_PATH
fi

# Generate dynamic and static files
$SIMULATION gen staticdat $N $L $R
$SIMULATION gen dynamicdat $STATIC_PATH

OUTPUT_TABLE_PATH="$OUTPUT_TABLE_PREFIX$N$OUTPUT_TABLE_TYPE" # e.g. "optimus_graph_N.csv" or "optimus_graph_periodic_N.csv"
# Delete and Create output table and set the (M, time) columns
rm -f $OUTPUT_TABLE_PATH
touch $OUTPUT_TABLE_PATH

# Add identifiers of columns to the start of the file
echo "M, Nanosec"$'\r' >> $OUTPUT_TABLE_PATH

for M in `seq $M_MIN $M_MAX`; do
	echo -n "$M "
	$SIMULATION cim $STATIC_PATH $DYNAMIC_PATH $M $RC $PERIODIC
	COLUMN_M_TIME="$M, `head -n 1 $OUTPUT_DATA_PATH`" # Get the M value and the time in nano seconds at the first line
	echo $COLUMN_M_TIME$'\r' >> $OUTPUT_TABLE_PATH
done