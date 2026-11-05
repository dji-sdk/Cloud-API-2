#ifndef NM_PRODUCT_INTERFACE_H
#define NM_PRODUCT_INTERFACE_H

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

#define MAX_RULES           7
#define MAX_TARGET          30
#define MAX_INTERFACE       5
#define MAX_ROUTE           20
#define MAX_JSON            4096

#define IP_ADDR_LENGTH      17
#define PORT_LENGTH         6
#define PROTOCOL_LENGTH     7
#define TOS_LENGTH          5
#define APP_NAME_LEN        32
#define PROBE_TARGET_LEN    256
#define COUNTRY_CODE_LEN    8
#define IF_LEN              32
#define OPT_LEN             32
#define MAC_ADDR_LENGTH     18
#define DCP_DOMAIN_LEN      256

typedef uint8_t response_status_t;
enum ResponseStatus {
    RESPONSE_SUCCESS = 0,
    RESPONSE_ERROR = 1,
};

typedef uint8_t message_type_t;
enum NmMessageType {
    SET_TEST_INFO               = 1,
    SET_TRAFFIC_CONTROL         = 2,
    GET_TRAFFIC_CONTROL         = 3,
    SET_ETH_CONFIG              = 4,
    GET_ETH_CONFIG              = 5,
    SET_NET_PROBE_TARGET        = 6,
    GET_NET_PROBE_RESULT        = 7,
    GET_AGENT_METRICS           = 8,
    SET_AGENT_BW_LIMIT          = 9,
    GET_ROUTE_INFO              = 10,
    SET_DCP_DOMAIN              = 11,
    NM_RPC_MESSAGE_MAX,
};

typedef uint8_t probe_type_t;
enum NmProbeType {
    PROBE_TYPE_UNKNOW   = 0,
    PROBE_ICMP          = 1,
    PROBE_DNS           = 2,
    PROBE_NTP           = 3,
    PROBE_TCP           = 4,
};
typedef uint8_t eth_speed_type_t;
enum EthSpeedTypes {
    ETH_TYPE_NO_CONNETION = 0,
    ETH_TYPE_TOO_LOW      = 1,
    ETH_TYPE_100M         = 2,
    ETH_TYPE_1000M        = 3,
    ETH_TYPE_NOT_SPUPPORT = 4,
};

typedef uint8_t agent_type_t;
enum AgentType {
    AGENT_SDR = 1,
    AGENT_LTE,
    AGENT_USB,
    AGENT_ETH,
    AGENT_WLAN,
};

typedef uint8_t if_status_t;
enum IfStatus {
    IF_DOWN = 0,
    IF_UP = 1,
};


struct traffic_match_condition {
    char src_ip[IP_ADDR_LENGTH];
    char dst_ip[IP_ADDR_LENGTH];
    char src_port[PORT_LENGTH];
    char dst_port[PORT_LENGTH];
    char protocol[PROTOCOL_LENGTH];
    char tos[TOS_LENGTH];
    int prio;
}__attribute__((packed, aligned(1)));

struct traffic_action {
    int limit_rate_percent;
    int prio;
}__attribute__((packed, aligned(1)));

struct traffic_rule {
    char app_name[APP_NAME_LEN];
    struct traffic_match_condition match;
    struct traffic_action  action;
}__attribute__((packed, aligned(1)));

struct interface_alloc_bw {
    char                if_name[IF_LEN];
    int                 priority = 0;
    uint32_t            bw_kbps = 0;
}__attribute__((packed, aligned(1)));

struct class_statis_info {
    char                if_name[IF_LEN];
    uint64_t            tx_packets = 0;
}__attribute__((packed, aligned(1)));

struct traffic_rule_info {
    struct traffic_rule rule;
    uint8_t interface_number;
    int     classid;
    struct interface_alloc_bw  alloc_info[MAX_INTERFACE];
    struct class_statis_info   class_statis[MAX_INTERFACE];
}__attribute__((packed, aligned(1)));

struct probe_target {
    probe_type_t    probe_type;
    char            probe_target[PROBE_TARGET_LEN];
}__attribute__((packed, aligned(1)));

struct probe_target_result {
    char                if_name[IF_LEN];
    agent_type_t        agent_type;
    char                probe_target[PROBE_TARGET_LEN];
    probe_type_t        probe_type;
    int                 rtt;
    char                optional[OPT_LEN];
}__attribute__((packed, aligned(1)));

struct agent_bw_limit {
    agent_type_t        type;
    char                if_name[IF_LEN];
    uint32_t            bw_limit;
}__attribute__((packed, aligned(1)));

struct route_nexthop {
    char                if_name[IF_LEN];
    char                gw[IP_ADDR_LENGTH];
    int                 priority;
    uint8_t             is_use_money ;
    uint32_t            bw_kbps;
}__attribute__((packed, aligned(1)));

struct route_dst {
    char dst[IP_ADDR_LENGTH];
    char dst_mask[IP_ADDR_LENGTH];
    uint8_t next_hops_number;
    struct route_nexthop next_hops[MAX_INTERFACE];
}__attribute__((packed, aligned(1)));

