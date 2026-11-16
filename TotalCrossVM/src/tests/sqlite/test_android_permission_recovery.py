#!/usr/bin/env python3
"""Compile and exercise the *real* SQLite Android recovery C functions.

Runs with the host C compiler, without the TotalCross VM or an Android device.
Stubbed SQLite and Android bridge functions provide deterministic IO/permission
outcomes. This verifies retry count, error scope, handle cleanup, and that an
already-open connection is preserved.
"""
from pathlib import Path
import shutil
import subprocess
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[4]
NATIVE_DB = ROOT / "TotalCrossVM/src/nm/db/NativeDB.c"
FILE_H = ROOT / "TotalCrossVM/src/nm/io/File.h"
FILE_C = ROOT / "TotalCrossVM/src/nm/io/posix/File_c.h"
PERMISSIONS_JAVA = ROOT / (
    "TotalCrossVM/android/app/src/main/java/totalcross/android/"
    "AndroidPermissionManager.java"
)


def extract_function(source, start):
    first = source.find(start)
    if first < 0:
        raise AssertionError("Missing function: " + start)
    opening = source.index("{", first)
    depth = 0
    for i in range(opening, len(source)):
        if source[i] == "{":
            depth += 1
        elif source[i] == "}":
            depth -= 1
            if depth == 0:
                return source[first:i + 1]
    raise AssertionError("Unbalanced C braces: " + start)


MOCKS = r"""
#include <assert.h>
#include <stdbool.h>
#include <errno.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#define ANDROID 1
#define TC_API
#define null NULL
#define TRACE(x)
typedef int int32;
typedef void *TCObject;
typedef char *CharP;
typedef struct TestSqlite { int id; } sqlite3;
typedef struct TestParams {
    int32 i32[1];
    TCObject obj[2];
    void *currentContext;
} TestParams, *NMParams;
#define SQLITE_OK 0
#define SQLITE_PERM 3
#define SQLITE_READONLY 8
#define SQLITE_CANTOPEN 14
#define SQLITE_CORRUPT 11

typedef struct SimulatedAttempt {
    int rc;
    int os_errno;
    int returns_handle;
} SimulatedAttempt;
static SimulatedAttempt configured[2];
static int attemptCount, permissionCount, closeCount, exceptionCount, freeCount;
static int mutexInitCount, savedError, grant;
static sqlite3 fake = {42};
static sqlite3 *published;
static const char *lastMessage;

static void reset_simulation(
    int rc, int os_error, int first_handle, int second_rc,
    int second_os_error, int second_handle, int grant_value)
{
    configured[0] = (SimulatedAttempt){rc, os_error, first_handle};
    configured[1] = (SimulatedAttempt){second_rc, second_os_error, second_handle};
    attemptCount = permissionCount = closeCount = exceptionCount = 0;
    freeCount = mutexInitCount = savedError = 0;
    lastMessage = NULL;
    grant = grant_value;
    published = NULL;
}

static int sqlite3_open_v2(const char *path, sqlite3 **handle, int flags,
                           const char *vfs)
{
    assert(path && handle && vfs == NULL);
    assert(attemptCount < 2 && flags == 6);
    SimulatedAttempt entry = configured[attemptCount++];
    *handle = entry.returns_handle ? &fake : NULL;
    savedError = entry.os_errno;
    return entry.rc;
}
static int sqlite3_system_errno(sqlite3 *handle)
{
    assert(handle == &fake);
    return savedError;
}
static int sqlite3_close(sqlite3 *handle)
{
    assert(handle == &fake);
    closeCount++;
    return 0;
}
static bool fileRequestStoragePermission(void)
{
    permissionCount++;
    return grant != 0;
}
static sqlite3 *gethandle(void *ctx, TCObject obj)
{
    return published;
}
static void sethandle(void *ctx, TCObject obj, sqlite3 *handle)
{
    published = handle;
}
static void initSqlite3Mutex(TCObject obj)
{
    assert(published == &fake);
    mutexInitCount++;
}
static CharP newSafeString(TCObject obj, int32 *unused)
{
    const char *src = (const char *)obj;
    size_t len = strlen(src) + 1;
    char *copy = (char *)malloc(len);
    assert(copy);
    memcpy(copy, src, len);
    return copy;
}
static void freeSafeString(CharP value)
{
    assert(value);
    free(value);
    freeCount++;
}
static void throw_errorcode(void *ctx, TCObject obj, int32 rc)
{
    assert(published == &fake || published == NULL);
    exceptionCount++;
    savedError = rc;
}
static void throwexmsg(void *ctx, TCObject obj, const char *message)
{
    lastMessage = message;
    exceptionCount++;
}
"""

