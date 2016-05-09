#run with parameters PROBLEM TIMELIMIT

# DEFINED PROBLEMS:
# BinPacking
# TSP
# VRP
# PersonnelScheduling
# MAXSAT
# FlowShop
 
#INSTANCE: an integer value representing the instance to be tested.


problem=$1
timeLimit=$2
scaling=$3
acc=2
runs=31



# Set the number of instances of a problem.
if [ $problem == "BinPacking" ]; then
	instance[0]=7
	instance[1]=1
	instance[2]=9
	instance[3]=10
	instance[4]=11
elif [ $problem == "TSP" ]; then
	instance[0]=0
	instance[1]=8
	instance[2]=2
	instance[3]=7
	instance[4]=6
elif [ $problem == "PDP" ]; then
	#instance[0]=1
	#instance[1]=2
	#instance[2]=3
	#instance[3]=4
	#instance[4]=5
	#instance[5]=6
	instance[0]=7
	#instance[7]=8
	#instance[8]=9
	#instance[9]=10
	#instance[10]=11
elif [ $problem == "VRP" ]; then
	instance[0]=6
	instance[1]=2
	instance[2]=5
	instance[3]=1
	instance[4]=9
elif [ $problem == "PersonnelScheduling" ]; then
	instance[0]=5
	instance[1]=9
	instance[2]=8
	instance[3]=10
	instance[4]=11
elif [ $problem == "MAXSAT" ]; then
	instance[0]=3
	instance[1]=5
	instance[2]=4
	instance[3]=10
	instance[4]=11
elif [ $problem == "FlowShop" ]; then
	instance[0]=1
	instance[1]=8
	instance[2]=3
	instance[3]=10
	instance[4]=11
fi


	for i in ${instance[@]}
	do
    		for (( j = 0 ; j < $runs; j++ ))
    		do
          	java -Xms3058m -Xmx5848m -cp hypdp-0.0.1-SNAPSHOT-jar-with-dependencies.jar:build main.Main $problem $i $timeLimit $acc $scaling > resultados/$problem/$i/$problem'-'$i'-'$j'-ac-'$acc'-C-'$scaling.txt 
    		done
	done
