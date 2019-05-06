#!/bin/bash

# Parse variables
POSITIONAL=()
while [[ $# -gt 0 ]]
do
key="$1"

case $key in
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

echo ====================== INFO =========================
echo 'source image: '$sourceImage
echo 'ipa: '$ipaDest	
echo 'certificate name: '$certificateName
echo 'provision file: '$provisionFile
echo 'method: '$method
echo ======================= LOG =========================

workDir=$(cd $BASEDIR; pwd)
echo $workDir

echo "Removing temp files..."
$(rm -R "${workDir}/build")
$(rm -R "${workDir}/Assets.xcassets")
$(rm -R "${workDir}/temp")
$(rm "${workDir}/Info.plist")

appDir=$(unzip -l "${ipaDest}" | egrep -o -m1 'Payload.*.app');
appName=$(echo "${appDir}" | sed -e 's/Payload\/\(.*\).app/\1/')
mkdir -p "${BASEDIR}/temp"
cp -R "${BASEDIR}/TotalCross.xcarchive/" "${BASEDIR}/temp/${appName}.xcarchive/"



mv "${BASEDIR}/temp/${appName}.xcarchive/Products/Applications/TotalCross.app" "${BASEDIR}/temp/${appName}.xcarchive/Products/Applications/${appName}.app"

# Extract plist and tczs from ipa
unzip -jo "${ipaDest}" "${appDir}/Info.plist" -d "${BASEDIR}/temp/"
unzip -jo "${ipaDest}" "${appDir}/*.tcz" -d "${BASEDIR}/temp/tczs"

# Copy tczs to temp/${appName}.xcarquive/Products/Applications/*.app/
cp "${BASEDIR}"/temp/tczs/*.tcz "${BASEDIR}/temp/${appName}.xcarchive/Products/Applications/${appName}.app/"
# Copia Infoplist to temp/${appName}.xcarquive/Products/Applications/*.app/
cp "${BASEDIR}/temp/Info.plist" "${BASEDIR}/temp/${appName}.xcarchive/Products/Applications/${appName}.app/"

# Build assets.car
bash "${BASEDIR}/xcassetsGenerator.sh" "${sourceImage}"	
mkdir -p "${BASEDIR}/build"
xcrun actool "${BASEDIR}/Assets.xcassets" --compile "${BASEDIR}/build" --platform iphoneos --minimum-deployment-target 8.0 --app-icon AppIcon --output-partial-info-plist "${BASEDIR}/build/partial.plist"

# Build assets.car
cp "${BASEDIR}/build/assets.car" "${BASEDIR}/temp/${appName}.xcarchive/Products/Applications/${appName}.app/Assets.car"
cp "${BASEDIR}"/build/AppIcon* "${BASEDIR}/temp/${appName}.xcarchive/Products/Applications/${appName}.app/"

# Copy CFBundleIcons and *ipad to Info.plist
defaults delete "$workDir/temp/${appName}.xcarchive/Products/Applications/${appName}.app/Info.plist" CFBundleIconFiles
defaults delete "$workDir/temp/${appName}.xcarchive/Products/Applications/${appName}.app/Info.plist" UILaunchStoryboardName
/usr/libexec/PlistBuddy -x -c "Merge \"${workDir}\"/build/partial.plist" "${workDir}/temp/${appName}.xcarchive/Products/Applications/${appName}.app/Info.plist"
defaults write "${workDir}/temp/${appName}.xcarchive/Products/Applications/${appName}.app/Info.plist" DTPlatformVersion 12.0 
defaults write "${workDir}/temp/${appName}.xcarchive/Products/Applications/${appName}.app/Info.plist" DTSDKName iphoneos12.0 
	
#Signing 
rm -rf "${workDir}/temp/${appName}.xcarchive/Products/Applications/${appName}.app/_CodeSignature"
cp "${provisionFile}" "${workDir}/temp/${appName}.xcarchive/Products/Applications/${appName}.app/embedded.mobileprovision"
/usr/bin/codesign -f -s "${certificateName}" "${workDir}/temp/${appName}.xcarchive/Products/Applications/${appName}.app"

#Build ExportOptions.plist
cp "${workDir}/ExportOptions.plist" "${workDir}/temp/"
bundleID=$(defaults read "${workDir}/temp/Info" CFBundleIdentifier)
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

defaults write "${workDir}/temp/ExportOptions" provisioningProfiles "<dict><key>${bundleID}</key><string>${provName}</string></dict>"
defaults write "${workDir}/temp/ExportOptions" teamID $teamID
signingCertificate=$(echo "${certificateName}" | sed -e 's/\(.*\)\:.*/\1/')
echo $signingCertificate
defaults write "$workDir/temp/ExportOptions" signingCertificate "${signingCertificate}"
defaults write "$workDir/temp/ExportOptions" method $method

#Modifying Info.plist into root of App.xcarchive
versionString=$(defaults read "${workDir}/temp/Info" CFBundleShortVersionString)
version=$(defaults read "${workDir}/temp/Info" CFBundleVersion)

defaults write "${workDir}/temp/${appName}.xcarchive/Info" ApplicationProperties -dict-add SigningIdentity "'${certificateName}'"
defaults write "${workDir}/temp/${appName}.xcarchive/Info" ApplicationProperties -dict-add ApplicationPath "Applications/${appName}.app"
defaults write "${workDir}/temp/${appName}.xcarchive/Info" ApplicationProperties -dict-add CFBundleIdentifier "${bundleID}"
defaults write "${workDir}/temp/${appName}.xcarchive/Info" ApplicationProperties -dict-add Team "${teamID}"
defaults write "${workDir}/temp/${appName}.xcarchive/Info" ApplicationProperties -dict-add CFBundleShortVersionString "${versionString}"
defaults write "${workDir}/temp/${appName}.xcarchive/Info" ApplicationProperties -dict-add CFBundleVersion "${version}"
defaults write "${workDir}/temp/${appName}.xcarchive/Info" Name "${appName}"
defaults write "${workDir}/temp/${appName}.xcarchive/Info" SchemeName "${appName}"

rm -R "${workDir}/build"
rm -R "${workDir}/Assets.xcassets"
xcodebuild -exportArchive -archivePath "${workDir}/temp/${appName}.xcarchive" -exportPath build -exportOptionsPlist "${workDir}/temp/ExportOptions.plist"

rm -R "${workDir}/temp"
rm "${workDir}/Info.plist"
