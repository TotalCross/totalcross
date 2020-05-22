#!/bin/bash

mkdir -p /tmp/output/windows
echo TEST >/tmp/output/windows/data
exit 0

source $TRAVIS_BUILD_DIR/.travis/common.sh

remove_vm() {
    STATUS=$1
    MESSAGE=$2

    echo $MESSAGE: $STATUS
    gcloud compute instances delete $VMNAME --zone us-central1-b --quiet
    exit $STATUS
}


# get cloud credentials
openssl aes-256-cbc -d -a -pass pass:$JENKINKS_PASS -in jenkins-sa.json.enc -out ~/jenkins-sa.json -d || exit 1

# compile SDK
cd $TRAVIS_BUILD_DIR/TotalCrossSDK ; sh ./gradlew clean dist --console=plain
cd $TRAVIS_BUILD_DIR/TotalCrossVM/builders ; ant makeNativeHT
cd $TRAVIS_BUILD_DIR/LitebaseSDK/builders ; ant makeNativeHT
cd $TRAVIS_BUILD_DIR/LitebaseSDK ; ant device

# clean up files
cd $TRAVIS_BUILD_DIR
rm -rf .git
rm -rf gradle
rm -rf TotalCrossVM/builders/vc2013
rm -rf TotalCrossSDK/build
rm -rf TotalCrossSDK/etc/tools/sqlite
rm -rf TotalCrossSDK/etc/tools/jdeb
rm -rf TotalCrossSDK/etc/libs


cd ~
curl https://dl.google.com/dl/cloudsdk/channels/rapid/downloads/google-cloud-sdk-290.0.1-windows-x86_64.zip -o ~/cloud.zip || exit 1
unzip -q ./cloud.zip || exit 1
./google-cloud-sdk/install.sh --quiet || exit 1

gcloud config set disable_usage_reporting false || exit 1
gcloud auth activate-service-account --key-file ~/jenkins-sa.json || exit 1
gcloud config set project totalcross || exit 1



TIMESTAMP=`date +%s`
VMNAME=build-windows-$TIMESTAMP

# create
gcloud compute instances create $VMNAME \
      --image-family build-windows \
      --image-project totalcross \
      --scopes=compute-rw \
      --zone us-central1-b || exit 1
#      --preemptible
#      --metadata-from-file=startup-script=$TRAVIS_BUILD_DIR/.travis/windows-self-destroy.sh

# get vm external IP      
VM_EXTERNAL_IP=$(gcloud compute instances describe $VMNAME --format='value(networkInterfaces[0].accessConfigs[0].natIP)' --zone us-central1-b)
if [ "$VM_EXTERNAL_IP" = "" ] ; then remove_vm 1 "Unknown IP"; fi
echo "Using external IP '$VM_EXTERNAL_IP'"

echo "Sleeping for 100 seconds, waiting the Windows machine to boot"
sleep 100

echo Trying to connect with VM
# create dir
sshpass -p $VM_WINDOWS_PASS ssh -oStrictHostKeyChecking=no totalcrossplatform@$VM_EXTERNAL_IP "mkdir %userprofile%\\tc_repo\\" || remove_vm $? "Create folder"
echo "TC folder created"

# copy files
sshpass -p $VM_WINDOWS_PASS scp -r -oStrictHostKeyChecking=no $TRAVIS_BUILD_DIR/* totalcrossplatform@$VM_EXTERNAL_IP:%userprofile%/tc_repo/ || remove_vm $? "Copy files"
echo "TC files copied"

# Exec script
sshpass -p $VM_WINDOWS_PASS ssh -oStrictHostKeyChecking=no totalcrossplatform@$VM_EXTERNAL_IP "cd tc_repo && TotalCrossVM\\builders\\vc2008\\build.bat" || remove_vm $? "Build"
echo "Status of build"

# Remove VM
remove_vm 0 "All OK"

