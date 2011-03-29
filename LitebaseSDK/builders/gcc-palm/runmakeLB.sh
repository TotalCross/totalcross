cd /PalmDev/LitebaseSDK_200/builders/gcc-palm
make_580 $1 -j $NUMBER_OF_PROCESSORS
if test $? -eq 0
then
    /cygdrive/c/Arquiv~1/palmOne/QuickInstall.exe p:/LitebaseSDK_200/builders/gcc-palm/Litebase.prc
    /cygdrive/c/palm/QuickInstall.exe p:/LitebaseSDK_200/builders/gcc-palm/Litebase.prc
fi
