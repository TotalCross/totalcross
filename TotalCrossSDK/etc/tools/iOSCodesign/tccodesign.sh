#!/bin/bash 

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

echo "Removing temp files..."
$(rm -R "${workDir}/build")
$(rm -R "${workDir}/Assets.xcassets")
$(rm -R "${workDir}/temp")
$(rm "${workDir}/Info.plist")

appDir=$(unzip -l "${ipaDest}" | egrep -o -m1 'Payload.*.app');
appName=$(echo "${appDir}" | sed -e 's/Payload\/\(.*\).app/\1/')

cp -R "${BASEDIR}/TotalCross.xcarchive/" "${tempDir}/${appName}.xcarchive/"



mv "${tempDir}/${appName}.xcarchive/Products/Applications/TotalCross.app" "${tempDir}/${appName}.xcarchive/Products/Applications/${appName}.app"

# Extract plist and tczs from ipa
unzip -jo "${ipaDest}" "${appDir}/Info.plist" -d "${tempDir}/"
unzip -jo "${ipaDest}" "${appDir}/*.tcz" -d "${tempDir}/tczs"

# Copy tczs to temp/${appName}.xcarquive/Products/Applications/*.app/
cp "${tempDir}"/tczs/*.tcz "${tempDir}/${appName}.xcarchive/Products/Applications/${appName}.app/"
# Copia Infoplist to temp/${appName}.xcarquive/Products/Applications/*.app/
cp "${tempDir}/Info.plist" "${tempDir}/${appName}.xcarchive/Products/Applications/${appName}.app/"

# Build assets.car
bash "${BASEDIR}/xcassetsGenerator.sh" "${sourceImage}" "${tempDir}"
mkdir -p "${tempDir}/assets"
xcrun actool "${tempDir}/Assets.xcassets" --compile "${tempDir}/assets" --platform iphoneos --minimum-deployment-target 8.0 --app-icon AppIcon --output-partial-info-plist "${tempDir}/assets/partial.plist"

# Build assets.car
cp "${tempDir}"/assets/Assets.car "${tempDir}/${appName}.xcarchive/Products/Applications/${appName}.app/Assets.car"
cp "${tempDir}"/assets/AppIcon* "${tempDir}/${appName}.xcarchive/Products/Applications/${appName}.app/"

# Copy CFBundleIcons and *ipad to Info.plist
defaults delete "${tempDir}/${appName}.xcarchive/Products/Applications/${appName}.app/Info.plist" CFBundleIconFiles
/usr/libexec/PlistBuddy -x -c "Merge ${tempDir}/assets/partial.plist" "${tempDir}/${appName}.xcarchive/Products/Applications/${appName}.app/Info.plist"
defaults write "${tempDir}/${appName}.xcarchive/Products/Applications/${appName}.app/Info.plist" DTPlatformVersion 12.0
defaults write "${tempDir}/${appName}.xcarchive/Products/Applications/${appName}.app/Info.plist" DTSDKName iphoneos12.0
	
#Signing 
rm -rf "${tempDir}/${appName}.xcarchive/Products/Applications/${appName}.app/_CodeSignature"
cp "${provisionFile}" "${tempDir}/${appName}.xcarchive/Products/Applications/${appName}.app/embedded.mobileprovision"
/usr/bin/codesign -f -s "${certificateName}" "${tempDir}/${appName}.xcarchive/Products/Applications/${appName}.app"

#Build ExportOptions.plist
cp "${workDir}/ExportOptions.plist" "${tempDir}/"
bundleID=$(defaults read "${tempDir}/Info" CFBundleIdentifier)
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

defaults write "${tempDir}/ExportOptions" provisioningProfiles "<dict><key>${bundleID}</key><string>${provName}</string></dict>"
defaults write "${tempDir}/ExportOptions" teamID $teamID
signingCertificate=$(echo "${certificateName}" | sed -e 's/\(.*\)\:.*/\1/')
echo $signingCertificate
defaults write "${tempDir}/ExportOptions" signingCertificate "${signingCertificate}"
defaults write "${tempDir}/ExportOptions" method $method

#Modifying Info.plist into root of App.xcarchive
versionString=$(defaults read "${tempDir}/Info" CFBundleShortVersionString)
version=$(defaults read "${tempDir}/Info" CFBundleVersion)

defaults write "${tempDir}/${appName}.xcarchive/Info" ApplicationProperties -dict-add SigningIdentity "'${certificateName}'"
defaults write "${tempDir}/${appName}.xcarchive/Info" ApplicationProperties -dict-add ApplicationPath "Applications/${appName}.app"
defaults write "${tempDir}/${appName}.xcarchive/Info" ApplicationProperties -dict-add CFBundleIdentifier "${bundleID}"
defaults write "${tempDir}/${appName}.xcarchive/Info" ApplicationProperties -dict-add Team "${teamID}"
defaults write "${tempDir}/${appName}.xcarchive/Info" ApplicationProperties -dict-add CFBundleShortVersionString "${versionString}"
defaults write "${tempDir}/${appName}.xcarchive/Info" ApplicationProperties -dict-add CFBundleVersion "${version}"
defaults write "${tempDir}/${appName}.xcarchive/Info" Name "${appName}"
defaults write "${tempDir}/${appName}.xcarchive/Info" SchemeName "${appName}"

rm -R "${workDir}/build"
rm -R "${workDir}/Assets.xcassets"
xcodebuild -exportArchive -archivePath "${tempDir}/${appName}.xcarchive" -exportPath "${output}" -exportOptionsPlist "${tempDir}/ExportOptions.plist"
