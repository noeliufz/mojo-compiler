.PHONY: all build clean
all:	build
build:
	javacc -OUTPUT_DIRECTORY="lib/mojo/parse" src/mojo/Parser.jj
	javac -sourcepath lib -d bin src/*/*.java
clean:
	rm -rf bin lib output
