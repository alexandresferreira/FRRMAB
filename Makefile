run: clean compile exec

compile:
	javac  -cp hypdp-0.0.1-SNAPSHOT-jar-with-dependencies.jar -d build/ src/*/*.java
			
exec:
	java -cp hypdp-0.0.1-SNAPSHOT-jar-with-dependencies.jar:build main.Main
				
clean:
	rm -rf build/*
	clear
