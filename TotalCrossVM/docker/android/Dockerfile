FROM fabernovel/android:api-28-gcloud-ndk-v1.2.0

ENV BUILD_FOLDER /build

WORKDIR ${BUILD_FOLDER}

COPY . ${BUILD_FOLDER}

RUN cd TotalCrossVM/android && ./gradlew assembleDebug copyApk -x test