#ifndef AWS_AUTH_EXPORTS_H
#define AWS_AUTH_EXPORTS_H

/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

#if defined(AWS_CRT_USE_WINDOWS_DLL_SEMANTICS) || defined(_WIN32)
#    ifdef AWS_AUTH_USE_IMPORT_EXPORT
#        ifdef AWS_AUTH_EXPORTS
#            define AWS_AUTH_API __declspec(dllexport)
#        else
#            define AWS_AUTH_API __declspec(dllimport)
#        endif /* AWS_AUTH_EXPORTS */
#    else
#        define AWS_AUTH_API
#    endif /*USE_IMPORT_EXPORT */

#else
#    if defined(AWS_AUTH_USE_IMPORT_EXPORT) && defined(AWS_AUTH_EXPORTS)
#        define AWS_AUTH_API __attribute__((visibility("default")))
#    else
#        define AWS_AUTH_API
#    endif

#endif /* defined(AWS_CRT_USE_WINDOWS_DLL_SEMANTICS) || defined(_WIN32) */

#endif /* AWS_AUTH_EXPORTS_H */
