// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "thread_util.h"

#include <sys/types.h>

#include <cassert>
#if __WIN32__
#include <windows.h>
#include <processthreadsapi.h>
#else
#include <pthread.h>
#include <unistd.h>
#endif

#include <cstdint>

#ifdef __ANDROID__
#include <sys/prctl.h>
#endif

#ifdef __EMSCRIPTEN__
#include <emscripten/threading.h>
#endif

#include "base/utils/utils.h"

namespace drc_sdk {
uint64_t ThreadUtil::GetThreadId() {
#ifdef __ANDROID__
  return gettid();
#elif __linux__
  return gettid();
#elif defined(__APPLE__)
  uint64_t tid = 0;
  pthread_threadid_np(nullptr, &tid);
  return tid;
#elif __WIN32__
  return GetCurrentThreadId();
#elif __EMSCRIPTEN__
  return static_cast<uint64_t>(emscripten_main_runtime_thread_id());
#endif
  return 0;
}

void ThreadUtil::SetCurrentThreadName(const std::string& name) {
  if (!name.empty()) {
#if defined(__APPLE__)
    pthread_setname_np(name.c_str());
#elif __ANDROID__
    pthread_setname_np(pthread_self(), name.c_str());
#elif __WIN32__
    // use dynamic load for compatibility of Windows Server 2016
    HMODULE kernel_module = LoadLibraryA("KernelBase.dll");
    typedef HRESULT (CALLBACK* SetThreadDescriptionFunc)(
            HANDLE hThread,
            PCWSTR lpThreadDescription);
    if (kernel_module != nullptr) {
        auto func = reinterpret_cast<SetThreadDescriptionFunc>(GetProcAddress(kernel_module, "SetThreadDescription"));
        if (func != nullptr) {
            auto hr = func(GetCurrentThread(), UTF8StringToWString(name).c_str());
            assert(SUCCEEDED(hr));
        }
        FreeLibrary(kernel_module);
    }
#elif __EMSCRIPTEN__
    emscripten_set_thread_name(emscripten_main_runtime_thread_id(), name.c_str());
#endif
  }
}

std::string ThreadUtil::GetCurrentThreadName() {
  static const size_t kNameLength = 16;
  char name[kNameLength] = {'\0'};
  if (std::string(name).empty()) {
#ifdef __ANDROID__
    prctl(PR_GET_NAME, name);
#elif defined(__APPLE__)
    pthread_getname_np(pthread_self(), name, kNameLength);
#elif __WIN32__
    PWSTR desc;
      // use dynamic load for compatibility of Windows Server 2016
      HMODULE kernel_module = LoadLibraryA("KernelBase.dll");
      typedef HRESULT (CALLBACK* GetThreadDescriptionFunc)(
              HANDLE hThread,
              PWSTR  *ppszThreadDescription);
      if (kernel_module != nullptr) {
          auto func = reinterpret_cast<GetThreadDescriptionFunc>(GetProcAddress(kernel_module, "GetThreadDescription"));
          if (func != nullptr) {
              auto hr = func(GetCurrentThread(), &desc);
              if (SUCCEEDED(hr)) {
                  auto result = WStringToUTF8String(std::wstring(desc));
                  LocalFree(desc);
                  return result;
              } else {
                  assert(false);
              }
          }
          FreeLibrary(kernel_module);
      }
#elif __EMSCRIPTEN__
    return "";
#endif
  }
  return std::string(name);
}

} // namespace drc_sdk
