#!/bin/bash

mensagem2jeff() {
	echo "=== $* ==="
}

if [ false ]; then
(
  mensagem2jeff "STARTED TC-SDK BUILD"

  curl https://totalcross-transfer.s3-us-west-2.amazonaws.com/ScanditBarcodeScanner.framework-b1.zip --output ScanditBarcodeScanner.framework-b1.zip
  unzip ScanditBarcodeScanner.framework-b1.zip

  if [ -f Podfile ]; then
    mensagem2jeff "COM Podfile"
    
    #which pod && POD_EXEC=pod || {
      # pod not installed in path
      #POD_EXEC=`gem which cocoapods | sed 's_gems/.*$_bin/pod_'`
    #}
    #echo $POD_EXEC
    #$POD_EXEC install
    /usr/local/bin/pod install
    
    /usr/bin/xcodebuild -workspace TotalCross.xcworkspace -list
  fi
  
  xcode_run() {
    echo "rodando o build para schema '$1'"
    /usr/bin/xcodebuild -workspace TotalCross.xcworkspace -scheme "$1" -configuration Release build GCC_PREPROCESSOR_DEFINITIONS="POSIX linux darwin TOTALCROSS TC_EXPORTS FORCE_LIBC_ALLOC ENABLE_DEMO" RUN_CLANG_STATIC_ANALYZER=${USE_STATIC_ANALYZER}    
  }
  
  xcode_clean() {
    echo "rodando o clean para schema '$1'"
    /usr/bin/xcodebuild -workspace TotalCross.xcworkspace -scheme "$1" -configuration Release clean GCC_PREPROCESSOR_DEFINITIONS="POSIX linux darwin TOTALCROSS TC_EXPORTS FORCE_LIBC_ALLOC ENABLE_DEMO" RUN_CLANG_STATIC_ANALYZER=${USE_STATIC_ANALYZER}    
  }
  
  has_google_toolbox=false
  has_pods_totalcross=false
  
  schemas=`/usr/bin/xcodebuild -workspace TotalCross.xcworkspace -list`
  
  for schema in $schemas; do
    xcode_clean "$schema"
    
    echo "analizando schema $schema"
    case "$schema" in
      GoogleToolboxForMac)
        has_google_toolbox=true
        ;;
      Pods-TotalCross)
        has_pods_totalcross=true
        ;;
    esac
  done
  
  $has_google_toolbox && xcode_run GoogleToolboxForMac
  $has_pods_totalcross && xcode_run Pods-TotalCross
  
  for schema in $schemas; do
    case "$schema" in
      TotalCross)
        echo "deixando schema TotalCross por último"
        ;;
      GoogleToolboxForMac|Pods-TotalCross|*copy)
        echo "ignorando schema $schema"
        ;;
      *)
        xcode_run "$schema"
        ;;
    esac
  done
  xcode_run TotalCross
  ######/usr/bin/xcodebuild -workspace TotalCross.xcworkspace -scheme TotalCross -configuration Release clean build GCC_PREPROCESSOR_DEFINITIONS="POSIX linux darwin TOTALCROSS TC_EXPORTS FORCE_LIBC_ALLOC ENABLE_DEMO" RUN_CLANG_STATIC_ANALYZER=${USE_STATIC_ANALYZER}
  #else
  #  mensagem2jeff "SEM Podfile"
  #  /usr/bin/xcodebuild -project tcvm.xcodeproj -alltargets -configuration Release clean build GCC_PREPROCESSOR_DEFINITIONS="POSIX linux darwin TOTALCROSS TC_EXPORTS FORCE_LIBC_ALLOC ENABLE_DEMO" RUN_CLANG_STATIC_ANALYZER=${USE_STATIC_ANALYZER}
  #fi

  
)

(
  if [ ! -f ${PROJECT_PATH}/Podfile ]; then
  mensagem2jeff "IGNORANDO BUILD LITEBASE POR QUESTÕES DE JÁ FOI FEITO NA NOVA ITERAÇÃO"
    #cd TotalCross/LitebaseSDK/builders/xcode
    #mensagem2jeff "BUILD LITEBASE"
    #/usr/bin/xcodebuild -alltargets -project Litebase.xcodeproj -configuration Release clean build GCC_PREPROCESSOR_DEFINITIONS="POSIX linux darwin LB_EXPORTS FORCE_LIBC_ALLOC ENABLE_DEMO" RUN_CLANG_STATIC_ANALYZER=${USE_STATIC_ANALYZER}
  else
    mensagem2jeff "IGNORANDO BUILD LITEBASE POR QUESTÕES DE JÁ FOI FEITO COM PODFILE"
  fi
)

(
  if [ -d TotalCross/TotalCrossVM/libs/zxing/iphone/ZXingWidget ]
  then
     mensagem2jeff "BUILD ZXING"
     cd TotalCross/TotalCrossVM/libs/zxing/iphone/ZXingWidget
     /usr/bin/xcodebuild -alltargets -project ZXingWidget.xcodeproj -configuration Release clean build RUN_CLANG_STATIC_ANALYZER=${USE_STATIC_ANALYZER}
  fi
)

mensagem2jeff "TERMINOU BUILD BONITO"

fi
