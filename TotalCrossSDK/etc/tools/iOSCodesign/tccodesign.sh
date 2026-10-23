#!/bin/bash
# Copyright (C) 2020-2021 TotalCross Global Mobile Platform Ltda
# Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

# Display usage before validating any options.
usage() {
    cat <<'EOF'
Usage: tccodesign.sh [OPTIONS]

Required:
  --ipa FILE                    Input TotalCross IPA
  -prov, --provision-file FILE  Provisioning profile
  -c, --certificate NAME        Code signing certificate identity
  -m, --method METHOD           Xcode export method

Optional:
  -o, --output DIR              Base output directory (default: build/)
  --group NAME                  Append a group subdirectory to the output
  --icon FILE                   Application icon (default: icone-totalcross.png)
  -h, --help                    Show this help

Output examples:
  (no output options)           build/
  --group client-a              build/client-a/
  --output /exports             /exports/
  --output /exports --group a   /exports/a/

Group identifiers must start with a letter or digit, be at most 64
characters long, and contain only letters, digits, '-' or '_'.
EOF
}

usageError() {
    echo "ERROR: $*" >&2
    echo "Run 'tccodesign.sh --help' for usage." >&2
    exit 2
}

if [[ $# -eq 0 ]]; then
    usage
    exit 0
fi

# Explicit help takes priority even when other options are incomplete.
for arg in "$@"; do
    if [[ "$arg" == "--help" || "$arg" == "-h" ]]; then
        usage
        exit 0
    fi
done

output=""
group=""
sourceImage=""
ipaDest=""
provisionFile=""
certificateName=""
method=""
seenOptions="|"

while [[ $# -gt 0 ]]; do
    case "$1" in
        --output|-o)               option=output ;;
        --group)                   option=group ;;
        --icon)                    option=icon ;;
        --ipa)                     option=ipa ;;
        -prov|--provision-file)    option=provision ;;
        -c|--certificate)         option=certificate ;;
        -m|--method)              option=method ;;
        *) usageError "Unknown argument: $1" ;;
    esac

    if [[ "$seenOptions" == *"|${option}|"* ]]; then
        usageError "Repeated option: $1"
    fi
    if [[ $# -lt 2 || -z "$2" || "$2" == -* ]]; then
        usageError "Option $1 requires a non-empty value."
    fi
    seenOptions="${seenOptions}${option}|"
    case "${option}" in
        output)      output="$2" ;;
        group)       group="$2" ;;
        icon)        sourceImage="$2" ;;
        ipa)         ipaDest="$2" ;;
        provision)   provisionFile="$2" ;;
        certificate) certificateName="$2" ;;
        method)      method="$2" ;;
    esac
    shift 2
done

missing=()
[[ -n "${ipaDest}" ]] || missing+=("--ipa")
[[ -n "${provisionFile}" ]] || missing+=("--provision-file")
[[ -n "${certificateName}" ]] || missing+=("--certificate")
[[ -n "${method}" ]] || missing+=("--method")
if [[ ${#missing[@]} -gt 0 ]]; then
    usageError "Missing required options: ${missing[*]}"
fi

if [[ -n "${group}" && ! "${group}" =~ ^[a-zA-Z0-9][a-zA-Z0-9_-]{0,63}$ ]]; then
    usageError "Invalid group identifier: ${group}. Use up to 64 letters, digits, '-' or '_'."
fi

BASEDIR=$(dirname $0)
workDir=$(cd $BASEDIR; pwd)

# Output is relative to the current working directory unless absolute.
# --group appends a validated, single-level subdirectory.
if [[ -z "${output}" ]]; then
    output="build"
fi
if [[ -n "${group}" ]]; then
    output="${output%/}/${group}"
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

# The signing archive supplies the native executable. Reject an archive built
# from a different VM so re-signing cannot silently replace the input binary.
archiveApp="${tempDir}/${appName}.xcarchive/Products/Applications/${appName}.app"
ipaExecutable=$(/usr/libexec/PlistBuddy -c "Print :CFBundleExecutable" "${tempDir}/Info.plist") || {
    echo "ERROR: Unable to read CFBundleExecutable from the input IPA." >&2
    exit 1
}
archiveExecutable=$(/usr/libexec/PlistBuddy -c "Print :CFBundleExecutable" "${Infoplist}") || {
    echo "ERROR: Unable to read CFBundleExecutable from the signing archive." >&2
    exit 1
}

if [[ -z "${ipaExecutable}" || "${ipaExecutable}" != "${archiveExecutable}" ]]; then
    echo "ERROR: Executable names differ between the IPA and signing archive." >&2
    echo "  IPA: ${ipaExecutable}" >&2
    echo "  Archive: ${archiveExecutable}" >&2
    exit 1
fi

archiveBinary="${archiveApp}/${archiveExecutable}"
ipaBinary="${tempDir}/input-ipa-executable"
if [[ ! -f "${archiveBinary}" ]]; then
    echo "ERROR: Signing archive executable not found: ${archiveBinary}" >&2
    exit 1
fi
if ! unzip -p "${ipaDest}" "${appDir}/${ipaExecutable}" > "${ipaBinary}"; then
    echo "ERROR: Unable to extract executable from the input IPA." >&2
    exit 1
fi

machoUuids() {
    xcrun dwarfdump --uuid "$1" 2>/dev/null |
        awk '$1 == "UUID:" { print $2, $3 }' | LC_ALL=C sort
}

ipaUuids=$(machoUuids "${ipaBinary}")
archiveUuids=$(machoUuids "${archiveBinary}")
if [[ -z "${ipaUuids}" || -z "${archiveUuids}" || "${ipaUuids}" != "${archiveUuids}" ]]; then
    echo "ERROR: The input IPA and signing archive have different native executables." >&2
    echo "  IPA Mach-O UUIDs: ${ipaUuids:-unavailable}" >&2
    echo "  Archive Mach-O UUIDs: ${archiveUuids:-unavailable}" >&2
    echo "Use the TotalCross.xcarchive from the SDK build that produced the IPA." >&2
    exit 1
fi
echo "Native executable verified: ${ipaUuids}"

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
xcrun actool "${tempDir}/Assets.xcassets" --compile "${tempDir}/assets" --platform iphoneos --minimum-deployment-target 13.0 --app-icon AppIcon --output-partial-info-plist "${tempDir}/assets/partial.plist"

# Build assets.car
cp "${tempDir}"/assets/Assets.car "${tempDir}/${appName}.xcarchive/Products/Applications/${appName}.app/Assets.car"
cp "${tempDir}"/assets/AppIcon* "${tempDir}/${appName}.xcarchive/Products/Applications/${appName}.app/"

# Copy CFBundleIcons and *ipad to Info.plist
/usr/libexec/PlistBuddy  ${verbose} -c "Delete :CFBundleIconFiles" ${Infoplist}
/usr/libexec/PlistBuddy ${verbose} -c "Merge ${tempDir}/assets/partial.plist" ${Infoplist}


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
