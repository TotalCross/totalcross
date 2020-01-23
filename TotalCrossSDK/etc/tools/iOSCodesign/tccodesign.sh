#!/bin/bash -x

# Parse variables
POSITIONAL=()
while [[ $# -gt 0 ]]
do
key="$1"

case $key in
    --output|-o)
    output="$2"
    shift # past argument
    shift # past value
    ;;
    --icon)
    sourceImage="$2"
    shift # past argument
    shift # past value
    ;;
    --ipa)
    ipaDest="$2"
    shift # past argument
    shift # past value
    ;;
    -prov|--provision-file)
    provisionFile="$2"
    shift # past argument
    shift # past value
    ;;
    -c|--certificate)
    certificateName="$2"
    shift # past argument
    shift # past argument
    ;;
    -m|--method)
    method="$2"
    shift # past argument
    shift # past argument
    ;;
    *)    # unknown option
    POSITIONAL+=("$1") # save it in an array for later
    shift # past argument
    ;;
esac
done
set -- "${POSITIONAL[@]}" # restore positional parameters

BASEDIR=$(dirname $0)
workDir=$(cd $BASEDIR; pwd)

#if outputArg is empty, build to build folder
if [ -z "$output" ]
then
    output="build"
fi

# if icon is null, use default TotalCross icon
if [ -z "$sourceImage" ]
then
    sourceImage="${BASEDIR}/icone-totalcross.png"
fi

echo ====================== INFO =========================
echo 'source image: '$sourceImage
echo 'ipa: '$ipaDest	
echo 'certificate name: '$certificateName
echo 'provision file: '$provisionFile
echo 'method: '$method
echo 'build dir: '$output
echo ======================= LOG =========================



#create temp dir
$(mkdir -p "${workDir}/tmp")
tempDir="$(mktemp -d ${workDir}/tmp/foo.XXXX)"

appDir=$(unzip -l "${ipaDest}" | egrep -o -m1 'Payload.*.app');
appName=$(echo "${appDir}" | sed -e 's/Payload\/\(.*\).app/\1/')

cp -R "${BASEDIR}/TotalCross.xcarchive/" "${tempDir}/${appName}.xcarchive/"



mv "${tempDir}/${appName}.xcarchive/Products/Applications/TotalCross.app" "${tempDir}/${appName}.xcarchive/Products/Applications/${appName}.app"

# Extract plist and tczs from ipa
unzip -jo "${ipaDest}" "${appDir}/Info.plist" -d "${tempDir}"
# Extract GoogleService-Info and convert to binary format
unzip -jo "${ipaDest}" "${appDir}/GoogleService-Info.plist" -d "${tempDir}"
# Extract all tczs
unzip -jo "${ipaDest}" "${appDir}/*.tcz" -d "${tempDir}/tczs"
# Extract files in pkg folder
unzip -jo "${ipaDest}" "${appDir}/pkg/*" -d "${tempDir}/pkg"

#New Info.plist
Infoplist="${tempDir}/${appName}.xcarchive/Products/Applications/${appName}.app/Info.plist"

