#!/bin/bash
set -o pipefail
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

fail() {
    echo "ERROR: $*" >&2
    exit 1
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

workDir=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P) || fail "Cannot resolve script directory."
BASEDIR="${workDir}"

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

# Validate before creating output directories or temporary files.
[[ -f "${ipaDest}" && -r "${ipaDest}" ]] || usageError "Input IPA not readable: ${ipaDest}"
[[ -f "${provisionFile}" && -r "${provisionFile}" ]] || usageError "Provisioning profile not readable: ${provisionFile}"
[[ -f "${sourceImage}" && -r "${sourceImage}" ]] || usageError "Icon not readable: ${sourceImage}"
[[ -d "${BASEDIR}/TotalCross.xcarchive/Products/Applications/TotalCross.app" ]] || fail "Missing TotalCross.xcarchive application."
for requiredFile in "ExportOptions.plist" "TotalCross.entitlements" "xcassetsGenerator.sh"; do
    [[ -f "${workDir}/${requiredFile}" ]] || fail "Missing signing support file: ${requiredFile}"
done
[[ -x /usr/libexec/PlistBuddy && -x /usr/bin/codesign ]] || fail "Required macOS signing tools are unavailable."
for requiredCommand in unzip mktemp xcrun xcodebuild security plutil convert; do
    command -v "${requiredCommand}" >/dev/null 2>&1 || fail "Required command is unavailable: ${requiredCommand}"
done

echo ====================== INFO =========================
echo 'source image: '$sourceImage
echo 'ipa: '$ipaDest	
echo 'certificate name: '$certificateName
echo 'provision file: '$provisionFile
echo 'method: '$method
echo 'build dir: '$output
echo ======================= LOG =========================



# Keep signing resources private and isolated from other executions.
umask 077
tempRoot="${TMPDIR:-/tmp}"
tempRoot="${tempRoot%/}"
tempDir=$(mktemp -d "${tempRoot}/tccodesign.XXXXXXXX") || {
    echo "ERROR: Unable to create temporary signing workspace." >&2
    exit 1
}

cleanup() {
    if [[ -n "${tempDir:-}" && -d "${tempDir}" ]]; then
        rm -rf -- "${tempDir}"
    fi
}
trap cleanup EXIT
trap 'exit 130' INT
trap 'exit 143' TERM
trap 'exit 129' HUP

# Identify exactly one root application from ZIP entry paths.
# Do not parse the human-readable 'unzip -l' listing as a filename.
zipEntries=$(unzip -Z -1 "${ipaDest}") || fail "Cannot read IPA archive."
appInfo=""
hasGoogleService=false
hasPkg=false
while IFS= read -r zipEntry; do
    if [[ "${zipEntry}" =~ ^Payload/[^/]+[.]app/Info[.]plist$ ]]; then
        [[ -z "${appInfo}" ]] || fail "IPA contains multiple applications."
        appInfo="${zipEntry}"
    fi
done <<< "${zipEntries}"
[[ -n "${appInfo}" ]] || fail "IPA does not contain Payload/<app>.app/Info.plist."
appDir="${appInfo%/Info.plist}"
appName="${appDir#Payload/}"
appName="${appName%.app}"

# Reject ambiguous path/glob characters in the app bundle name.
if [[ -z "${appName}" || "${appName}" == "." || "${appName}" == ".." ||
      "${appName}" == *'['* || "${appName}" == *']'* ||
      "${appName}" == *'*'* || "${appName}" == *'?'* ||
      "${appName}" == *'\\'* || "${appName}" =~ [[:cntrl:]] ]]; then
    fail "Unsafe app bundle directory name in IPA."
fi
while IFS= read -r zipEntry; do
    if [[ "${zipEntry}" == "${appDir}/GoogleService-Info.plist" ]]; then
        hasGoogleService=true
    fi
    if [[ "${zipEntry}" == "${appDir}/pkg/"* && "${zipEntry}" != "${appDir}/pkg/" ]]; then
        hasPkg=true
    fi
done <<< "${zipEntries}"

archiveDir="${tempDir}/${appName}.xcarchive"
cp -R "${BASEDIR}/TotalCross.xcarchive/" "${archiveDir}/" || fail "Cannot copy signing archive."
if [[ "${appName}" != "TotalCross" ]]; then
    mv "${archiveDir}/Products/Applications/TotalCross.app" \
       "${archiveDir}/Products/Applications/${appName}.app" || fail "Cannot prepare application bundle."
fi

