// Reference the GitLab connection name from your Jenkins Global configuration (http://JENKINS_URL/configure, GitLab section)
//properties([gitLabConnection('gitlab-2690')])

node('master') {
    stage('Checkout') {
        wrap([$class: 'TimestamperBuildWrapper']) {
        checkout scm // Jenkins will clone the appropriate git branch, no env vars needed
        dir ("TotalCrossSDK") {
            sh './gradlew clean dist'
        }
        
withAnt(installation: 'ant') {
    dir("TotalCrossVM/builders") {
    if (isUnix()) {
      sh "ant makeNativeHT"
    } else {
      bat "ant makeNativeHT"
    }
    }

        dir("LitebaseSDK") {
    if (isUnix()) {
      sh "ant device"
    } else {
      bat "ant device"
    }
        }
            dir("LitebaseSDK/builders") {
    if (isUnix()) {
      sh "ant makeNativeHT"
    } else {
      bat "ant makeNativeHT"
    }
            }
}

        stash 'scm'
        // archiveArtifacts artifacts: 'LitebaseSDK/dist/lib/*.tcz, TotalCrossSDK/dist/vm/*.tcz', fingerprint: true, onlyIfSuccessful: true
    }
    }
}

parallel firstBranch: {
    // node('master') {
    //     stage('SDK') {
    //         unstash 'scm'
    //         sh 'cd TotalCrossSDK && ./gradlew clean dist'
    //     } 
    // }
}, iOS: {
    node('MacInCloud') {
        stage('iOS') {
            timeout(time: 15) {
            wrap([$class: 'TimestamperBuildWrapper']) {
            unstash 'scm'
            sh 'cd TotalCrossVM/builders/xcode && chmod a+x build.sh && ./build.sh'
            xcodeBuild (
                appURL: '', 
                assetPackManifestURL: '', 
                buildDir: '', 
                bundleID: '', 
                bundleIDInfoPlistPath: '', 
                cfBundleShortVersionStringValue: '', 
                cfBundleVersionValue: '', 
                cleanBeforeBuild: false, 
                configuration: 'Release', 
                developmentTeamName: 'totalcross',
                displayImageURL: '', 
                fullSizeImageURL: '', 
                generateArchive: true, 
                ipaExportMethod: 'ad-hoc', 
                ipaName: '', 
                ipaOutputDirectory: '', 
                keychainId: 'MacInCloud-keychain',
                logfileOutputDirectory: '', 
                manualSigning: false, 
                provisioningProfiles: [[provisioningProfileAppId: '', provisioningProfileUUID: '']], 
                resultBundlePath: '', 
                sdk: '', 
                symRoot: '', 
                target: '', 
                thinning: '', 
                unlockKeychain: true, 
                xcodeProjectFile: '', 
                xcodeProjectPath: 'TotalCrossVM/builders/xcode', 
                xcodeSchema: 'TotalCross', 
                xcodeWorkspaceFile: 'TotalCross', 
                xcodebuildArguments: ''
            )
            sh 'xcodebuild -exportArchive -archivePath TotalCrossVM/builders/xcode/build/Release-iphoneos/TotalCross.xcarchive -exportPath TotalCrossVM/builders/xcode/build/Release-iphoneos -exportOptionsPlist TotalCrossVM/builders/xcode/ExportOptions.plist'
            sh 'mkdir -p TotalCrossSDK/dist/vm/ios && mv TotalCrossVM/builders/xcode/build/Release-iphoneos/TotalCross*.ipa TotalCrossSDK/dist/vm/ios/TotalCross.ipa'
            stash name:'iOS', includes:'TotalCrossSDK/dist/vm/ios/TotalCross.ipa, TotalCrossVM/builders/xcode/build/Release-iphoneos/**/*'
        } 
            }
        }
    }
 }, Android: {
    node('build-android') {
         stage('Android') {
             timeout(time: 12) {
             wrap([$class: 'TimestamperBuildWrapper']) {
             unstash 'scm'
             dir("TotalCrossVM/builders/droid") {
                sh './gradlew clean assembleRelease copyApk'
             }
             stash name:'Android', includes:'output/release/**/*, TotalCrossVM/builders/droid/app/build/**/*'
             }
             }
         }
     }
// }, Linux_x86: {
//     node('build-linux') {
//         stage('Linux x86') {
//             checkout scm // Jenkins will clone the appropriate git branch, no env vars needed
//             sh 'cd TotalCrossVM/builders/gcc-posix && chmod a+x build.sh && ./build.sh'
//             archiveArtifacts (
//                 artifacts: 'TotalCrossVM/builders/gcc-posix/tcvm/linux/**/libtcvm.so, LitebaseSDK/builders/gcc/linux/**/libLitebase.so', 
//                 excludes: 'TotalCrossVM/builders/gcc-posix/tcvm/linux/**/.libs/*, LitebaseSDK/builders/gcc/linux/**/.libs/*'
//             )
//         }

//         // // Further build steps happen here
//         // stage('Clean Verify') {
//         //     //sh 'mvn clean verify'
//         //     sh 'cd TotalCrossVM/builders/gcc-linux-arm/sdl && ./build.sh'
//         // }         
//     }
 }, Windows: {
    node('build-windows') {
         stage('Windows') {
             timeout(time: 5) {
             wrap([$class: 'TimestamperBuildWrapper']) {
             deleteDir()
             unstash 'scm'
            def runScript = readFile('TotalCrossVM/builders/vc2008/build.bat')     
            bat runScript
stash (
    includes: '**/dist/**/*.dll, **/dist/**/*.cab, output/release/**/*', 
    excludes: '*.log', 
    name: 'vs2008'
)
             }
             }
         }
     }
// }, Linux_ARM: {
//     node('build-linux-arm') {
//         stage('Linux ARM') {
//             unstash 'scm'
//             // sh 'cd ${WORKSPACE}/TotalCrossVM/builders/gcc-linux-arm/sdl && ./build.sh'
//         }         
//     }
},failFast: true