# Copy tczs to ${tempDir}/${appName}.xcarquive/Products/Applications/*.app/
cp "${tempDir}"/tczs/*.tcz "${tempDir}/${appName}.xcarchive/Products/Applications/${appName}.app/"
# Copy Infoplist to ${tempDir}/${appName}.xcarquive/Products/Applications/*.app/
cp "${tempDir}/Info.plist" ${Infoplist}
# Copy GoogleService-Info
cp "${tempDir}/GoogleService-Info.plist" "${tempDir}/${appName}.xcarchive/Products/Applications/${appName}.app/"
# Copy files in pkg folder
cp -R "${tempDir}/pkg" "${tempDir}/${appName}.xcarchive/Products/Applications/${appName}.app/pkg"


# Build assets.car
bash "${BASEDIR}/xcassetsGenerator.sh" "${sourceImage}" "${tempDir}"
mkdir -p "${tempDir}/assets"
xcrun actool "${tempDir}/Assets.xcassets" --compile "${tempDir}/assets" --platform iphoneos --minimum-deployment-target 8.0 --app-icon AppIcon --output-partial-info-plist "${tempDir}/assets/partial.plist"

# Build assets.car
cp "${tempDir}"/assets/Assets.car "${tempDir}/${appName}.xcarchive/Products/Applications/${appName}.app/Assets.car"
cp "${tempDir}"/assets/AppIcon* "${tempDir}/${appName}.xcarchive/Products/Applications/${appName}.app/"

# Copy CFBundleIcons and *ipad to Info.plist
/usr/libexec/PlistBuddy  ${verbose} -c "Delete :CFBundleIconFiles" ${Infoplist}
/usr/libexec/PlistBuddy ${verbose} -c "Merge ${tempDir}/assets/partial.plist" ${Infoplist}

/usr/libexec/PlistBuddy ${verbose} -c "Set :DTPlatformVersion 12.0" ${Infoplist}
/usr/libexec/PlistBuddy ${verbose} -c "Set :DTSDKName iphoneos12.0" ${Infoplist} 

# Modify entitlements for push notification
cp "${workDir}/TotalCross.entitlements" "${tempDir}/TotalCross.entitlements"

 if [ $method != "development" ]
 then
     plutil -replace aps-environment -string production "${tempDir}/TotalCross.entitlements"
 fi

#Signing 
rm -rf "${tempDir}/${appName}.xcarchive/Products/Applications/${appName}.app/_CodeSignature"
cp "${provisionFile}" "${tempDir}/${appName}.xcarchive/Products/Applications/${appName}.app/embedded.mobileprovision"

#Check if mobileprovision contains aps-environment in its entitlements
apsType=$(/usr/libexec/PlistBuddy -c 'Print :Entitlements:aps-environment' /dev/stdin <<< $(security cms -D -i "${provisionFile}"))
if [ "${apsType}" = "development" ] || [ "${apsType}" = "production" ] # mobile provision contains aps-envenrionment in its Entitlements
then
/usr/bin/codesign -f -s "${certificateName}" --entitlements "${tempDir}/TotalCross.entitlements" "${tempDir}/${appName}.xcarchive/Products/Applications/${appName}.app"
else
/usr/libexec/PlistBuddy ${verbose} -c "Delete :UIBackgroundModes" ${Infoplist}
/usr/bin/codesign -f -s "${certificateName}" "${tempDir}/${appName}.xcarchive/Products/Applications/${appName}.app"
fi

#Build ExportOptions.plist
cp "${workDir}/ExportOptions.plist" "${tempDir}/"
bundleID=$(/usr/libexec/PlistBuddy -c "Print :CFBundleIdentifier" "${tempDir}/Info.plist")
provName=$(/usr/libexec/PlistBuddy -c 'Print :Name' /dev/stdin <<< $(security cms -D -i "${provisionFile}"))
provUUID=$(/usr/libexec/PlistBuddy -c 'Print :UUID' /dev/stdin <<< $(security cms -D -i "${provisionFile}"))
teamID=$(/usr/libexec/PlistBuddy -c 'Print :Entitlements:com.apple.developer.team-identifier' /dev/stdin <<< $(security cms -D -i "${provisionFile}"))

# Add mobileprovision to local provision files
cp "${provisionFile}" ~/"Library/MobileDevice/Provisioning Profiles/${provUUID}.mobileprovision"
echo    ====================== MOBILEPROVISION =========================
echo "      name: ${provName}"
echo "      bundleID: ${bundleID}"
echo "      teamID: ${teamID}"
echo    ================================================================

/usr/libexec/PlistBuddy ${verbose} -c "Add :provisioningProfiles:${bundleID} string '${provName}'" "${tempDir}/ExportOptions.plist"
/usr/libexec/PlistBuddy ${verbose} -c "Delete :provisioningProfiles:com.totalcross.sdk" "${tempDir}/ExportOptions.plist"
/usr/libexec/PlistBuddy ${verbose} -c "Set teamID ${teamID}" "${tempDir}/ExportOptions.plist"
signingCertificate=$(echo "${certificateName}" | sed -e 's/\(.*\)\:.*/\1/')
echo $signingCertificate
/usr/libexec/PlistBuddy ${verbose} -c "Set :signingCertificate '${signingCertificate}'" "${tempDir}/ExportOptions.plist"
/usr/libexec/PlistBuddy ${verbose} -c "Set :method '${method}'" "${tempDir}/ExportOptions.plist"

#Modifying Info.plist into root of App.xcarchive
versionString=$(/usr/libexec/PlistBuddy -c "Print :CFBundleShortVersionString" "${tempDir}/Info.plist")
version=$(/usr/libexec/PlistBuddy -c "Print :CFBundleVersion" "${tempDir}/Info.plist")


/usr/libexec/PlistBuddy ${verbose} -c "Set :ApplicationProperties:SigningIdentity '${certificateName}'" "${tempDir}/${appName}.xcarchive/Info.plist"
/usr/libexec/PlistBuddy ${verbose} -c "Set :ApplicationProperties:ApplicationPath 'Applications/${appName}.app'" "${tempDir}/${appName}.xcarchive/Info.plist"
/usr/libexec/PlistBuddy ${verbose} -c "Set :ApplicationProperties:CFBundleIdentifier '${bundleID}'" "${tempDir}/${appName}.xcarchive/Info.plist"
/usr/libexec/PlistBuddy ${verbose} -c "Set :ApplicationProperties:Team '${teamID}'" "${tempDir}/${appName}.xcarchive/Info.plist"
/usr/libexec/PlistBuddy ${verbose} -c "Set :ApplicationProperties:CFBundleShortVersionString '${versionString}'" "${tempDir}/${appName}.xcarchive/Info.plist"
/usr/libexec/PlistBuddy ${verbose} -c "Set :ApplicationProperties:CFBundleVersion '${version}'" "${tempDir}/${appName}.xcarchive/Info.plist"
/usr/libexec/PlistBuddy ${verbose} -c "Set :Name '${appName}'" "${tempDir}/${appName}.xcarchive/Info.plist"
/usr/libexec/PlistBuddy ${verbose} -c "Set :SchemeName '${appName}'" "${tempDir}/${appName}.xcarchive/Info.plist"

xcodebuild -exportArchive -archivePath "${tempDir}/${appName}.xcarchive" -exportPath "${output}" -exportOptionsPlist "${tempDir}/ExportOptions.plist"