# Extract mandatory IPA resources without following paths from the archive.
unzip -p "${ipaDest}" "${appInfo}" > "${tempDir}/Info.plist" || fail "Cannot extract IPA Info.plist."
mkdir -p "${tempDir}/tczs" || fail "Cannot create TCZ staging directory."
unzip -oqj "${ipaDest}" "${appDir}/*.tcz" -d "${tempDir}/tczs" || fail "Cannot extract application TCZ files."
tczFiles=("${tempDir}"/tczs/*.tcz)
[[ -f "${tczFiles[0]}" ]] || fail "IPA has no application TCZ files."
for tczFile in "${tczFiles[@]}"; do
    [[ -f "${tczFile}" && ! -L "${tczFile}" ]] || fail "Unsafe TCZ resource in IPA."
done

# Some applications do not use Firebase or bundle files in pkg/.
if [[ "${hasGoogleService}" == true ]]; then
    unzip -p "${ipaDest}" "${appDir}/GoogleService-Info.plist" > "${tempDir}/GoogleService-Info.plist" || fail "Cannot extract Firebase configuration."
fi
if [[ "${hasPkg}" == true ]]; then
    mkdir -p "${tempDir}/pkg" || fail "Cannot create pkg staging directory."
    unzip -oqj "${ipaDest}" "${appDir}/pkg/*" -d "${tempDir}/pkg" || fail "Cannot extract pkg resources."
    [[ -z "$(find "${tempDir}/pkg" -type l -print -quit)" ]] || fail "Unsafe symlink in IPA pkg resources."
fi

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

# Copy required application resources and optional configuration files.
cp "${tczFiles[@]}" "${archiveApp}/" || fail "Cannot copy TCZ files."
cp "${tempDir}/Info.plist" "${Infoplist}" || fail "Cannot copy IPA Info.plist."
if [[ "${hasGoogleService}" == true ]]; then
    cp "${tempDir}/GoogleService-Info.plist" "${archiveApp}/" || fail "Cannot copy Firebase configuration."
fi
if [[ "${hasPkg}" == true ]]; then
    cp -R "${tempDir}/pkg" "${archiveApp}/pkg" || fail "Cannot copy pkg resources."
fi


# Build assets.car
bash "${BASEDIR}/xcassetsGenerator.sh" "${sourceImage}" "${tempDir}" || fail "Cannot generate app icon assets."
mkdir -p "${tempDir}/assets" || fail "Cannot prepare asset compilation directory."
xcrun actool "${tempDir}/Assets.xcassets" --compile "${tempDir}/assets" --platform iphoneos --minimum-deployment-target 13.0 --app-icon AppIcon --output-partial-info-plist "${tempDir}/assets/partial.plist" || fail "Asset catalog compilation failed."

# Build assets.car
cp "${tempDir}"/assets/Assets.car "${archiveApp}/Assets.car" || fail "Cannot copy compiled assets."
cp "${tempDir}"/assets/AppIcon* "${archiveApp}/" || fail "Cannot copy app icon assets."

# Copy CFBundleIcons and *ipad to Info.plist
/usr/libexec/PlistBuddy -c "Delete :CFBundleIconFiles" "${Infoplist}" >/dev/null 2>&1 || true
/usr/libexec/PlistBuddy -c "Merge ${tempDir}/assets/partial.plist" "${Infoplist}" || fail "Cannot merge app icon metadata."


# Modify entitlements for push notification
cp "${workDir}/TotalCross.entitlements" "${tempDir}/TotalCross.entitlements" || fail "Cannot copy entitlements."

if [[ "${method}" != "development" ]]; then
    plutil -replace aps-environment -string production "${tempDir}/TotalCross.entitlements" || fail "Cannot set push environment."
fi

#Signing 
rm -rf "${archiveApp}/_CodeSignature" || fail "Cannot remove old signature."
cp "${provisionFile}" "${archiveApp}/embedded.mobileprovision" || fail "Cannot embed provisioning profile."

#Check if mobileprovision contains aps-environment in its entitlements
profilePlist="${tempDir}/provision.plist"
security cms -D -i "${provisionFile}" > "${profilePlist}" || fail "Cannot decode provisioning profile."
apsType=$(/usr/libexec/PlistBuddy -c 'Print :Entitlements:aps-environment' "${profilePlist}" 2>/dev/null) || apsType=""
if [ "${apsType}" = "development" ] || [ "${apsType}" = "production" ] # mobile provision contains aps-envenrionment in its Entitlements
then
/usr/bin/codesign -f -s "${certificateName}" --entitlements "${tempDir}/TotalCross.entitlements" "${archiveApp}" || fail "Code signing failed."
else
/usr/libexec/PlistBuddy -c "Delete :UIBackgroundModes" "${Infoplist}" >/dev/null 2>&1 || true
/usr/bin/codesign -f -s "${certificateName}" "${archiveApp}" || fail "Code signing failed."
fi

#Build ExportOptions.plist
cp "${workDir}/ExportOptions.plist" "${tempDir}/" || fail "Cannot copy export options."
bundleID=$(/usr/libexec/PlistBuddy -c "Print :CFBundleIdentifier" "${tempDir}/Info.plist") || fail "IPA has no bundle identifier."
provName=$(/usr/libexec/PlistBuddy -c 'Print :Name' "${profilePlist}") || fail "Provisioning profile has no name."
provUUID=$(/usr/libexec/PlistBuddy -c 'Print :UUID' "${profilePlist}") || fail "Provisioning profile has no UUID."
teamID=$(/usr/libexec/PlistBuddy -c 'Print :Entitlements:com.apple.developer.team-identifier' "${profilePlist}") || fail "Provisioning profile has no team ID."

# Add mobileprovision to local provision files
profilesDir="${HOME}/Library/MobileDevice/Provisioning Profiles"
mkdir -p "${profilesDir}" || fail "Cannot create provisioning profile directory."
cp "${provisionFile}" "${profilesDir}/${provUUID}.mobileprovision" || fail "Cannot install provisioning profile."
echo    ====================== MOBILEPROVISION =========================
echo "      name: ${provName}"
echo "      bundleID: ${bundleID}"
echo "      teamID: ${teamID}"
echo    ================================================================

/usr/libexec/PlistBuddy -c "Add :provisioningProfiles:${bundleID} string '${provName}'" "${tempDir}/ExportOptions.plist" || fail "Unable to update export or archive metadata."
/usr/libexec/PlistBuddy -c "Delete :provisioningProfiles:com.totalcross.sdk" "${tempDir}/ExportOptions.plist" >/dev/null 2>&1 || true
/usr/libexec/PlistBuddy -c "Set teamID ${teamID}" "${tempDir}/ExportOptions.plist" || fail "Unable to update export or archive metadata."
signingCertificate=$(echo "${certificateName}" | sed -e 's/\(.*\)\:.*/\1/')
echo $signingCertificate
/usr/libexec/PlistBuddy -c "Set :signingCertificate '${signingCertificate}'" "${tempDir}/ExportOptions.plist" || fail "Unable to update export or archive metadata."
/usr/libexec/PlistBuddy -c "Set :method '${method}'" "${tempDir}/ExportOptions.plist" || fail "Unable to update export or archive metadata."

#Modifying Info.plist into root of App.xcarchive
versionString=$(/usr/libexec/PlistBuddy -c "Print :CFBundleShortVersionString" "${tempDir}/Info.plist") || fail "IPA has no version string."
version=$(/usr/libexec/PlistBuddy -c "Print :CFBundleVersion" "${tempDir}/Info.plist") || fail "IPA has no build version."


/usr/libexec/PlistBuddy -c "Set :ApplicationProperties:SigningIdentity '${certificateName}'" "${tempDir}/${appName}.xcarchive/Info.plist" || fail "Unable to update export or archive metadata."
/usr/libexec/PlistBuddy -c "Set :ApplicationProperties:ApplicationPath 'Applications/${appName}.app'" "${tempDir}/${appName}.xcarchive/Info.plist" || fail "Unable to update export or archive metadata."
/usr/libexec/PlistBuddy -c "Set :ApplicationProperties:CFBundleIdentifier '${bundleID}'" "${tempDir}/${appName}.xcarchive/Info.plist" || fail "Unable to update export or archive metadata."
/usr/libexec/PlistBuddy -c "Set :ApplicationProperties:Team '${teamID}'" "${tempDir}/${appName}.xcarchive/Info.plist" || fail "Unable to update export or archive metadata."
/usr/libexec/PlistBuddy -c "Set :ApplicationProperties:CFBundleShortVersionString '${versionString}'" "${tempDir}/${appName}.xcarchive/Info.plist" || fail "Unable to update export or archive metadata."
/usr/libexec/PlistBuddy -c "Set :ApplicationProperties:CFBundleVersion '${version}'" "${tempDir}/${appName}.xcarchive/Info.plist" || fail "Unable to update export or archive metadata."
/usr/libexec/PlistBuddy -c "Set :Name '${appName}'" "${tempDir}/${appName}.xcarchive/Info.plist" || fail "Unable to update export or archive metadata."
/usr/libexec/PlistBuddy -c "Set :SchemeName '${appName}'" "${tempDir}/${appName}.xcarchive/Info.plist" || fail "Unable to update export or archive metadata."

mkdir -p "${output}" || fail "Cannot create output directory: ${output}"
xcodebuild -exportArchive -archivePath "${archiveDir}" -exportPath "${output}" -exportOptionsPlist "${tempDir}/ExportOptions.plist" || fail "IPA export failed."
echo "Signed IPA exported to: ${output}"
