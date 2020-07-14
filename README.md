<a href="https://totalcross.com/" target="_blank">![TotalCross](./logo_header.png)</a>

<div align="center"> 
<h1> TotalCross</h1> </div>
<p align="center">Create your Graphical User Inteface in <em><strong>record time!</strong></em></p>

## Start coding with TotalCross

TotalCross exists to make Graphical User Interface creation easy.

To start you only need to install it in your machine, and you have two options:

### Install TotalCross VSCode plugin

The quickest way to start using TotalCross is to download the [VSCode plugin](https://marketplace.visualstudio.com/items?itemName=totalcross.vscode-totalcross). We highly recommend this route as it is a smoother process.

Make sure all dependencies are fulfilled ([Java JDK 1.8+](https://www.azul.com/downloads/zulu-community/?version=java-8-lts&architecture=x86-64-bit&package=jdk), [Maven 3.6.2+](https://maven.apache.org/download.cgi), and
[Microsoft Java Extension Plugin](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack)), create a new project, and you are ready to go!

### ... or Compile TotalCross yourself

If you prefer to clone our repository and compile it yourself the process is a bit more involved, but we'll guide you through it.

First you will build the TotalCross SDK and next you will build the TotalCross VM.

#### Building TotalCross SDK

After cloning the project (`git clone https://github.com/TotalCross/totalcross.git TotalCross`) you will have:

```bash
TotalCross/
├─ LitebaseSDK/
├─ TotalCrossSDK/
└─ TotalCrossVM/
```

You will need to enter inside `TotalCrossSDK` folder, please:

```bash
$ cd TotalCrossSDK
```

The next step you need to call _Gradle_:

```bash
~/TotalCrossSDK$ ./gradlew dist
```

If you don't have any package errors, your folder will be something like this:

```bash
TotalCross
├─ LitebaseSDK
├─ TotalCrossSDK
│    ├─ bin/
│    ├─ build/
│    ├─ dist/
│    │    ├─vm/
│    │    │    ├─ TCBase.tcz
│    │    │    ├─ TCFont.tcz
│    │    │    └─ TCUI.tcz
│    │    └─totalcross-sdk.jar
│    ├─ docs/
│    ├─ etc/
│    ├─ gradle/
│    ├─ src/
│    ├─ build.gradle
│    ├─ build.xml
│    ├─ gradlew
│    ├─ gradlew.bat
│    ├─ license.txt
│    └─ proguard.txt
└─ TotalCrossVM
```

Look to the `dist` folder, if you have the same files you just need to copy `dist` to your valid SDK folder

```bash
~/TotalCrossSDK$ cp -r dist $PATH_TO_VALID_SDK/
```

#### Build TotalCross SDK

These are the steps to generate your custom VM. Our build process **needs Docker**, please [install it](https://docs.docker.com/get-docker/) and check your installation:

```bash
$ docker --version
```

After cloning the project (`git clone https://github.com/TotalCross/totalcross.git TotalCross`) you will have:

```bash
TotalCross/
├─ LitebaseSDK/
├─ TotalCrossSDK/
└─ TotalCrossVM/
```

You will need to enter inside `TotalCrossVM/builders` folder, please:

```bash
$ cd TotalCrossVM/builders
```

Your folder structure will be something like this:

```bash
TotalCross
├─ LitebaseSDK
├─ TotalCrossSDK
└─ TotalCrossVM
     ├─ builders/
     │    ├─ droid/
     │    ├─ gcc-linux-arm/
     │    ├─ gcc-posix/
     │    ├─ vc2008/
     │    ├─ vc2013/
     │    ├─ vc2017/
     │    ├─ xcode/
     │    └─ build.xml
     ├─ deps/
     └─ src/
```

### Linux x86-64

Enter into `gcc-posix` folder:

```bash
~TotalCrossVM/builders$ cd gcc-posix
```

First, let's build the docker image:

```bash
~TotalCrossVM/builders/gcc-posix$ cd docker
~TotalCrossVM/builders/gcc-posix/docker$ ./build.sh
```

If you have no problems you should check the image:

```bash
~TotalCrossVM/builders/gcc-posix/docker$ docker images
REPOSITORY                              TAG                 IMAGE ID            CREATED             SIZE
totalcross/amd64-cross-compile          bionic              cd8fb68f0fc6        a minute ago        1.03GB
<none>                                  <none>              1a0e943d6239        27 hours ago        464MB
.
.
.
~TotalCrossVM/builders/gcc-posix/docker$ cd ..
```

Next, let's build `libtcvm.so`:

```bash
~TotalCrossVM/builders/gcc-posix$ cd tcvm
~TotalCrossVM/builders/gcc-posix/tcvm$ ./build.sh
```

If you don't have any build errors, your folder will be something like this:

```bash
TotalCross
├─ LitebaseSDK
├─ TotalCrossSDK
└─ TotalCrossVM
     ├─ builders/
     │    ├─ droid/
     │    ├─ gcc-linux-arm/
     │    ├─ gcc-posix/
     │    │    ├─ docker
     │    │    ├─ launcher
     │    │    └─ tcvm
     │    │         ├─ bin/
     │    │         │    └─ libtcvm.so
     │    │         ├─ build.sh
     │    │         ├─ libskia.a
     │    │         └─ Makefile
     │    ├─ vc2008/
     │    ├─ vc2013/
     │    ├─ vc2017/
     │    ├─ xcode/
     │    └─ build.xml
     ├─ deps/
     └─ src/
```

Look to the `bin` folder, now you just need to copy `libtcvm.so` to your valid SDK folder

```bash
~TotalCrossVM/builders/gcc-posix/tcvm$ cp bin/libtcvm.so $PATH_TO_VALID_SDK/dist/vm/linux
```

### Linux ARM

Enter into `gcc-linux-arm` folder:

```bash
~TotalCrossVM/builders$ cd gcc-linux-arm
```

First, let's build the docker image:

```bash
~TotalCrossVM/builders/gcc-linux-arm$ cd docker-builder-image
~TotalCrossVM/builders/gcc-linux-arm/docker-builder-image$ ./build.sh
```

If you have no problems you should check the image:

```bash
~TotalCrossVM/builders/gcc-posix/docker$ docker images
REPOSITORY                              TAG                 IMAGE ID            CREATED             SIZE
totalcross/totalcross/cross-compile     latest              cd8fb68f0fc6        a minute ago        1.03GB
<none>                                  <none>              1a0eea5a6dv0        15 hours ago        1.46GB
.
.
.
~TotalCrossVM/builders/gcc-linux-arm/docker-builder-image$ cd ..
```

Next, let's build `libtcvm.so`:

```bash
~TotalCrossVM/builders/gcc-linux-arm$ cd tcvm
~TotalCrossVM/builders/gcc-linux-arm/tcvm$ ./build.sh
```

If you don't have any build errors, your folder will be something like this:

```bash
TotalCross
├─ LitebaseSDK
├─ TotalCrossSDK
└─ TotalCrossVM
     ├─ builders/
     │    ├─ droid/
     │    ├─ gcc-linux-arm/
     │    │    ├─ docker-builder-image
     │    │    ├─ launcher
     │    │    └─ tcvm
     │    │         ├─ bin/
     │    │         │    └─ libtcvm.so
     │    │         ├─ build.sh
     │    │         ├─ libskia.a
     │    │         └─ Makefile
     │    ├─ gcc-posix/
     │    ├─ vc2008/
     │    ├─ vc2013/
     │    ├─ vc2017/
     │    ├─ xcode/
     │    └─ build.xml
     ├─ deps/
     └─ src/
```

Look to the `bin` folder, now you just need to copy `libtcvm.so` to your valid SDK folder

```bash
~TotalCrossVM/builders/gcc-linux-arm/tcvm$ cp bin/libtcvm.so $PATH_TO_VALID_SDK/dist/vm/linux_arm
```

## What next?

Check our documentation for a [quick starting guide](https://learn.totalcross.com/documentation/get-started) (aprox. 8 minutes) and learn how TotalCross Components will save you tons of time when you build your GUI.

## Have any questions?

Join our [Telegram group](https://t.me/totalcrosscommunity). [Bruno](https://github.com/brunoamuniz) and [Italo](https://github.com/ItaloYeltsin) are super quick to welcome and provide help to new users.

There's also a handy [FAQ.md](./FAQ.md) file with all sorts of useful information, as what is inside this repo, how TotalCross works, how to become a contributor, and more.

## Our contributors

We'd like to give a BIG shout-out to our three first external contributors! These people have helped make TotalCross better by enriching ongoing discussions, reporting bugs, opening issues, and publishing relevant content (videos, articles and etc):

- [@otavio](https://github.com/otavio)
- [@jeffque](https://github.com/jeffque)
- [@microhobby](https://github.com/microhobby)

Guys, you rock!

## RoadMap

Find out what TotalCross world domination plans are by clicking [here](https://learn.totalcross.com/roadmap).

### Where you can find us:

- [Docs](learn.totalcross.com);
- [Issues](gitlab.com/totalcross/totalcross/issues);
- [Telegram](https://t.me/totalcrosscommunity);
- [Medium](https://medium.com/totalcross-community/about);
- [Youtube](https://www.youtube.com/channel/UCSXUBRBC4Ec3_o9R7-3XX-w);
- [Twitter](https://twitter.com/TotalCross);
- [Instagram](https://www.instagram.com/totalcross/).
