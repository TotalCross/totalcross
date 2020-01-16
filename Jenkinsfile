pipeline{
    agent {
        label "master"
    }
    options {
        skipDefaultCheckout()
        timestamps()
        timeout(time: 30, unit: 'MINUTES')
        preserveStashes(buildCount: 5)
        parallelsAlwaysFailFast()
    }
    stages{
        stage('Checkout') {
            steps {
                checkout scm // Jenkins will clone the appropriate git branch, no env vars needed
                execGradle("TotalCrossSDK", "clean dist")
                execAnt("TotalCrossVM/builders", 'makeNativeHT')
                execAnt("LitebaseSDK/builders", 'makeNativeHT')
                execAnt("LitebaseSDK", 'device')
            }
            post{
                success{
                    stash(
                        name: 'scm',
                        excludes: '**/.git/**/*, **/.gradle/**/*, TotalCrossVM/builders/vc2013/**/*, TotalCrossSDK/build/**/*, TotalCrossSDK/etc/tools/sqlite/**/*, TotalCrossSDK/etc/tools/jdeb/**/*, TotalCrossSDK/etc/libs/**/*'
                    )
                }
            }
        }
        stage('Build') {
            parallel {
                stage('Windows') {
                    agent {
                        label "build-windows"
                    }
                    steps {
                        deleteDir()
                        unstash 'scm'
                        bat readFile('TotalCrossVM/builders/vc2008/build.bat')
                    }
                    post{
                        success{
                            stash(
                                name: 'vs2008',
                                includes: '**/dist/**/*.dll, **/dist/**/*.cab, output/release/**/*',
                                excludes: '*.log, *.inf'
                            )
                        }
                    }
                }

                stage('Android') {
                    agent {
                        label "build-android"
                    }
                    steps {
                        deleteDir()
                        unstash 'scm'
                        execGradle("TotalCrossVM/builders/droid", "clean assembleRelease copyApk")
                    }
                    post{
                        success{
                            stash(
                                name: 'Android',
                                includes: 'output/release/**/*, TotalCrossVM/builders/droid/app/build/outputs/**/*'
                            )
                        }
                    }
                }

                stage('iOS') {
                    agent {
                        label "MacInCloud"
                    }
                    steps {
                        unstash 'scm'
                        sh 'cd TotalCrossVM/builders/xcode && chmod a+x build.sh && ./build.sh'
                        xcodeBuild(
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
                    }
                    post{
                        success{
                            stash(
                                name: 'iOS',
                                includes: 'TotalCrossSDK/dist/vm/ios/TotalCross.ipa, TotalCrossVM/builders/xcode/build/Release-iphoneos/**/*',
                                excludes: '**/TotalCross.xcarchive/dSYMs/**/*, **/BCSymbolMaps/**/*'
                            )
                        }
                    }
                }

                stage('Javadoc') {
                    steps {
                        execAnt("TotalCrossSDK", "javadoc")
                    }
                }
            }
        }

        stage('Package') {
            environment {
                TC_VERSION = "${sh(script: 'cd TotalCrossSDK && ./gradlew properties -q | grep "version: " | awk \'{print $2}\' | tr -d \'[:space:]\'', , returnStdout: true).trim()}"
            }
            steps {
                unstash 'iOS'
                unstash 'Android'
                unstash 'vs2008'
                sh 'mv LitebaseSDK/dist/lib/LitebaseLib.tcz TotalCrossSDK/dist/vm'
                sh 'mv LitebaseSDK/dist/lib/Win32/Litebase.dll TotalCrossSDK/dist/vm/win32'
                sh 'cp -Rn output/release/TotalCrossSDK/dist/vm TotalCrossSDK/dist/'
                sh 'cp TotalCrossSDK/etc/tools/makecab/*CEinstall* TotalCrossSDK/dist/vm/wince/'
                sh 'mv TotalCrossVM/builders/xcode/build/Release-iphoneos/*.xcarchive TotalCrossSDK/etc/tools/iOSCodesign/'
                sh 'mkdir -p TotalCrossSDK/src/lb && cp -Rf LitebaseSDK/src/java/samples TotalCrossSDK/src/lb'
                sh 'cp -Rf output/release/TotalCrossSDK/etc TotalCrossSDK'
            }
            post{
                success{
                    fileOperations(
                        [
                            folderCreateOperation('release'),
                            fileCopyOperation(
                                includes: 'TotalCrossSDK/license.txt, TotalCrossSDK/README.txt',
                                flattenFiles: false,
                                targetLocation: 'release'),
                            fileCopyOperation(
                                includes: 'TotalCrossSDK/dist/**/*',
                                excludes: '**/*.inf, **/*.log',
                                flattenFiles: false,
                                targetLocation: 'release'),
                            fileCopyOperation(
                                includes: 'TotalCrossSDK/docs/html/**/*',
                                flattenFiles: false,
                                targetLocation: 'release'),
                            fileCopyOperation(
                                includes: 'TotalCrossSDK/etc/**/*',
                                excludes: '**/*.inf, **/tools/ant/*ant*.jar, **/obfuscator/**/*, **/scripts/**/*, **/tools/jdeb/**/*',
                                flattenFiles: false,
                                targetLocation: 'release'),
                            fileCopyOperation(
                                includes: 'TotalCrossSDK/src/**/*',
                                excludes: 'TotalCrossSDK/src/test/**/*, TotalCrossSDK/src/main/java/ras/**/*, TotalCrossSDK/src/main/java/tc/tools/**/*, TotalCrossSDK/src/main/java/**/*4D.java, TotalCrossSDK/src/main/java/**/package.html, TotalCrossSDK/src/**/subbuild.xml',
                                flattenFiles: false,
                                targetLocation: 'release'),
                            folderRenameOperation(source: 'release/TotalCrossSDK', destination: 'release/TotalCross'),
                            fileZipOperation('release/TotalCross'),
                            fileRenameOperation(source: 'TotalCross.zip', destination: "TotalCross-${TC_VERSION}.zip")
                        ]
                    )

                    archiveArtifacts(
                        artifacts: 'TotalCross-*.zip, TotalCrossSDK/build/libs/totalcross-sdk*',
                        excludes: '**/totalcross-sdk-*-intermediate.jar',
                        fingerprint: true
                    )
                    fingerprint 'release/TotalCross/dist/**/*'
                }
            }
        }
    }
}

def execAnt(String path, String command) {
    withAnt(installation: 'ant') {
        dir(path) {
            if (isUnix()) {
                sh "ant ${command}"
            } else {
                bat "ant ${command}"
            }
        }
    }
}

def execGradle(String path, String command) {
    dir(path) {
        if (isUnix()) {
            sh "./gradlew ${command}"
        } else {
            bat "gradlew.bat ${command}"
        }
    }
}