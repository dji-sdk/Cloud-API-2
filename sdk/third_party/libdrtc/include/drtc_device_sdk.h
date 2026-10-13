#ifndef DRTC_DEVICE_SDK_H
#define DRTC_DEVICE_SDK_H

#include "drtc_device_sdk_define.h"

#define DRTC_IN
#define DRTC_OUT
#define DRTC_INOUT

namespace drtc
{
    class DrtcDeviceSdk {
      public:
        class Listener {
            public:
                virtual void DrtcOnJoinRoomNotify(
                    DRTC_OUT     const DrtcJoinRoomNotify              &notify) = 0;

                virtual void DrtcOnLeaveRoomNotify(
                    DRTC_OUT     const DrtcLeaveRoomNotify             &notify) = 0;

                virtual void DrtcOnPublishStreamNotify(
                    DRTC_OUT     const DrtcPublishStreamNotify         &notify) = 0;

                virtual void DrtcOnUnpublishStreamNotify(
                    DRTC_OUT     const DrtcUnpublishStreamNotify       &notify) = 0;

                virtual void DrtcOnTokenWillExpireNotify(
                    DRTC_OUT     const DrtcTokenWillExpireNotify       &notify) = 0;

                virtual void DrtcOnTokenDidExpireNotify(
                    DRTC_OUT     const DrtcTokenDidExpireNotify        &notify) = 0;

                virtual void DrtcOnPeerStateChangeNotify(
                    DRTC_OUT     const DrtcClientStateChangeNotify     &notify) = 0;

                virtual void DrtcOnLocalStateChangeNotify(
                    DRTC_OUT     const drtc_client_state_event_t       &notify) = 0;

                virtual void DrtcOnExchangeTokenResponse(
                    DRTC_OUT     const DrtcExchangeTokenResp                 &out) = 0;

                virtual void DrtcOnJoinRoomResponse(
                    DRTC_OUT     const DrtcJoinRoomResp                 &out) = 0;

                virtual void DrtcOnLeaveRoomResponse(
                    DRTC_OUT     const DrtcLeaveRoomResp                &out) = 0;

                virtual void DrtcOnPublishResponse(
                    DRTC_OUT     const DrtcPublishResp                  &out,
                    DRTC_OUT     int                              request_id) = 0;

                virtual void DrtcOnUnpublishResponse(
                    DRTC_OUT     const DrtcUnpublishResp                &out,
                    DRTC_OUT     int                              request_id) = 0;

                virtual void DrtcOnSubscribeResponse(
                    DRTC_OUT     const DrtcSubscribeResp                &out,
                    DRTC_OUT     int                              request_id) = 0;

                virtual void DrtcOnUnsubscribeResponse(
                    DRTC_OUT     const DrtcUnsubscribeResp              &out,
                    DRTC_OUT     int                              request_id) = 0;

                virtual void DrtcOnUpdateTokenResponse(
                    DRTC_OUT     const DrtcUpdateTokenResp              &out,
                    DRTC_OUT     int                              request_id) = 0;

                virtual void DrtcOnData(
                    DRTC_OUT     const DrtcClient                   &peer_client,
                    DRTC_OUT     const DrtcStream                   &stream,
                    DRTC_OUT     DeviceSdkData                      &data) = 0;

                virtual void DrtcOnDataEvent(
                    DRTC_OUT     const DrtcClient                   &client,
                    DRTC_OUT     const DrtcStream                   &stream,
                    DRTC_OUT     DrtcDataEvent                      &event) = 0;

                virtual void DrtcOnMessageReceived(
                    DRTC_OUT     const DrtcClient                   &peer_client,
                    DRTC_OUT     DeviceSdkMessage                   &message) = 0;

                virtual void DrtcOnUserMessage(
                    DRTC_OUT     const DrtcClient                   &peer_client,
                    DRTC_OUT     const DeviceSdkMessage             &message,
                    DRTC_OUT     uint8_t                            transmission_type) = 0;

                virtual void DrtcOnDataBitrateChanged(
                    DRTC_OUT     uint32_t                           target_kbps) = 0;

                virtual void DrtcOnGlobalError(
                    DRTC_OUT     drtc_devicesdk_error_code_t            code,
                    DRTC_OUT     const char                             *msg) = 0;

                virtual void DrtcOnStats(
                    DRTC_OUT     const char                             *msg,
                    DRTC_OUT     uint32_t                               len) = 0;

