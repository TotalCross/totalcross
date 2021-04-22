#!/bin/bash
checkfile(){
    DIR=$1
    FILE=$2
    if(test -f "$DIR/$FILE") then
        FILESIZE=$(wc -c "$DIR/$FILE" | xargs | cut -f1 -d ' ')
        echo "$DIR/$FILE" "has"  $FILESIZE "bytes"
    else
        echo -e  "\033[31m$DIR/$FILE WAS NOT FOUND\033[m"
    fi
}
checkdir(){
    DIR=$1
    FILE=$2
    if(test -d "$DIR/$FILE") then
        FILESIZE=$(du -sk "$DIR/$FILE" | cut -f1)
        echo "$DIR/$FILE" "has"  $FILESIZE "Kbytes"
    else
        echo -e  "\033[31m$DIR/$FILE WAS NOT FOUND\033[m"
    fi
}

BASEDIR=$(cd ..; pwd)
OUTDIR=$BASEDIR/build

pushd $OUTDIR
    #tcz
    checkfile TotalCross/dist/vm LitebaseLib.tcz
    checkfile TotalCross/dist/vm TCBase.tcz
    checkfile TotalCross/dist/vm TCFont.tcz
    checkfile TotalCross/dist/vm TCUI.tcz
    #ANDROID
    checkfile TotalCross/dist/vm/android TotalCross.apk
    checkfile TotalCross/etc/tools/android AndroidManifest_singleApk.xml
    checkfile TotalCross/etc/tools/android resources_singleApk.arsc
    checkfile TotalCross/etc/tools/android AndroidManifest_includeSms.xml
    checkfile TotalCross/etc/tools/android resources_includeSms.arsc
    checkfile TotalCross/etc/launchers/android resources.ap_
    checkfile TotalCross/etc/launchers/android Launcher.jar
    #IOS
    checkfile TotalCross/dist/vm/ios TotalCross.ipa
    checkdir TotalCross/etc/tools/iOSCodesign TotalCross.xcarchive
    checkfile TotalCross/etc/launchers/ios Launcher
    #WIN32
    checkfile TotalCross/dist/vm/win32 TCVM.dll
    checkfile TotalCross/dist/vm/win32 Litebase.dll
    checkfile TotalCross/etc/launchers/win32 Launcher.exe
    #Wince
    checkfile TotalCross/dist/vm/wince TotalCross.CAB
    checkfile TotalCross/dist/vm/wince Litebase.dll
    checkfile TotalCross/dist/vm/wince Bematech.dll
    checkfile TotalCross/dist/vm/wince CEinstall.ini
    checkfile TotalCross/dist/vm/wince CabWiz.log
    checkfile TotalCross/dist/vm/wince Dolphin.dll
    checkfile TotalCross/dist/vm/wince Intermec.dll
    checkfile TotalCross/dist/vm/wince Motorola.dll
    checkfile TotalCross/dist/vm/wince OpticonH16.dll
    checkfile TotalCross/dist/vm/wince Pidion.dll
    checkfile TotalCross/dist/vm/wince TCVM.dll
    checkfile TotalCross/dist/vm/wince TotalCross.inf
    checkfile TotalCross/dist/vm/wince _CEinstall-RunMe.bat
    checkfile TotalCross/etc/launchers/wince Launcher.exe
popd

