#!/bin/sh

mkdir -p .cxx && cd .cxx
cmake ../src/main/cpp -DCMAKE_BUILD_TYPE=Release
make -j 4
echo Build completed successfully.