CASES = r"""
static void run_case(const char *path, int rc, int os_error, int grant_value,
                     int retry_rc, int expected_tries, int expected_perms,
                     int expected_closes, int expected_throws, int expected_init)
{
    reset_simulation(rc, os_error, 1, retry_rc, EACCES, 1, grant_value);
    TestParams values = {0};
    values.i32[0] = 6;
    values.obj[0] = (TCObject)0x1234;
    values.obj[1] = (TCObject)path;

    tdsNDB__open_si(&values);
    assert(attemptCount == expected_tries);
    assert(permissionCount == expected_perms);
    assert(closeCount == expected_closes);
    assert(exceptionCount == expected_throws);
    assert(mutexInitCount == expected_init);
    assert(freeCount == 1);
    assert(published == (expected_init ? &fake : NULL));
}
int main(void)
{
    // Successful Android WRITE permission recovery: exactly one retry.
    run_case("/sdcard/first.db", SQLITE_CANTOPEN, EACCES, 1, SQLITE_OK,
             2, 1, 1, 0, 1);

    // User denies: no retry; original SQLite code propagates.
    run_case("/sdcard/deny.db", SQLITE_CANTOPEN, EPERM, 0, SQLITE_OK,
             1, 1, 1, 1, 0);

    // Another OS error: never request permission.
    run_case("/sdcard/missing.db", SQLITE_CANTOPEN, ENOENT, 1, SQLITE_OK,
             1, 0, 1, 1, 0);

    // The same EACCES in a private app directory must not prompt.
    run_case("/data/user/0/app/files/internal.db", SQLITE_CANTOPEN, EACCES,
             1, SQLITE_OK, 1, 0, 1, 1, 0);

    // Corruption is not a storage runtime permission.
    run_case("/sdcard/corrupt.db", SQLITE_CORRUPT, EACCES, 1, SQLITE_OK,
             1, 0, 1, 1, 0);

    // Permission was granted but access is still forbidden: one retry max.
    run_case("/storage/emulated/0/blocked.db", SQLITE_CANTOPEN, EACCES,
             1, SQLITE_CANTOPEN, 2, 1, 2, 1, 0);

    // Existing database must not be closed on a second open attempt.
    reset_simulation(SQLITE_OK, 0, 1, SQLITE_OK, 0, 1, 1);
    published = &fake;
    TestParams existing = {0};
    existing.i32[0] = 6;
    existing.obj[0] = (TCObject)0x1234;
    existing.obj[1] = (TCObject)"/sdcard/already.db";
    tdsNDB__open_si(&existing);
    assert(attemptCount == 0);
    assert(permissionCount == 0);
    assert(closeCount == 0);
    assert(exceptionCount == 1);
    assert(lastMessage && strcmp(lastMessage, "DB already open") == 0);
    assert(published == &fake);

    // Fully accessible path: no permission prompt, no retry.
    run_case("/sdcard/ready.db", SQLITE_OK, 0, 1, SQLITE_OK,
             1, 0, 0, 0, 1);

    puts("8 SQLite C native recovery contract scenarios PASS");
    return 0;
}
"""


class NativeSQLiteAndroidPermissionTests(unittest.TestCase):
    def test_real_native_functions_compile_and_behave(self):
        compiler = shutil.which("cc") or shutil.which("clang") or shutil.which("gcc")
        self.assertIsNotNone(compiler, "A C compiler is required")
        c = NATIVE_DB.read_text()
        fragment = "\n\n".join((
            extract_function(c, "static bool sqliteIsAndroidSharedStoragePath("),
            extract_function(c, "static bool sqliteOpenNeedsAndroidStoragePermission("),
            extract_function(c, "TC_API void tdsNDB__open_si("),
        ))
        with tempfile.TemporaryDirectory(prefix="tc-sqlite-android-") as tmp:
            source = Path(tmp) / "test.c"
            binary = Path(tmp) / "test"
            source.write_text(MOCKS + "\n" + fragment + "\n" + CASES)
            compile_run = subprocess.run(
                [compiler, "-std=c11", "-Wall", "-Wextra", "-Wno-unused-parameter",
                 "-Werror", "-o", str(binary), str(source)],
                capture_output=True, text=True)
            self.assertEqual(compile_run.returncode, 0,
                             compile_run.stdout + compile_run.stderr)
            check = subprocess.run([str(binary)], capture_output=True, text=True)
            self.assertEqual(check.returncode, 0, check.stdout + check.stderr)
            self.assertIn("8 SQLite C native recovery contract scenarios PASS",
                          check.stdout)

    def test_shared_bridge_is_declared_and_non_static(self):
        self.assertIn("bool fileRequestStoragePermission(void);", FILE_H.read_text())
        implementation = FILE_C.read_text()
        self.assertIn("bool fileRequestStoragePermission(void)", implementation)
        self.assertNotIn("static bool fileRequestStoragePermission(void)", implementation)
        self.assertIn("requestStoragePermission", implementation)

    def test_android10_only_legacy_candidate(self):
        java = PERMISSIONS_JAVA.read_text()
        self.assertIn("Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q", java)
        self.assertNotIn("Build.VERSION.SDK_INT < Build.VERSION_CODES.Q", java)


if __name__ == "__main__":
    unittest.main()
