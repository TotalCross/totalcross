#!/bin/bash -e

# --------------------------------------------------------
# Generate app icons and xcassets file from a single image
# Ben Clayton, Calvium Ltd.
#  https://gist.github.com/benvium/2be6d673aa9ac284bb8a
# --------------------------------------------------------
#
# Usage with an input of 1024x1024 PNG file
#   generateAppIcon.sh AppIcon.png
#
# Updated in October 2017 for RobLabs.com
# https://gist.github.com/roblabs/527458cbe46b0483cd2d594c7b9e583f
# Based on Xcode Version 9.0 (9A235)
# requires imagemagick
  # `brew install imagemagick`

sourceIconName=$1
BASEDIR=$2
# Ensure we're running in location of script.
#cd "`dirname $0`"

# Check imagemagick is installed
# http://stackoverflow.com/questions/592620/check-if-a-program-exists-from-a-bash-script
command -v convert >/dev/null 2>&1 || { echo >&2 "I require imagemagick but it's not installed. If you have brew installed, execute 'brew install imagemagick' Aborting."; exit 1; }

iconPath= "${BASEDIR}/Assets.xcassets/AppIcon.appiconset"

mkdir -p "$iconPath"


convert "${sourceIconName}" -alpha off --resize 20x20 "${iconPath}/20.png" 
convert "${sourceIconName}" -alpha off --resize 16x16 "${iconPath}/16.png" 
convert "${sourceIconName}" -alpha off --resize 29x29 "${iconPath}/29.png" 
convert "${sourceIconName}" -alpha off --resize 32x32 "${iconPath}/32.png" 
convert "${sourceIconName}" -alpha off --resize 40x40 "${iconPath}/40.png" 
convert "${sourceIconName}" -alpha off --resize 48x48 "${iconPath}/48.png" 
convert "${sourceIconName}" -alpha off --resize 50x50 "${iconPath}/50.png" 
convert "${sourceIconName}" -alpha off --resize 55x55 "${iconPath}/55.png" 
convert "${sourceIconName}" -alpha off --resize 57x57 "${iconPath}/57.png" 
convert "${sourceIconName}" -alpha off --resize 58x58 "${iconPath}/58.png" 
convert "${sourceIconName}" -alpha off --resize 60x60 "${iconPath}/60.png" 
convert "${sourceIconName}" -alpha off --resize 64x64 "${iconPath}/64.png" 
convert "${sourceIconName}" -alpha off --resize 72x72 "${iconPath}/72.png" 
convert "${sourceIconName}" -alpha off --resize 76x76 "${iconPath}/76.png" 
convert "${sourceIconName}" -alpha off --resize 80x80 "${iconPath}/80.png" 
convert "${sourceIconName}" -alpha off --resize 87x87 "${iconPath}/87.png" 
convert "${sourceIconName}" -alpha off --resize 88x88 "${iconPath}/88.png" 
convert "${sourceIconName}" -alpha off --resize 100x100 "${iconPath}/100.png" 
convert "${sourceIconName}" -alpha off --resize 114x114 "${iconPath}/114.png" 
convert "${sourceIconName}" -alpha off --resize 120x120 "${iconPath}/120.png" 
convert "${sourceIconName}" -alpha off --resize 128x128 "${iconPath}/128.png" 
convert "${sourceIconName}" -alpha off --resize 144x144 "${iconPath}/144.png" 
convert "${sourceIconName}" -alpha off --resize 152x152 "${iconPath}/152.png" 
convert "${sourceIconName}" -alpha off --resize 167x167 "${iconPath}/167.png" 
convert "${sourceIconName}" -alpha off --resize 172x172 "${iconPath}/172.png" 
convert "${sourceIconName}" -alpha off --resize 180x180 "${iconPath}/180.png" 
convert "${sourceIconName}" -alpha off --resize 196x196 "${iconPath}/196.png" 
convert "${sourceIconName}" -alpha off --resize 216x216 "${iconPath}/216.png" 
convert "${sourceIconName}" -alpha off --resize 256x256 "${iconPath}/256.png" 
convert "${sourceIconName}" -alpha off --resize 512x512 "${iconPath}/512.png" 
convert "${sourceIconName}" -alpha off --resize 1024x1024 "${iconPath}/1024.png" 


