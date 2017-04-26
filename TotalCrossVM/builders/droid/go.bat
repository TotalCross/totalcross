call gradlew assembleRelease
dir build\outputs\apk\droid-standard-release.apk 
copy /y build\outputs\apk\droid-standard-release.apk c:\TotalCross3\dist\vm\android\TotalCross.apk
"C:\Program Files\7-Zip\7z" e -y build\outputs\apk\droid-singleApk-release.apk AndroidManifest.xml resources.arsc
move /y AndroidManifest.xml c:\TotalCross3\etc\tools\android\AndroidManifest_singleapk.xml
move /y resources.arsc c:\TotalCross3\etc\tools\android\resources_singleapk.arsc