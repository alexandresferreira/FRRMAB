problem=$1

scaling[0]=0.1
scaling[1]=0.08
scaling[2]=0.05
scaling[3]=0.01
scaling[4]=0.008
scaling[5]=0.005
scaling[6]=0.001
scaling[7]=0.0008
scaling[8]=0.0005
scaling[9]=0.0001


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
elif [ $problem == "VRP" ]; then
        instance[0]=6
        instance[1]=2
        instance[2]=5
        instance[3]=1
        instance[4]=9
elif [ $problem == "PDP" ]; then
	instance[0]=1
	instance[1]=2
	instance[2]=3
	instance[3]=4
	instance[4]=5
	instance[5]=6
	instance[6]=7
	instance[7]=8
	instance[8]=9
	instance[9]=10
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
	  for c in ${scaling[@]}
	  do
		SUM=0.0
		MIN=99999999999.9
		FILE=resultados/$problem/$i/valores-$c.txt

		# Conta as linhas que tenham conteúdo
		LINES=$(wc -l $FILE | cut -d " " -f 1)

		for k in $(seq $LINES)
		do
   		LINEVALUE=$(cat $FILE | head -n $k | tail -n 1)
   		#soma
   		SUM=$(echo "x=$SUM+$LINEVALUE; if(x<1) print 0; x" | bc -l)
		 #minimo
	    	RESULT=$(echo $LINEVALUE'<'$MIN | bc -l)
   	    	if [ "$RESULT" -eq "1" ]; then
			MIN=$LINEVALUE
	    	fi
		done

		#media
		MEDIA=$(echo "x=$SUM/$LINES; x" | bc -l)

		echo "$MEDIA $MIN" >> resultados/$problem/$i/media.txt			
	     #echo -e >> resultados/$problem/$i/media.txt
	done
done
#EOF
