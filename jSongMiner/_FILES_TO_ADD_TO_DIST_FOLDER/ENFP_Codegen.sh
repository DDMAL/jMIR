#!/bin/bash

# This script runs the Echo Nest fingerprinter binary. The first
# parameter to this script must be the path of the directory
# holding this binary. The remaining paramters are passed to the
# binary itself. 


# Change to the directory specified in the first parameter
cd $1
shift

# Set up the environment to run the Echo Nest fingerprinter binary
pushd /research/cory/greenstone2-svn/ext/video-and-audio
source setup.bash
popd
export LD_LIBRARY_PATH=.:$LD_LIBRARY_PATH

# Run the Echo Nest fingerprinter binary
./codegen.Linux-i686 $*
