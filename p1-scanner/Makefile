.PHONY: all build clean
all:	build
build:
	javacc -OUTPUT_DIRECTORY="lib/mojo/parse" src/mojo/Parser.jj
	javac -sourcepath lib -d bin lib/mojo/parse/*.java
clean:
	rm -rf bin lib output
