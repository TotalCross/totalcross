# 1. Introduction

In order to have build `libtcvm.so` and the TotalCross Launcher for **linux arm** devices you're gonna have to install **docker 2.0.4.0 or higher**. Also, make sure you have checked **Experimental features** by going in the docker `Preferences > Daemon > Experimental Features`. This will enable you to use the feature `docker buildx` allowing to build linux arm containers.

The following structure of this guide is divided in: section 2 shows how to build the totalcross docker image builder for linux arm devices, section 3 demonstrates how to build SDL static library `libSDL2.a` which is a prerequisite of `libtcvm.so`, section 4 shows how to build the libtcvm.so, section 5 show how to build the executable file `Launcher` and section 6 demonstrates how to test the changes made.

# 2. Building Docker Image Builder for linux-arm devices 

Open Terminal and `cd` to `vm/TotalCrossVM/builder/gcc-linux-arm/docker-builder-image`. Type and execute the `build.sh`. 

This process takes a little bit of time, wait until `debian:jessie` is pulled from the docker oficial repository and all packages dependencies are installed. When the process is done, type the command `docker images` and make sure `totalcross/cross-compile` appears in your list of docker images, as follows:

```shell
REPOSITORY                 TAG                 IMAGE ID            CREATED             SIZE
totalcross/cross-compile   latest              a9e9c6b93519        25 hours ago        480MB
<none>                     <none>              1a0e943d6239        27 hours ago        464MB
.
.
.

```

Once you have this docker image working, unless you don't have a new dependencie package to add, you don`t need to rebuild this docker images everytime you make a change in the totalcross sources.

# 3. Building SDL 

As explaneid before. We use `totalcross/cross-compile` to build all required libraries, except `libskia.a` for building `libtcvm.so`.

Thus, open Terminal and `cd` to `vm/TotalCrossVM/builder/gcc-linux-arm/sdl`. Execute the command `./build.sh`. This procedure will take a little bit of time, creating in the end the folder `build` which contains our required library `libSDL2.a`.

You had to statically rebuild this library with no audio component to ensure that the **SDL2** would run in the linux Colibri distribution that comes by default on the **Toradex** boards without installing any dependencie.

# 4. Building the heart of our Virtual Machine (libtcvm.so)

No more explanation needed, we have **SDL2** compiled and `totalcross/cross-compile` working. In the terminal, go to `vm/TotalCrossVM/builder/gcc-linux-arm/tcvm` and execute `build.sh`. Our library `libtcvm.so` will be inside `bin/`, folder created after the process of compiling the lib is finished.

# 5. Build the Launcher

Our Launcher, which is our executable file requires `libtcvm.so`. That's why this is our last component to be built. Go to `vm/TotalCrossVM/builder/gcc-linux-arm/launcher` and execute `build.sh`, and that's all. Our executable will be rest in the folder `bin/` created after the build process.

# 6. Testing Everything

Choose a totalcross SDK installation to copy `libtcvm.so` to `[YOUR SDK INSTALLATION FOLDER]/dist/vm/linux_arm` and the executable `Launcher` to `[YOUR SDK INSTALLATION FOLDER]/etc/launchers/linux_arm`. Set the totalcross deployer to use this SDK installation and add the flag `-linux_arm` to the tc.Deploy args. 

