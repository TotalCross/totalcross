cd /PalmDev/TotalCrossVM/builders/gcc-palm/tcvm
make_580 $1 -j $NUMBER_OF_PROCESSORS
if test $? -eq 0
then
    /cygdrive/c/palm/QuickInstall.exe p:/TotalCrossVM/builders/gcc-palm/tcvm/TCVM.prc
fi
