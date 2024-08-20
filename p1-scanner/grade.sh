#!/bin/bash

# build

if [ "$1" = "-make" ]; then
    shift
    make clean && make all

    if [ "$?" != 0 ]; then
	echo "0 / 0 passed. (make failed)"
	exit 1
    fi
fi

if [ "$1" = "-color" ]; then
    shift
    RED='[0;31m'
    GREEN='[0;32m'
    NORM='[0;00m'
else
    RED=
    GREEN=
    NORM=
fi

# output/ stores the stdout, err, and diff of running each test
mkdir -p output/

if [ "$#" = 0 ]; then
    TESTS=tests/*.tok
else
    TESTS="$@"
fi

# try to use the timeout command if we can
TIMEOUT=$(which timeout)
if [ "$?" = "0" ]; then
    TIMEOUT="timeout 10"
else
    TIMEOUT=""
fi

# counts of test outcomes
passed=0
failed=0
bonus=0
total=0
# Run each test one by one.
for t in $TESTS; do
    
    total=$(($total + 1))   # count tests
    printf "run  $t"        # print status
    
    exp=$t.expect # expect file
    out=output/${t##tests/}.out    # output file (no slashes)
    err=output/${t##tests/}.err    # stderr file (no slashes)
    
    #############################################################
    # run the student's solution, using timeout if we have it
    $TIMEOUT java -ea -classpath bin/ ParserTokenManager < $t 2> $err > $out
    #############################################################
    
    # compare with expected output
    diff -B $exp $out 2>/dev/null > $out.diff

    # report status
    if [ "$?" = "0" ]; then
	printf "\r${GREEN}ok   ${NORM}\n"
	if [[ $t =~ "xc_" ]]; then
	    bonus=$(($bonus + 1))
	fi
	passed=$(($passed + 1))
    else
	printf "\r${RED}fail ${NORM}\n"
	failed=$(($failed + 1))
    fi
done

printf "%d / %d passed %d bonus\n" $passed $total $bonus
