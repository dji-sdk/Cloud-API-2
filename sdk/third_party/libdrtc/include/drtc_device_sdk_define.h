#ifndef DRTC_DEVICE_SDK_DEFINE_H
#define DRTC_DEVICE_SDK_DEFINE_H

#include <stdint.h>
#include <vector>

#define DEVICE_TYPE_LENGTH    16
#define STREAM_ID_LENGTH      64
#define APPENDIX_LENGTH       128
#define CLIENT_ID_LENGTH      64
#define ROOM_ID_LENGTH        64
#define APP_ID_LENGTH         32
#define TOKEN_LENGTH          1024
#define COUNTRY_CODE_LENGTH   64
#define DEVICE_SN_LENGTH      64
#define COORDINATE_LENGTH     64
#define MAX_MESSAGE_LENGTH    1024
#define MAX_ROOM_CLIENT_NUM   100
#define MAX_CLIENT_STREAM_NUM 10

typedef int32_t devicesdk_result_t;
typedef uint8_t devicesdk_log_level_t;
enum DeviceSdkLoggerLevel
{
    DEVICESDK_LOG_LEVEL_OFF,
    DEVICESDK_LOG_LEVEL_FATAL,
    DEVICESDK_LOG_LEVEL_ERROR,
    DEVICESDK_LOG_LEVEL_WARNING,
    DEVICESDK_LOG_LEVEL_INFO,
    DEVICESDK_LOG_LEVEL_DEBUG,
    DEVICESDK_LOG_LEVEL_TRACE,
    DEVICESDK_LOG_LEVEL_ALL,
};
typedef void (*DeviceSdkLogFunction)(devicesdk_log_level_t level, const char* trace, int len);

typedef int32_t drtc_devicesdk_error_code_t;
enum DeviceSdkErrorCode
{
    DEVICESDK_SUCCESS       = 0,
    DEVICESDK_FAILURE       = 10001,
    DEVICESDK_TIMEOUT       = 10002,
    DEVICESDK_STATUS_ERROR  = 10003,
    DEVICESDK_INVALID_PARAM = 10004,
    DEVICESDK_CRYPTO_ERROR  = 10005,
    DEVICESDK_INVALID_CERT  = 10006,

    // global error code
    DEVICESDK_ARBITRATION_ERROR   = 10101,
    DEVICESDK_JOINROOM_TIMEOUT    = 10102,
    DEVICESDK_RECOVER_LOCAL_ERROR = 10103,
    DEVICESDK_ROOM_CLOSE          = 10104
};

typedef int32_t drtc_response_code_t;
enum DrtcResponseCode
{
    RESPONSE_OK                           = 0,
    RESPONSE_INVALID_REQUEST              = 21001,
    RESPONSE_INVALID_PARAMETER            = 21002,
    RESPONSE_SERVER_INTERNAL_ERROR        = 21003,
    RESPONSE_PRODUCE_NOT_FOUND            = 21004,
    RESPONSE_PRODUCER_ALREADY_EXIST       = 21005,
    RESPONSE_CONSUME_NOT_FOUND            = 21006,
    RESPONSE_CONSUMER_ALREADY_EXIST       = 21007,
    RESPONSE_CLIENT_NOT_FOUND             = 21008,
    RESPONSE_CLIENT_ALREADY_EXIST         = 21009,
    RESPONSE_INVALID_TOKEN                = 21010,
    RESPONSE_PERMISSION_DENIED            = 21011,
    RESPONSE_TOO_MANY_REQUEST             = 21012,
    RESPONSE_E2EE_NEGOTIATION_FAILED      = 21013,
    RESPONSE_SIGNALING_NEGOTIATION_FAILED = 21014,
    RESPONSE_CONN_NEGOTIATION_FAILED      = 21015,
    RESPONSE_RTP_NEGOTIATION_FAILED       = 21016,
    RESPONSE_CAST_NEGOTIATION_FAILED      = 21017,
    RESPONSE_SERVER_OVERLOAD              = 21018,
    RESPONSE_ROOM_CLIENT_LIMIT            = 21019,
    RESPONSE_ROOM_CLOSED                  = 21020
};

