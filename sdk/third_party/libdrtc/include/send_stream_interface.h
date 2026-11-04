#ifndef DRPC_SEND_STREAM_INTERFACE_H
#define DRPC_SEND_STREAM_INTERFACE_H

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
void netlink_send_streams(int32_t stream_id, const binary_t * txInput);
#ifdef __cplusplus
}
#endif


#endif