                virtual void DrtcOnClientKickoutNotify(
                    DRTC_OUT     const DrtcClientKickoutNotify          &notify) = 0;

                virtual void DrtcOnNetworkQualityNotify(
                    DRTC_OUT     const DrtcNetworkQualityNotify          &notify) = 0;

                virtual void DrtcOnClientBandwidthNotify(
                    DRTC_OUT     const DrtcClient                   &peer_client,
                    DRTC_OUT     uint32_t                           bw_kbps) = 0;

                virtual ~Listener() {};
        };

    public:
        DrtcDeviceSdk() {};
        virtual ~DrtcDeviceSdk() {};
        virtual devicesdk_result_t DrtcInit(
            DRTC_IN      DrtcDeviceSdk::Listener            *listener,
            DRTC_IN      const DeviceSdkLogFunction              log_fun,
            DRTC_IN      const devicesdk_log_level_t             level,
            DRTC_IN      const DrtcClient                   &self_client) = 0;

        virtual devicesdk_result_t DrtcDeinit() = 0;

        virtual devicesdk_result_t DrtcExchangeToken(
            DRTC_IN      const DrtcExchangeTokenReq                  &in) = 0;

        virtual devicesdk_result_t DrtcJoinRoom(
            DRTC_IN      const DrtcJoinRoomReq                  &in) = 0;

        virtual devicesdk_result_t DrtcLeaveRoom(
            DRTC_IN      const DrtcLeaveRoomReq                 &in) = 0;

        virtual devicesdk_result_t DrtcPublish(
            DRTC_IN      const DrtcPublishReq                   &in,
            DRTC_OUT     int                                    &request_id) = 0;

        virtual devicesdk_result_t DrtcUnpublish(
            DRTC_IN      const DrtcUnpublishReq                 &in,
            DRTC_OUT     int                                    &request_id) = 0;

        virtual devicesdk_result_t DrtcSubscribe(
            DRTC_IN      const DrtcSubscribeReq                 &in,
            DRTC_OUT     int                                    &request_id) = 0;

        virtual devicesdk_result_t DrtcUnsubscribe(
            DRTC_IN      const DrtcUnsubscribeReq               &in,
            DRTC_OUT     int                                    &request_id) = 0;

        virtual devicesdk_result_t DrtcSendData(
            DRTC_IN       const DrtcStream                      &stream,
            DRTC_IN       const DeviceSdkData                   &data) = 0;

        /* Deprecated Now */
        virtual devicesdk_result_t DrtcSendDataEvent(
            DRTC_IN       const DrtcStream                     &stream,
            DRTC_IN       const DrtcDataEvent                  &event) = 0;

        virtual devicesdk_result_t DrtcSendMessage(
            DRTC_IN       const DrtcClient                     &peer_client,
            DRTC_IN       const DeviceSdkMessage               &message,
            DRTC_OUT      int                                  &message_id) = 0;

        virtual devicesdk_result_t DrtcSendUnicastUserMessage(
            DRTC_IN       const DrtcClient                     &peer_client,
            DRTC_IN       const DeviceSdkMessage               &message) = 0;

        virtual devicesdk_result_t DrtcBroadcastUserMessage(
            DRTC_IN       const DeviceSdkMessage               &message) = 0;

        virtual devicesdk_result_t DrtcUpdateToken(
            DRTC_IN       const char                           *token,
            DRTC_OUT      int                                  &request_id) = 0;

        virtual devicesdk_result_t DrtcRequestVideoKeyFrame(
            DRTC_IN       const DrtcClient                     &producer_client,
            DRTC_IN       const DrtcStream                     &producer_stream) = 0;

        virtual void SetLogLevel(
            DRTC_IN      const devicesdk_log_level_t                 level) = 0;

        virtual void SetFeatureToggle(
            DRTC_IN     const char* user,
            DRTC_IN     int user_len,
            DRTC_IN     const char* toggle,
            DRTC_IN     int toggle_len
        ) = 0;

        virtual void DrtcUpdateNetworkAvailable(
            DRTC_IN      bool available) = 0;

        virtual void DrtcAuthorizeMonitorReport(
            DRTC_IN      bool enable) = 0;

        virtual const char* DrtcGetVersion() = 0;

    };
}

#endif // DRTC_DEVICE_SDK_H