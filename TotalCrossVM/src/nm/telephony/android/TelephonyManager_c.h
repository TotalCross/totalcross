// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

static TCObject getStaticStringFieldFromClass(JNIEnv* env, Context currentContext, jclass aClass, CharP fieldName) {
    char buffer[128];
    jfieldID jfID = (*env)->GetStaticFieldID(env, aClass, fieldName, "Ljava/lang/String;");
    jstring jStringField = (jstring) (*env)->GetStaticObjectField(env, aClass, jfID);
    if (jStringField != null) {
        jstring2CharP(jStringField, buffer);
        (*env)->DeleteLocalRef(env, jStringField);
        return createStringObjectFromCharP(currentContext, buffer, -1);
    }
    return null;
}

static Err android_ttTM_nativeInitialize(Context currentContext, TCObject* deviceIds, TCObject* simSerialNumbers, TCObject* lineNumbers) {
    JNIEnv* env = getJNIEnv();
    jmethodID method = (*env)->GetStaticMethodID(env, applicationClass, "requestPhoneStatePermission", "()I");
    jint result = (*env)->CallStaticIntMethod(env, applicationClass, method);
    if (result <= 0) {
        return -1;
    }

    jclass jSettingsClass = androidFindClass(env, "totalcross/android/Settings4A");
    jmethodID fillTelephonySettings = (*env)->GetStaticMethodID(env, jSettingsClass, "fillTelephonySettings", "()V");
    (*env)->CallStaticVoidMethod(env, jSettingsClass, fillTelephonySettings);

    // deviceIds
    (*deviceIds) = createStringArray(currentContext, 2);
    if (*deviceIds != null) {
        TCObject imeiObj = getStaticStringFieldFromClass(env, currentContext, jSettingsClass, "imei");
        if (imeiObj != null) {
            setObjectLock(ARRAYOBJ_GET(*deviceIds, 0) = imeiObj, UNLOCKED);
        }

        TCObject imei2Obj = getStaticStringFieldFromClass(env, currentContext, jSettingsClass, "imei2");
        if (imei2Obj != null) {
            setObjectLock(ARRAYOBJ_GET(*deviceIds, 1) = imei2Obj, UNLOCKED);
        }
        setObjectLock((*deviceIds), UNLOCKED);
    }

    // simSerialNumbers
    (*simSerialNumbers) = createStringArray(currentContext, 1);
    if (*simSerialNumbers != null) {
        TCObject iccidObj = getStaticStringFieldFromClass(env, currentContext, jSettingsClass, "iccid");
        if (iccidObj != null) {
            setObjectLock(ARRAYOBJ_GET(*simSerialNumbers, 0) = iccidObj, UNLOCKED);
        }
        setObjectLock((*deviceIds), UNLOCKED);
    }


    // phone number - needed to move to here or jni on android 5 will abort
    (*lineNumbers) = createStringArray(currentContext, 1);
    if (*lineNumbers != null) {
        TCObject lineNumberObj = getStaticStringFieldFromClass(env, currentContext, jSettingsClass, "lineNumber");
        if (lineNumberObj != null) {
            setObjectLock(ARRAYOBJ_GET(*lineNumbers, 0) = lineNumberObj, UNLOCKED);
        }
        setObjectLock((*deviceIds), UNLOCKED);
    }

    return NO_ERROR;
}
