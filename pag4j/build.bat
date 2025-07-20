@echo off

mkdir .cxx
pushd .cxx
cmake ../src/main/cpp -G Ninja -DCMAKE_BUILD_TYPE=Release
cmake --build . --config Release
echo Build completed successfully.
popd
