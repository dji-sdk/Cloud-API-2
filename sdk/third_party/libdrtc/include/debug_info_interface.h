#ifndef DRPC_DEBUG_INFO_INTERFACE_H
#define DRPC_DEBUG_INFO_INTERFACE_H

#ifndef BINARY_STRUCT_H
#define BINARY_STRUCT_H
// Aliases data types declarations
typedef struct binary_t binary_t;

// Structures/unions data types declarations
struct binary_t
{
    uint8_t * data;
    uint32_t dataLength;
};

#endif // BINARY_STRUCT_H


#ifdef __cplusplus
extern "C" {
#endif
void send_debug_info(const binary_t * txInput);

binary_t * get_debug_info(void);

#ifdef __cplusplus
}
#endif


#endif

