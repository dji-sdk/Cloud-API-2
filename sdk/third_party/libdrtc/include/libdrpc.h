#ifndef _LIBDRPC_INTERFACE_H_
#define _LIBDRPC_INTERFACE_H_
#include <stdint.h>

// typedef uint8_t drpc_role_t;
// enum drpc_role {
//     DRPC_ROLE_CLIENT = 0,
//     DRPC_ROLE_SERVER,
// };

typedef uint8_t log_level_t;
enum DrpcLogLevel
{
    DRPC_LOG_LEVEL_OFF,
    DRPC_LOG_LEVEL_FATAL,
    DRPC_LOG_LEVEL_ERROR,
    DRPC_LOG_LEVEL_WARNING,
    DRPC_LOG_LEVEL_INFO,
    DRPC_LOG_LEVEL_DEBUG,
    DRPC_LOG_LEVEL_TRACE,
    DRPC_LOG_LEVEL_ALL,
};

typedef void(*DrpcLogFunction)(log_level_t level, const char* trace, int len);

typedef struct __libdrpc_obj_t {
    void        *private_data; // private data
} libdrpc_obj_t;

#ifdef __cplusplus
extern "C" {
#endif
extern int libdrpc_init(libdrpc_obj_t *obj, DrpcLogFunction log_fun, log_level_t level, const char* module_name, const char* config_path);
extern int libdrpc_deinit(libdrpc_obj_t *obj);

#ifdef __cplusplus
}
#endif


#endif /*end _LIBDRPC_INTERFACE_H_ */