node('master') {
    stage('Package') {
        wrap([$class: 'TimestamperBuildWrapper']) {
            unstash 'iOS'
            unstash 'Android'
            unstash 'vs2008'
            sh 'mv LitebaseSDK/dist/lib/LitebaseLib.tcz TotalCrossSDK/dist/vm'
            sh 'mv LitebaseSDK/dist/lib/Win32/Litebase.dll TotalCrossSDK/dist/vm/win32'
            sh 'cp -Rn output/release/TotalCrossSDK/dist/vm TotalCrossSDK/dist/'
            sh 'cp TotalCrossSDK/etc/tools/makecab/*CEinstall* TotalCrossSDK/dist/vm/wince/'
            sh 'mv TotalCrossSDK/build/docs/javadoc TotalCrossSDK/docs/html'
            sh 'mv TotalCrossVM/builders/xcode/build/Release-iphoneos/*.xcarchive TotalCrossSDK/etc/tools/iOSCodesign/'
            sh 'mv LitebaseSDK/src/java/litebase TotalCrossSDK/src/main'
            sh 'cp -Rf output/release/TotalCrossSDK/etc TotalCrossSDK'
            archiveArtifacts (
                artifacts: 'TotalCrossSDK/license.txt, TotalCrossSDK/README.txt, TotalCrossSDK/dist/**/*, TotalCrossSDK/src/main/**/*, TotalCrossSDK/docs/html/*, TotalCrossSDK/docs/html/totalcross/**/*, TotalCrossSDK/etc/**/*',
                excludes: 'TotalCrossSDK/etc/obfuscator/**/*, TotalCrossSDK/etc/scripts/**/*, TotalCrossSDK/etc/tools/jdeb/**/*, TotalCrossSDK/**/totalcross.inf, TotalCrossSDK/**/*.log, TotalCrossSDK/src/main/java/ras/**/*, TotalCrossSDK/src/main/java/tc/tools/**/*, TotalCrossSDK/src/main/java/**/*4D.java, TotalCrossSDK/src/main/java/**/package.html, TotalCrossSDK/src/**/subbuild.xml, TotalCrossSDK/etc/tools/iOSCodesign/TotalCross.xcarchive/BCSymbolMaps/**/*, TotalCrossSDK/etc/tools/iOSCodesign/TotalCross.xcarchive/dSYMs/**/*'
            )
        }
    }
}