cat > "${iconPath}/Contents.json" << EOF
{
   "images":[
      {
         "size":"60x60",
         "expected-size":"180",
         "filename":"180.png",
         "folder":"Assets.xcassets/AppIcon.appiconset/",
         "idiom":"iphone",
         "scale":"3x"
      },
      {
         "size":"40x40",
         "expected-size":"80",
         "filename":"80.png",
         "folder":"Assets.xcassets/AppIcon.appiconset/",
         "idiom":"iphone",
         "scale":"2x"
      },
      {
         "size":"40x40",
         "expected-size":"120",
         "filename":"120.png",
         "folder":"Assets.xcassets/AppIcon.appiconset/",
         "idiom":"iphone",
         "scale":"3x"
      },
      {
         "size":"60x60",
         "expected-size":"120",
         "filename":"120.png",
         "folder":"Assets.xcassets/AppIcon.appiconset/",
         "idiom":"iphone",
         "scale":"2x"
      },
      {
         "size":"57x57",
         "expected-size":"57",
         "filename":"57.png",
         "folder":"Assets.xcassets/AppIcon.appiconset/",
         "idiom":"iphone",
         "scale":"1x"
      },
      {
         "size":"29x29",
         "expected-size":"58",
         "filename":"58.png",
         "folder":"Assets.xcassets/AppIcon.appiconset/",
         "idiom":"iphone",
         "scale":"2x"
      },
      {
         "size":"29x29",
         "expected-size":"29",
         "filename":"29.png",
         "folder":"Assets.xcassets/AppIcon.appiconset/",
         "idiom":"iphone",
         "scale":"1x"
      },
      {
         "size":"29x29",
         "expected-size":"87",
         "filename":"87.png",
         "folder":"Assets.xcassets/AppIcon.appiconset/",
         "idiom":"iphone",
         "scale":"3x"
      },
      {
         "size":"57x57",
         "expected-size":"114",
         "filename":"114.png",
         "folder":"Assets.xcassets/AppIcon.appiconset/",
         "idiom":"iphone",
         "scale":"2x"
      },
      {
         "size":"20x20",
         "expected-size":"40",
         "filename":"40.png",
         "folder":"Assets.xcassets/AppIcon.appiconset/",
         "idiom":"iphone",
         "scale":"2x"
      },
      {
         "size":"20x20",
         "expected-size":"60",
         "filename":"60.png",
         "folder":"Assets.xcassets/AppIcon.appiconset/",
         "idiom":"iphone",
         "scale":"3x"
      },
      {
         "size":"1024x1024",
         "filename":"1024.png",
         "expected-size":"1024",
         "idiom":"ios-marketing",
         "folder":"Assets.xcassets/AppIcon.appiconset/",
         "scale":"1x"
      },
      {
         "size":"40x40",
         "expected-size":"80",
         "filename":"80.png",
         "folder":"Assets.xcassets/AppIcon.appiconset/",
         "idiom":"ipad",
         "scale":"2x"
      },
      {
         "size":"72x72",
         "expected-size":"72",
         "filename":"72.png",
         "folder":"Assets.xcassets/AppIcon.appiconset/",
         "idiom":"ipad",
         "scale":"1x"
      },
      {
         "size":"76x76",
         "expected-size":"152",
         "filename":"152.png",
         "folder":"Assets.xcassets/AppIcon.appiconset/",
         "idiom":"ipad",
         "scale":"2x"
      },
      {
         "size":"50x50",
         "expected-size":"100",
         "filename":"100.png",
         "folder":"Assets.xcassets/AppIcon.appiconset/",
         "idiom":"ipad",
         "scale":"2x"
      },
      {
         "size":"29x29",
         "expected-size":"58",
         "filename":"58.png",
         "folder":"Assets.xcassets/AppIcon.appiconset/",
         "idiom":"ipad",
         "scale":"2x"
      },
      {
         "size":"76x76",
         "expected-size":"76",
         "filename":"76.png",
         "folder":"Assets.xcassets/AppIcon.appiconset/",
         "idiom":"ipad",
         "scale":"1x"
      },
      {
         "size":"29x29",
         "expected-size":"29",
         "filename":"29.png",
         "folder":"Assets.xcassets/AppIcon.appiconset/",
         "idiom":"ipad",
         "scale":"1x"
      },
      {
         "size":"50x50",
         "expected-size":"50",
         "filename":"50.png",
         "folder":"Assets.xcassets/AppIcon.appiconset/",
         "idiom":"ipad",
         "scale":"1x"
      },
      {
         "size":"72x72",
         "expected-size":"144",
         "filename":"144.png",
         "folder":"Assets.xcassets/AppIcon.appiconset/",
         "idiom":"ipad",
         "scale":"2x"
      },
      {
         "size":"40x40",
         "expected-size":"40",
         "filename":"40.png",
         "folder":"Assets.xcassets/AppIcon.appiconset/",
         "idiom":"ipad",
         "scale":"1x"
      },
      {
         "size":"83.5x83.5",
         "expected-size":"167",
         "filename":"167.png",
         "folder":"Assets.xcassets/AppIcon.appiconset/",
         "idiom":"ipad",
         "scale":"2x"
      },
      {
         "size":"20x20",
         "expected-size":"20",
         "filename":"20.png",
         "folder":"Assets.xcassets/AppIcon.appiconset/",
         "idiom":"ipad",
         "scale":"1x"
      },
      {
         "size":"20x20",
         "expected-size":"40",
         "filename":"40.png",
         "folder":"Assets.xcassets/AppIcon.appiconset/",
         "idiom":"ipad",
         "scale":"2x"
      },
      {
         "idiom":"watch",
         "filename":"172.png",
         "folder":"Assets.xcassets/AppIcon.appiconset/",
         "subtype":"38mm",
         "scale":"2x",
         "size":"86x86",
         "expected-size":"172",
         "role":"quickLook"
      },
      {
         "idiom":"watch",
         "filename":"80.png",
         "folder":"Assets.xcassets/AppIcon.appiconset/",
         "subtype":"38mm",
         "scale":"2x",
         "size":"40x40",
         "expected-size":"80",
         "role":"appLauncher"
      },
      {
         "idiom":"watch",
         "filename":"88.png",
         "folder":"Assets.xcassets/AppIcon.appiconset/",
         "subtype":"40mm",
         "scale":"2x",
         "size":"44x44",
         "expected-size":"88",
         "role":"appLauncher"
      },
      {
         "idiom":"watch",
         "filename":"100.png",
         "folder":"Assets.xcassets/AppIcon.appiconset/",
         "subtype":"44mm",
         "scale":"2x",
         "size":"50x50",
         "expected-size":"100",
         "role":"appLauncher"
      },
      {
         "idiom":"watch",
         "filename":"196.png",
         "folder":"Assets.xcassets/AppIcon.appiconset/",
         "subtype":"42mm",
         "scale":"2x",
         "size":"98x98",
         "expected-size":"196",
         "role":"quickLook"
      },
      {
         "idiom":"watch",
         "filename":"216.png",
         "folder":"Assets.xcassets/AppIcon.appiconset/",
         "subtype":"44mm",
         "scale":"2x",
         "size":"108x108",
         "expected-size":"216",
         "role":"quickLook"
      },
      {
         "idiom":"watch",
         "filename":"48.png",
         "folder":"Assets.xcassets/AppIcon.appiconset/",
         "subtype":"38mm",
         "scale":"2x",
         "size":"24x24",
         "expected-size":"48",
         "role":"notificationCenter"
      },
      {
         "idiom":"watch",
         "filename":"55.png",
         "folder":"Assets.xcassets/AppIcon.appiconset/",
         "subtype":"42mm",
         "scale":"2x",
         "size":"27.5x27.5",
         "expected-size":"55",
         "role":"notificationCenter"
      },
      {
         "size":"29x29",
         "expected-size":"87",
         "filename":"87.png",
         "folder":"Assets.xcassets/AppIcon.appiconset/",
         "idiom":"watch",
         "role":"companionSettings",
         "scale":"3x"
      },
      {
         "size":"29x29",
         "expected-size":"58",
         "filename":"58.png",
         "folder":"Assets.xcassets/AppIcon.appiconset/",
         "idiom":"watch",
         "role":"companionSettings",
         "scale":"2x"
      },
      {
         "size":"1024x1024",
         "expected-size":"1024",
         "filename":"1024.png",
         "folder":"Assets.xcassets/AppIcon.appiconset/",
         "idiom":"watch-marketing",
         "scale":"1x"
      },
      {
         "size":"128x128",
         "expected-size":"128",
         "filename":"128.png",
         "folder":"Assets.xcassets/AppIcon.appiconset/",
         "idiom":"mac",
         "scale":"1x"
      },
      {
         "size":"256x256",
         "expected-size":"256",
         "filename":"256.png",
         "folder":"Assets.xcassets/AppIcon.appiconset/",
         "idiom":"mac",
         "scale":"1x"
      },
      {
         "size":"128x128",
         "expected-size":"256",
         "filename":"256.png",
         "folder":"Assets.xcassets/AppIcon.appiconset/",
         "idiom":"mac",
         "scale":"2x"
      },
      {
         "size":"256x256",
         "expected-size":"512",
         "filename":"512.png",
         "folder":"Assets.xcassets/AppIcon.appiconset/",
         "idiom":"mac",
         "scale":"2x"
      },
      {
         "size":"32x32",
         "expected-size":"32",
         "filename":"32.png",
         "folder":"Assets.xcassets/AppIcon.appiconset/",
         "idiom":"mac",
         "scale":"1x"
      },
      {
         "size":"512x512",
         "expected-size":"512",
         "filename":"512.png",
         "folder":"Assets.xcassets/AppIcon.appiconset/",
         "idiom":"mac",
         "scale":"1x"
      },
      {
         "size":"16x16",
         "expected-size":"16",
         "filename":"16.png",
         "folder":"Assets.xcassets/AppIcon.appiconset/",
         "idiom":"mac",
         "scale":"1x"
      },
      {
         "size":"16x16",
         "expected-size":"32",
         "filename":"32.png",
         "folder":"Assets.xcassets/AppIcon.appiconset/",
         "idiom":"mac",
         "scale":"2x"
      },
      {
         "size":"32x32",
         "expected-size":"64",
         "filename":"64.png",
         "folder":"Assets.xcassets/AppIcon.appiconset/",
         "idiom":"mac",
         "scale":"2x"
      },
      {
         "size":"512x512",
         "expected-size":"1024",
         "filename":"1024.png",
         "folder":"Assets.xcassets/AppIcon.appiconset/",
         "idiom":"mac",
         "scale":"2x"
      }
   ]
}
EOF
