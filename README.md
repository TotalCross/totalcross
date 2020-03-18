# TotalCross - A Free and Open GUI Creator for embedded system and mobile applications

TotalCross is an open source and free GUI Creator for embedded systems and a framework for developing cross-platform applications for android, iOS, wince, windows and desktop, Linux desktop and Linux arm using Kotlin or Java or any Java Based language.

### Where you can find us
* [Docs](learn.totalcross.com);
* [Issues](gitlab.com/totalcross/totalcross/issues);
* [Telegram](https://t.me/totalcrosscommunity);
* [Medium](https://medium.com/totalcross-community/about);
* [Youtube](https://www.youtube.com/channel/UCSXUBRBC4Ec3_o9R7-3XX-w);
* [Twitter](https://twitter.com/TotalCross);
* [Instagram](https://www.instagram.com/totalcross/).

## How TotalCross works?
![Usage flow](https://i.imgur.com/awacOIe.png)

The developer can use Kotlin or Java to create applications using TotalCross Java API which provides rich GUI components.  The application source code is then compiled resulting in the ByteCode Java, which is converted to our TotalCross Optimized Bytecode, packaged and distributed for the platforms the user specified which can be window and Linux desktop, iOS, Android and Linux arm.

## What is inside this repository?
O TotalCross SDK is comprised by two main components:

* TotalCross Java API -  It is the combination of several apis such as GUI components, database and GPIO to create a single more robust API that guarantees quality and support in application development
* TCVM. - The heart of totalcross sdk is present our virtual machine, originally idealized in a master's thesis, and already built and improved over 10 years. It's log-based (Java) architecture, bytecode "itself with its own folders" for the most frequent and implemented **almost 100% with C guarantees performance equivalent to native development**. To read more about the TCVM click here.


## Developing for TotalCross

In order to create a better tool and develop GUI for embedded systems and develop cross platform applications, it helps us in the community, so we have separated some challenges for those who want to contribute:

* Creating graphical components in the Java API:
    * Create support for maps;
    * Create customize widgets on camera.
* Improving the TC Java API:
    * Adding API port for Python.
* Improving a TC VM 
    * Create support for external and native libraries.
* Interacting in the [issues repository](https://gitlab.com/totalcross/TotalCross/-/issues);
    * Report and fixes new bugs;
    * Responding issues report;
    * Suggest and vote on new features.
* Make documentation easier and more complete:
    * Improving the [getting started](https://learn.totalcross.com/get-started/requirements);
    * Point out points of documentation improvements.

See how to [be a contributor](CONTRIBUTING.md)!

## RoadMap
Until 2020.2 the TotalCross team are working to launch:

* Know Code tool - To android XML for TotalCross;  
* Improve the VSCode plugin to facilitate the development process;
* Custom Cam; 
* Support for external and native library; 
* “Sharing” feature for Android and iOS;

To find out what TotalCross world domination plans are, just watch the [first 2020 Webinar](https://www.youtube.com/watch?v=iQfkP5lfzEw).
