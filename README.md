# Cell Index Method
Java Implementation of the Cell Index Method.
## Build
To build the project, it is necessary to have Maven and Java 1.8 installed.
Then, run

    $ mvn clean package
    
## Execution
To run the program, from the root folder

    $ java -jar core/target/core-1.0.0.RELEASE-jar-with-dependencies.jar <arguments>

Check the list of available arguments as follows, from the root folder

    $ java -jar core/target/core-1.0.0.RELEASE-jar-with-dependencies.jar help

## Simulation
Bash files were added to $PROJECT_FOLDER/resources/bin.
These files allow doing several simulations and taking some statistics about cell-index-method performance with different N and M values, according to user's parameters.

Please open this files and read the description at their top to check which variables are required and what do they mean.
Simulation's output folder will be at $PROJECT_FOLDER/output.

**IMPORTANT**
Make sure to correctly modify the $PROJECT_FOLDER variable of the bash files at the resources/bin/ folder for the scripts to work correctly.
Current value is: 
    *PROJECT_FOLDER="$HOME/Programs/idea_workspace/cell-index-method"*

###Usage example
From the PROJECT_FOLDER/resources/bin folder, run

    $ ./analyser.sh 20 0.25 1 1 13 false 50
