#include <jni.h>
#include <android/log.h>
#include <dirent.h>
#include <cstdio>
#include <cstring>
#include <cstdlib>
#include <fstream>
#include <vector>
#include <algorithm>
#include <sys/stat.h>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include <unistd.h>
#include <fcntl.h>
#include <sys/mman.h>
#include <sys/stat.h>
#include <sys/syscall.h>
#include <bits/glibc-syscalls.h>
#include <string>
#include <sstream>

#define LOG_TAG "FridaDetect"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

std::string readFile(const char* path) {
    std::ifstream file(path);
    if (!file.is_open()) return "";
    std::stringstream ss;
    ss << file.rdbuf();
    return ss.str();
}

bool isFridaServerDetected() {
    DIR *dir = opendir("/proc");
    if (!dir) {
        LOGE("Failed to open /proc");
        return false;
    }

    const char* suspicious_names[] = {
            "frida", "gum-js", "gmain", "frida-server", "frida-helper",
            "frida-injector", "r2frida", "frida-agent", nullptr
    };

    struct dirent *entry;
    char path[256];
    char comm[256];

    while ((entry = readdir(dir)) != nullptr) {
        if (entry->d_type == DT_DIR) {
            int pid = atoi(entry->d_name);
            if (pid > 0) {
                snprintf(path, sizeof(path), "/proc/%d/comm", pid);
                FILE *fp = fopen(path, "r");
                if (fp) {
                    if (fgets(comm, sizeof(comm), fp)) {
                        // Remove newline at end if present
                        comm[strcspn(comm, "\n")] = 0;
                        for (int i = 0; suspicious_names[i] != nullptr; i++) {
                            if (strstr(comm, suspicious_names[i]) != nullptr) {
                                LOGE("Frida or related tool detected: %s", comm);
                                fclose(fp);
                                closedir(dir);
                                return true;
                            }
                        }
                    }
                    fclose(fp);
                }
            }
        }
    }
    closedir(dir);
    LOGI("No Frida or related tool detected.");
    return false;
}

bool isObjectionInjected() {
    std::ifstream maps("/proc/self/maps");
    if (!maps.is_open()) return false;

    std::string line;
    std::vector<std::string> suspiciousLibs = {
            "libfrida", "frida-agent", "objection"
    };

    while (std::getline(maps, line)) {
        for (const auto& lib : suspiciousLibs) {
            if (line.find(lib) != std::string::npos) {
                return true;
            }
        }
    }
    return false;
}

bool isMagiskMountDetected() {
    std::ifstream mounts("/proc/self/mounts");
    if (!mounts.is_open()) return false;

    std::string line;
    while (std::getline(mounts, line)) {
        if (line.find("/data/adb/modules") != std::string::npos ||
            line.find("/sbin/.magisk") != std::string::npos) {
            return true;
        }
    }
    return false;
}

//bool checkAssetFile(AAssetManager* mgr, const char* filename) {
//    AAsset* file = AAssetManager_open(mgr, filename, AASSET_MODE_UNKNOWN);
//    if (file) {
//        AAsset_close(file);
//        return true;
//    }
//    return false;
//}

bool isMagiskPropertySet() {
    const char* prop = std::getenv("MAGISK_HIDE");
    return (prop != nullptr);
}

bool isMagiskSocketPresent() {
    struct stat info;
    return stat("/dev/.magisk_unix_socket", &info) == 0;
}

bool fileExists(const char* path) {
    struct stat st;
    return (stat(path, &st) == 0);
}

bool checkSuspiciousProperties() {
    const char* suspiciousKeywords[] = {
            "generic", "unknown", "emulator", "sdk", "google_sdk", "genymotion",
            "x86", "goldfish", "ranchu", "vbox", "test-keys"
    };

    std::string buildProp = readFile("/system/build.prop");
    if (buildProp.empty()) return false;

    for (const auto& keyword : suspiciousKeywords) {
        if (buildProp.find(keyword) != std::string::npos) {
            LOGI("Found suspicious keyword in build.prop: %s", keyword);
            return true;
        }
    }
    return false;
}

bool checkEmulatorFiles() {
    const char* emulatorFiles[] = {
            "/dev/qemu_pipe",
            "/dev/socket/qemud",
            "/dev/qemu_trace",
            "/system/lib/libc_malloc_debug_qemu.so",
            "/sys/qemu_trace",
            "/system/bin/qemu-props"
    };

    for (const auto& path : emulatorFiles) {
        if (fileExists(path)) {
            LOGI("Emulator file found: %s", path);
            return true;
        }
    }
    return false;
}

bool checkQemuDriver() {
    std::string drivers = readFile("/proc/tty/drivers");
    if (drivers.find("goldfish") != std::string::npos) {
        LOGI("Found goldfish driver in /proc/tty/drivers");
        return true;
    }
    return false;
}

bool checkEmulatorProcesses() {
    std::string processes = readFile("/proc/self/cmdline");
    const char* suspiciousProcesses[] = {
            "qemu", "genyd", "androvm", "vbox"
    };

    for (const auto& proc : suspiciousProcesses) {
        if (processes.find(proc) != std::string::npos) {
            LOGI("Suspicious process detected: %s", proc);
            return true;
        }
    }
    return false;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_ipay_securitykit_NativeHelper_checkFridaServer(
        JNIEnv *env,
        jobject /* this */) {
    return isFridaServerDetected() ? JNI_TRUE : JNI_FALSE;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_ipay_securitykit_NativeHelper_checkObjectionInjected(JNIEnv* env, jobject /* this */) {
    return isObjectionInjected() ? JNI_TRUE : JNI_FALSE;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_ipay_securitykit_NativeHelper_checkMagiskMounted(JNIEnv* env, jobject /* this */) {
    return (isMagiskMountDetected() || isMagiskSocketPresent() || isMagiskPropertySet()) ? JNI_TRUE : JNI_FALSE;
}

//extern "C"
//JNIEXPORT jboolean JNICALL
//Java_com_ipay_securitykit_NativeHelper_isApktoolArtifactPresent(JNIEnv* env, jobject /* this */, jobject assetManager) {
//    AAssetManager* mgr = AAssetManager_fromJava(env, assetManager);
//    if (!mgr) return JNI_FALSE;
//
//    const char* knownArtifacts[] = {
//            "apktool.yml",
//            "AndroidManifest.xml.orig",
//            "resources.arsc",
//            "res/values/public.xml"
//    };
//
//    for (const char* file : knownArtifacts) {
//        if (checkAssetFile(mgr, file)) {
//            return JNI_TRUE;
//        }
//    }
//
//    return JNI_FALSE;
//}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_ipay_securitykit_NativeHelper_nativeIsEmulator(JNIEnv* env, jobject /* this */) {
    if (checkSuspiciousProperties()) return JNI_TRUE;
    if (checkEmulatorFiles()) return JNI_TRUE;
    if (checkQemuDriver()) return JNI_TRUE;
    if (checkEmulatorProcesses()) return JNI_TRUE;

    return JNI_FALSE;
}