DROID_PROJECTS_HOME=$(cygpath -a ${DROID_PROJECTS_HOME})
NDK_HOME=$(cygpath -a ${NDK_HOME})
TC_HOME=$(cygpath -a ${TC_HOME})
LB_HOME=$(cygpath -a ${LB_HOME})

# Re-create all symbolic links
rm -f ${NDK_HOME}/apps
rm -f ${DROID_PROJECTS_HOME}/apps/litebase/LitebaseSDK
rm -f ${DROID_PROJECTS_HOME}/apps/litebase/TotalCrossVM
rm -f ${NDK_HOME}/out
rm -f ${NDK_HOME}/LitebaseSDK
rm -f ${NDK_HOME}/TotalCrossVM
ln -s ${DROID_PROJECTS_HOME}/apps ${NDK_HOME}
ln -s ${LB_HOME} ${DROID_PROJECTS_HOME}/apps/litebase
ln -s ${TC_HOME} ${DROID_PROJECTS_HOME}/apps/litebase
ln -s ${DROID_PROJECTS_HOME}/out ${NDK_HOME}
ln -s ${LB_HOME} ${NDK_HOME}
ln -s ${TC_HOME} ${NDK_HOME}

# move to the ndk root folder
cd ${NDK_HOME}
make APP=litebase -j $NUMBER_OF_PROCESSORS
