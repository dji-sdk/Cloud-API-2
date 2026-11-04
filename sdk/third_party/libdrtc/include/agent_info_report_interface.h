#ifndef DRPC_AGENT_INFO_REPORT_INTERFACE_H
#define DRPC_AGENT_INFO_REPORT_INTERFACE_H

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

//   0                   1                   2                   3
//   0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
//  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
//  | Agent Info Tag                | Length                        |
//  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
//  |                           Value...                            :
//  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
#define IP_LENGTH 16
#define MAC_LENGTH 18
#define IF_LENGTH 16
#define AGENT_STATIS_LENGTH 1024

typedef uint16_t agent_info_tag_t;
enum AgentInfoTag {
    IDENTITY_INFO = 1,
    LOCAL_LINK_INFO = 2,
    PEER_LINK_INFO = 3,
    NETWORK_INFO = 4,
    DNS_INFO = 5,
    INTERNET_GW_INFO = 6,
    AGENT_STATIS_INFO = 7,
};

typedef uint8_t link_type_t;
enum AgentLinkType {
    TYPE_SDR = 1,
    TYPE_LTE,
    TYPE_USB,
    TYPE_ETH,
    TYPE_WLAN
};
typedef uint8_t link_status_t;
enum AgentLinkStatus {
    LINK_DISCONNECTED = 0,
    LINK_CONNECTED,
};

typedef uint8_t network_status_t;
enum AgentNetWorkStatus {
    NETWORK_DOWN = 0,
    NETWORK_UP,
};

typedef uint8_t network_type_t;
enum AgentNetWorkType {
    NETWORK_NOARP = 1,
    NETWORK_BROADCAST,
};

struct identity_info_t {
    link_type_t     type;
    char            if_name[IF_LENGTH];
}__attribute__((packed, aligned(1))) ;

struct local_link_info_t {
    char            mac_id[MAC_LENGTH];
    char            preferred_peer_mac_id[MAC_LENGTH];
    uint32_t        max_bw_kbps;
}__attribute__((packed, aligned(1))) ;

struct peer_link_info_t {
    link_status_t   status;
    uint32_t        bw_kbps;
    uint32_t        ingress_bw_kbps;
    uint8_t         b_ingress_support;
    char            mac_id[MAC_LENGTH];
}__attribute__((packed, aligned(1))) ;

struct network_info_t {
    network_status_t    status;
    network_type_t      type;
    char                ip[IP_LENGTH];
    uint8_t             subnet_mask_length;
}__attribute__((packed, aligned(1))) ;

struct dns_info_t {
    uint8_t is_dns_allocd; //1: dns_allocd
    uint8_t ip_must_dns; //1: must_dns
    char dns1[IP_LENGTH];
    char dns2[IP_LENGTH];
}__attribute__((packed, aligned(1)));

struct internet_gw_info_t {
    uint8_t            has_internet_gateway; //1: internet_gateway
    char               gw_ip[IP_LENGTH];
    uint8_t            detect_network_self; //1: agent detect 0: network_manager detect
    uint8_t            is_internet_reachable; //1: internet_reachable
    uint16_t           rtt;
    uint8_t            is_use_money; //1: use_money
}__attribute__((packed, aligned(1))) ;

struct agent_statis_info_t {
    char               statis_json[AGENT_STATIS_LENGTH];
}__attribute__((packed, aligned(1))) ;


#ifdef __cplusplus
extern "C" {
#endif
void agent_info_report(const binary_t * data);

#ifdef __cplusplus
}
#endif


#endif

