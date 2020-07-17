## What is TotalCross?

TotalCross exists to make Graphical User Inteface creation easy:

- [KnowCode](https://github.com/TotalCross/KnowCodeXML): A computer vision project to help developers build user interfaces in less time. A library that converts a design image to a file that runs android XML UI + TotalCross SDK on Linux Arm, iOS, Android and more;
- TotalCross SDK: Is a free open source GUI Creator and framework for developing cross-platform applications for Embedded Systems, Android, iOS, Wince, Windows and Linux desktop applications, and Linux Arm. Current supported languages are Kotlin, Java and any Java Based language.

[![FOSSA Status](https://app.fossa.com/api/projects/git%2Bgithub.com%2FTotalCross%2Ftotalcross.svg?type=shield)](https://app.fossa.com/projects/git%2Bgithub.com%2FTotalCross%2Ftotalcross?ref=badge_shield)

## How TotalCross SDK works?

![Usage flow](https://i.imgur.com/Df3NGui.png)

You can use Kotlin or Java to create applications using TotalCross Java API which provides rich GUI components. The application source code is then compiled resulting in ByteCode Java, which is converted to TotalCross Optimized Bytecode, packaged and distributed to the platforms the user specifies which can be Window and Linux desktop, iOS, Android and Linux ARM.

## What is inside this repository?

The TotalCross SDK, comprised by two main components:

- TotalCross Java API - Combination of several APIs such as GUI components, Database and GPIO to create a single more robust API that guarantees quality and support in application development.
- TCVM - Totalcross Virtual Machine, originally idealized as a Master's Thesis, has been built and improved over 10 years. It's log-based (Java) architecture, bytecode "itself with its own folders" for the most frequent and implemented **almost 100% with C guarantees performance equivalent to native development**. To read more about the TCVM click [here](https://learn.totalcross.com/#virtual-machine-features).

## How do I join the Community?

TotalCross's goal is to build a large and supportive community composed by enthusiastic mobile and embedded developers. You can be part of the [telegram](https://t.me/totalcrosscommunity) and here on [GitHub](https://github.com/totalcross/totalcross).

### How do I become a TotalCross contributor?

In order to create a better tool and develop GUI for embedded systems and develop cross platform applications, it helps us in the community, so we have separated some challenges for those who want to contribute:

- Creating graphical components in the Java API:
  - Create support for maps;
  - Create customized widgets on camera.
- Improving the TC Java API:
  - Adding API port for Python.
- Improving the TCVM:
  - Create support for external and native libraries.
- Interacting in the [issues repository](https://github.com/TotalCross/totalcross/issues);
  - Reporting and fixing new bugs;
  - Responding issues report;
  - Suggesting and voting on new features.
- Making documentation more clear and complete:
  - Improving the [getting started](https://learn.totalcross.com/get-started/);
  - Pointing out improvements needed in our documentation and suggest new themes by opening [issues](https://github.com/TotalCross/totalcross/issues).

##### 👉 [More information on how to become a contributor on our CONTRIBUTING.md file](CONTRIBUTING.md)!
