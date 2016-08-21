#!/bin/bash
# This script creates a table that shows the mean time of running X times a simulation, given the following variables:
# (N, L, r, rc, M min, M max, periodic bounds, repeat)
#
# The argument type are as follows:
# N                 Integer
# L                 Integer
# r                 Double
# rc                Double
# M min             Integer
# M max             Integer
# periodic bounds   Boolean
# repeat            Integer
# The 'M min' and 'M max' variables correspond to the bounds of the loop with a step of 1 in ascending order.
# The output table will correspond to a column with the M value and another with mean time in the order of miloseconds,
# and a column with the variance in the order of milliseconds. The output folder is 'output' and the file is named
# after the number of particles

# Number of exact parameters required to run the script
PARAMS_REQUIRED=8

# Paths to prerequired files
RELATIVE_PROJECT_FOLDER="."

OUTPUT_FOLDER="$RELATIVE_PROJECT_FOLDER/output"
STATIC_PATH="$OUTPUT_FOLDER/static.dat"
DYNAMIC_PATH="$OUTPUT_FOLDER/dynamic.dat"
SIM_OUTPUT_PATH="$OUTPUT_FOLDER/output.dat"

# Prefix identifier of the output table
OUTPUT_NAIVE_PATH="$OUTPUT_FOLDER/optimum_graph_"
OUTPUT_PB_PATH="$OUTPUT_FOLDER/optimum_graph_pb_"

# Extension of the output table
OUTPUT_TABLE_TYPE=".csv"

JAR="java -jar $RELATIVE_PROJECT_FOLDER/core/target/core-1.0-SNAPSHOT-jar-with-dependencies.jar"

# Generate static file
function gen_staticdat {
    ${JAR} gen staticdat ${1} ${2} ${3} > /dev/null
}

function gen_dynamicdat {
    ${JAR} gen dynamicdat ${STATIC_PATH}
}

function cim_call {
    ${JAR} cim ${STATIC_PATH} ${DYNAMIC_PATH} ${M} ${RC} ${PERIODIC_BOUNDS} > /dev/null
}

function force_call {
    ${JAR} force ${STATIC_PATH} ${DYNAMIC_PATH} ${RC} ${PERIODIC_BOUNDS} > /dev/null
}

function gen_output_table_file {
    local OUTPUT_TABLE_PREFIX

    # Assign to the file the periodic bounds prefix if the simulation will be run with periodic bounds
    if [ ${1} = true ]; then
	    OUTPUT_TABLE_PREFIX=${OUTPUT_PB_PATH}
    else
	    OUTPUT_TABLE_PREFIX=${OUTPUT_NAIVE_PATH}
    fi

    # e.g. "optimum_graph_N.csv" or "optimum_graph_periodic_N.csv"
    local OUTPUT_TABLE_PATH="$OUTPUT_TABLE_PREFIX$2$OUTPUT_TABLE_TYPE"

    # Delete and create output table file and set the (M, time) columns
    rm -f ${OUTPUT_TABLE_PATH}
    touch ${OUTPUT_TABLE_PATH}

    # Add N number to the file
    echo "N, $N"$'\r' >> ${OUTPUT_TABLE_PATH}

    # Prepare for Cell Index Method
    echo "Cell Index Method"$'\r' >> ${OUTPUT_TABLE_PATH}

    # Add identifiers of columns to the start of the file
    echo "M, mean(milliseconds), variance(milliseconds)"$'\r' >> ${OUTPUT_TABLE_PATH}

    echo ${OUTPUT_TABLE_PATH}
}

# Start of Script

if [ $# -ne ${PARAMS_REQUIRED} ]; then
    # [true/false] corresponds to the periodic bounds boolean argument required to run a simulation
	echo "This script requires $PARAMS_REQUIRED parameters (N, L, r, rc, M min, M max, periodic bounds, repeat)"
	exit 1
fi

# Assign arguments to readable variables
N=$1
L=$2
R=$3
RC=$4
M_MIN=$5
M_MAX=$6
PERIODIC_BOUNDS=$7
REPEAT=$8

# echo "Cleaning output directory..."
#
# if [ -d $OUTPUT_FOLDER ]; then
#     rm -rf $OUTPUT_FOLDER
# fi
# mkdir $OUTPUT_FOLDER
#
# echo "[DONE]"

echo "Generating static.dat file..."

gen_staticdat ${N} ${L} ${R}

echo "[DONE]"

OUTPUT_TABLE_PATH=`gen_output_table_file ${PERIODIC_BOUNDS} ${N}`

echo "Simulation for Cell Index Method..."

for M in `seq ${M_MIN} ${M_MAX}`; do
	echo "\tRunning simulation $REPEAT times for M = $M"

	# Reset values
	MEAN_ACCUMULATOR=0
	VARIANCE_ACCUMULATOR=0

	for i in `seq 1 ${REPEAT}`; do
	    gen_dynamicdat
	    cim_call
        PERCENTAGE_COMPLETED=$(bc <<< "scale=2;$i/$REPEAT * 100")
        echo -ne "\t\tCompleted...$PERCENTAGE_COMPLETED%\r" # A % completed value
	    TIME=`head -n 1 ${SIM_OUTPUT_PATH}`
	    MEAN_ACCUMULATOR=$(bc <<< "scale=2;$MEAN_ACCUMULATOR + $TIME")
	    VARIANCE_ACCUMULATOR=$(bc <<< "scale=2;$VARIANCE_ACCUMULATOR + $TIME * $TIME")
	done
	MEAN=$(bc <<< "scale=2;$MEAN_ACCUMULATOR/$REPEAT")
	VARIANCE=$(bc <<< "scale=2;$VARIANCE_ACCUMULATOR/$REPEAT - $MEAN * $MEAN")
	COLUMN_M_TIME="$M, $MEAN, $VARIANCE" # Get the M value and the time in milliseconds at the first line
	echo ${COLUMN_M_TIME}$'\r' >> ${OUTPUT_TABLE_PATH}
	echo "[DONE]"
done

echo "[DONE]"

echo "Simulation for Brute Force Method..."

    # one empty line on the .csv file
    echo $'\r' >> ${OUTPUT_TABLE_PATH}

    echo "Brute Force Method"$'\r' >> ${OUTPUT_TABLE_PATH}
    echo "time(milliseconds)"$'\r' >> ${OUTPUT_TABLE_PATH}

    force_call
    TIME=`head -n 1 ${SIM_OUTPUT_PATH}`
    echo ${TIME}$'\r' >> ${OUTPUT_TABLE_PATH}

echo "[DONE]"