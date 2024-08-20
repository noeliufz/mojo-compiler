.PHONY: all prep build clean
all:	build
prep:
	javacc -OUTPUT_DIRECTORY="lib/mojo/parse" src/mojo/Parser.jj
build:
	javac -cp bin -d bin src/*/*.java
clean:
	rm -rf bin/Assem bin/Canon bin/Mips bin/Translate bin/interp bin/RegAlloc
	rm -rf output
	find . -name "*~" -exec rm {} \;

MJ = java -cp bin mojo.Main -main
# TARGET = x64
TARGET = x64-osx
CC = gcc
CFLAGS = --target=x86_64-apple-macos

%.s:	tests/%.mj
	@echo "Compiling mj file to s file"
	$(MJ) -target=$(TARGET) $<
%:	tests/%.s
	$(CC) $(CFLAGS) -o $@ $<
