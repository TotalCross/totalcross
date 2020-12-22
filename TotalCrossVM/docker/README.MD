# Compiling and Testing TotalCross Virtual Machine for Linux/Linux ARM

First things first, in order to compile a virtual machine for Linux or Linux ARM, one need to install the following requirements on your machine:

- [Docker CE](https://docs.docker.com/get-docker/) ;
- [CMake](https://cmake.org/) 3.5.1 or later installed (if you are building from the target platform);

Once you have all requirements installed will be able to build a TotalCross Virtual Machine and test.

## Building the VM

Basically, to start building TotalCross VM for Linux and Linux ARM for testing, one just need two key elements: i) the `libtcvm.so` which represents our VM; and ii) the executable `Launcher` which loads the VM library and points the main tcz file of your application. 

### Using docker to build

The Dockerfiles and shell scripts required to build TotalCross for Linux take place on `TotalCrossVM/docker`, as you can see bellow:

```
TotalcrossVM/docker
├── amd64
│   ├── Dockerfile
│   ├── build
│   ├── build-docker.sh
│   ├── build.bat
│   └── cmake.sh
└── arm32v7
    ├── Dockerfile
    ├── build
    ├── build-docker.sh
    ├── build.bat
    └── cmake.sh
````

So, go the folder of the target architecture (`cd TotalCrossVM/docker/${arch}`) and execute the following steps:

- Build the docker image: `./build-docker.sh`;
- Execute the build file that uses the image above: `./cmake.sh`;

the build folder will take place at the same folder with the following structure:
```
build
├── CMakeCache.txt
├── CMakeFiles
├── Launcher
├── build.ninja
├── cmake_install.cmake
├── libtcvm.so
└── rules.ninja
```
The Launcher

## Testing the VM

To test the TotalCross Linux VM, one need to:

- copy Launcher and `libtcvm.so` to a folder inside your target device, i.e, raspberry pi;
- choose a TotalCross project to generate and copy `tcz` files to the same folder on your target device;
- execute Launcher

To do the first step, let's clone our [TCSample repo](https://github.com/TotalCross/tc-sample). Edit it's pom.xml to only generating the TotalCross Zip Files (tcz) by deleting `platforms` tag (lines ~ 59-63).
```xml
- <platforms>
-    <platform>-android</platform>
-    <platform>-linux</platform>
-    <platform>-linux_arm</platform>
- </platforms>
```
Run `mvn package` and the tcz files will take place at `target` folder.

```
target
└── install
    ├── linux
    └── linux_arm
```
The final folder on your target device will have the follow structure:
```
.
├── Launcher
├── libtcvm.so
├── Material_Icons.tcz
├── TCBase.tcz
├── TCFont.tcz
├── TCSample.tcz
└── TCUI.tcz
```
Execute the following command to test: `./Launcher TCSample`
