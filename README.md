<div align="center"> <a href="https://totalcross.com/" target="_blank"> <img src="./totalcross.gif" alt="totalcross logo"/></a></div>

<div align="center"> 
<h1> TotalCross</h1> </div>
<p align="center">The fastest way to build GUI for embedded devices</strong></em></p>

<div align="center">
  <a href="https://learn.totalcross.com/documentation/get-started" target="_blank">Get Started</a>
  <span>&nbsp;&nbsp;•&nbsp;&nbsp;</span>
  <a href="https://totalcross.com/" target="_blank">Website</a>
  <span>&nbsp;&nbsp;•&nbsp;&nbsp;</span>
  <a href="http://learn.totalcross.com/" target="_blank">Docs</a>
  <span>&nbsp;&nbsp;•&nbsp;&nbsp;</span>
  <a href="https://medium.com/totalcross-community" target="_blank">Blog</a>
  <span>&nbsp;&nbsp;•&nbsp;&nbsp;</span>
  <a href="https://t.me/totalcrosscommunity" target="_blank">Telegram</a>
  <span>&nbsp;&nbsp;•&nbsp;&nbsp;</span>
  <a href="https://www.youtube.com/c/totalcross" target="_blank">Videos</a>
  <span>&nbsp;&nbsp;•&nbsp;&nbsp;</span>
  <a href="https://totalcross.com/community/" target="_blank">Community</a>
</div>

[![FOSSA Status](https://app.fossa.com/api/projects/git%2Bgithub.com%2FTotalCross%2Ftotalcross.svg?type=shield)](https://app.fossa.com/projects/git%2Bgithub.com%2FTotalCross%2Ftotalcross?ref=badge_shield)

## Install

TotalCross exists to make Graphical User Interface creation easy.

To start you only need to install it on your machine, and you have two options:

### Install TotalCross VSCode plugin

The quickest way to start using TotalCross is to download the [VSCode plugin](https://marketplace.visualstudio.com/items?itemName=totalcross.vscode-totalcross). We highly recommend this route as it is a smoother process.

Make sure all dependencies are fulfilled ([Java JDK 1.8+](https://www.azul.com/downloads/zulu-community/?version=java-8-lts&architecture=x86-64-bit&package=jdk), [Maven 3.6.2+](https://maven.apache.org/download.cgi), and
[Microsoft Java Extension Plugin](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack)), create a new project, and you are ready to go!

### ... or run TotalCross from scratch yourself!

If you prefer to run TotalCross yourself so you can develop on your choice of IDE, clone our [HelloWorld](https://github.com/TotalCross/HelloWorld) repository, make sure you have all dependencies listed above in place, run `mvn package` and you are ready to go!

## Usage

This is how you create a button with TotalCross:

```java
package com.totalcross;
import totalcross.ui.gfx.Color;
import totalcross.sys.Settings;
import totalcross.ui.Button;
import totalcross.ui.MainWindow;
public class HelloWorld extends MainWindow {

    private Button btnRed;
    public HelloWorld(){
        setUIStyle(Settings.MATERIAL_UI);
    }
    @Override
    public void initUI(){
        btnRed = new Button("Red");
        btnRed.setBackForeColors(Color.RED, Color.WHITE);
        add(btnRed, CENTER,CENTER );
    }
}
```

This is how you extend a button to full screen width:

```java
     public void initUI() {
        btnRed = new Button("Red");
        btnRed.setBackForeColors(Color.RED, Color.WHITE);
        add(btnRed, CENTER, CENTER, PARENTSIZE, PREFERRED);
    }
```

This is how you round borders on a button:

```java
     public void initUI() {
        btnRed = new Button("Red", Button.BORDER_ROUND);
        btnRed.setBackForeColors(Color.RED, Color.WHITE);
        add(btnRed, CENTER, CENTER, PARENTSIZE, PREFERRED);
    }
```

This is how event handling happens:

```java
     public void initUI() {
        btnRed = new Button("Red", Button.BORDER_ROUND);
        btnRed.setBackForeColors(Color.RED, Color.WHITE);
        btnRed.addPressListener((event) -> {
            // DO SOMETHING
        });
        add(btnRed, CENTER, CENTER, PARENTSIZE, PREFERRED);
    }

```

Cool, right? Easy as pie! :)

## What next?

Check out our [documentation](https://learn.totalcross.com/documentation/components) or read through a [quick starting guide](https://learn.totalcross.com/documentation/get-started) (aprox. 8 minutes) and learn how TotalCross Components will save you tons of time when you build your GUI.

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
