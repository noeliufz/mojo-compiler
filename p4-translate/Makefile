.PHONY: all prep build clean
all:	build
prep:
	javacc -OUTPUT_DIRECTORY="lib/mojo/parse" src/mojo/Parser.jj
build:
	javac -cp bin -d bin src/*/*.java
clean:
	rm -rf lib output
	rm -rf bin/Translate bin/interp bin/x64 bin/Canon
	rm -rf bin/mojo/Translate* bin/mojo/Absyn* bin/mojo/Error* bin/mojo/ProcBody* bin/mojo/Scope*
	find . -name "*~" -exec rm {} \;
