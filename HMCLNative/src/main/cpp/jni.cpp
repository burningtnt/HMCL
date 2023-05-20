#include <jni.h>
#include <processthreadsapi.h>

extern "C" {
	/*
	 * Class:     org_jackhuang_hmcl_util_platform_ManagedProcess
	 * Method:    getPIDOnWindows
	 * Signature: (J)J
	 */
	JNIEXPORT jlong JNICALL Java_org_jackhuang_hmcl_util_platform_ManagedProcess_getPIDOnWindows (JNIEnv *env, jclass jclass, jlong handle) {
		return (jlong) GetProcessId((HANDLE) handle);
	}
}
