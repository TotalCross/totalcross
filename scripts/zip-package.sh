#!/bin/bash

BASEDIR=$(cd ..; pwd)
INDIR=$BASEDIR/build/TotalCross
OUTDIR=$BASEDIR

FILENAME=$BASEDIR/TotalCross-4.5.0.zip
rm -f $FILENAME
zip -rq --exclude=*.DS_Store* $FILENAME $INDIR