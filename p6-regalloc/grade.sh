#!/bin/bash

help_text="$0 [...specific test cases; all if none listed] [options]
    --clean              Remove test output files
    --clean-only         Do not also run the tests after cleaning
    -c  --color          Enable coloured output
    -m  --make           Run make before tests
    -M  --make-clean     Run make clean and make before tests
    +e  --no-err         Ignore the output to stderr
    -h  --help           Print this help message
    -p  --parallel       Run all the tests in parallel
    -E  --show-err       Print the error output
    -O  --show-out-diff  Print the file diff between the program output and the 
                         expected output
    -s  --strict-errors  Check exact match of stderr output
"

# Modified from a script by Peter Oslington <peter.oslington@anu.edu.au>

shopt -s extglob 
trap 'printf "\n"; exit 1' INT

#######################################
# Argument Parsing                    #
#######################################

# Bash array of tests
TESTS=()

CLEAR="\33[2K"

# Parse arguments
for arg in "$@"; do

    ### Bunch of build options

    # Make from scratch source files
    if [[ $arg = "-M" ]] || [[ $arg = "--make-clean" ]]; then
      make clean && make all

      if [[ "$?" != 0 ]]; then
        echo "0 / 0 passed. (make failed)"
        exit 1
      fi

    # Run make before tests
  elif [[ $arg = "-m" ]] ||  [[ $arg = "--make" ]]; then
    make build

    if [[ "$?" != 0 ]]; then
      echo "0 / 0 passed. (make failed)"
      exit 1
    fi

    # Clean up all tests and exit
    # Will remove all .s files in the top level directory
  elif [[ $arg = "--clean-only" ]]; then
    rm -r output 2> /dev/null
    exit 0

    # Clean up all tests before running
    # Will remove all .s files in the top level directory
  elif [[ $arg = "--clean" ]]; then
    rm -r output 2> /dev/null

    ### Output formatting

    # Add colour output
  elif [[ $arg = "--color" ]] || [[ $arg == "-c" ]] ; then
    RED='[0;31m'
    GREEN='[0;32m'
    YELLOW='[0;33m'
    NORM='[0;00m'

    ### Printing behaviour

    # Dump output diff after test
  elif [[ $arg = "--show-out-diff" ]] || [[ $arg = "-O" ]]; then
    PRINT_OUTPUT_DIFF=1

    # Dump err after test
  elif [[ $arg = "--show-err" ]] || [[ $arg = "-E" ]]; then
    PRINT_ERROR=1

  elif [[ $arg = "--no-err" ]] || [[ $arg = "+e" ]]; then
    RUN_ERROR_TEST=1

  elif [[ $arg = "--strict-errors" ]] || [[ $arg = "-s" ]]; then
    RUN_ERROR_TEST=1

  elif [[ $arg = "--parallel" ]] || [[ $arg = "-p" ]] ; then
    PARALLEL=1

  elif [[ $arg = "--help" ]] || [[ $arg = "-h" ]]; then
    printf "$help_text"
    exit 0


    # Else assume is test name
  else
    if [[ ${arg:0:1} == "-" ]]; then
      echo "Unrecognized option: '$arg'"
      exit 1
    else
      TESTS+=("$arg")
    fi
    fi
  done

#######################################
# Define a bunch of testing functions #
#######################################

