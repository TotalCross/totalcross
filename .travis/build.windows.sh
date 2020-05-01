#!/bin/bash


cd $HOME
curl https://dl.google.com/dl/cloudsdk/channels/rapid/downloads/google-cloud-sdk-290.0.1-windows-x86_64.zip -o cloud.zip
unzip $HOME/cloud.zip

./google-cloud-sdk/install.bat --quiet