struct agent_metrics {
    char                if_name[IF_LEN];
    if_status_t         status;
    agent_type_t        agent_type;
    uint8_t             use_static_config;
    char                ip[IP_ADDR_LENGTH];
    int                 subnet_mask_length;
    char                gw_ip[IP_ADDR_LENGTH];
    char                dns1[IP_ADDR_LENGTH];
    char                dns2[IP_ADDR_LENGTH];
    eth_speed_type_t    speed_type;
    char                mac_addr[MAC_ADDR_LENGTH];
    uint32_t            phy_max_bw_kbps;
    uint32_t            setting_max_bw_kbps;
    uint64_t            tx_bytes;
    uint64_t            tx_kbps;
    uint64_t            rx_bytes;
    uint64_t            rx_kbps;
    uint64_t            rx_packets;
    uint64_t            tx_packets;
    uint64_t            rx_errors;
    uint64_t            tx_errors;
}__attribute__((packed, aligned(1)));

//SET_TRAFFIC_CONTROL
struct  set_traffic_control_request{
    int rules_number;
    struct traffic_rule rules[MAX_RULES];
}__attribute__((packed, aligned(1))) ;

struct  set_traffic_control_response{
    response_status_t status;
}__attribute__((packed, aligned(1))) ;

//GET_TRAFFIC_CONTROL
struct  get_traffic_control_request{
    int reserve;
}__attribute__((packed, aligned(1))) ;

struct  get_traffic_control_response{
    response_status_t status;
    int rules_number;
    struct traffic_rule_info rules[MAX_RULES];
}__attribute__((packed, aligned(1))) ;

//SET_ETH_CONFIG
struct set_eth_config_request{
    uint8_t     use_static_config;
    char        ip[IP_ADDR_LENGTH];
    int         subnet_mask_length;
    char        gw_ip[IP_ADDR_LENGTH];
    char        dns1[IP_ADDR_LENGTH];
    char        dns2[IP_ADDR_LENGTH];
}__attribute__((packed, aligned(1))) ;

struct set_eth_config_response{
    response_status_t status;
}__attribute__((packed, aligned(1))) ;

//GET_ETH_CONFIG
struct  get_eth_config_request{
    int reserve;
}__attribute__((packed, aligned(1))) ;

struct get_eth_config_response{
    response_status_t      status;
    uint8_t             use_static_config;
    char                ip[IP_ADDR_LENGTH];
    int                 subnet_mask_length;
    char                gw_ip[IP_ADDR_LENGTH];
    char                dns1[IP_ADDR_LENGTH];
    char                dns2[IP_ADDR_LENGTH];
    eth_speed_type_t    speed_type;
    char                mac_addr[MAC_ADDR_LENGTH];
}__attribute__((packed, aligned(1))) ;

//SET_NET_PROBE_TARGET
struct set_net_probe_target_request{
    uint8_t  use_cn;
    uint8_t  target_number;
    struct probe_target target[MAX_TARGET];
}__attribute__((packed, aligned(1))) ;

struct set_net_probe_target_response{
    response_status_t status;
}__attribute__((packed, aligned(1))) ;

//GET_NET_PROBE_RESULT
struct get_net_probe_result_request{
    int reserve;
}__attribute__((packed, aligned(1))) ;

struct get_net_probe_result_response{
    response_status_t status;
    uint8_t     use_cn;
    uint8_t     target_number;
    struct      probe_target_result target[MAX_TARGET];
}__attribute__((packed, aligned(1))) ;

//GET_AGENT_METRICS
struct get_agent_mertrics_request{
    char                if_name[IF_LEN];
    agent_type_t        type;
    uint8_t             type_or_name; //type:0 name:1
}__attribute__((packed, aligned(1))) ;

struct get_agent_mertrics_response{
    response_status_t status;
     uint8_t     metrics_number;
    struct agent_metrics metrics[MAX_INTERFACE];
}__attribute__((packed, aligned(1))) ;

//SET_AGENT_BW_LIMIT
struct set_agent_bw_limit_request{
    uint8_t     type_or_name; //type:0 name:1
    uint8_t     setting_number;
    struct      agent_bw_limit agents[MAX_INTERFACE];
}__attribute__((packed, aligned(1))) ;

struct set_agent_bw_limit_response{
    response_status_t status;
}__attribute__((packed, aligned(1))) ;


//GET_ROUTE_INFO
struct get_route_info_request{
    int reserve;
}__attribute__((packed, aligned(1))) ;

struct get_route_info_response{
    response_status_t status;
    uint8_t     dst_number;
    struct      route_dst dsts[MAX_ROUTE];
}__attribute__((packed, aligned(1))) ;

//SET_TEST_INFO
struct set_test_info_request{
    char      json[MAX_JSON];
}__attribute__((packed, aligned(1))) ;

struct set_test_info_response{
    response_status_t status;
    char      json[MAX_JSON];
}__attribute__((packed, aligned(1))) ;

//SET_DCP_DOMAIN
struct set_dcp_domain_request{
    char      domain[DCP_DOMAIN_LEN];
    uint8_t   is_add;
}__attribute__((packed, aligned(1))) ;

struct set_dcp_domain_response{
    response_status_t status;
}__attribute__((packed, aligned(1))) ;


#ifdef __cplusplus
extern "C" {
#endif

binary_t * request_network_manager(uint32_t message_type, const binary_t * txInput);
#ifdef __cplusplus
}
#endif


#endif

