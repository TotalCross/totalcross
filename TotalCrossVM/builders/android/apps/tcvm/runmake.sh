DROID_PROJECTS_HOME=$(cygpath -a ${DROID_PROJECTS_HOME})
NDK_HOME=$(cygpath -a ${NDK_HOME})
TC_HOME=$(cygpath -a ${TC_HOME})

# Re-create all symbolic links
rm -f ${NDK_HOME}/apps
rm -f ${DROID_PROJECTS_HOME}/apps/tcvm/TotalCrossVM
rm -f ${NDK_HOME}/out
rm -f ${NDK_HOME}/TotalCrossVM
ln -s ${DROID_PROJECTS_HOME}/apps ${NDK_HOME}
ln -s ${TC_HOME} ${DROID_PROJECTS_HOME}/apps/tcvm
ln -s ${DROID_PROJECTS_HOME}/out ${NDK_HOME}
ln -s ${TC_HOME} ${NDK_HOME}

# move to the ndk root folder
cd ${NDK_HOME}
make APP=tcvm -j $NUMBER_OF_PROCESSORS