typedef int32_t drtc_arbitration_code_t;
enum DrtcArbitrationCode
{
    ARBITRATION_INVALID_PARAM                       = 50101,
    ARBITRATION_APP_ID_NOT_EXIST                    = 50103,
    ARBITRATION_APP_STATUS_NOT_PUBLISHED            = 50105,
    ARBITRATION_ROOM_ID_NOT_FOUND                   = 50106,
    ARBITRATION_ROOM_PENDING_DESTORY                = 50117,
    ARBITRATION_TOKEN_INVALID                       = 50201,
    ARBITRATION_TOKEN_VERIFY_ERROR                  = 50202,
    ARBITRATION_TOKEN_PARSE_ERROR                   = 50203,
    ARBITRATION_ROOM_ID_MISMATCH                    = 50204,
    ARBITRATION_APP_ID_MISMATCH                     = 50205,
    ARBITRATION_CLIENT_ID_MISMATCH                  = 50206,
    ARBITRATION_WORKER_NOT_AVALIABLE                = 50301,
    ARBITRATION_COMPETITION_FAILED                  = 50302,
    ARBITRATION_INTERNAL_ERROR                      = 50401,
    ARBITRATION_CACHE_ERROR                         = 50501,
    ARBITRATION_DATABASE_ERROR                      = 50502,
    ARBITRATION_REGION_SESSION_MGR_SERVICE_ERROR    = 50506,
};

typedef int32_t drtc_reason_code_t;
enum DrtcReasonCode
{
    REASON_CLIENT_UNEXPECTED_OPERATION = 401,
    REASON_CLIENT_SESSION_TIMEOUT      = 402,
    REASON_CLIENT_LEFT                 = 403,
    REASON_CLIENT_DUPLICATED           = 404,
    REASON_CLIENT_KICKOUT_BY_MANAGER   = 405,
    REASON_CLIENT_ROOM_CLOSED          = 406,
    REASON_CLIENT_TOKEN_EXPIRED        = 407,
    REASON_ROOM_CLOSED_BY_POLICY       = 408,
    REASON_ROOM_CLOSED_BY_APPLICATION  = 409,

    REASON_SERVER_INTERNAL_ERROR = 501,
    REASON_SERVER_TERMINATED     = 502
};

typedef uint8_t drtc_device_type_t;
enum DrtcDeviceType
{
    DRTC_DEVICE_UNKNOWN = 0,
    DRTC_DEVICE_UAV     = 1,
    DRTC_DEVICE_WEB     = 2,
    DRTC_DEVICE_DOCK    = 3,
    DRTC_DEVICE_RC      = 4,
    DRTC_DEVICE_APP     = 5,
    DRTC_DEVICE_NODE    = 6,
};

typedef uint8_t drtc_client_state_event_t;
enum DrtcStateEvent
{
    TIMEOUT     = 0,
    RECONNECTED = 1,
};

typedef uint8_t drtc_event_type_t;
enum DrtcDataEventType
{
    DrtcRequestKeyFrame = 0,
    DrtcCancelKeyFrame  = 1,
};

typedef uint8_t drtc_stream_type_t;
enum DrtcStreamType
{
    DRTC_STREAM_UNKNOWN = 0,
    DRTC_STREAM_VIDEO   = 1,
    DRTC_STREAM_COMMAND = 2,
    DRTC_STREAM_CONTROL = 3,
    DRTC_STREAM_AUDIO   = 10,
};

typedef uint8_t drtc_media_type_t;
enum DrtcVideoType
{
    DRTC_VIDEO_UNKNOWN = 0,
    DRTC_VIDEO_AVC     = 1,
    DRTC_VIDEO_HEVC    = 2,
    DRTC_VIDEO_AV1     = 3,
};

enum DrtcAudioType
{
    DRTC_AUDIO_OPUS    = 1,
};

typedef uint8_t drtc_message_type_t;
enum DrtcMessageType
{
    DrtcMessageUnreliableRTCM           = 1,
    DrtcMessageUnreliableControl        = 2,
    DrtcMessageUnicaseUnreliableMessage = 3,
    DrtcMessageUnreliableData           = 4,
};

typedef uint8_t drtc_netowork_quality_t;
enum DrtcNetworkQuality
{
    NetworkQualityUnknown   = 0,
    NetworkQualityExcellent = 1,
    NetworkQualityGood      = 2,
    NetworkQualityPoor      = 3,
    NetworkQualityBad       = 4,
    NetworkQualityVeryBad   = 5,
    NetworkQualityDOWN      = 6,
};

struct DrtcStream
{
    drtc_stream_type_t stream_type {DRTC_STREAM_UNKNOWN};
    drtc_media_type_t media_type {DRTC_VIDEO_UNKNOWN};
    char stream_id[STREAM_ID_LENGTH] {};
};

struct PublishedStream
{
    DrtcStream stream;
    char appendix[APPENDIX_LENGTH] {}; // every thing you want
};

struct DrtcClient
{
    char client_id[CLIENT_ID_LENGTH] {};
    drtc_device_type_t device_type {DRTC_DEVICE_UNKNOWN};
};

