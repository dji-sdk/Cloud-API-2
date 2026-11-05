#ifndef DRPC_MODEM_INFO_INTERFACE_H
#define DRPC_MODEM_INFO_INTERFACE_H

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
#define MODEM_NAME_LEN 32
#define GW_NAME_LEN 16

struct modem_info_common {
    uint8_t version;
}__attribute__((packed, aligned(1))) ;

struct modem_info {
    char        name[MODEM_NAME_LEN];
    uint32_t    bw_kbps;
    int32_t     rsrp;
    int32_t     rsrq;
    int32_t     rssi;
}__attribute__((packed, aligned(1))) ;

struct modem_info_v1 {
    struct modem_info_common common;
    uint8_t modem_num;
    struct modem_info modems[];
}__attribute__((packed, aligned(1))) ;


struct route_modem_info {
    char                name[MODEM_NAME_LEN];
    uint32_t            bw_kbps;
    char                gw[GW_NAME_LEN];
    int                 priority;
    bool                is_use_money;
    uint8_t             link_type;
}__attribute__((packed, aligned(1))) ;

struct modem_info_v2 {
    struct modem_info_common common;
    uint8_t modem_num;
    struct route_modem_info modems[];
}__attribute__((packed, aligned(1))) ;

struct route_modem_info_v3 {
    char                name[MODEM_NAME_LEN];
    uint32_t            bw_tx_kbps;
    uint32_t            bw_rx_kbps;
    char                gw[GW_NAME_LEN];
    int                 priority;
    bool                is_use_money;
    uint8_t             link_type;
    bool                bw_tx_supported;   /* modem can report egress (TX) bandwidth */
    bool                bw_rx_supported;   /* modem can report ingress (RX) bandwidth */
}__attribute__((packed, aligned(1))) ;

struct modem_info_v3 {
    struct modem_info_common common;
    uint8_t modem_num;
    struct route_modem_info_v3 modems[];
}__attribute__((packed, aligned(1))) ;

/*
 * ===== modem_info v4: TLV 可扩展协议 =====
 *
 * 目标：字段频繁增减时不再改结构体。新增/删除字段 = 增删一个 type id；
 *       wire 用 TLV(Type-Length-Value) 承载，未知 type 按 len 跳过 -> 前向兼容。
 *
 * payload 布局：
 *   [ modem_info_common.version = 4 ]      1 字节，用于版本路由
 *   [ MTLV_MODEM ][len][ 字段 TLV 序列 ]    modem #0
 *   [ MTLV_MODEM ][len][ 字段 TLV 序列 ]    modem #1
 *   ...                                    顺序 walk 到 buffer 末尾，无需 modem_num
 *
 * 每个 TLV：固定 4 字节头(type:u16 + len:u16，主机序) + len 字节 value，见 struct modem_tlv。
 *
 * value 编码约定：
 *   - 整数：定宽，主机字节序(与 v1/v2/v3 原生 packed 一致；生产/消费同 SoC)。
 *           每个 type 的宽度固定(见下方注释)，消费端可用 len 校验。
 *   - bool：u8(0/1)；string：原始 UTF-8 字节，长度 = len，无结尾 NUL。
 *   - 浮点：IEEE-754 float(4 字节，主机序)。
 *   - container：value 是一串子 TLV，用同一套规则递归解析。
 *   - 读取请 memcpy 到本地变量(对齐安全)，勿直接强转指针解引用。
 *
 * 扩展约定：
 *   - 只增不改：已分配 type id 的含义/编码/宽度永不变；要变就新增 id。
 *   - 消费端必须忽略不认识的 type(按 len 跳过)，并容忍任意字段缺失。
 *   - 每个 MTLV_MODEM 容器内第一个字段 tag 恒为 MTLV_LINK_TYPE(判定 modem 类)。
 *   - 带宽 MTLV_MODEM_TX_BW/RX_BW 不出现即表示不支持获取(无单独 *_SUPPORTED)。
 *   - tag 分通用(所有 modem)与某类专属(如 4G)两组；新 modem 类专属字段各占一段 id
 *     (建议 0x0500+ 起)，避免撞 id。
 *
 * LibDrpc 仅提供本枚举与格式约定，不提供 TLV 读写代码(由应用层实现)。
 */
struct modem_tlv {
    uint16_t type;
    uint16_t len;        /* value 字节数 */
    uint8_t  val[];      /* 柔性数组，长度 = len */
}__attribute__((packed, aligned(1))) ;

enum modem_tlv_type {
    /* 0x0001 容器 */
    MTLV_MODEM             = 0x0001, /* container: 该 modem 的字段 TLV 序列 */

    /* ===== 通用 tag(所有 modem 都可携带) ===== */
    MTLV_NAME             = 0x0010, /* string: modem 名，消费端关联 key */
    MTLV_COST_LEVEL       = 0x0011, /* u8: 费用等级，越大越贵，0=免费 */
    MTLV_LINK_TYPE        = 0x0012, /* u8: 链路类型/modem 类判别器；约定恒为容器内第一个 tag */
    MTLV_PRIORITY         = 0x0013, /* i32: 优先级 */
    MTLV_MODEM_TX_BW      = 0x0014, /* u32: kbps；不存在该 tag = 不支持获取 */
    MTLV_MODEM_RX_BW      = 0x0015, /* u32: kbps；不存在该 tag = 不支持获取 */
    MTLV_GW               = 0x0016, /* string: 网关 IP */

    /* ===== 4G 类专属 tag(仅 4G modem 携带) ===== */
    MTLV_SIMSLOT_POS      = 0x0200, /* u8: sim 卡槽位置编号 */
    MTLV_RSRP             = 0x0201, /* i32: dBm */
    MTLV_RSRQ             = 0x0202, /* i32: dB  */
    MTLV_RSSI             = 0x0203, /* i32: dBm */
    MTLV_SNR              = 0x0204, /* i32: dB(整数) */
    MTLV_QUEUE_SIZE       = 0x0205, /* u32: bytes */
    MTLV_ALLOW_RATE       = 0x0206, /* u32: kbps */
    MTLV_ACTUAL_RATE      = 0x0207, /* u32: kbps */

};

struct modem_info_v4 {
    struct modem_info_common common;  /* version = 4 */
    uint8_t tlv[];                    /* 一串 MTLV_MODEM TLV，总长 = dataLength - 1 */
}__attribute__((packed, aligned(1))) ;



#ifdef __cplusplus
extern "C" {
#endif

binary_t * get_modem_info(void);

#ifdef __cplusplus
}
#endif


#endif

