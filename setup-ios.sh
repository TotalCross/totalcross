#!/bin/bash
  
KEYCHAIN_PASS=pxt22195
security unlock-keychain -p $KEYCHAIN_PASS login.keychain

FILE=${PWD}/ScanditBarcodeScanner.framework-b1.zip
if [ ! -f "$FILE" ]; then
    echo "$FILE does not exist."
    curl https://totalcross-transfer.s3-us-west-2.amazonaws.com/ScanditBarcodeScanner.framework-b1.zip --output ScanditBarcodeScanner.framework-b1.zip
fi
