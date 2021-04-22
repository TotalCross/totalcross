#!/bin/bash

BASEDIR=$(cd ..; pwd)
OUTDIR=$BASEDIR/build/TotalCross

echo "STARTING iOS"

# iOS
IOS_USER=user180640
IOS_SERVER=NY741.macincloud.com

scp build-ios.sh $IOS_USER@$IOS_SERVER:~/build/
ssh $IOS_USER@$IOS_SERVER "~/build/build-ios.sh"
scp $IOS_USER@$IOS_SERVER:~/build/ios.zip $BASEDIR/build/ios.zip
unzip -u -j -q $BASEDIR/build/ios.zip **/TotalCross.ipa -d $OUTDIR/dist/vm/ios
unzip -u -q $BASEDIR/build/ios.zip TotalCross.xcarchive/* -d $OUTDIR/etc/tools/iOSCodesign