struct DrtcDataEvent
{
    drtc_event_type_t event_type;
    const char* event_msg;
};
struct DrtcClientPublishedStream
{
    DrtcClient client;
    uint8_t stream_num = 0;
    PublishedStream stream_list[MAX_CLIENT_STREAM_NUM] {};
};

struct DrtcExchangeTokenReq
{
    char app_id[APP_ID_LENGTH] {};
    char client_id[CLIENT_ID_LENGTH] {};
    char token[TOKEN_LENGTH] {};
    char reference_sn[DEVICE_SN_LENGTH] {};
    char device_type[DEVICE_TYPE_LENGTH] {};
};

struct DrtcExchangeTokenResp
{
    drtc_response_code_t code;
    char message[MAX_MESSAGE_LENGTH] {};
    char token[TOKEN_LENGTH] {};
    char room_id[ROOM_ID_LENGTH] {};
};

struct DrtcJoinRoomReq
{
    char room_id[ROOM_ID_LENGTH] {};
    char app_id[APP_ID_LENGTH] {};
    char token[TOKEN_LENGTH] {};
    char country[COUNTRY_CODE_LENGTH] {};
    char latitude[COORDINATE_LENGTH] {};
    char longitude[COORDINATE_LENGTH] {};
};

struct DrtcJoinRoomResp
{
    drtc_response_code_t code;
    uint8_t client_num = 0;
    DrtcClientPublishedStream published_stream[MAX_ROOM_CLIENT_NUM] {};
};

struct DrtcJoinRoomNotify
{
    DrtcClient client;
};

struct DrtcLeaveRoomReq
{
    char room_id[ROOM_ID_LENGTH] {};
};

struct DrtcLeaveRoomResp
{
    drtc_response_code_t code;
};

struct DrtcLeaveRoomNotify
{
    DrtcClient client;
    drtc_reason_code_t reason;
};

struct DrtcPublishReq
{
    int32_t timeout_ms;
    char room_id[ROOM_ID_LENGTH] {};
    DrtcStream stream;
    char appendix[APPENDIX_LENGTH] {}; // every thing you want
};

struct DrtcPublishResp
{
    drtc_response_code_t code;
    DrtcStream stream;
};

struct DrtcPublishStreamNotify
{
    DrtcClient client;
    DrtcStream stream;
    char appendix[APPENDIX_LENGTH] {}; // every thing you want
};

struct DrtcUnpublishReq
{
    int32_t timeout_ms;
    char room_id[ROOM_ID_LENGTH] {};
    DrtcStream stream;
};

struct DrtcUnpublishResp
{
    drtc_response_code_t code;
    DrtcStream stream;
};

struct DrtcUnpublishStreamNotify
{
    DrtcClient client;
    DrtcStream stream;
};

struct DrtcUpdateTokenResp
{
    drtc_response_code_t code;
};

struct DrtcTokenWillExpireNotify
{
    int expires_in_seconds;
};

struct DrtcTokenDidExpireNotify
{
    // nothing now
};

struct DrtcSubscribeReq
{
    int32_t timeout_ms;
    char room_id[ROOM_ID_LENGTH] {};
    DrtcClient producer_client;
    DrtcStream producer_stream;
};

struct DrtcSubscribeResp
{
    drtc_response_code_t code;
    DrtcClient producer_client;
    DrtcStream producer_stream;
};

struct DrtcUnsubscribeReq
{
    int32_t timeout_ms;
    char room_id[ROOM_ID_LENGTH] {};
    DrtcClient producer_client;
    DrtcStream producer_stream;
};

struct DrtcUnsubscribeResp
{
    drtc_response_code_t code;
    DrtcClient producer_client;
    DrtcStream producer_stream;
};

struct DrtcClientStateChangeNotify
{
    DrtcClient client;
    drtc_client_state_event_t event;
};

struct DrtcClientKickoutNotify
{
    DrtcClient client;
    drtc_reason_code_t reason;
};

struct DrtcPathNQI {
    uint8_t                 type;
    uint8_t                 idx;
    drtc_netowork_quality_t nqi;
};

struct DrtcNetworkQualityNotify
{
    drtc_netowork_quality_t  uplink_quality;
    drtc_netowork_quality_t  downlink_quality;
    std::vector<DrtcPathNQI> per_path;
};

struct DeviceSdkData
{
    uint8_t* vir_addr;
    uint32_t length;
    uint32_t length_finished;
    int32_t timeout_ms;
    uint32_t timestamp;
};

struct DeviceSdkMessage
{
    uint8_t* vir_addr;
    uint32_t length;
    DrtcMessageType type;
    bool is_binary;
};

#endif // DRTC_DEVICE_SDK_H
