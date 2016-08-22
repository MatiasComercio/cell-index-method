#!/bin/bash

# This script creates a table that shows the mean time of running X times a simulation with different N values,
# given the following variables:
# (L, r, rc, M min, M max, periodic bounds, repeat)

# Number of exact parameters required to run the script
PARAMS_REQUIRED=7

PROJECT_FOLDER="$HOME/Programs/idea_workspace/cell-index-method"
OPTIMUSM_SCRIPT="./optimusM.sh"

# Start of Script

if [ $# -ne ${PARAMS_REQUIRED} ]; then
    # [true/false] corresponds to the periodic bounds boolean argument required to run a simulation
	echo "This script requires $PARAMS_REQUIRED parameters (L, r, rc, M min, M max, periodic bounds, repeat)"
	exit 1
fi

# Assign arguments to readable variables
L=$1
R=$2
RC=$3
M_MIN=$4
M_MAX=$5
PERIODIC_BOUNDS=$6
REPEAT=$7

echo -e "------------------------------------"
for N in 100 500 750 1000 1100 ; do
    echo -e "** Running 'optimusM.sh' with N = ${N}... **"
    $OPTIMUSM_SCRIPT $N $L $R $RC $M_MIN $M_MAX $PERIODIC_BOUNDS $REPEAT
    echo -e "------------------------------------"
done

mv logs/* $PROJECT_FOLDER/logs
rm -r logs