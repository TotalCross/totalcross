# Compiling and Testing TotalCross Virtual Machine for Windows or WinCE
First things first, in order to compile a virtual machine for Windows desktop, one need to install the following requirements on your machine:

- [Visual Studio](visualstudio.microsoft.com) latest version;
- Visual Studio 2008(only needed to build WinCE);
- [Cmake](https://cmake.org/) 3.5.1 or later installed;
- [Visual Studio Code](visualstudio.microsoft.com) latest version.

Once you have all requirements installed will be able to build a TotalCross Virtual Machine, test and debug it on Visual Studio Code.

## Building the VM

### Windows

In order to be able to start a change on the iOS VM, we need to generate the Visual Studio project that describes our VM (TCVM.sln). To do so, execute the following commands on you terminal:

- go to the project folder: `cd TotalCrossVM/vc2008`;
- to generate TCVM win32 project: `cmake ../ -G"Visual Studio 9 2008"`;
- to generate TCVM wince project: `cmake ../ -G"Visual Studio 9 2008 Pocket PC 2003 (ARMV4)"`.

Next is building the `TCVM.dll` for this you'll need VSCode and the CMake Tools extension. For this first you need to set the toolchain to a `x86` one like on the image below:
![](https://imgur.com/YY3VvLV.png)

Then you set the target to `tcvm`:
![](https://imgur.com/PIjFkhi.png)

Lastly for the build press Build and your tcvm.dll will be ready to debug your applcation:
![](https://imgur.com/YLo5AaS.png)

Now we are able to develop new features and fix some bugs and Test on your Visual Studio Code.

### Windows CE

For Windows CE you only need to build the tcvm project within TCVM.sln. For this you need to check the `Solution Configurations` is set to `Debug` and `Platform Configurations` is set to `Pocket PC 2003 (ARMV4)`, then you can build the project:
![](https://imgur.com/MFwBfvP.png)

## Testing the VM

### Windows

Now that all the files are on To do the first step, let's clone our [TCSample repo](https://github.com/TotalCross/tc-sample). Edit it's pom.xml to generate the Windows application `platforms` tag (lines ~ 59-63).
```xml
 <platforms>
    <platform>-wince</platform>
    <platform>-win32</platform>
 </platforms>
```
Run `mvn package` and the tcz files will take place at `target\install\win32` folder.

```
win32
    ├── LitebaseLib.tcz
    ├── Material_Icons.tcz
    ├── Roboto Medium.tcz
    ├── TCBase.tcz
    ├── TCFont.tcz
    ├── TCSample.exe
    ├── TCSample.tcz
    ├── TCUI.tcz
    └── TCVM.dll
```
On your `TotalCrossVM\build` folder, copy all the files with the exception of `TCVM.dll` from the TCSample's `target\install\win32` folder.

On the TotalCross project on your VSCode go to the Run tab and create your launch.json file
![](https://imgur.com/npdLmQX.png)

Choose `C++ (Windows)` on the menu
![](https://imgur.com/8J0jV2P.png)

Doing this will create a `launch.json`. On it you have to change the program to the path to your exe on the build folder and the args to what you want ie.:
```json
    "program": "${workspaceFolder}/build/TCSample.exe",
    "args": ["/scr -1,-1,800,600"],
```

This would run your simulator for debug on 800x600, these parameters can be seen on our [Device Simulator Documentation](https://learn.totalcross.com/documentation/guides/device-simulator#screen-sizes-parameters). Now your app is ready to debug!

### Windows CE

**Step 1:** Package your application to get the .exe and .tcz of your application:

![Files you will need.](https://raw.githubusercontent.com/TotalCross/totalcross-docs/master/.gitbook/assets/packaged_files.png)

**Step 2:** build your SDK, this can be done by going to your TotalCrossSDK folder on a prompt and running `gradlew dist` \(this step is only needed if you haven't done it before\)

![Building your sdk to use on the debug.](https://raw.githubusercontent.com/TotalCross/totalcross-docs/master/.gitbook/assets/building_sdk.gif)

**Step 3:** open the TCVM solution with Visual Studio 2008 Professional Edition, the TCVM.sln file is located at TotalCrossVM/builders/vc2008:

![How to open the TCVM.sln file.](https://raw.githubusercontent.com/TotalCross/totalcross-docs/master/.gitbook/assets/open_solution.gif)

**Step 4:** set TCVM as the StartUp Project:

![Changing the StartUp Project](https://raw.githubusercontent.com/TotalCross/totalcross-docs/master/.gitbook/assets/set_as_startup_project.gif)

**Step 5:** change the emulator to USA Windows Mobile 5.0 Pocket PC T2 Emulator and the solution platform to Pocket PC 2003\(ARMV4\):

![Changing the emulator and solution platform.](https://raw.githubusercontent.com/TotalCross/totalcross-docs/master/.gitbook/assets/changing_emulator_and_solution_platform.gif)

**Step 6:** set your application for deployment:`YourApp.tcz|path\to\you\app\tcz|\TotalCross|0 YourApp.exe|path\to\you\app\exe|\TotalCross|0`change the path accordingly to your application wince folder\(if you didn't change anything it should be on target/install/wince on your app folder\).

![](https://raw.githubusercontent.com/TotalCross/totalcross-docs/master/.gitbook/assets/changing_emulator_and_solution_platform%20(1).gif)

**Step 7:** run the debug:

![Running the debug!](https://raw.githubusercontent.com/TotalCross/totalcross-docs/master/.gitbook/assets/running_debug.gif)

> :warning: If you are debugging on wince **and** win32 you need to remove the CMakeCache.txt after you you run cmake for the first architecture.