# Actually run the test, outputting to the normal folders
function run_test() {

  # Test name is first argument
  test_name=$1

  # Output files
  out=output/${test_name##tests/}.out # output file (no slashes)
  err=output/${test_name##tests/}.err # output file (no slashes)

  $TIMEOUT java -ea -cp bin/ mojo.Main $t 2> $err > $out

}

# Compare the output files to the expected values
function check_output() {

  # Test name is first argument
  test_name=$1

  expected_out=$t.out             # expected stdout
  out=output/${test_name##tests/}.out # output file (no slashes)
  outdiff=$out.diff

  diff --strip-trailing-cr $expected_out $out 2>/dev/null > $outdiff

  if [[ $? != 0 ]]
  then
    # Print errors
    if [[ $PRINT_OUTPUT_DIFF == 1 ]]; then
      printf "${CLEAR}${YELLOW}Output Diff:${NORM}\n"
      cat $outdiff
      echo
    fi
    return 1
  else    
    return 0
  fi

}

# Compare the error files to the expected values
function check_strict_error() {

  # Test name is first argument
  test_name=$1

  err=output/${test_name##tests/}.err 
  expected_err=${test_name}.err
  errdiff=$err.diff

  diff $expected_err $err 2>/dev/null > $errdiff

  # does the expected error file exist and is the error diff file nonempty
  # is there an expected err file
  if [[ -e $expected_err ]] 
  then

    # Check using the diff
    if [[ -s $errdiff ]]
    then
      # Print the error diff
      if [[ $PRINT_ERROR == 1 ]]
      then
        printf "${CLEAR}${YELLOW}Error Diff:${NORM}\n"
        cat $errdiff
        echo
      fi

      return 1
    fi
  else

    # Check that there were no errors
    if [[ -s $err ]]
    then
      # Print the error
      if [[ $PRINT_ERROR == 1 ]]
      then
        printf "${CLEAR}${YELLOW}Error Outputted:${NORM}\n"
        cat $err
        echo
      fi

      return 1
    fi
  fi

  return 0
}

function check_error() {

  # Test name is first argument
  test_name=$1

  err=output/${test_name##tests/}.err 
  expected_err=${test_name}.err


  # does the expected error file exist and is the error diff file nonempty
  # is there an expected err file
  if [[ -s $expected_err ]] 
  then

    errpat=$(head -n 1 $expected_err | sed "-es/column\ [0-9]*/column/g")
    touch $err
	  sed "-es/column\ [0-9]*/column/g" $err > $err.nocol
	  grep -F "$errpat" $err.nocol > /dev/null
    was_error=$?

    # Check using the diff
    if [[ $was_error != 0 ]]
    then
      # Print the error diff
      if [[ $PRINT_ERROR == 1 ]]
      then
        printf "${CLEAR}${YELLOW}Error Expected:${NORM}\n"
        cat $expected_err
        printf "${CLEAR}${YELLOW}Error Outputted:${NORM}\n"
        cat $err
        echo
      fi

      return 1
    fi
  else

    # Check that there were no errors
    if [[ -s $err ]]
    then
      # Print the error
      if [[ $PRINT_ERROR == 1 ]]
      then
        printf "${CLEAR}${YELLOW}Error Outputted:${NORM}\n"
        cat $err
        echo
      fi

      return 1
    fi
  fi

  return 0
}


#######################################
# Variable setup                      #
#######################################


# Setup for tests
mkdir -p output/

# Set default tests to all
if [ ${#TESTS[@]} == 0 ]; then
  TESTS=(tests/*.mj)
fi

# Use timeout if present
TIMEOUT=$(which timeout)
if [ "$?" = "0" ]; then
  TIMEOUT="timeout 10"
else
  TIMEOUT=""
fi

# During test print behaviour
PRINT_OUTPUT_DIFF=${PRINT_OUTPUT_DIFF:=0}
PRINT_ERROR_DIFF=${PRINT_ERROR_DIFF:=0}
PARALLEL=${PARALLEL:=0}

# Tests to run
RUN_OUTPUT_TEST=${RUN_OUTPUT_TEST:=1}
RUN_ERROR_TEST=${RUN_ERROR_TEST:=1}
RUN_STRICT_ERROR_TEST=${RUN_STRICT_ERROR_TEST:=0}



# Init counts
passed=0
failed=0
bonus=0
total=${#TESTS[@]}

#######################################
# Main test loop                      #
#######################################

if [ $PARALLEL == 1 ]; then
  for t in ${TESTS[@]}; do
    # Print running message
    printf "${YELLOW}run${NORM}   ${t}\n"
    # Generate output files
    run_test $t &
  done

  wait
fi

for t in ${TESTS[@]}; do

  if [ $PARALLEL == 0 ]; then
    # Print running message
    printf "${YELLOW}run${NORM}   $t\r"
    # Generate output files
    run_test $t
  fi

  # Expected output files
  expout=$t.out             # expected stdout
  experr=$t.err             # expected stderr

  if [[ $RUN_OUTPUT_TEST == 1 ]]
  then
    check_output $t
    out_res=$?
  else
    out_res=0
  fi

  if [[ $RUN_ERROR_TEST == 1 ]]
  then
    if [[ $RUN_STRICT_ERROR_TEST == 1 ]]
    then
      check_strict_error $t
      err_res=$?
    else
      check_error $t
      err_res=$?
    fi
  else
    err_res=0
  fi

  if [[ $out_res == 0 && $err_res == 0 ]]; then
    passed=$(($passed + 1))
    if [[ $t =~ "xc_" ]]; then
      bonus=$(($bonus + 1))
    fi
    printf "${CLEAR}${GREEN}ok   ${NORM} ${t}\n"
  else
    printf "${CLEAR}${RED}fail ${NORM} ${t}\n"
  fi
done

printf "%d / %d passed %d bonus\n" $passed $total $bonus

[ "$passed" = "$total" ]
exit $?
