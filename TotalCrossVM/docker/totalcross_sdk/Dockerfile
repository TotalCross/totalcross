FROM fabernovel/android:api-28-gcloud-ndk-v1.2.0

ENV BUILD_FOLDER /build

COPY . ${BUILD_FOLDER}

WORKDIR ${BUILD_FOLDER}

RUN cd TotalCrossSDK/ && ./gradlew dist -x test