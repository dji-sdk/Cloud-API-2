// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "cloud_message_manager.h"
#include <base/common/logger.h>
#include "transport_manager.h"
#include "message_v1.h"
#include "message_v2.h"
#include "message_v3.h"

using namespace message_engine;

CloudMessageManager::CloudMessageManager(const std::shared_ptr<TransportManager>& transport_manager) : transport_manager_(transport_manager) {
    LOG_INFO("message_engine startup [CloudMessageManager]");
    if (transport_manager_ == nullptr) {
        LOG_ERROR("message_engine startup [CloudMessageManager] transport_manager is nullptr");
    }
}

CloudMessageManager::~CloudMessageManager() {
    LOG_INFO("message_engine shutdown [CloudMessageManager]");
}

void CloudMessageManager::CancelObserveMessage(ListenIdCode listen_id) {
    transport_manager_->CancelObserveMessage(listen_id);
}

void CloudMessageManager::BuildReplyMessage(const std::shared_ptr<MessageWrapper>& msg, const std::shared_ptr<MessageWrapper>& original_msg) {
    switch (msg->GetMessageProtocolType()) {
        case MessageProtocolType::V1: {
            auto msg1 = std::dynamic_pointer_cast<MessageV1>(msg);
            auto msg2 = std::dynamic_pointer_cast<MessageV1>(original_msg);
            msg1->SetTidBid(msg2);
            msg1->SetPeerId(msg2->GetPeerId());
            break;
        }
        case MessageProtocolType::V2: {
            auto msg1 = std::dynamic_pointer_cast<MessageV2>(msg);
            auto msg2 = std::dynamic_pointer_cast<MessageV2>(original_msg);
            msg1->SetSeq(msg2->GetSeq());
            msg1->SetPeerId(msg2->GetPeerId());
            break;
        }
        case MessageProtocolType::V3: {
            auto msg1 = std::dynamic_pointer_cast<MessageV3>(msg);
            auto msg2 = std::dynamic_pointer_cast<MessageV3>(original_msg);
            msg1->SetSeq(msg2);
            msg1->SetPeerId(msg2->GetPeerId());
            break;
        }
        default:
            LOG_ERROR("[CloudMessageManager] UuidCopy, Unsupported message protocol type");
    }
}

///------ 用户自定义接口实现 start ------
MeErrorCode CloudMessageManager::SendCompressedSticksControlSouthbound(const std::shared_ptr<CompressedSticksControlSouthbound>& msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    // Force allocate non-zero V3 seq before enqueue (device DecodeMessageV3 rejects seq==0).
    (void)msg->GetUuid();
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendCompressedSticksControlSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    return transport_ptr->Send(msg);
}

// MeErrorCode CloudMessageManager::SendOsdNorthbound(const std::shared_ptr<OsdNorthbound>& msg, const std::shared_ptr<Executor>& user_define_executor) {
//     if (msg == nullptr) {
//         return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
//     }
//     auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
//     if (transport_ptr == nullptr) {
//         LOG_ERROR("[CloudMessageManager] ObserveCompressedSticksControlSouthbound, transport_ptr is nullptr");
//         return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
//     }
//     return transport_ptr->Send(msg);
// }

// MeErrorCode CloudMessageManager::SendStateNorthbound(const std::shared_ptr<StateNorthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<StateSouthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<Executor>& user_define_executor) {
//     if (msg == nullptr) {
//         return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
//     }
//     auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
//     if (transport_ptr == nullptr) {
//         LOG_ERROR("[CloudMessageManager] ObserveCompressedSticksControlSouthbound, transport_ptr is nullptr");
//         return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
//     }
//     if (southbound_msg_callback == nullptr) {
//         return transport_ptr->Send(msg);
//     }
//     auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
//         if (error_code != MeErrorCode::ME_NO_ERROR) {
//             southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
//             return;
//         }
//         if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
//             LOG_ERROR("[CloudMessageManager] SendStateNorthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
//             return;
//         }
//         auto v1_message = std::dynamic_pointer_cast<MessageV1>(message);
//         if (v1_message == nullptr) {
//             LOG_ERROR("[CloudMessageManager] SendStateNorthbound response, convert MessageWrapper to V1/V2/V3 message failed");
//             return;
//         }
//         auto user_message = std::make_shared<StateSouthbound>(v1_message);
//         southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
//     }, user_define_executor);
//     return error_code;
// }

// ListenIdCode CloudMessageManager::ObserveStateSouthbound(const std::function<void(const std::shared_ptr<StateSouthbound>& msg)>& observe_callback, const std::shared_ptr<Executor>& user_define_executor) {
//     if (observe_callback == nullptr) {
//         return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
//     }
//     auto observe_message = std::make_shared<RequestsReplyFlighttaskResourceGetSouthbound>();
//     auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
//         if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
//             LOG_ERROR("[CloudMessageManager] ObserveStateSouthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
//             return;
//         }
//         auto v1_message = std::dynamic_pointer_cast<MessageV1>(message);
//         if (v1_message == nullptr) {
//             LOG_ERROR("[CloudMessageManager] ObserveStateSouthbound, convert MessageWrapper to V1/V2/V3 message failed");
//             return;
//         }
//         auto user_message = std::make_shared<StateSouthbound>(v1_message);
//         observe_callback(user_message);
//     }, user_define_executor);
//     return error_code_or_listen_id;
// }

// ListenIdCode CloudMessageManager::ObserveSetPropertySouthbound(const std::function<void(const std::shared_ptr<SetPropertySouthbound>& msg)>& observe_callback, const std::shared_ptr<Executor>& user_define_executor) {
//     if (observe_callback == nullptr) {
//         return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
//     }
//     auto observe_message = std::make_shared<SetPropertySouthbound>();
//     auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
//         if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
//             LOG_ERROR("[CloudMessageManager] ObserveSetPropertySouthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
//             return;
//         }
//         auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
//         if (message_v1 == nullptr) {
//             LOG_ERROR("[CloudMessageManager] ObserveSetPropertySouthbound, convert MessageWrapper to V1/V2/V3 message failed");
//             return;
//         }
//         auto user_message = std::make_shared<SetPropertySouthbound>(message_v1);
//         observe_callback(user_message);
//     }, user_define_executor);
//     return error_code_or_listen_id;
// }

// MeErrorCode CloudMessageManager::SendSetReplyPropertyNorthbound(const std::shared_ptr<SetReplyPropertyNorthbound>& msg, const std::shared_ptr<SetPropertySouthbound>& original_msg, const std::shared_ptr<Executor>& user_define_executor) {
//     if (msg == nullptr) {
//         return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
//     }
//     if (original_msg == nullptr) {
//         return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_ORIGINAL_MESSAGE;
//     }
//     BuildReplyMessage(msg, original_msg); // 根据原消息的uuid，复制给回复消息
//     auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
//     if (transport_ptr == nullptr) {
//         LOG_ERROR("[CloudMessageManager] SendSetReplyPropertyNorthbound, transport_ptr is nullptr");
//         return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
//     }
//     return transport_ptr->Send(msg);
// }

// MeErrorCode CloudMessageManager::SendEventsAirsenseWarningNorthbound(const std::shared_ptr<EventsAirsenseWarningNorthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<EventsReplyAirsenseWarningSouthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<Executor>& user_define_executor) {
//     if (msg == nullptr) {
//         return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
//     }
//     auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
//     if (transport_ptr == nullptr) {
//         LOG_ERROR("[CloudMessageManager] SendEventsAirsenseWarningNorthbound, transport_ptr is nullptr");
//         return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
//     }
//     if (southbound_msg_callback == nullptr) {
//         return transport_ptr->Send(msg);
//     }
//     auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
//         if (error_code != MeErrorCode::ME_NO_ERROR) {
//             southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
//             return;
//         }
//         if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
//             LOG_ERROR("[CloudMessageManager] SendEventsAirsenseWarningNorthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
//             return;
//         }
//         auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
//         if (message_v1 == nullptr) {
//             LOG_ERROR("[CloudMessageManager] SendEventsAirsenseWarningNorthbound response, convert MessageWrapper to V1/V2/V3 message failed");
//             return;
//         }
//         auto user_message = std::make_shared<EventsReplyAirsenseWarningSouthbound>(message_v1);
//         southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
//     }, user_define_executor);
//     return error_code;
// }

// ListenIdCode CloudMessageManager::ObserveEventsReplyAirsenseWarningSouthbound(const std::function<void(const std::shared_ptr<EventsReplyAirsenseWarningSouthbound>& msg)>& observe_callback, const std::shared_ptr<Executor>& user_define_executor) {
//     if (observe_callback == nullptr) {
//         return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
//     }
//     auto observe_message = std::make_shared<EventsReplyAirsenseWarningSouthbound>();
//     auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
//         if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
//             LOG_ERROR("[CloudMessageManager] ObserveEventsReplyAirsenseWarningSouthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
//             return;
//         }
//         auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
//         if (message_v1 == nullptr) {
//             LOG_ERROR("[CloudMessageManager] ObserveEventsReplyAirsenseWarningSouthbound, convert MessageWrapper to V1/V2/V3 message failed");
//             return;
//         }
//         auto user_message = std::make_shared<EventsReplyAirsenseWarningSouthbound>(message_v1);
//         observe_callback(user_message);
//     }, user_define_executor);
//     return error_code_or_listen_id;
// }
///------ 用户自定义接口实现 end ------

MeErrorCode CloudMessageManager::SendServicesAiBoxAppFileExportSouthbound(const std::shared_ptr<ServicesAiBoxAppFileExportSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyAiBoxAppFileExportNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendServicesAiBoxAppFileExportSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendServicesAiBoxAppFileExportSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendServicesAiBoxAppFileExportSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendServicesAiBoxAppFileExportSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyAiBoxAppFileExportNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveServicesReplyAiBoxAppFileExportNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyAiBoxAppFileExportNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<ServicesReplyAiBoxAppFileExportNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyAiBoxAppFileExportNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyAiBoxAppFileExportNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyAiBoxAppFileExportNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendEventsAiBoxAppFileExportProgressSouthbound(const std::shared_ptr<EventsAiBoxAppFileExportProgressSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<EventsReplyAiBoxAppFileExportProgressNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendEventsAiBoxAppFileExportProgressSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendEventsAiBoxAppFileExportProgressSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendEventsAiBoxAppFileExportProgressSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendEventsAiBoxAppFileExportProgressSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<EventsReplyAiBoxAppFileExportProgressNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveEventsReplyAiBoxAppFileExportProgressNorthbound(const std::function<void(const std::shared_ptr<EventsReplyAiBoxAppFileExportProgressNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<EventsReplyAiBoxAppFileExportProgressNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveEventsReplyAiBoxAppFileExportProgressNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveEventsReplyAiBoxAppFileExportProgressNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<EventsReplyAiBoxAppFileExportProgressNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendServicesAiBoxAppListSouthbound(const std::shared_ptr<ServicesAiBoxAppListSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyAiBoxAppListNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendServicesAiBoxAppListSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendServicesAiBoxAppListSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendServicesAiBoxAppListSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendServicesAiBoxAppListSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyAiBoxAppListNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveServicesReplyAiBoxAppListNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyAiBoxAppListNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<ServicesReplyAiBoxAppListNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyAiBoxAppListNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyAiBoxAppListNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyAiBoxAppListNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendEventsAiBoxAppListInfosSouthbound(const std::shared_ptr<EventsAiBoxAppListInfosSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<EventsReplyAiBoxAppListInfosNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendEventsAiBoxAppListInfosSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendEventsAiBoxAppListInfosSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendEventsAiBoxAppListInfosSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendEventsAiBoxAppListInfosSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<EventsReplyAiBoxAppListInfosNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveEventsReplyAiBoxAppListInfosNorthbound(const std::function<void(const std::shared_ptr<EventsReplyAiBoxAppListInfosNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<EventsReplyAiBoxAppListInfosNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveEventsReplyAiBoxAppListInfosNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveEventsReplyAiBoxAppListInfosNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<EventsReplyAiBoxAppListInfosNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendServicesAiBoxAppOperateSouthbound(const std::shared_ptr<ServicesAiBoxAppOperateSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyAiBoxAppOperateNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendServicesAiBoxAppOperateSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendServicesAiBoxAppOperateSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendServicesAiBoxAppOperateSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendServicesAiBoxAppOperateSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyAiBoxAppOperateNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveServicesReplyAiBoxAppOperateNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyAiBoxAppOperateNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<ServicesReplyAiBoxAppOperateNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyAiBoxAppOperateNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyAiBoxAppOperateNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyAiBoxAppOperateNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

ListenIdCode CloudMessageManager::ObserveEventsAiObjectDetectionAlertNorthbound(const std::function<void(const std::shared_ptr<EventsAiObjectDetectionAlertNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<EventsAiObjectDetectionAlertNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveEventsAiObjectDetectionAlertNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveEventsAiObjectDetectionAlertNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<EventsAiObjectDetectionAlertNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendEventsReplyAiObjectDetectionAlertSouthbound(const std::shared_ptr<EventsReplyAiObjectDetectionAlertSouthbound>& msg, const std::shared_ptr<EventsAiObjectDetectionAlertNorthbound>& original_msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    if (original_msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_ORIGINAL_MESSAGE;
    }
    BuildReplyMessage(msg, original_msg); // 根据原消息的uuid, 复制给回复消息
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendEventsReplyAiObjectDetectionAlertSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    return transport_ptr->Send(msg);
}

MeErrorCode CloudMessageManager::SendServicesAircraftPowerCtrlSouthbound(const std::shared_ptr<ServicesAircraftPowerCtrlSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyAircraftPowerCtrlNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendServicesAircraftPowerCtrlSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendServicesAircraftPowerCtrlSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendServicesAircraftPowerCtrlSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendServicesAircraftPowerCtrlSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyAircraftPowerCtrlNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveServicesReplyAircraftPowerCtrlNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyAircraftPowerCtrlNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<ServicesReplyAircraftPowerCtrlNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyAircraftPowerCtrlNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyAircraftPowerCtrlNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyAircraftPowerCtrlNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendServicesCalculateCandidateRelayAircraftSouthbound(const std::shared_ptr<ServicesCalculateCandidateRelayAircraftSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyCalculateCandidateRelayAircraftNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendServicesCalculateCandidateRelayAircraftSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendServicesCalculateCandidateRelayAircraftSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendServicesCalculateCandidateRelayAircraftSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendServicesCalculateCandidateRelayAircraftSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyCalculateCandidateRelayAircraftNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveServicesReplyCalculateCandidateRelayAircraftNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyCalculateCandidateRelayAircraftNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<ServicesReplyCalculateCandidateRelayAircraftNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyCalculateCandidateRelayAircraftNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyCalculateCandidateRelayAircraftNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyCalculateCandidateRelayAircraftNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendServicesCameraFocalLengthSetSouthbound(const std::shared_ptr<ServicesCameraFocalLengthSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyCameraFocalLengthSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendServicesCameraFocalLengthSetSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendServicesCameraFocalLengthSetSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendServicesCameraFocalLengthSetSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendServicesCameraFocalLengthSetSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyCameraFocalLengthSetNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveServicesReplyCameraFocalLengthSetNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyCameraFocalLengthSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<ServicesReplyCameraFocalLengthSetNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyCameraFocalLengthSetNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyCameraFocalLengthSetNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyCameraFocalLengthSetNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendServicesCameraLookAtSouthbound(const std::shared_ptr<ServicesCameraLookAtSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyCameraLookAtNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendServicesCameraLookAtSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendServicesCameraLookAtSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendServicesCameraLookAtSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendServicesCameraLookAtSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyCameraLookAtNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveServicesReplyCameraLookAtNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyCameraLookAtNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<ServicesReplyCameraLookAtNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyCameraLookAtNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyCameraLookAtNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyCameraLookAtNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

ListenIdCode CloudMessageManager::ObserveEventsCandidateAircraftsCalculateResultsNorthbound(const std::function<void(const std::shared_ptr<EventsCandidateAircraftsCalculateResultsNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<EventsCandidateAircraftsCalculateResultsNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveEventsCandidateAircraftsCalculateResultsNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveEventsCandidateAircraftsCalculateResultsNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<EventsCandidateAircraftsCalculateResultsNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendEventsReplyCandidateAircraftsCalculateResultsSouthbound(const std::shared_ptr<EventsReplyCandidateAircraftsCalculateResultsSouthbound>& msg, const std::shared_ptr<EventsCandidateAircraftsCalculateResultsNorthbound>& original_msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    if (original_msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_ORIGINAL_MESSAGE;
    }
    BuildReplyMessage(msg, original_msg); // 根据原消息的uuid, 复制给回复消息
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendEventsReplyCandidateAircraftsCalculateResultsSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    return transport_ptr->Send(msg);
}

MeErrorCode CloudMessageManager::SendServicesCccDebugCommandSouthbound(const std::shared_ptr<ServicesCccDebugCommandSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyCccDebugCommandNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendServicesCccDebugCommandSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendServicesCccDebugCommandSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendServicesCccDebugCommandSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendServicesCccDebugCommandSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyCccDebugCommandNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveServicesReplyCccDebugCommandNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyCccDebugCommandNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<ServicesReplyCccDebugCommandNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyCccDebugCommandNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyCccDebugCommandNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyCccDebugCommandNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

ListenIdCode CloudMessageManager::ObserveEventsCccDebugEventNorthbound(const std::function<void(const std::shared_ptr<EventsCccDebugEventNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<EventsCccDebugEventNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveEventsCccDebugEventNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveEventsCccDebugEventNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<EventsCccDebugEventNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendEventsReplyCccDebugEventSouthbound(const std::shared_ptr<EventsReplyCccDebugEventSouthbound>& msg, const std::shared_ptr<EventsCccDebugEventNorthbound>& original_msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    if (original_msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_ORIGINAL_MESSAGE;
    }
    BuildReplyMessage(msg, original_msg); // 根据原消息的uuid, 复制给回复消息
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendEventsReplyCccDebugEventSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    return transport_ptr->Send(msg);
}

MeErrorCode CloudMessageManager::SendServicesClearSdrTopoInfoSouthbound(const std::shared_ptr<ServicesClearSdrTopoInfoSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyClearSdrTopoInfoNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendServicesClearSdrTopoInfoSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendServicesClearSdrTopoInfoSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendServicesClearSdrTopoInfoSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendServicesClearSdrTopoInfoSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyClearSdrTopoInfoNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveServicesReplyClearSdrTopoInfoNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyClearSdrTopoInfoNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<ServicesReplyClearSdrTopoInfoNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyClearSdrTopoInfoNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyClearSdrTopoInfoNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyClearSdrTopoInfoNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendServicesClearedToLandSouthbound(const std::shared_ptr<ServicesClearedToLandSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyClearedToLandNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendServicesClearedToLandSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendServicesClearedToLandSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendServicesClearedToLandSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendServicesClearedToLandSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyClearedToLandNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveServicesReplyClearedToLandNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyClearedToLandNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<ServicesReplyClearedToLandNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyClearedToLandNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyClearedToLandNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyClearedToLandNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendServicesCloudEnvironmentSwitchSouthbound(const std::shared_ptr<ServicesCloudEnvironmentSwitchSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyCloudEnvironmentSwitchNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendServicesCloudEnvironmentSwitchSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendServicesCloudEnvironmentSwitchSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendServicesCloudEnvironmentSwitchSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendServicesCloudEnvironmentSwitchSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyCloudEnvironmentSwitchNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveServicesReplyCloudEnvironmentSwitchNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyCloudEnvironmentSwitchNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<ServicesReplyCloudEnvironmentSwitchNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyCloudEnvironmentSwitchNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyCloudEnvironmentSwitchNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyCloudEnvironmentSwitchNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendServicesCommandSendSouthbound(const std::shared_ptr<ServicesCommandSendSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyCommandSendNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendServicesCommandSendSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendServicesCommandSendSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendServicesCommandSendSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendServicesCommandSendSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyCommandSendNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveServicesReplyCommandSendNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyCommandSendNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<ServicesReplyCommandSendNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyCommandSendNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyCommandSendNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyCommandSendNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

ListenIdCode CloudMessageManager::ObserveDrcDelayInfoPushNorthbound(const std::function<void(const std::shared_ptr<DrcDelayInfoPushNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcDelayInfoPushNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message) -> void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcDelayInfoPushNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcDelayInfoPushNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcDelayInfoPushNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

ListenIdCode CloudMessageManager::ObserveEventsDeviceExitHomingNotifyNorthbound(const std::function<void(const std::shared_ptr<EventsDeviceExitHomingNotifyNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<EventsDeviceExitHomingNotifyNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveEventsDeviceExitHomingNotifyNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveEventsDeviceExitHomingNotifyNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<EventsDeviceExitHomingNotifyNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendEventsReplyDeviceExitHomingNotifySouthbound(const std::shared_ptr<EventsReplyDeviceExitHomingNotifySouthbound>& msg, const std::shared_ptr<EventsDeviceExitHomingNotifyNorthbound>& original_msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    if (original_msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_ORIGINAL_MESSAGE;
    }
    BuildReplyMessage(msg, original_msg); // 根据原消息的uuid, 复制给回复消息
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendEventsReplyDeviceExitHomingNotifySouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    return transport_ptr->Send(msg);
}

MeErrorCode CloudMessageManager::SendServicesDeviceFormatSouthbound(const std::shared_ptr<ServicesDeviceFormatSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyDeviceFormatNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendServicesDeviceFormatSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendServicesDeviceFormatSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendServicesDeviceFormatSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendServicesDeviceFormatSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyDeviceFormatNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveServicesReplyDeviceFormatNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyDeviceFormatNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<ServicesReplyDeviceFormatNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyDeviceFormatNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyDeviceFormatNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyDeviceFormatNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendEventsDeviceFormatSouthbound(const std::shared_ptr<EventsDeviceFormatSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<EventsReplyDeviceFormatNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendEventsDeviceFormatSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendEventsDeviceFormatSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendEventsDeviceFormatSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendEventsDeviceFormatSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<EventsReplyDeviceFormatNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveEventsReplyDeviceFormatNorthbound(const std::function<void(const std::shared_ptr<EventsReplyDeviceFormatNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<EventsReplyDeviceFormatNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveEventsReplyDeviceFormatNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveEventsReplyDeviceFormatNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<EventsReplyDeviceFormatNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendServicesDeviceV1ForwardStartSouthbound(const std::shared_ptr<ServicesDeviceV1ForwardStartSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyDeviceV1ForwardStartNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendServicesDeviceV1ForwardStartSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendServicesDeviceV1ForwardStartSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendServicesDeviceV1ForwardStartSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendServicesDeviceV1ForwardStartSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyDeviceV1ForwardStartNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveServicesReplyDeviceV1ForwardStartNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyDeviceV1ForwardStartNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<ServicesReplyDeviceV1ForwardStartNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyDeviceV1ForwardStartNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyDeviceV1ForwardStartNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyDeviceV1ForwardStartNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendServicesDeviceV1ForwardStopSouthbound(const std::shared_ptr<ServicesDeviceV1ForwardStopSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyDeviceV1ForwardStopNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendServicesDeviceV1ForwardStopSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendServicesDeviceV1ForwardStopSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendServicesDeviceV1ForwardStopSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendServicesDeviceV1ForwardStopSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyDeviceV1ForwardStopNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveServicesReplyDeviceV1ForwardStopNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyDeviceV1ForwardStopNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<ServicesReplyDeviceV1ForwardStopNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyDeviceV1ForwardStopNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyDeviceV1ForwardStopNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyDeviceV1ForwardStopNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendServicesDonglePowerSwitchSouthbound(const std::shared_ptr<ServicesDonglePowerSwitchSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyDonglePowerSwitchNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendServicesDonglePowerSwitchSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendServicesDonglePowerSwitchSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendServicesDonglePowerSwitchSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendServicesDonglePowerSwitchSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyDonglePowerSwitchNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveServicesReplyDonglePowerSwitchNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyDonglePowerSwitchNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<ServicesReplyDonglePowerSwitchNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyDonglePowerSwitchNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyDonglePowerSwitchNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyDonglePowerSwitchNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

ListenIdCode CloudMessageManager::ObserveEventsDonglePowerSwitchNorthbound(const std::function<void(const std::shared_ptr<EventsDonglePowerSwitchNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<EventsDonglePowerSwitchNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveEventsDonglePowerSwitchNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveEventsDonglePowerSwitchNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<EventsDonglePowerSwitchNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendEventsReplyDonglePowerSwitchSouthbound(const std::shared_ptr<EventsReplyDonglePowerSwitchSouthbound>& msg, const std::shared_ptr<EventsDonglePowerSwitchNorthbound>& original_msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    if (original_msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_ORIGINAL_MESSAGE;
    }
    BuildReplyMessage(msg, original_msg); // 根据原消息的uuid, 复制给回复消息
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendEventsReplyDonglePowerSwitchSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    return transport_ptr->Send(msg);
}

MeErrorCode CloudMessageManager::SendDrcDrcAiAlarmStartSouthbound(const std::shared_ptr<DrcDrcAiAlarmStartSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcAiAlarmStartNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcAiAlarmStartSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcAiAlarmStartSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcAiAlarmStartSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcAiAlarmStartSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcAiAlarmStartNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcAiAlarmStartNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcAiAlarmStartNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcAiAlarmStartNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcAiAlarmStartNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcAiAlarmStartNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcAiAlarmStartNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

ListenIdCode CloudMessageManager::ObserveDrcDrcAiAlarmStatePushNorthbound(const std::function<void(const std::shared_ptr<DrcDrcAiAlarmStatePushNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcDrcAiAlarmStatePushNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message) -> void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcDrcAiAlarmStatePushNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcDrcAiAlarmStatePushNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcDrcAiAlarmStatePushNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcAiAlarmStopSouthbound(const std::shared_ptr<DrcDrcAiAlarmStopSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcAiAlarmStopNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcAiAlarmStopSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcAiAlarmStopSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcAiAlarmStopSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcAiAlarmStopSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcAiAlarmStopNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcAiAlarmStopNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcAiAlarmStopNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcAiAlarmStopNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcAiAlarmStopNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcAiAlarmStopNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcAiAlarmStopNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcAiIdentifyFilterSetSouthbound(const std::shared_ptr<DrcDrcAiIdentifyFilterSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcAiIdentifyFilterSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcAiIdentifyFilterSetSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcAiIdentifyFilterSetSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcAiIdentifyFilterSetSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcAiIdentifyFilterSetSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcAiIdentifyFilterSetNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcAiIdentifyFilterSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcAiIdentifyFilterSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcAiIdentifyFilterSetNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcAiIdentifyFilterSetNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcAiIdentifyFilterSetNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcAiIdentifyFilterSetNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcAiIdentifyScoreModeSetSouthbound(const std::shared_ptr<DrcDrcAiIdentifyScoreModeSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcAiIdentifyScoreModeSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcAiIdentifyScoreModeSetSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcAiIdentifyScoreModeSetSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcAiIdentifyScoreModeSetSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcAiIdentifyScoreModeSetSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcAiIdentifyScoreModeSetNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcAiIdentifyScoreModeSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcAiIdentifyScoreModeSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcAiIdentifyScoreModeSetNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcAiIdentifyScoreModeSetNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcAiIdentifyScoreModeSetNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcAiIdentifyScoreModeSetNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcAiIdentifyScoreResetSouthbound(const std::shared_ptr<DrcDrcAiIdentifyScoreResetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcAiIdentifyScoreResetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcAiIdentifyScoreResetSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcAiIdentifyScoreResetSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcAiIdentifyScoreResetSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcAiIdentifyScoreResetSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcAiIdentifyScoreResetNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcAiIdentifyScoreResetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcAiIdentifyScoreResetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcAiIdentifyScoreResetNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcAiIdentifyScoreResetNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcAiIdentifyScoreResetNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcAiIdentifyScoreResetNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcAiIdentifyScoreSetSouthbound(const std::shared_ptr<DrcDrcAiIdentifyScoreSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcAiIdentifyScoreSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcAiIdentifyScoreSetSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcAiIdentifyScoreSetSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcAiIdentifyScoreSetSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcAiIdentifyScoreSetSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcAiIdentifyScoreSetNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcAiIdentifyScoreSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcAiIdentifyScoreSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcAiIdentifyScoreSetNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcAiIdentifyScoreSetNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcAiIdentifyScoreSetNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcAiIdentifyScoreSetNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcAiIdentifySetSouthbound(const std::shared_ptr<DrcDrcAiIdentifySetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcAiIdentifySetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcAiIdentifySetSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcAiIdentifySetSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcAiIdentifySetSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcAiIdentifySetSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcAiIdentifySetNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcAiIdentifySetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcAiIdentifySetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcAiIdentifySetNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcAiIdentifySetNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcAiIdentifySetNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcAiIdentifySetNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

ListenIdCode CloudMessageManager::ObserveDrcDrcAiInfoPushNorthbound(const std::function<void(const std::shared_ptr<DrcDrcAiInfoPushNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcDrcAiInfoPushNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message) -> void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcDrcAiInfoPushNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcDrcAiInfoPushNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcDrcAiInfoPushNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcAiModelSelectSouthbound(const std::shared_ptr<DrcDrcAiModelSelectSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcAiModelSelectNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcAiModelSelectSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcAiModelSelectSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcAiModelSelectSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcAiModelSelectSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcAiModelSelectNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcAiModelSelectNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcAiModelSelectNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcAiModelSelectNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcAiModelSelectNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcAiModelSelectNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcAiModelSelectNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcAiModelsConfigSouthbound(const std::shared_ptr<DrcDrcAiModelsConfigSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcAiModelsConfigNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcAiModelsConfigSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcAiModelsConfigSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcAiModelsConfigSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcAiModelsConfigSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcAiModelsConfigNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcAiModelsConfigNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcAiModelsConfigNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcAiModelsConfigNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcAiModelsConfigNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcAiModelsConfigNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcAiModelsConfigNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcAiSpotlightZoomConfirmSouthbound(const std::shared_ptr<DrcDrcAiSpotlightZoomConfirmSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcAiSpotlightZoomConfirmNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcAiSpotlightZoomConfirmSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcAiSpotlightZoomConfirmSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcAiSpotlightZoomConfirmSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcAiSpotlightZoomConfirmSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcAiSpotlightZoomConfirmNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcAiSpotlightZoomConfirmNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcAiSpotlightZoomConfirmNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcAiSpotlightZoomConfirmNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcAiSpotlightZoomConfirmNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcAiSpotlightZoomConfirmNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcAiSpotlightZoomConfirmNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcAiSpotlightZoomSelectSouthbound(const std::shared_ptr<DrcDrcAiSpotlightZoomSelectSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcAiSpotlightZoomSelectNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcAiSpotlightZoomSelectSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcAiSpotlightZoomSelectSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcAiSpotlightZoomSelectSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcAiSpotlightZoomSelectSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcAiSpotlightZoomSelectNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcAiSpotlightZoomSelectNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcAiSpotlightZoomSelectNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcAiSpotlightZoomSelectNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcAiSpotlightZoomSelectNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcAiSpotlightZoomSelectNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcAiSpotlightZoomSelectNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcAiSpotlightZoomSetSouthbound(const std::shared_ptr<DrcDrcAiSpotlightZoomSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcAiSpotlightZoomSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcAiSpotlightZoomSetSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcAiSpotlightZoomSetSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcAiSpotlightZoomSetSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcAiSpotlightZoomSetSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcAiSpotlightZoomSetNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcAiSpotlightZoomSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcAiSpotlightZoomSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcAiSpotlightZoomSetNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcAiSpotlightZoomSetNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcAiSpotlightZoomSetNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcAiSpotlightZoomSetNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcAiSpotlightZoomStopSouthbound(const std::shared_ptr<DrcDrcAiSpotlightZoomStopSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcAiSpotlightZoomStopNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcAiSpotlightZoomStopSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcAiSpotlightZoomStopSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcAiSpotlightZoomStopSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcAiSpotlightZoomStopSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcAiSpotlightZoomStopNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcAiSpotlightZoomStopNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcAiSpotlightZoomStopNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcAiSpotlightZoomStopNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcAiSpotlightZoomStopNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcAiSpotlightZoomStopNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcAiSpotlightZoomStopNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcAiSpotlightZoomTrackSouthbound(const std::shared_ptr<DrcDrcAiSpotlightZoomTrackSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcAiSpotlightZoomTrackNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcAiSpotlightZoomTrackSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcAiSpotlightZoomTrackSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcAiSpotlightZoomTrackSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcAiSpotlightZoomTrackSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcAiSpotlightZoomTrackNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcAiSpotlightZoomTrackNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcAiSpotlightZoomTrackNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcAiSpotlightZoomTrackNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcAiSpotlightZoomTrackNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcAiSpotlightZoomTrackNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcAiSpotlightZoomTrackNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcAlgorithmDehazeLevelSetSouthbound(const std::shared_ptr<DrcDrcAlgorithmDehazeLevelSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcAlgorithmDehazeLevelSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcAlgorithmDehazeLevelSetSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcAlgorithmDehazeLevelSetSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcAlgorithmDehazeLevelSetSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcAlgorithmDehazeLevelSetSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcAlgorithmDehazeLevelSetNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcAlgorithmDehazeLevelSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcAlgorithmDehazeLevelSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcAlgorithmDehazeLevelSetNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcAlgorithmDehazeLevelSetNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcAlgorithmDehazeLevelSetNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcAlgorithmDehazeLevelSetNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcAlgorithmDehazeModeSetSouthbound(const std::shared_ptr<DrcDrcAlgorithmDehazeModeSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcAlgorithmDehazeModeSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcAlgorithmDehazeModeSetSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcAlgorithmDehazeModeSetSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcAlgorithmDehazeModeSetSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcAlgorithmDehazeModeSetSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcAlgorithmDehazeModeSetNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcAlgorithmDehazeModeSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcAlgorithmDehazeModeSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcAlgorithmDehazeModeSetNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcAlgorithmDehazeModeSetNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcAlgorithmDehazeModeSetNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcAlgorithmDehazeModeSetNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcAssistantVideoModeSetSouthbound(const std::shared_ptr<DrcDrcAssistantVideoModeSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcAssistantVideoModeSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcAssistantVideoModeSetSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcAssistantVideoModeSetSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcAssistantVideoModeSetSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcAssistantVideoModeSetSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcAssistantVideoModeSetNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcAssistantVideoModeSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcAssistantVideoModeSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcAssistantVideoModeSetNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcAssistantVideoModeSetNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcAssistantVideoModeSetNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcAssistantVideoModeSetNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcAssistantVideoSwitchSetSouthbound(const std::shared_ptr<DrcDrcAssistantVideoSwitchSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcAssistantVideoSwitchSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcAssistantVideoSwitchSetSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcAssistantVideoSwitchSetSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcAssistantVideoSwitchSetSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcAssistantVideoSwitchSetSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcAssistantVideoSwitchSetNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcAssistantVideoSwitchSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcAssistantVideoSwitchSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcAssistantVideoSwitchSetNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcAssistantVideoSwitchSetNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcAssistantVideoSwitchSetNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcAssistantVideoSwitchSetNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

ListenIdCode CloudMessageManager::ObserveDrcDrcBatteriesInfoPushNorthbound(const std::function<void(const std::shared_ptr<DrcDrcBatteriesInfoPushNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcDrcBatteriesInfoPushNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message) -> void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcDrcBatteriesInfoPushNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcDrcBatteriesInfoPushNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcDrcBatteriesInfoPushNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcCameraAimSouthbound(const std::shared_ptr<DrcDrcCameraAimSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraAimNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraAimSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraAimSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraAimSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraAimSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcCameraAimNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcCameraAimNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraAimNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcCameraAimNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcCameraAimNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcCameraAimNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcCameraAimNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcCameraApertureValueSetSouthbound(const std::shared_ptr<DrcDrcCameraApertureValueSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraApertureValueSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraApertureValueSetSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraApertureValueSetSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraApertureValueSetSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraApertureValueSetSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcCameraApertureValueSetNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcCameraApertureValueSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraApertureValueSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcCameraApertureValueSetNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcCameraApertureValueSetNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcCameraApertureValueSetNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcCameraApertureValueSetNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcCameraDenoiseLevelSetSouthbound(const std::shared_ptr<DrcDrcCameraDenoiseLevelSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraDenoiseLevelSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraDenoiseLevelSetSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraDenoiseLevelSetSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraDenoiseLevelSetSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraDenoiseLevelSetSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcCameraDenoiseLevelSetNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcCameraDenoiseLevelSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraDenoiseLevelSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcCameraDenoiseLevelSetNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcCameraDenoiseLevelSetNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcCameraDenoiseLevelSetNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcCameraDenoiseLevelSetNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcCameraDewarpingSetSouthbound(const std::shared_ptr<DrcDrcCameraDewarpingSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraDewarpingSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraDewarpingSetSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraDewarpingSetSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraDewarpingSetSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraDewarpingSetSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcCameraDewarpingSetNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcCameraDewarpingSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraDewarpingSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcCameraDewarpingSetNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcCameraDewarpingSetNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcCameraDewarpingSetNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcCameraDewarpingSetNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcCameraExposureModeSetSouthbound(const std::shared_ptr<DrcDrcCameraExposureModeSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraExposureModeSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraExposureModeSetSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraExposureModeSetSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraExposureModeSetSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraExposureModeSetSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcCameraExposureModeSetNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcCameraExposureModeSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraExposureModeSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcCameraExposureModeSetNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcCameraExposureModeSetNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcCameraExposureModeSetNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcCameraExposureModeSetNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcCameraExposureSetSouthbound(const std::shared_ptr<DrcDrcCameraExposureSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraExposureSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraExposureSetSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraExposureSetSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraExposureSetSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraExposureSetSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcCameraExposureSetNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcCameraExposureSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraExposureSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcCameraExposureSetNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcCameraExposureSetNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcCameraExposureSetNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcCameraExposureSetNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcCameraFocalLengthSetSouthbound(const std::shared_ptr<DrcDrcCameraFocalLengthSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraFocalLengthSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraFocalLengthSetSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraFocalLengthSetSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraFocalLengthSetSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraFocalLengthSetSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcCameraFocalLengthSetNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcCameraFocalLengthSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraFocalLengthSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcCameraFocalLengthSetNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcCameraFocalLengthSetNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcCameraFocalLengthSetNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcCameraFocalLengthSetNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcCameraFocusModeSetSouthbound(const std::shared_ptr<DrcDrcCameraFocusModeSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraFocusModeSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraFocusModeSetSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraFocusModeSetSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraFocusModeSetSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraFocusModeSetSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcCameraFocusModeSetNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcCameraFocusModeSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraFocusModeSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcCameraFocusModeSetNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcCameraFocusModeSetNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcCameraFocusModeSetNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcCameraFocusModeSetNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcCameraFocusValueSetSouthbound(const std::shared_ptr<DrcDrcCameraFocusValueSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraFocusValueSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraFocusValueSetSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraFocusValueSetSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraFocusValueSetSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraFocusValueSetSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcCameraFocusValueSetNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcCameraFocusValueSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraFocusValueSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcCameraFocusValueSetNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcCameraFocusValueSetNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcCameraFocusValueSetNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcCameraFocusValueSetNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcCameraFrameZoomSouthbound(const std::shared_ptr<DrcDrcCameraFrameZoomSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraFrameZoomNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraFrameZoomSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraFrameZoomSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraFrameZoomSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraFrameZoomSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcCameraFrameZoomNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcCameraFrameZoomNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraFrameZoomNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcCameraFrameZoomNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcCameraFrameZoomNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcCameraFrameZoomNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcCameraFrameZoomNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcCameraIsoSetSouthbound(const std::shared_ptr<DrcDrcCameraIsoSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraIsoSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraIsoSetSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraIsoSetSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraIsoSetSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraIsoSetSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcCameraIsoSetNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcCameraIsoSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraIsoSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcCameraIsoSetNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcCameraIsoSetNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcCameraIsoSetNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcCameraIsoSetNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcCameraLookAtSouthbound(const std::shared_ptr<DrcDrcCameraLookAtSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraLookAtNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraLookAtSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraLookAtSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraLookAtSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraLookAtSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcCameraLookAtNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcCameraLookAtNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraLookAtNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcCameraLookAtNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcCameraLookAtNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcCameraLookAtNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcCameraLookAtNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcCameraMechanicalShutterSetSouthbound(const std::shared_ptr<DrcDrcCameraMechanicalShutterSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraMechanicalShutterSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraMechanicalShutterSetSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraMechanicalShutterSetSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraMechanicalShutterSetSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraMechanicalShutterSetSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcCameraMechanicalShutterSetNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcCameraMechanicalShutterSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraMechanicalShutterSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcCameraMechanicalShutterSetNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcCameraMechanicalShutterSetNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcCameraMechanicalShutterSetNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcCameraMechanicalShutterSetNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcCameraModeSwitchSouthbound(const std::shared_ptr<DrcDrcCameraModeSwitchSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraModeSwitchNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraModeSwitchSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraModeSwitchSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraModeSwitchSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraModeSwitchSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcCameraModeSwitchNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcCameraModeSwitchNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraModeSwitchNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcCameraModeSwitchNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcCameraModeSwitchNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcCameraModeSwitchNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcCameraModeSwitchNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcCameraNightModeSetSouthbound(const std::shared_ptr<DrcDrcCameraNightModeSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraNightModeSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraNightModeSetSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraNightModeSetSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraNightModeSetSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraNightModeSetSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcCameraNightModeSetNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcCameraNightModeSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraNightModeSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcCameraNightModeSetNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcCameraNightModeSetNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcCameraNightModeSetNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcCameraNightModeSetNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcCameraNightVisionEnableSouthbound(const std::shared_ptr<DrcDrcCameraNightVisionEnableSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraNightVisionEnableNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraNightVisionEnableSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraNightVisionEnableSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraNightVisionEnableSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraNightVisionEnableSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcCameraNightVisionEnableNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcCameraNightVisionEnableNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraNightVisionEnableNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcCameraNightVisionEnableNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcCameraNightVisionEnableNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcCameraNightVisionEnableNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcCameraNightVisionEnableNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

ListenIdCode CloudMessageManager::ObserveDrcDrcCameraOsdInfoPushNorthbound(const std::function<void(const std::shared_ptr<DrcDrcCameraOsdInfoPushNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcDrcCameraOsdInfoPushNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message) -> void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcDrcCameraOsdInfoPushNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcDrcCameraOsdInfoPushNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcDrcCameraOsdInfoPushNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcCameraPhotoFormatSetSouthbound(const std::shared_ptr<DrcDrcCameraPhotoFormatSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraPhotoFormatSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraPhotoFormatSetSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraPhotoFormatSetSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraPhotoFormatSetSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraPhotoFormatSetSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcCameraPhotoFormatSetNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcCameraPhotoFormatSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraPhotoFormatSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcCameraPhotoFormatSetNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcCameraPhotoFormatSetNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcCameraPhotoFormatSetNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcCameraPhotoFormatSetNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

ListenIdCode CloudMessageManager::ObserveDrcDrcCameraPhotoInfoPushNorthbound(const std::function<void(const std::shared_ptr<DrcDrcCameraPhotoInfoPushNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcDrcCameraPhotoInfoPushNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message) -> void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcDrcCameraPhotoInfoPushNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcDrcCameraPhotoInfoPushNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcDrcCameraPhotoInfoPushNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcCameraPhotoSizeSetSouthbound(const std::shared_ptr<DrcDrcCameraPhotoSizeSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraPhotoSizeSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraPhotoSizeSetSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraPhotoSizeSetSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraPhotoSizeSetSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraPhotoSizeSetSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcCameraPhotoSizeSetNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcCameraPhotoSizeSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraPhotoSizeSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcCameraPhotoSizeSetNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcCameraPhotoSizeSetNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcCameraPhotoSizeSetNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcCameraPhotoSizeSetNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcCameraPhotoStopSouthbound(const std::shared_ptr<DrcDrcCameraPhotoStopSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraPhotoStopNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraPhotoStopSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraPhotoStopSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraPhotoStopSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraPhotoStopSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcCameraPhotoStopNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcCameraPhotoStopNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraPhotoStopNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcCameraPhotoStopNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcCameraPhotoStopNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcCameraPhotoStopNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcCameraPhotoStopNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcCameraPhotoTakeSouthbound(const std::shared_ptr<DrcDrcCameraPhotoTakeSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraPhotoTakeNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraPhotoTakeSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraPhotoTakeSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraPhotoTakeSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraPhotoTakeSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcCameraPhotoTakeNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcCameraPhotoTakeNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraPhotoTakeNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcCameraPhotoTakeNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcCameraPhotoTakeNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcCameraPhotoTakeNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcCameraPhotoTakeNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcCameraPointFocusActionSouthbound(const std::shared_ptr<DrcDrcCameraPointFocusActionSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraPointFocusActionNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraPointFocusActionSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraPointFocusActionSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraPointFocusActionSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraPointFocusActionSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcCameraPointFocusActionNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcCameraPointFocusActionNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraPointFocusActionNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcCameraPointFocusActionNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcCameraPointFocusActionNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcCameraPointFocusActionNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcCameraPointFocusActionNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcCameraRecordingStartSouthbound(const std::shared_ptr<DrcDrcCameraRecordingStartSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraRecordingStartNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraRecordingStartSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraRecordingStartSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraRecordingStartSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraRecordingStartSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcCameraRecordingStartNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcCameraRecordingStartNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraRecordingStartNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcCameraRecordingStartNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcCameraRecordingStartNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcCameraRecordingStartNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcCameraRecordingStartNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcCameraRecordingStopSouthbound(const std::shared_ptr<DrcDrcCameraRecordingStopSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraRecordingStopNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraRecordingStopSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraRecordingStopSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraRecordingStopSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraRecordingStopSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcCameraRecordingStopNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcCameraRecordingStopNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraRecordingStopNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcCameraRecordingStopNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcCameraRecordingStopNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcCameraRecordingStopNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcCameraRecordingStopNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcCameraScreenDragSouthbound(const std::shared_ptr<DrcDrcCameraScreenDragSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraScreenDragNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraScreenDragSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraScreenDragSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraScreenDragSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraScreenDragSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcCameraScreenDragNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcCameraScreenDragNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraScreenDragNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcCameraScreenDragNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcCameraScreenDragNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcCameraScreenDragNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcCameraScreenDragNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcCameraScreenSplitSouthbound(const std::shared_ptr<DrcDrcCameraScreenSplitSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraScreenSplitNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraScreenSplitSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraScreenSplitSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraScreenSplitSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraScreenSplitSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcCameraScreenSplitNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcCameraScreenSplitNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraScreenSplitNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcCameraScreenSplitNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcCameraScreenSplitNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcCameraScreenSplitNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcCameraScreenSplitNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcCameraSettingsResetSouthbound(const std::shared_ptr<DrcDrcCameraSettingsResetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraSettingsResetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraSettingsResetSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraSettingsResetSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraSettingsResetSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraSettingsResetSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcCameraSettingsResetNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcCameraSettingsResetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraSettingsResetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcCameraSettingsResetNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcCameraSettingsResetNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcCameraSettingsResetNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcCameraSettingsResetNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcCameraShutterSetSouthbound(const std::shared_ptr<DrcDrcCameraShutterSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraShutterSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraShutterSetSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraShutterSetSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraShutterSetSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraShutterSetSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcCameraShutterSetNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcCameraShutterSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraShutterSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcCameraShutterSetNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcCameraShutterSetNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcCameraShutterSetNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcCameraShutterSetNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

ListenIdCode CloudMessageManager::ObserveDrcDrcCameraStatePushNorthbound(const std::function<void(const std::shared_ptr<DrcDrcCameraStatePushNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcDrcCameraStatePushNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message) -> void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcDrcCameraStatePushNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcDrcCameraStatePushNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcDrcCameraStatePushNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcCameraWatermarkSetSouthbound(const std::shared_ptr<DrcDrcCameraWatermarkSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCameraWatermarkSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraWatermarkSetSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraWatermarkSetSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraWatermarkSetSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCameraWatermarkSetSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcCameraWatermarkSetNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcCameraWatermarkSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCameraWatermarkSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcCameraWatermarkSetNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcCameraWatermarkSetNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcCameraWatermarkSetNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcCameraWatermarkSetNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcCleanFogSouthbound(const std::shared_ptr<DrcDrcCleanFogSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCleanFogNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcCleanFogSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCleanFogSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCleanFogSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCleanFogSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcCleanFogNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcCleanFogNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCleanFogNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcCleanFogNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcCleanFogNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcCleanFogNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcCleanFogNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcCountDownActionSetSouthbound(const std::shared_ptr<DrcDrcCountDownActionSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcCountDownActionSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcCountDownActionSetSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCountDownActionSetSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCountDownActionSetSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcCountDownActionSetSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcCountDownActionSetNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcCountDownActionSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcCountDownActionSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcCountDownActionSetNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcCountDownActionSetNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcCountDownActionSetNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcCountDownActionSetNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

ListenIdCode CloudMessageManager::ObserveDrcDrcCountDownInfoPushNorthbound(const std::function<void(const std::shared_ptr<DrcDrcCountDownInfoPushNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcDrcCountDownInfoPushNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message) -> void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcDrcCountDownInfoPushNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcDrcCountDownInfoPushNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcDrcCountDownInfoPushNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

ListenIdCode CloudMessageManager::ObserveDrcDrcDeviceHeartBeatNorthbound(const std::function<void(const std::shared_ptr<DrcDrcDeviceHeartBeatNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcDrcDeviceHeartBeatNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message) -> void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcDrcDeviceHeartBeatNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcDrcDeviceHeartBeatNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcDrcDeviceHeartBeatNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcDroneHoverSetSouthbound(const std::shared_ptr<DrcDrcDroneHoverSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcDroneHoverSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcDroneHoverSetSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcDroneHoverSetSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcDroneHoverSetSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcDroneHoverSetSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcDroneHoverSetNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcDroneHoverSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcDroneHoverSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcDroneHoverSetNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcDroneHoverSetNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcDroneHoverSetNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcDroneHoverSetNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

ListenIdCode CloudMessageManager::ObserveDrcDrcDroneStatePushNorthbound(const std::function<void(const std::shared_ptr<DrcDrcDroneStatePushNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcDrcDroneStatePushNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message) -> void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcDrcDroneStatePushNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcDrcDroneStatePushNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcDrcDroneStatePushNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcEmergencyLandingSouthbound(const std::shared_ptr<DrcDrcEmergencyLandingSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcEmergencyLandingNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcEmergencyLandingSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcEmergencyLandingSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcEmergencyLandingSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcEmergencyLandingSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcEmergencyLandingNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcEmergencyLandingNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcEmergencyLandingNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcEmergencyLandingNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcEmergencyLandingNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcEmergencyLandingNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcEmergencyLandingNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcFlashLedSetSouthbound(const std::shared_ptr<DrcDrcFlashLedSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcFlashLedSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcFlashLedSetSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcFlashLedSetSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcFlashLedSetSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcFlashLedSetSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcFlashLedSetNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcFlashLedSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcFlashLedSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcFlashLedSetNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcFlashLedSetNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcFlashLedSetNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcFlashLedSetNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcFollowMouseParameterSetSouthbound(const std::shared_ptr<DrcDrcFollowMouseParameterSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcFollowMouseParameterSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcFollowMouseParameterSetSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcFollowMouseParameterSetSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcFollowMouseParameterSetSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcFollowMouseParameterSetSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcFollowMouseParameterSetNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcFollowMouseParameterSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcFollowMouseParameterSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcFollowMouseParameterSetNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcFollowMouseParameterSetNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcFollowMouseParameterSetNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcFollowMouseParameterSetNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcForceLandingSouthbound(const std::shared_ptr<DrcDrcForceLandingSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcForceLandingNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcForceLandingSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcForceLandingSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcForceLandingSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcForceLandingSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcForceLandingNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcForceLandingNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcForceLandingNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcForceLandingNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcForceLandingNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcForceLandingNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcForceLandingNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcGimbalResetSouthbound(const std::shared_ptr<DrcDrcGimbalResetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcGimbalResetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcGimbalResetSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcGimbalResetSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcGimbalResetSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcGimbalResetSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcGimbalResetNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcGimbalResetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcGimbalResetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcGimbalResetNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcGimbalResetNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcGimbalResetNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcGimbalResetNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcInfraredFillLightEnableSouthbound(const std::shared_ptr<DrcDrcInfraredFillLightEnableSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcInfraredFillLightEnableNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcInfraredFillLightEnableSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcInfraredFillLightEnableSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcInfraredFillLightEnableSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcInfraredFillLightEnableSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcInfraredFillLightEnableNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcInfraredFillLightEnableNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcInfraredFillLightEnableNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcInfraredFillLightEnableNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcInfraredFillLightEnableNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcInfraredFillLightEnableNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcInfraredFillLightEnableNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcInitialStateSubscribeSouthbound(const std::shared_ptr<DrcDrcInitialStateSubscribeSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcInitialStateSubscribeNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcInitialStateSubscribeSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcInitialStateSubscribeSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcInitialStateSubscribeSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcInitialStateSubscribeSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcInitialStateSubscribeNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcInitialStateSubscribeNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcInitialStateSubscribeNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcInitialStateSubscribeNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcInitialStateSubscribeNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcInitialStateSubscribeNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcInitialStateSubscribeNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcIntervalPhotoSetSouthbound(const std::shared_ptr<DrcDrcIntervalPhotoSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcIntervalPhotoSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcIntervalPhotoSetSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcIntervalPhotoSetSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcIntervalPhotoSetSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcIntervalPhotoSetSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcIntervalPhotoSetNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcIntervalPhotoSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcIntervalPhotoSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcIntervalPhotoSetNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcIntervalPhotoSetNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcIntervalPhotoSetNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcIntervalPhotoSetNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcIrMeteringAreaSetSouthbound(const std::shared_ptr<DrcDrcIrMeteringAreaSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcIrMeteringAreaSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcIrMeteringAreaSetSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcIrMeteringAreaSetSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcIrMeteringAreaSetSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcIrMeteringAreaSetSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcIrMeteringAreaSetNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcIrMeteringAreaSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcIrMeteringAreaSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcIrMeteringAreaSetNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcIrMeteringAreaSetNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcIrMeteringAreaSetNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcIrMeteringAreaSetNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcIrMeteringModeSetSouthbound(const std::shared_ptr<DrcDrcIrMeteringModeSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcIrMeteringModeSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcIrMeteringModeSetSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcIrMeteringModeSetSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcIrMeteringModeSetSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcIrMeteringModeSetSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcIrMeteringModeSetNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcIrMeteringModeSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcIrMeteringModeSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcIrMeteringModeSetNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcIrMeteringModeSetNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcIrMeteringModeSetNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcIrMeteringModeSetNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcIrMeteringPointSetSouthbound(const std::shared_ptr<DrcDrcIrMeteringPointSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcIrMeteringPointSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcIrMeteringPointSetSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcIrMeteringPointSetSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcIrMeteringPointSetSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcIrMeteringPointSetSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcIrMeteringPointSetNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcIrMeteringPointSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcIrMeteringPointSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcIrMeteringPointSetNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcIrMeteringPointSetNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcIrMeteringPointSetNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcIrMeteringPointSetNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcLightBrightnessSetSouthbound(const std::shared_ptr<DrcDrcLightBrightnessSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcLightBrightnessSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcLightBrightnessSetSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcLightBrightnessSetSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcLightBrightnessSetSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcLightBrightnessSetSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcLightBrightnessSetNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcLightBrightnessSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcLightBrightnessSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcLightBrightnessSetNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcLightBrightnessSetNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcLightBrightnessSetNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcLightBrightnessSetNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcLightCalibrationSouthbound(const std::shared_ptr<DrcDrcLightCalibrationSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcLightCalibrationNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcLightCalibrationSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcLightCalibrationSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcLightCalibrationSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcLightCalibrationSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcLightCalibrationNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcLightCalibrationNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcLightCalibrationNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcLightCalibrationNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcLightCalibrationNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcLightCalibrationNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcLightCalibrationNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcLightFineTuningSetSouthbound(const std::shared_ptr<DrcDrcLightFineTuningSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcLightFineTuningSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcLightFineTuningSetSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcLightFineTuningSetSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcLightFineTuningSetSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcLightFineTuningSetSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcLightFineTuningSetNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcLightFineTuningSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcLightFineTuningSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcLightFineTuningSetNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcLightFineTuningSetNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcLightFineTuningSetNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcLightFineTuningSetNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcLightLightGimbalControlSetSouthbound(const std::shared_ptr<DrcDrcLightLightGimbalControlSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcLightLightGimbalControlSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcLightLightGimbalControlSetSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcLightLightGimbalControlSetSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcLightLightGimbalControlSetSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcLightLightGimbalControlSetSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcLightLightGimbalControlSetNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcLightLightGimbalControlSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcLightLightGimbalControlSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcLightLightGimbalControlSetNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcLightLightGimbalControlSetNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcLightLightGimbalControlSetNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcLightLightGimbalControlSetNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcLightModeSetSouthbound(const std::shared_ptr<DrcDrcLightModeSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcLightModeSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcLightModeSetSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcLightModeSetSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcLightModeSetSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcLightModeSetSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcLightModeSetNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcLightModeSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcLightModeSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcLightModeSetNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcLightModeSetNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcLightModeSetNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcLightModeSetNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcLightTypeSetSouthbound(const std::shared_ptr<DrcDrcLightTypeSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcLightTypeSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcLightTypeSetSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcLightTypeSetSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcLightTypeSetSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcLightTypeSetSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcLightTypeSetNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcLightTypeSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcLightTypeSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcLightTypeSetNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcLightTypeSetNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcLightTypeSetNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcLightTypeSetNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcLightWideFieldModeSetSouthbound(const std::shared_ptr<DrcDrcLightWideFieldModeSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcLightWideFieldModeSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcLightWideFieldModeSetSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcLightWideFieldModeSetSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcLightWideFieldModeSetSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcLightWideFieldModeSetSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcLightWideFieldModeSetNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcLightWideFieldModeSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcLightWideFieldModeSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcLightWideFieldModeSetNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcLightWideFieldModeSetNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcLightWideFieldModeSetNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcLightWideFieldModeSetNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcLinkageZoomSetSouthbound(const std::shared_ptr<DrcDrcLinkageZoomSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcLinkageZoomSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcLinkageZoomSetSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcLinkageZoomSetSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcLinkageZoomSetSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcLinkageZoomSetSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcLinkageZoomSetNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcLinkageZoomSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcLinkageZoomSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcLinkageZoomSetNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcLinkageZoomSetNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcLinkageZoomSetNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcLinkageZoomSetNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcLiveLensChangeSouthbound(const std::shared_ptr<DrcDrcLiveLensChangeSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcLiveLensChangeNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcLiveLensChangeSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcLiveLensChangeSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcLiveLensChangeSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcLiveLensChangeSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcLiveLensChangeNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcLiveLensChangeNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcLiveLensChangeNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcLiveLensChangeNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcLiveLensChangeNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcLiveLensChangeNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcLiveLensChangeNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

ListenIdCode CloudMessageManager::ObserveDrcDrcLiveVideoInfoPushNorthbound(const std::function<void(const std::shared_ptr<DrcDrcLiveVideoInfoPushNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcDrcLiveVideoInfoPushNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message) -> void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcDrcLiveVideoInfoPushNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcDrcLiveVideoInfoPushNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcDrcLiveVideoInfoPushNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendServicesDrcModeEnterSouthbound(const std::shared_ptr<ServicesDrcModeEnterSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyDrcModeEnterNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendServicesDrcModeEnterSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendServicesDrcModeEnterSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendServicesDrcModeEnterSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendServicesDrcModeEnterSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyDrcModeEnterNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveServicesReplyDrcModeEnterNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyDrcModeEnterNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<ServicesReplyDrcModeEnterNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyDrcModeEnterNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyDrcModeEnterNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyDrcModeEnterNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendServicesDrcModeExitSouthbound(const std::shared_ptr<ServicesDrcModeExitSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyDrcModeExitNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendServicesDrcModeExitSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendServicesDrcModeExitSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendServicesDrcModeExitSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendServicesDrcModeExitSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyDrcModeExitNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveServicesReplyDrcModeExitNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyDrcModeExitNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<ServicesReplyDrcModeExitNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyDrcModeExitNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyDrcModeExitNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyDrcModeExitNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcNightLightsStateSetSouthbound(const std::shared_ptr<DrcDrcNightLightsStateSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcNightLightsStateSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcNightLightsStateSetSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcNightLightsStateSetSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcNightLightsStateSetSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcNightLightsStateSetSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcNightLightsStateSetNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcNightLightsStateSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcNightLightsStateSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcNightLightsStateSetNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcNightLightsStateSetNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcNightLightsStateSetNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcNightLightsStateSetNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcOpenParachuteSouthbound(const std::shared_ptr<DrcDrcOpenParachuteSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcOpenParachuteNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcOpenParachuteSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcOpenParachuteSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcOpenParachuteSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcOpenParachuteSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcOpenParachuteNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcOpenParachuteNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcOpenParachuteNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcOpenParachuteNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcOpenParachuteNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcOpenParachuteNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcOpenParachuteNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

ListenIdCode CloudMessageManager::ObserveDrcDrcParachuteInfoPushNorthbound(const std::function<void(const std::shared_ptr<DrcDrcParachuteInfoPushNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcDrcParachuteInfoPushNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message) -> void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcDrcParachuteInfoPushNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcDrcParachuteInfoPushNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcDrcParachuteInfoPushNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcPhotoStorageSetSouthbound(const std::shared_ptr<DrcDrcPhotoStorageSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcPhotoStorageSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcPhotoStorageSetSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcPhotoStorageSetSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcPhotoStorageSetSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcPhotoStorageSetSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcPhotoStorageSetNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcPhotoStorageSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcPhotoStorageSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcPhotoStorageSetNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcPhotoStorageSetNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcPhotoStorageSetNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcPhotoStorageSetNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcPreRecordedStatusSetSouthbound(const std::shared_ptr<DrcDrcPreRecordedStatusSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcPreRecordedStatusSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcPreRecordedStatusSetSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcPreRecordedStatusSetSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcPreRecordedStatusSetSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcPreRecordedStatusSetSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcPreRecordedStatusSetNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcPreRecordedStatusSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcPreRecordedStatusSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcPreRecordedStatusSetNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcPreRecordedStatusSetNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcPreRecordedStatusSetNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcPreRecordedStatusSetNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcPreRecordedTimeSetSouthbound(const std::shared_ptr<DrcDrcPreRecordedTimeSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcPreRecordedTimeSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcPreRecordedTimeSetSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcPreRecordedTimeSetSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcPreRecordedTimeSetSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcPreRecordedTimeSetSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcPreRecordedTimeSetNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcPreRecordedTimeSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcPreRecordedTimeSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcPreRecordedTimeSetNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcPreRecordedTimeSetNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcPreRecordedTimeSetNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcPreRecordedTimeSetNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

ListenIdCode CloudMessageManager::ObserveDrcDrcPsdkFloatingWindowTextNorthbound(const std::function<void(const std::shared_ptr<DrcDrcPsdkFloatingWindowTextNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcDrcPsdkFloatingWindowTextNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message) -> void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcDrcPsdkFloatingWindowTextNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcDrcPsdkFloatingWindowTextNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcDrcPsdkFloatingWindowTextNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcPsdkInputBoxTextSetSouthbound(const std::shared_ptr<DrcDrcPsdkInputBoxTextSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcPsdkInputBoxTextSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcPsdkInputBoxTextSetSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcPsdkInputBoxTextSetSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcPsdkInputBoxTextSetSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcPsdkInputBoxTextSetSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcPsdkInputBoxTextSetNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcPsdkInputBoxTextSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcPsdkInputBoxTextSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcPsdkInputBoxTextSetNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcPsdkInputBoxTextSetNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcPsdkInputBoxTextSetNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcPsdkInputBoxTextSetNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

ListenIdCode CloudMessageManager::ObserveDrcDrcPsdkStateInfoNorthbound(const std::function<void(const std::shared_ptr<DrcDrcPsdkStateInfoNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcDrcPsdkStateInfoNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message) -> void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcDrcPsdkStateInfoNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcDrcPsdkStateInfoNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcDrcPsdkStateInfoNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

ListenIdCode CloudMessageManager::ObserveDrcDrcPsdkUiResourceNorthbound(const std::function<void(const std::shared_ptr<DrcDrcPsdkUiResourceNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcDrcPsdkUiResourceNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message) -> void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcDrcPsdkUiResourceNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcDrcPsdkUiResourceNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcDrcPsdkUiResourceNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcPsdkWidgetValueSetSouthbound(const std::shared_ptr<DrcDrcPsdkWidgetValueSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcPsdkWidgetValueSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcPsdkWidgetValueSetSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcPsdkWidgetValueSetSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcPsdkWidgetValueSetSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcPsdkWidgetValueSetSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcPsdkWidgetValueSetNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcPsdkWidgetValueSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcPsdkWidgetValueSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcPsdkWidgetValueSetNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcPsdkWidgetValueSetNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcPsdkWidgetValueSetNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcPsdkWidgetValueSetNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcRtcQualityTestSouthbound(const std::shared_ptr<DrcDrcRtcQualityTestSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcRtcQualityTestNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcRtcQualityTestSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcRtcQualityTestSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcRtcQualityTestSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcRtcQualityTestSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcRtcQualityTestNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcRtcQualityTestNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcRtcQualityTestNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcRtcQualityTestNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcRtcQualityTestNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcRtcQualityTestNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcRtcQualityTestNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcSetFlyingMaxSpeedSouthbound(const std::shared_ptr<DrcDrcSetFlyingMaxSpeedSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcSetFlyingMaxSpeedNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcSetFlyingMaxSpeedSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcSetFlyingMaxSpeedSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcSetFlyingMaxSpeedSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcSetFlyingMaxSpeedSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcSetFlyingMaxSpeedNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcSetFlyingMaxSpeedNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcSetFlyingMaxSpeedNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcSetFlyingMaxSpeedNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcSetFlyingMaxSpeedNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcSetFlyingMaxSpeedNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcSetFlyingMaxSpeedNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcSetIrDisplayModeSouthbound(const std::shared_ptr<DrcDrcSetIrDisplayModeSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcSetIrDisplayModeNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcSetIrDisplayModeSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcSetIrDisplayModeSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcSetIrDisplayModeSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcSetIrDisplayModeSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcSetIrDisplayModeNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcSetIrDisplayModeNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcSetIrDisplayModeNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcSetIrDisplayModeNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcSetIrDisplayModeNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcSetIrDisplayModeNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcSetIrDisplayModeNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcSetIrEnhanceModeSouthbound(const std::shared_ptr<DrcDrcSetIrEnhanceModeSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcSetIrEnhanceModeNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcSetIrEnhanceModeSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcSetIrEnhanceModeSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcSetIrEnhanceModeSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcSetIrEnhanceModeSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcSetIrEnhanceModeNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcSetIrEnhanceModeNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcSetIrEnhanceModeNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcSetIrEnhanceModeNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcSetIrEnhanceModeNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcSetIrEnhanceModeNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcSetIrEnhanceModeNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcSetIrMixRateSouthbound(const std::shared_ptr<DrcDrcSetIrMixRateSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcSetIrMixRateNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcSetIrMixRateSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcSetIrMixRateSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcSetIrMixRateSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcSetIrMixRateSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcSetIrMixRateNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcSetIrMixRateNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcSetIrMixRateNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcSetIrMixRateNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcSetIrMixRateNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcSetIrMixRateNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcSetIrMixRateNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcSetIsothermColorRenderingTypeSouthbound(const std::shared_ptr<DrcDrcSetIsothermColorRenderingTypeSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcSetIsothermColorRenderingTypeNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcSetIsothermColorRenderingTypeSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcSetIsothermColorRenderingTypeSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcSetIsothermColorRenderingTypeSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcSetIsothermColorRenderingTypeSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcSetIsothermColorRenderingTypeNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcSetIsothermColorRenderingTypeNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcSetIsothermColorRenderingTypeNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcSetIsothermColorRenderingTypeNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcSetIsothermColorRenderingTypeNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcSetIsothermColorRenderingTypeNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcSetIsothermColorRenderingTypeNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcSetIsothermEnableSouthbound(const std::shared_ptr<DrcDrcSetIsothermEnableSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcSetIsothermEnableNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcSetIsothermEnableSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcSetIsothermEnableSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcSetIsothermEnableSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcSetIsothermEnableSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcSetIsothermEnableNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcSetIsothermEnableNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcSetIsothermEnableNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcSetIsothermEnableNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcSetIsothermEnableNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcSetIsothermEnableNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcSetIsothermEnableNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcSetIsothermLowerUpperRangeSouthbound(const std::shared_ptr<DrcDrcSetIsothermLowerUpperRangeSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcSetIsothermLowerUpperRangeNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcSetIsothermLowerUpperRangeSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcSetIsothermLowerUpperRangeSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcSetIsothermLowerUpperRangeSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcSetIsothermLowerUpperRangeSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcSetIsothermLowerUpperRangeNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcSetIsothermLowerUpperRangeNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcSetIsothermLowerUpperRangeNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcSetIsothermLowerUpperRangeNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcSetIsothermLowerUpperRangeNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcSetIsothermLowerUpperRangeNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcSetIsothermLowerUpperRangeNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcSetIsothermRenderingPolygonSouthbound(const std::shared_ptr<DrcDrcSetIsothermRenderingPolygonSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcSetIsothermRenderingPolygonNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcSetIsothermRenderingPolygonSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcSetIsothermRenderingPolygonSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcSetIsothermRenderingPolygonSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcSetIsothermRenderingPolygonSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcSetIsothermRenderingPolygonNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcSetIsothermRenderingPolygonNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcSetIsothermRenderingPolygonNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcSetIsothermRenderingPolygonNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcSetIsothermRenderingPolygonNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcSetIsothermRenderingPolygonNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcSetIsothermRenderingPolygonNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcSetIsothermRoiBasedRenderingEnableSouthbound(const std::shared_ptr<DrcDrcSetIsothermRoiBasedRenderingEnableSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcSetIsothermRoiBasedRenderingEnableNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcSetIsothermRoiBasedRenderingEnableSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcSetIsothermRoiBasedRenderingEnableSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcSetIsothermRoiBasedRenderingEnableSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcSetIsothermRoiBasedRenderingEnableSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcSetIsothermRoiBasedRenderingEnableNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcSetIsothermRoiBasedRenderingEnableNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcSetIsothermRoiBasedRenderingEnableNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcSetIsothermRoiBasedRenderingEnableNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcSetIsothermRoiBasedRenderingEnableNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcSetIsothermRoiBasedRenderingEnableNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcSetIsothermRoiBasedRenderingEnableNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcSpeakerAudioPlayStartSouthbound(const std::shared_ptr<DrcDrcSpeakerAudioPlayStartSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcSpeakerAudioPlayStartNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcSpeakerAudioPlayStartSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcSpeakerAudioPlayStartSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcSpeakerAudioPlayStartSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcSpeakerAudioPlayStartSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcSpeakerAudioPlayStartNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcSpeakerAudioPlayStartNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcSpeakerAudioPlayStartNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcSpeakerAudioPlayStartNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcSpeakerAudioPlayStartNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcSpeakerAudioPlayStartNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcSpeakerAudioPlayStartNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcSpeakerPlayModeSetSouthbound(const std::shared_ptr<DrcDrcSpeakerPlayModeSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcSpeakerPlayModeSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcSpeakerPlayModeSetSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcSpeakerPlayModeSetSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcSpeakerPlayModeSetSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcSpeakerPlayModeSetSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcSpeakerPlayModeSetNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcSpeakerPlayModeSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcSpeakerPlayModeSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcSpeakerPlayModeSetNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcSpeakerPlayModeSetNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcSpeakerPlayModeSetNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcSpeakerPlayModeSetNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

ListenIdCode CloudMessageManager::ObserveDrcDrcSpeakerPlayProgressNorthbound(const std::function<void(const std::shared_ptr<DrcDrcSpeakerPlayProgressNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcDrcSpeakerPlayProgressNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message) -> void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcDrcSpeakerPlayProgressNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcDrcSpeakerPlayProgressNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcDrcSpeakerPlayProgressNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcSpeakerPlayStopSouthbound(const std::shared_ptr<DrcDrcSpeakerPlayStopSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcSpeakerPlayStopNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcSpeakerPlayStopSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcSpeakerPlayStopSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcSpeakerPlayStopSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcSpeakerPlayStopSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcSpeakerPlayStopNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcSpeakerPlayStopNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcSpeakerPlayStopNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcSpeakerPlayStopNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcSpeakerPlayStopNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcSpeakerPlayStopNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcSpeakerPlayStopNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcSpeakerPlayVolumeSetSouthbound(const std::shared_ptr<DrcDrcSpeakerPlayVolumeSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcSpeakerPlayVolumeSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcSpeakerPlayVolumeSetSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcSpeakerPlayVolumeSetSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcSpeakerPlayVolumeSetSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcSpeakerPlayVolumeSetSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcSpeakerPlayVolumeSetNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcSpeakerPlayVolumeSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcSpeakerPlayVolumeSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcSpeakerPlayVolumeSetNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcSpeakerPlayVolumeSetNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcSpeakerPlayVolumeSetNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcSpeakerPlayVolumeSetNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcSpeakerRealTimeDataTransmitSouthbound(const std::shared_ptr<DrcDrcSpeakerRealTimeDataTransmitSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcSpeakerRealTimeDataTransmitNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcSpeakerRealTimeDataTransmitSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcSpeakerRealTimeDataTransmitSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcSpeakerRealTimeDataTransmitSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcSpeakerRealTimeDataTransmitSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcSpeakerRealTimeDataTransmitNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcSpeakerRealTimeDataTransmitNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcSpeakerRealTimeDataTransmitNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcSpeakerRealTimeDataTransmitNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcSpeakerRealTimeDataTransmitNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcSpeakerRealTimeDataTransmitNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcSpeakerRealTimeDataTransmitNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcSpeakerRealTimeStartSouthbound(const std::shared_ptr<DrcDrcSpeakerRealTimeStartSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcSpeakerRealTimeStartNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcSpeakerRealTimeStartSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcSpeakerRealTimeStartSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcSpeakerRealTimeStartSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcSpeakerRealTimeStartSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcSpeakerRealTimeStartNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcSpeakerRealTimeStartNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcSpeakerRealTimeStartNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcSpeakerRealTimeStartNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcSpeakerRealTimeStartNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcSpeakerRealTimeStartNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcSpeakerRealTimeStartNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcSpeakerRealTimeStopSouthbound(const std::shared_ptr<DrcDrcSpeakerRealTimeStopSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcSpeakerRealTimeStopNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcSpeakerRealTimeStopSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcSpeakerRealTimeStopSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcSpeakerRealTimeStopSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcSpeakerRealTimeStopSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcSpeakerRealTimeStopNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcSpeakerRealTimeStopNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcSpeakerRealTimeStopNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcSpeakerRealTimeStopNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcSpeakerRealTimeStopNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcSpeakerRealTimeStopNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcSpeakerRealTimeStopNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcSpeakerReplaySouthbound(const std::shared_ptr<DrcDrcSpeakerReplaySouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcSpeakerReplayNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcSpeakerReplaySouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcSpeakerReplaySouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcSpeakerReplaySouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcSpeakerReplaySouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcSpeakerReplayNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcSpeakerReplayNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcSpeakerReplayNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcSpeakerReplayNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcSpeakerReplayNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcSpeakerReplayNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcSpeakerReplayNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcSpeakerTtsPlayStartSouthbound(const std::shared_ptr<DrcDrcSpeakerTtsPlayStartSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcSpeakerTtsPlayStartNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcSpeakerTtsPlayStartSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcSpeakerTtsPlayStartSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcSpeakerTtsPlayStartSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcSpeakerTtsPlayStartSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcSpeakerTtsPlayStartNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcSpeakerTtsPlayStartNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcSpeakerTtsPlayStartNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcSpeakerTtsPlayStartNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcSpeakerTtsPlayStartNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcSpeakerTtsPlayStartNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcSpeakerTtsPlayStartNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcSpeakerTtsSetSouthbound(const std::shared_ptr<DrcDrcSpeakerTtsSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcSpeakerTtsSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcSpeakerTtsSetSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcSpeakerTtsSetSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcSpeakerTtsSetSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcSpeakerTtsSetSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcSpeakerTtsSetNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcSpeakerTtsSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcSpeakerTtsSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcSpeakerTtsSetNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcSpeakerTtsSetNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcSpeakerTtsSetNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcSpeakerTtsSetNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcStealthStateSetSouthbound(const std::shared_ptr<DrcDrcStealthStateSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcStealthStateSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcStealthStateSetSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcStealthStateSetSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcStealthStateSetSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcStealthStateSetSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcStealthStateSetNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcStealthStateSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcStealthStateSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcStealthStateSetNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcStealthStateSetNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcStealthStateSetNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcStealthStateSetNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcThermalCurrentPaletteSetSouthbound(const std::shared_ptr<DrcDrcThermalCurrentPaletteSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcThermalCurrentPaletteSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcThermalCurrentPaletteSetSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcThermalCurrentPaletteSetSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcThermalCurrentPaletteSetSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcThermalCurrentPaletteSetSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcThermalCurrentPaletteSetNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcThermalCurrentPaletteSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcThermalCurrentPaletteSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcThermalCurrentPaletteSetNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcThermalCurrentPaletteSetNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcThermalCurrentPaletteSetNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcThermalCurrentPaletteSetNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcThermalGainModeSetSouthbound(const std::shared_ptr<DrcDrcThermalGainModeSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcThermalGainModeSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcThermalGainModeSetSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcThermalGainModeSetSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcThermalGainModeSetSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcThermalGainModeSetSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcThermalGainModeSetNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcThermalGainModeSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcThermalGainModeSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcThermalGainModeSetNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcThermalGainModeSetNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcThermalGainModeSetNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcThermalGainModeSetNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcThermalSuperResolutionEnableSouthbound(const std::shared_ptr<DrcDrcThermalSuperResolutionEnableSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcThermalSuperResolutionEnableNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcThermalSuperResolutionEnableSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcThermalSuperResolutionEnableSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcThermalSuperResolutionEnableSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcThermalSuperResolutionEnableSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcThermalSuperResolutionEnableNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcThermalSuperResolutionEnableNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcThermalSuperResolutionEnableNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcThermalSuperResolutionEnableNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcThermalSuperResolutionEnableNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcThermalSuperResolutionEnableNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcThermalSuperResolutionEnableNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcVideoResolutionSetSouthbound(const std::shared_ptr<DrcDrcVideoResolutionSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcVideoResolutionSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcVideoResolutionSetSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcVideoResolutionSetSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcVideoResolutionSetSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcVideoResolutionSetSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcVideoResolutionSetNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcVideoResolutionSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcVideoResolutionSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcVideoResolutionSetNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcVideoResolutionSetNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcVideoResolutionSetNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcVideoResolutionSetNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDrcVideoStorageSetSouthbound(const std::shared_ptr<DrcDrcVideoStorageSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDrcVideoStorageSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDrcVideoStorageSetSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDrcVideoStorageSetSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcVideoStorageSetSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDrcVideoStorageSetSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcVideoStorageSetNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDrcVideoStorageSetNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDrcVideoStorageSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDrcVideoStorageSetNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcVideoStorageSetNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDrcVideoStorageSetNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDrcVideoStorageSetNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendServicesDrcVoiceEnterSouthbound(const std::shared_ptr<ServicesDrcVoiceEnterSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyDrcVoiceEnterNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendServicesDrcVoiceEnterSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendServicesDrcVoiceEnterSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendServicesDrcVoiceEnterSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendServicesDrcVoiceEnterSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyDrcVoiceEnterNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveServicesReplyDrcVoiceEnterNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyDrcVoiceEnterNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<ServicesReplyDrcVoiceEnterNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyDrcVoiceEnterNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyDrcVoiceEnterNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyDrcVoiceEnterNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcDroneEmergencyStopSouthbound(const std::shared_ptr<DrcDroneEmergencyStopSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyDroneEmergencyStopNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcDroneEmergencyStopSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcDroneEmergencyStopSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcDroneEmergencyStopSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcDroneEmergencyStopSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDroneEmergencyStopNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyDroneEmergencyStopNorthbound(const std::function<void(const std::shared_ptr<DrcReplyDroneEmergencyStopNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyDroneEmergencyStopNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDroneEmergencyStopNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyDroneEmergencyStopNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyDroneEmergencyStopNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendEventsEmergencyStopNotifySouthbound(const std::shared_ptr<EventsEmergencyStopNotifySouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<EventsReplyEmergencyStopNotifyNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendEventsEmergencyStopNotifySouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendEventsEmergencyStopNotifySouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendEventsEmergencyStopNotifySouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendEventsEmergencyStopNotifySouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<EventsReplyEmergencyStopNotifyNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveEventsReplyEmergencyStopNotifyNorthbound(const std::function<void(const std::shared_ptr<EventsReplyEmergencyStopNotifyNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<EventsReplyEmergencyStopNotifyNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveEventsReplyEmergencyStopNotifyNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveEventsReplyEmergencyStopNotifyNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<EventsReplyEmergencyStopNotifyNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

ListenIdCode CloudMessageManager::ObserveEventsEsimOperatorSwitchNorthbound(const std::function<void(const std::shared_ptr<EventsEsimOperatorSwitchNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<EventsEsimOperatorSwitchNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveEventsEsimOperatorSwitchNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveEventsEsimOperatorSwitchNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<EventsEsimOperatorSwitchNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendEventsReplyEsimOperatorSwitchSouthbound(const std::shared_ptr<EventsReplyEsimOperatorSwitchSouthbound>& msg, const std::shared_ptr<EventsEsimOperatorSwitchNorthbound>& original_msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    if (original_msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_ORIGINAL_MESSAGE;
    }
    BuildReplyMessage(msg, original_msg); // 根据原消息的uuid, 复制给回复消息
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendEventsReplyEsimOperatorSwitchSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    return transport_ptr->Send(msg);
}

MeErrorCode CloudMessageManager::SendServicesEsimOperatorSwitchSouthbound(const std::shared_ptr<ServicesEsimOperatorSwitchSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyEsimOperatorSwitchNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendServicesEsimOperatorSwitchSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendServicesEsimOperatorSwitchSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendServicesEsimOperatorSwitchSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendServicesEsimOperatorSwitchSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyEsimOperatorSwitchNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveServicesReplyEsimOperatorSwitchNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyEsimOperatorSwitchNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<ServicesReplyEsimOperatorSwitchNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyEsimOperatorSwitchNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyEsimOperatorSwitchNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyEsimOperatorSwitchNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendServicesFileuploadListSouthbound(const std::shared_ptr<ServicesFileuploadListSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyFileuploadListNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendServicesFileuploadListSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendServicesFileuploadListSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendServicesFileuploadListSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendServicesFileuploadListSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyFileuploadListNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveServicesReplyFileuploadListNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyFileuploadListNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<ServicesReplyFileuploadListNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyFileuploadListNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyFileuploadListNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyFileuploadListNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendEventsFileuploadProgressSouthbound(const std::shared_ptr<EventsFileuploadProgressSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<EventsReplyFileuploadProgressNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendEventsFileuploadProgressSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendEventsFileuploadProgressSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendEventsFileuploadProgressSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendEventsFileuploadProgressSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<EventsReplyFileuploadProgressNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveEventsReplyFileuploadProgressNorthbound(const std::function<void(const std::shared_ptr<EventsReplyFileuploadProgressNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<EventsReplyFileuploadProgressNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveEventsReplyFileuploadProgressNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveEventsReplyFileuploadProgressNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<EventsReplyFileuploadProgressNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendServicesFileuploadStartSouthbound(const std::shared_ptr<ServicesFileuploadStartSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyFileuploadStartNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendServicesFileuploadStartSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendServicesFileuploadStartSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendServicesFileuploadStartSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendServicesFileuploadStartSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyFileuploadStartNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveServicesReplyFileuploadStartNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyFileuploadStartNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<ServicesReplyFileuploadStartNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyFileuploadStartNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyFileuploadStartNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyFileuploadStartNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendServicesFileuploadUpdateSouthbound(const std::shared_ptr<ServicesFileuploadUpdateSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyFileuploadUpdateNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendServicesFileuploadUpdateSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendServicesFileuploadUpdateSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendServicesFileuploadUpdateSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendServicesFileuploadUpdateSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyFileuploadUpdateNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveServicesReplyFileuploadUpdateNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyFileuploadUpdateNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<ServicesReplyFileuploadUpdateNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyFileuploadUpdateNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyFileuploadUpdateNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyFileuploadUpdateNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

ListenIdCode CloudMessageManager::ObserveEventsFlightAreasDroneLocationNorthbound(const std::function<void(const std::shared_ptr<EventsFlightAreasDroneLocationNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<EventsFlightAreasDroneLocationNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveEventsFlightAreasDroneLocationNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveEventsFlightAreasDroneLocationNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<EventsFlightAreasDroneLocationNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendEventsReplyFlightAreasDroneLocationSouthbound(const std::shared_ptr<EventsReplyFlightAreasDroneLocationSouthbound>& msg, const std::shared_ptr<EventsFlightAreasDroneLocationNorthbound>& original_msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    if (original_msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_ORIGINAL_MESSAGE;
    }
    BuildReplyMessage(msg, original_msg); // 根据原消息的uuid, 复制给回复消息
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendEventsReplyFlightAreasDroneLocationSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    return transport_ptr->Send(msg);
}

ListenIdCode CloudMessageManager::ObserveRequestsFlightAreasGetNorthbound(const std::function<void(const std::shared_ptr<RequestsFlightAreasGetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<RequestsFlightAreasGetNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveRequestsFlightAreasGetNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveRequestsFlightAreasGetNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<RequestsFlightAreasGetNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendRequestsReplyFlightAreasGetSouthbound(const std::shared_ptr<RequestsReplyFlightAreasGetSouthbound>& msg, const std::shared_ptr<RequestsFlightAreasGetNorthbound>& original_msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    if (original_msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_ORIGINAL_MESSAGE;
    }
    BuildReplyMessage(msg, original_msg); // 根据原消息的uuid, 复制给回复消息
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendRequestsReplyFlightAreasGetSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    return transport_ptr->Send(msg);
}

MeErrorCode CloudMessageManager::SendEventsFlightAreasSyncProgressSouthbound(const std::shared_ptr<EventsFlightAreasSyncProgressSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<EventsReplyFlightAreasSyncProgressNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendEventsFlightAreasSyncProgressSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendEventsFlightAreasSyncProgressSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendEventsFlightAreasSyncProgressSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendEventsFlightAreasSyncProgressSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<EventsReplyFlightAreasSyncProgressNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveEventsReplyFlightAreasSyncProgressNorthbound(const std::function<void(const std::shared_ptr<EventsReplyFlightAreasSyncProgressNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<EventsReplyFlightAreasSyncProgressNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveEventsReplyFlightAreasSyncProgressNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveEventsReplyFlightAreasSyncProgressNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<EventsReplyFlightAreasSyncProgressNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendServicesFlightAreasUpdateSouthbound(const std::shared_ptr<ServicesFlightAreasUpdateSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyFlightAreasUpdateNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendServicesFlightAreasUpdateSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendServicesFlightAreasUpdateSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendServicesFlightAreasUpdateSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendServicesFlightAreasUpdateSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyFlightAreasUpdateNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveServicesReplyFlightAreasUpdateNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyFlightAreasUpdateNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<ServicesReplyFlightAreasUpdateNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyFlightAreasUpdateNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyFlightAreasUpdateNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyFlightAreasUpdateNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendServicesFlightAuthorityGrabSouthbound(const std::shared_ptr<ServicesFlightAuthorityGrabSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyFlightAuthorityGrabNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendServicesFlightAuthorityGrabSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendServicesFlightAuthorityGrabSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendServicesFlightAuthorityGrabSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendServicesFlightAuthorityGrabSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyFlightAuthorityGrabNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveServicesReplyFlightAuthorityGrabNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyFlightAuthorityGrabNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<ServicesReplyFlightAuthorityGrabNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyFlightAuthorityGrabNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyFlightAuthorityGrabNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyFlightAuthorityGrabNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendServicesFlighttaskExecuteSouthbound(const std::shared_ptr<ServicesFlighttaskExecuteSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyFlighttaskExecuteNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendServicesFlighttaskExecuteSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendServicesFlighttaskExecuteSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendServicesFlighttaskExecuteSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendServicesFlighttaskExecuteSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyFlighttaskExecuteNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveServicesReplyFlighttaskExecuteNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyFlighttaskExecuteNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<ServicesReplyFlighttaskExecuteNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyFlighttaskExecuteNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyFlighttaskExecuteNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyFlighttaskExecuteNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendServicesFlighttaskPauseSouthbound(const std::shared_ptr<ServicesFlighttaskPauseSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyFlighttaskPauseNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendServicesFlighttaskPauseSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendServicesFlighttaskPauseSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendServicesFlighttaskPauseSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendServicesFlighttaskPauseSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyFlighttaskPauseNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveServicesReplyFlighttaskPauseNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyFlighttaskPauseNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<ServicesReplyFlighttaskPauseNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyFlighttaskPauseNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyFlighttaskPauseNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyFlighttaskPauseNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendServicesFlighttaskPrepareSouthbound(const std::shared_ptr<ServicesFlighttaskPrepareSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyFlighttaskPrepareNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendServicesFlighttaskPrepareSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendServicesFlighttaskPrepareSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendServicesFlighttaskPrepareSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendServicesFlighttaskPrepareSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyFlighttaskPrepareNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveServicesReplyFlighttaskPrepareNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyFlighttaskPrepareNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<ServicesReplyFlighttaskPrepareNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyFlighttaskPrepareNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyFlighttaskPrepareNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyFlighttaskPrepareNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

ListenIdCode CloudMessageManager::ObserveEventsFlighttaskProgressNorthbound(const std::function<void(const std::shared_ptr<EventsFlighttaskProgressNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<EventsFlighttaskProgressNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveEventsFlighttaskProgressNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveEventsFlighttaskProgressNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<EventsFlighttaskProgressNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendEventsReplyFlighttaskProgressSouthbound(const std::shared_ptr<EventsReplyFlighttaskProgressSouthbound>& msg, const std::shared_ptr<EventsFlighttaskProgressNorthbound>& original_msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    if (original_msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_ORIGINAL_MESSAGE;
    }
    BuildReplyMessage(msg, original_msg); // 根据原消息的uuid, 复制给回复消息
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendEventsReplyFlighttaskProgressSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    return transport_ptr->Send(msg);
}

ListenIdCode CloudMessageManager::ObserveRequestsFlighttaskProgressGetNorthbound(const std::function<void(const std::shared_ptr<RequestsFlighttaskProgressGetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<RequestsFlighttaskProgressGetNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveRequestsFlighttaskProgressGetNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveRequestsFlighttaskProgressGetNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<RequestsFlighttaskProgressGetNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendRequestsReplyFlighttaskProgressGetSouthbound(const std::shared_ptr<RequestsReplyFlighttaskProgressGetSouthbound>& msg, const std::shared_ptr<RequestsFlighttaskProgressGetNorthbound>& original_msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    if (original_msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_ORIGINAL_MESSAGE;
    }
    BuildReplyMessage(msg, original_msg); // 根据原消息的uuid, 复制给回复消息
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendRequestsReplyFlighttaskProgressGetSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    return transport_ptr->Send(msg);
}

MeErrorCode CloudMessageManager::SendEventsFlighttaskReadySouthbound(const std::shared_ptr<EventsFlighttaskReadySouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<EventsReplyFlighttaskReadyNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendEventsFlighttaskReadySouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendEventsFlighttaskReadySouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendEventsFlighttaskReadySouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendEventsFlighttaskReadySouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<EventsReplyFlighttaskReadyNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveEventsReplyFlighttaskReadyNorthbound(const std::function<void(const std::shared_ptr<EventsReplyFlighttaskReadyNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<EventsReplyFlighttaskReadyNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveEventsReplyFlighttaskReadyNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveEventsReplyFlighttaskReadyNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<EventsReplyFlighttaskReadyNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendServicesFlighttaskRecoverySouthbound(const std::shared_ptr<ServicesFlighttaskRecoverySouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyFlighttaskRecoveryNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendServicesFlighttaskRecoverySouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendServicesFlighttaskRecoverySouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendServicesFlighttaskRecoverySouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendServicesFlighttaskRecoverySouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyFlighttaskRecoveryNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveServicesReplyFlighttaskRecoveryNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyFlighttaskRecoveryNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<ServicesReplyFlighttaskRecoveryNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyFlighttaskRecoveryNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyFlighttaskRecoveryNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyFlighttaskRecoveryNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

ListenIdCode CloudMessageManager::ObserveRequestsFlighttaskResourceGetNorthbound(const std::function<void(const std::shared_ptr<RequestsFlighttaskResourceGetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<RequestsFlighttaskResourceGetNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveRequestsFlighttaskResourceGetNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveRequestsFlighttaskResourceGetNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<RequestsFlighttaskResourceGetNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendRequestsReplyFlighttaskResourceGetSouthbound(const std::shared_ptr<RequestsReplyFlighttaskResourceGetSouthbound>& msg, const std::shared_ptr<RequestsFlighttaskResourceGetNorthbound>& original_msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    if (original_msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_ORIGINAL_MESSAGE;
    }
    BuildReplyMessage(msg, original_msg); // 根据原消息的uuid, 复制给回复消息
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendRequestsReplyFlighttaskResourceGetSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    return transport_ptr->Send(msg);
}

MeErrorCode CloudMessageManager::SendServicesFlighttaskStopSouthbound(const std::shared_ptr<ServicesFlighttaskStopSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyFlighttaskStopNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendServicesFlighttaskStopSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendServicesFlighttaskStopSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendServicesFlighttaskStopSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendServicesFlighttaskStopSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyFlighttaskStopNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveServicesReplyFlighttaskStopNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyFlighttaskStopNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<ServicesReplyFlighttaskStopNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyFlighttaskStopNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyFlighttaskStopNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyFlighttaskStopNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendServicesFlighttaskUndoSouthbound(const std::shared_ptr<ServicesFlighttaskUndoSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyFlighttaskUndoNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendServicesFlighttaskUndoSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendServicesFlighttaskUndoSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendServicesFlighttaskUndoSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendServicesFlighttaskUndoSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyFlighttaskUndoNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveServicesReplyFlighttaskUndoNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyFlighttaskUndoNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<ServicesReplyFlighttaskUndoNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyFlighttaskUndoNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyFlighttaskUndoNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyFlighttaskUndoNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendServicesFlyToPointSouthbound(const std::shared_ptr<ServicesFlyToPointSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyFlyToPointNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendServicesFlyToPointSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendServicesFlyToPointSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendServicesFlyToPointSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendServicesFlyToPointSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyFlyToPointNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveServicesReplyFlyToPointNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyFlyToPointNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<ServicesReplyFlyToPointNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyFlyToPointNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyFlyToPointNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyFlyToPointNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

ListenIdCode CloudMessageManager::ObserveEventsFlyToPointProgressNorthbound(const std::function<void(const std::shared_ptr<EventsFlyToPointProgressNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<EventsFlyToPointProgressNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveEventsFlyToPointProgressNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveEventsFlyToPointProgressNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<EventsFlyToPointProgressNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendEventsReplyFlyToPointProgressSouthbound(const std::shared_ptr<EventsReplyFlyToPointProgressSouthbound>& msg, const std::shared_ptr<EventsFlyToPointProgressNorthbound>& original_msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    if (original_msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_ORIGINAL_MESSAGE;
    }
    BuildReplyMessage(msg, original_msg); // 根据原消息的uuid, 复制给回复消息
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendEventsReplyFlyToPointProgressSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    return transport_ptr->Send(msg);
}

MeErrorCode CloudMessageManager::SendServicesFlyToPointStopSouthbound(const std::shared_ptr<ServicesFlyToPointStopSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyFlyToPointStopNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendServicesFlyToPointStopSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendServicesFlyToPointStopSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendServicesFlyToPointStopSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendServicesFlyToPointStopSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyFlyToPointStopNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveServicesReplyFlyToPointStopNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyFlyToPointStopNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<ServicesReplyFlyToPointStopNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyFlyToPointStopNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyFlyToPointStopNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyFlyToPointStopNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendServicesFlyToPointUpdateSouthbound(const std::shared_ptr<ServicesFlyToPointUpdateSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyFlyToPointUpdateNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendServicesFlyToPointUpdateSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendServicesFlyToPointUpdateSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendServicesFlyToPointUpdateSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendServicesFlyToPointUpdateSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyFlyToPointUpdateNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveServicesReplyFlyToPointUpdateNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyFlyToPointUpdateNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<ServicesReplyFlyToPointUpdateNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyFlyToPointUpdateNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyFlyToPointUpdateNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyFlyToPointUpdateNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendEventsFlysafeDatabaseUpgradeSouthbound(const std::shared_ptr<EventsFlysafeDatabaseUpgradeSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<EventsReplyFlysafeDatabaseUpgradeNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendEventsFlysafeDatabaseUpgradeSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendEventsFlysafeDatabaseUpgradeSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendEventsFlysafeDatabaseUpgradeSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendEventsFlysafeDatabaseUpgradeSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<EventsReplyFlysafeDatabaseUpgradeNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveEventsReplyFlysafeDatabaseUpgradeNorthbound(const std::function<void(const std::shared_ptr<EventsReplyFlysafeDatabaseUpgradeNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<EventsReplyFlysafeDatabaseUpgradeNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveEventsReplyFlysafeDatabaseUpgradeNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveEventsReplyFlysafeDatabaseUpgradeNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<EventsReplyFlysafeDatabaseUpgradeNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendServicesFlysafeDatabaseUpgradeSouthbound(const std::shared_ptr<ServicesFlysafeDatabaseUpgradeSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyFlysafeDatabaseUpgradeNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendServicesFlysafeDatabaseUpgradeSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendServicesFlysafeDatabaseUpgradeSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendServicesFlysafeDatabaseUpgradeSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendServicesFlysafeDatabaseUpgradeSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyFlysafeDatabaseUpgradeNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveServicesReplyFlysafeDatabaseUpgradeNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyFlysafeDatabaseUpgradeNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<ServicesReplyFlysafeDatabaseUpgradeNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyFlysafeDatabaseUpgradeNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyFlysafeDatabaseUpgradeNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyFlysafeDatabaseUpgradeNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendDrcHeartBeatSouthbound(const std::shared_ptr<DrcHeartBeatSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<DrcReplyHeartBeatNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcHeartBeatSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendDrcHeartBeatSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] SendDrcHeartBeatSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendDrcHeartBeatSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyHeartBeatNorthbound>(message_v2);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveDrcReplyHeartBeatNorthbound(const std::function<void(const std::shared_ptr<DrcReplyHeartBeatNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcReplyHeartBeatNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyHeartBeatNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcReplyHeartBeatNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcReplyHeartBeatNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

ListenIdCode CloudMessageManager::ObserveEventsHmsNorthbound(const std::function<void(const std::shared_ptr<EventsHmsNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<EventsHmsNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message) -> void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveEventsHmsNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveEventsHmsNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<EventsHmsNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

ListenIdCode CloudMessageManager::ObserveDrcHsiInfoPushNorthbound(const std::function<void(const std::shared_ptr<DrcHsiInfoPushNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcHsiInfoPushNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message) -> void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcHsiInfoPushNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcHsiInfoPushNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcHsiInfoPushNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

ListenIdCode CloudMessageManager::ObserveRequestsInFlightTaskRequestNorthbound(const std::function<void(const std::shared_ptr<RequestsInFlightTaskRequestNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<RequestsInFlightTaskRequestNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveRequestsInFlightTaskRequestNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveRequestsInFlightTaskRequestNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<RequestsInFlightTaskRequestNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendRequestsReplyInFlightTaskRequestSouthbound(const std::shared_ptr<RequestsReplyInFlightTaskRequestSouthbound>& msg, const std::shared_ptr<RequestsInFlightTaskRequestNorthbound>& original_msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    if (original_msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_ORIGINAL_MESSAGE;
    }
    BuildReplyMessage(msg, original_msg); // 根据原消息的uuid, 复制给回复消息
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendRequestsReplyInFlightTaskRequestSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    return transport_ptr->Send(msg);
}

MeErrorCode CloudMessageManager::SendServicesInFlightWaylineCancelSouthbound(const std::shared_ptr<ServicesInFlightWaylineCancelSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyInFlightWaylineCancelNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendServicesInFlightWaylineCancelSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendServicesInFlightWaylineCancelSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendServicesInFlightWaylineCancelSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendServicesInFlightWaylineCancelSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyInFlightWaylineCancelNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveServicesReplyInFlightWaylineCancelNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyInFlightWaylineCancelNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<ServicesReplyInFlightWaylineCancelNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyInFlightWaylineCancelNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyInFlightWaylineCancelNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyInFlightWaylineCancelNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendServicesInFlightWaylineDeliverSouthbound(const std::shared_ptr<ServicesInFlightWaylineDeliverSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyInFlightWaylineDeliverNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendServicesInFlightWaylineDeliverSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendServicesInFlightWaylineDeliverSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendServicesInFlightWaylineDeliverSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendServicesInFlightWaylineDeliverSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyInFlightWaylineDeliverNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveServicesReplyInFlightWaylineDeliverNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyInFlightWaylineDeliverNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<ServicesReplyInFlightWaylineDeliverNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyInFlightWaylineDeliverNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyInFlightWaylineDeliverNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyInFlightWaylineDeliverNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

ListenIdCode CloudMessageManager::ObserveEventsInFlightWaylineProgressNorthbound(const std::function<void(const std::shared_ptr<EventsInFlightWaylineProgressNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<EventsInFlightWaylineProgressNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message) -> void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveEventsInFlightWaylineProgressNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveEventsInFlightWaylineProgressNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<EventsInFlightWaylineProgressNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendServicesInFlightWaylineRecoverSouthbound(const std::shared_ptr<ServicesInFlightWaylineRecoverSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyInFlightWaylineRecoverNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendServicesInFlightWaylineRecoverSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendServicesInFlightWaylineRecoverSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendServicesInFlightWaylineRecoverSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendServicesInFlightWaylineRecoverSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyInFlightWaylineRecoverNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveServicesReplyInFlightWaylineRecoverNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyInFlightWaylineRecoverNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<ServicesReplyInFlightWaylineRecoverNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyInFlightWaylineRecoverNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyInFlightWaylineRecoverNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyInFlightWaylineRecoverNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendServicesInFlightWaylineStopSouthbound(const std::shared_ptr<ServicesInFlightWaylineStopSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyInFlightWaylineStopNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendServicesInFlightWaylineStopSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendServicesInFlightWaylineStopSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendServicesInFlightWaylineStopSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendServicesInFlightWaylineStopSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyInFlightWaylineStopNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveServicesReplyInFlightWaylineStopNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyInFlightWaylineStopNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<ServicesReplyInFlightWaylineStopNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyInFlightWaylineStopNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyInFlightWaylineStopNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyInFlightWaylineStopNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendServicesLiveLensChangeSouthbound(const std::shared_ptr<ServicesLiveLensChangeSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyLiveLensChangeNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendServicesLiveLensChangeSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendServicesLiveLensChangeSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendServicesLiveLensChangeSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendServicesLiveLensChangeSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyLiveLensChangeNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveServicesReplyLiveLensChangeNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyLiveLensChangeNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<ServicesReplyLiveLensChangeNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyLiveLensChangeNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyLiveLensChangeNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyLiveLensChangeNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendServicesLiveSetQualitySouthbound(const std::shared_ptr<ServicesLiveSetQualitySouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyLiveSetQualityNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendServicesLiveSetQualitySouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendServicesLiveSetQualitySouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendServicesLiveSetQualitySouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendServicesLiveSetQualitySouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyLiveSetQualityNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveServicesReplyLiveSetQualityNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyLiveSetQualityNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<ServicesReplyLiveSetQualityNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyLiveSetQualityNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyLiveSetQualityNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyLiveSetQualityNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendServicesLiveStartPushSouthbound(const std::shared_ptr<ServicesLiveStartPushSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyLiveStartPushNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendServicesLiveStartPushSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendServicesLiveStartPushSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendServicesLiveStartPushSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendServicesLiveStartPushSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyLiveStartPushNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveServicesReplyLiveStartPushNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyLiveStartPushNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<ServicesReplyLiveStartPushNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyLiveStartPushNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyLiveStartPushNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyLiveStartPushNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendServicesLiveStopPushSouthbound(const std::shared_ptr<ServicesLiveStopPushSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyLiveStopPushNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendServicesLiveStopPushSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendServicesLiveStopPushSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendServicesLiveStopPushSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendServicesLiveStopPushSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyLiveStopPushNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveServicesReplyLiveStopPushNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyLiveStopPushNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<ServicesReplyLiveStopPushNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyLiveStopPushNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyLiveStopPushNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyLiveStopPushNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendServicesLoadAiBoxAppSouthbound(const std::shared_ptr<ServicesLoadAiBoxAppSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyLoadAiBoxAppNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendServicesLoadAiBoxAppSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendServicesLoadAiBoxAppSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendServicesLoadAiBoxAppSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendServicesLoadAiBoxAppSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyLoadAiBoxAppNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveServicesReplyLoadAiBoxAppNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyLoadAiBoxAppNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<ServicesReplyLoadAiBoxAppNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyLoadAiBoxAppNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyLoadAiBoxAppNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyLoadAiBoxAppNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendEventsLoadAiBoxAppProgressSouthbound(const std::shared_ptr<EventsLoadAiBoxAppProgressSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<EventsReplyLoadAiBoxAppProgressNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendEventsLoadAiBoxAppProgressSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendEventsLoadAiBoxAppProgressSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendEventsLoadAiBoxAppProgressSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendEventsLoadAiBoxAppProgressSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<EventsReplyLoadAiBoxAppProgressNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveEventsReplyLoadAiBoxAppProgressNorthbound(const std::function<void(const std::shared_ptr<EventsReplyLoadAiBoxAppProgressNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<EventsReplyLoadAiBoxAppProgressNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveEventsReplyLoadAiBoxAppProgressNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveEventsReplyLoadAiBoxAppProgressNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<EventsReplyLoadAiBoxAppProgressNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendServicesLoadAiModelSouthbound(const std::shared_ptr<ServicesLoadAiModelSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyLoadAiModelNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendServicesLoadAiModelSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendServicesLoadAiModelSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendServicesLoadAiModelSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendServicesLoadAiModelSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyLoadAiModelNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveServicesReplyLoadAiModelNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyLoadAiModelNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<ServicesReplyLoadAiModelNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyLoadAiModelNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyLoadAiModelNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyLoadAiModelNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendEventsLoadAiModelProgressSouthbound(const std::shared_ptr<EventsLoadAiModelProgressSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<EventsReplyLoadAiModelProgressNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendEventsLoadAiModelProgressSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendEventsLoadAiModelProgressSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendEventsLoadAiModelProgressSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendEventsLoadAiModelProgressSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<EventsReplyLoadAiModelProgressNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveEventsReplyLoadAiModelProgressNorthbound(const std::function<void(const std::shared_ptr<EventsReplyLoadAiModelProgressNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<EventsReplyLoadAiModelProgressNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveEventsReplyLoadAiModelProgressNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveEventsReplyLoadAiModelProgressNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<EventsReplyLoadAiModelProgressNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendServicesMeasureSdrSignalEnterSouthbound(const std::shared_ptr<ServicesMeasureSdrSignalEnterSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyMeasureSdrSignalEnterNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendServicesMeasureSdrSignalEnterSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendServicesMeasureSdrSignalEnterSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendServicesMeasureSdrSignalEnterSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendServicesMeasureSdrSignalEnterSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyMeasureSdrSignalEnterNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveServicesReplyMeasureSdrSignalEnterNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyMeasureSdrSignalEnterNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<ServicesReplyMeasureSdrSignalEnterNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyMeasureSdrSignalEnterNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyMeasureSdrSignalEnterNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyMeasureSdrSignalEnterNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendEventsObstacleAvoidanceNotifySouthbound(const std::shared_ptr<EventsObstacleAvoidanceNotifySouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<EventsReplyObstacleAvoidanceNotifyNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendEventsObstacleAvoidanceNotifySouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendEventsObstacleAvoidanceNotifySouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendEventsObstacleAvoidanceNotifySouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendEventsObstacleAvoidanceNotifySouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<EventsReplyObstacleAvoidanceNotifyNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveEventsReplyObstacleAvoidanceNotifyNorthbound(const std::function<void(const std::shared_ptr<EventsReplyObstacleAvoidanceNotifyNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<EventsReplyObstacleAvoidanceNotifyNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveEventsReplyObstacleAvoidanceNotifyNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveEventsReplyObstacleAvoidanceNotifyNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<EventsReplyObstacleAvoidanceNotifyNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

ListenIdCode CloudMessageManager::ObserveRequestsOfflineMapGetNorthbound(const std::function<void(const std::shared_ptr<RequestsOfflineMapGetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<RequestsOfflineMapGetNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveRequestsOfflineMapGetNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveRequestsOfflineMapGetNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<RequestsOfflineMapGetNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendRequestsReplyOfflineMapGetSouthbound(const std::shared_ptr<RequestsReplyOfflineMapGetSouthbound>& msg, const std::shared_ptr<RequestsOfflineMapGetNorthbound>& original_msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    if (original_msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_ORIGINAL_MESSAGE;
    }
    BuildReplyMessage(msg, original_msg); // 根据原消息的uuid, 复制给回复消息
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendRequestsReplyOfflineMapGetSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    return transport_ptr->Send(msg);
}

ListenIdCode CloudMessageManager::ObserveRequestsOfflineMapGetV2Northbound(const std::function<void(const std::shared_ptr<RequestsOfflineMapGetV2Northbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<RequestsOfflineMapGetV2Northbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveRequestsOfflineMapGetV2Northbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveRequestsOfflineMapGetV2Northbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<RequestsOfflineMapGetV2Northbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendRequestsReplyOfflineMapGetV2Southbound(const std::shared_ptr<RequestsReplyOfflineMapGetV2Southbound>& msg, const std::shared_ptr<RequestsOfflineMapGetV2Northbound>& original_msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    if (original_msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_ORIGINAL_MESSAGE;
    }
    BuildReplyMessage(msg, original_msg); // 根据原消息的uuid, 复制给回复消息
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendRequestsReplyOfflineMapGetV2Southbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    return transport_ptr->Send(msg);
}

MeErrorCode CloudMessageManager::SendEventsOfflineMapSyncProgressSouthbound(const std::shared_ptr<EventsOfflineMapSyncProgressSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<EventsReplyOfflineMapSyncProgressNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendEventsOfflineMapSyncProgressSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendEventsOfflineMapSyncProgressSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendEventsOfflineMapSyncProgressSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendEventsOfflineMapSyncProgressSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<EventsReplyOfflineMapSyncProgressNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveEventsReplyOfflineMapSyncProgressNorthbound(const std::function<void(const std::shared_ptr<EventsReplyOfflineMapSyncProgressNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<EventsReplyOfflineMapSyncProgressNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveEventsReplyOfflineMapSyncProgressNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveEventsReplyOfflineMapSyncProgressNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<EventsReplyOfflineMapSyncProgressNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendServicesOfflineMapUpdateSouthbound(const std::shared_ptr<ServicesOfflineMapUpdateSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyOfflineMapUpdateNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendServicesOfflineMapUpdateSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendServicesOfflineMapUpdateSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendServicesOfflineMapUpdateSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendServicesOfflineMapUpdateSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyOfflineMapUpdateNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveServicesReplyOfflineMapUpdateNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyOfflineMapUpdateNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<ServicesReplyOfflineMapUpdateNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyOfflineMapUpdateNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyOfflineMapUpdateNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyOfflineMapUpdateNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendServicesOpenParachuteSouthbound(const std::shared_ptr<ServicesOpenParachuteSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyOpenParachuteNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendServicesOpenParachuteSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendServicesOpenParachuteSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendServicesOpenParachuteSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendServicesOpenParachuteSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyOpenParachuteNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveServicesReplyOpenParachuteNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyOpenParachuteNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<ServicesReplyOpenParachuteNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyOpenParachuteNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyOpenParachuteNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyOpenParachuteNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

ListenIdCode CloudMessageManager::ObserveDrcOsdInfoPushNorthbound(const std::function<void(const std::shared_ptr<DrcOsdInfoPushNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<DrcOsdInfoPushNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message) -> void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V2) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcOsdInfoPushNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V2, message->GetMessageProtocolType());
            return;
        }
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        if (message_v2 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveDrcOsdInfoPushNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<DrcOsdInfoPushNorthbound>(message_v2);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendServicesPayloadAuthorityGrabSouthbound(const std::shared_ptr<ServicesPayloadAuthorityGrabSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyPayloadAuthorityGrabNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendServicesPayloadAuthorityGrabSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendServicesPayloadAuthorityGrabSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendServicesPayloadAuthorityGrabSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendServicesPayloadAuthorityGrabSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyPayloadAuthorityGrabNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveServicesReplyPayloadAuthorityGrabNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyPayloadAuthorityGrabNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<ServicesReplyPayloadAuthorityGrabNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyPayloadAuthorityGrabNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyPayloadAuthorityGrabNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyPayloadAuthorityGrabNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendServicesPhotoStorageSetSouthbound(const std::shared_ptr<ServicesPhotoStorageSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyPhotoStorageSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendServicesPhotoStorageSetSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendServicesPhotoStorageSetSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendServicesPhotoStorageSetSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendServicesPhotoStorageSetSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyPhotoStorageSetNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveServicesReplyPhotoStorageSetNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyPhotoStorageSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<ServicesReplyPhotoStorageSetNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyPhotoStorageSetNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyPhotoStorageSetNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyPhotoStorageSetNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

ListenIdCode CloudMessageManager::ObserveEventsPoiStatusNotifyNorthbound(const std::function<void(const std::shared_ptr<EventsPoiStatusNotifyNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<EventsPoiStatusNotifyNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message) -> void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveEventsPoiStatusNotifyNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveEventsPoiStatusNotifyNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<EventsPoiStatusNotifyNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendServicesRelayConnectSouthbound(const std::shared_ptr<ServicesRelayConnectSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyRelayConnectNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendServicesRelayConnectSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendServicesRelayConnectSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendServicesRelayConnectSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendServicesRelayConnectSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyRelayConnectNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveServicesReplyRelayConnectNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyRelayConnectNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<ServicesReplyRelayConnectNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyRelayConnectNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyRelayConnectNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyRelayConnectNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendServicesRelayDisconnectSouthbound(const std::shared_ptr<ServicesRelayDisconnectSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyRelayDisconnectNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendServicesRelayDisconnectSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendServicesRelayDisconnectSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendServicesRelayDisconnectSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendServicesRelayDisconnectSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyRelayDisconnectNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveServicesReplyRelayDisconnectNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyRelayDisconnectNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<ServicesReplyRelayDisconnectNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyRelayDisconnectNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyRelayDisconnectNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyRelayDisconnectNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendEventsRelayNonPairAttachResultSouthbound(const std::shared_ptr<EventsRelayNonPairAttachResultSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<EventsReplyRelayNonPairAttachResultNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendEventsRelayNonPairAttachResultSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendEventsRelayNonPairAttachResultSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendEventsRelayNonPairAttachResultSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendEventsRelayNonPairAttachResultSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<EventsReplyRelayNonPairAttachResultNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveEventsReplyRelayNonPairAttachResultNorthbound(const std::function<void(const std::shared_ptr<EventsReplyRelayNonPairAttachResultNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<EventsReplyRelayNonPairAttachResultNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveEventsReplyRelayNonPairAttachResultNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveEventsReplyRelayNonPairAttachResultNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<EventsReplyRelayNonPairAttachResultNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendServicesRelayPasswdHashSouthbound(const std::shared_ptr<ServicesRelayPasswdHashSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyRelayPasswdHashNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendServicesRelayPasswdHashSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendServicesRelayPasswdHashSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendServicesRelayPasswdHashSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendServicesRelayPasswdHashSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyRelayPasswdHashNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveServicesReplyRelayPasswdHashNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyRelayPasswdHashNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<ServicesReplyRelayPasswdHashNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyRelayPasswdHashNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyRelayPasswdHashNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyRelayPasswdHashNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendServicesRelayScanSouthbound(const std::shared_ptr<ServicesRelayScanSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyRelayScanNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendServicesRelayScanSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendServicesRelayScanSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendServicesRelayScanSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendServicesRelayScanSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyRelayScanNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveServicesReplyRelayScanNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyRelayScanNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<ServicesReplyRelayScanNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyRelayScanNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyRelayScanNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyRelayScanNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendEventsRelayScanResultSouthbound(const std::shared_ptr<EventsRelayScanResultSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<EventsReplyRelayScanResultNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendEventsRelayScanResultSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendEventsRelayScanResultSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendEventsRelayScanResultSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendEventsRelayScanResultSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<EventsReplyRelayScanResultNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveEventsReplyRelayScanResultNorthbound(const std::function<void(const std::shared_ptr<EventsReplyRelayScanResultNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<EventsReplyRelayScanResultNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveEventsReplyRelayScanResultNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveEventsReplyRelayScanResultNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<EventsReplyRelayScanResultNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendEventsReleaseTerminalControlAreaSouthbound(const std::shared_ptr<EventsReleaseTerminalControlAreaSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<EventsReplyReleaseTerminalControlAreaNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendEventsReleaseTerminalControlAreaSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendEventsReleaseTerminalControlAreaSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendEventsReleaseTerminalControlAreaSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendEventsReleaseTerminalControlAreaSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<EventsReplyReleaseTerminalControlAreaNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveEventsReplyReleaseTerminalControlAreaNorthbound(const std::function<void(const std::shared_ptr<EventsReplyReleaseTerminalControlAreaNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<EventsReplyReleaseTerminalControlAreaNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveEventsReplyReleaseTerminalControlAreaNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveEventsReplyReleaseTerminalControlAreaNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<EventsReplyReleaseTerminalControlAreaNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

ListenIdCode CloudMessageManager::ObserveRequestsRequestLandingPermissionNorthbound(const std::function<void(const std::shared_ptr<RequestsRequestLandingPermissionNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<RequestsRequestLandingPermissionNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveRequestsRequestLandingPermissionNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveRequestsRequestLandingPermissionNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<RequestsRequestLandingPermissionNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendRequestsReplyRequestLandingPermissionSouthbound(const std::shared_ptr<RequestsReplyRequestLandingPermissionSouthbound>& msg, const std::shared_ptr<RequestsRequestLandingPermissionNorthbound>& original_msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    if (original_msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_ORIGINAL_MESSAGE;
    }
    BuildReplyMessage(msg, original_msg); // 根据原消息的uuid, 复制给回复消息
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendRequestsReplyRequestLandingPermissionSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    return transport_ptr->Send(msg);
}

ListenIdCode CloudMessageManager::ObserveRequestsRequestsAiModelUrlNorthbound(const std::function<void(const std::shared_ptr<RequestsRequestsAiModelUrlNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<RequestsRequestsAiModelUrlNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveRequestsRequestsAiModelUrlNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveRequestsRequestsAiModelUrlNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<RequestsRequestsAiModelUrlNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendRequestsReplyRequestsAiModelUrlSouthbound(const std::shared_ptr<RequestsReplyRequestsAiModelUrlSouthbound>& msg, const std::shared_ptr<RequestsRequestsAiModelUrlNorthbound>& original_msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    if (original_msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_ORIGINAL_MESSAGE;
    }
    BuildReplyMessage(msg, original_msg); // 根据原消息的uuid, 复制给回复消息
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendRequestsReplyRequestsAiModelUrlSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    return transport_ptr->Send(msg);
}

MeErrorCode CloudMessageManager::SendServicesReturnHomeSouthbound(const std::shared_ptr<ServicesReturnHomeSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyReturnHomeNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendServicesReturnHomeSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendServicesReturnHomeSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendServicesReturnHomeSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendServicesReturnHomeSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyReturnHomeNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveServicesReplyReturnHomeNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyReturnHomeNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<ServicesReplyReturnHomeNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyReturnHomeNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyReturnHomeNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyReturnHomeNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendServicesReturnHomeCancelSouthbound(const std::shared_ptr<ServicesReturnHomeCancelSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyReturnHomeCancelNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendServicesReturnHomeCancelSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendServicesReturnHomeCancelSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendServicesReturnHomeCancelSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendServicesReturnHomeCancelSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyReturnHomeCancelNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveServicesReplyReturnHomeCancelNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyReturnHomeCancelNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<ServicesReplyReturnHomeCancelNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyReturnHomeCancelNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyReturnHomeCancelNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyReturnHomeCancelNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

ListenIdCode CloudMessageManager::ObserveEventsReturnHomeInfoNorthbound(const std::function<void(const std::shared_ptr<EventsReturnHomeInfoNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<EventsReturnHomeInfoNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveEventsReturnHomeInfoNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveEventsReturnHomeInfoNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<EventsReturnHomeInfoNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendEventsReplyReturnHomeInfoSouthbound(const std::shared_ptr<EventsReplyReturnHomeInfoSouthbound>& msg, const std::shared_ptr<EventsReturnHomeInfoNorthbound>& original_msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    if (original_msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_ORIGINAL_MESSAGE;
    }
    BuildReplyMessage(msg, original_msg); // 根据原消息的uuid, 复制给回复消息
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendEventsReplyReturnHomeInfoSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    return transport_ptr->Send(msg);
}

MeErrorCode CloudMessageManager::SendServicesReturnSpecificHomeSouthbound(const std::shared_ptr<ServicesReturnSpecificHomeSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyReturnSpecificHomeNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendServicesReturnSpecificHomeSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendServicesReturnSpecificHomeSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendServicesReturnSpecificHomeSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendServicesReturnSpecificHomeSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyReturnSpecificHomeNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveServicesReplyReturnSpecificHomeNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyReturnSpecificHomeNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<ServicesReplyReturnSpecificHomeNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyReturnSpecificHomeNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyReturnSpecificHomeNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyReturnSpecificHomeNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendServicesRidAvoidanceEnableSouthbound(const std::shared_ptr<ServicesRidAvoidanceEnableSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyRidAvoidanceEnableNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendServicesRidAvoidanceEnableSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendServicesRidAvoidanceEnableSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendServicesRidAvoidanceEnableSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendServicesRidAvoidanceEnableSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyRidAvoidanceEnableNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveServicesReplyRidAvoidanceEnableNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyRidAvoidanceEnableNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<ServicesReplyRidAvoidanceEnableNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyRidAvoidanceEnableNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyRidAvoidanceEnableNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyRidAvoidanceEnableNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

ListenIdCode CloudMessageManager::ObserveEventsRidWarningNorthbound(const std::function<void(const std::shared_ptr<EventsRidWarningNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<EventsRidWarningNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveEventsRidWarningNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveEventsRidWarningNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<EventsRidWarningNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendEventsReplyRidWarningSouthbound(const std::shared_ptr<EventsReplyRidWarningSouthbound>& msg, const std::shared_ptr<EventsRidWarningNorthbound>& original_msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    if (original_msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_ORIGINAL_MESSAGE;
    }
    BuildReplyMessage(msg, original_msg); // 根据原消息的uuid, 复制给回复消息
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendEventsReplyRidWarningSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    return transport_ptr->Send(msg);
}

MeErrorCode CloudMessageManager::SendServicesRtAgentChannelJoinSouthbound(const std::shared_ptr<ServicesRtAgentChannelJoinSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyRtAgentChannelJoinNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendServicesRtAgentChannelJoinSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendServicesRtAgentChannelJoinSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendServicesRtAgentChannelJoinSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendServicesRtAgentChannelJoinSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyRtAgentChannelJoinNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveServicesReplyRtAgentChannelJoinNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyRtAgentChannelJoinNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<ServicesReplyRtAgentChannelJoinNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyRtAgentChannelJoinNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyRtAgentChannelJoinNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyRtAgentChannelJoinNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

ListenIdCode CloudMessageManager::ObserveRequestsRtAgentChannelOpenNorthbound(const std::function<void(const std::shared_ptr<RequestsRtAgentChannelOpenNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<RequestsRtAgentChannelOpenNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveRequestsRtAgentChannelOpenNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveRequestsRtAgentChannelOpenNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<RequestsRtAgentChannelOpenNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendRequestsReplyRtAgentChannelOpenSouthbound(const std::shared_ptr<RequestsReplyRtAgentChannelOpenSouthbound>& msg, const std::shared_ptr<RequestsRtAgentChannelOpenNorthbound>& original_msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    if (original_msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_ORIGINAL_MESSAGE;
    }
    BuildReplyMessage(msg, original_msg); // 根据原消息的uuid, 复制给回复消息
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendRequestsReplyRtAgentChannelOpenSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    return transport_ptr->Send(msg);
}

ListenIdCode CloudMessageManager::ObserveRequestsRtAgentChannelStatusGetNorthbound(const std::function<void(const std::shared_ptr<RequestsRtAgentChannelStatusGetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<RequestsRtAgentChannelStatusGetNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveRequestsRtAgentChannelStatusGetNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveRequestsRtAgentChannelStatusGetNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<RequestsRtAgentChannelStatusGetNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendRequestsReplyRtAgentChannelStatusGetSouthbound(const std::shared_ptr<RequestsReplyRtAgentChannelStatusGetSouthbound>& msg, const std::shared_ptr<RequestsRtAgentChannelStatusGetNorthbound>& original_msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    if (original_msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_ORIGINAL_MESSAGE;
    }
    BuildReplyMessage(msg, original_msg); // 根据原消息的uuid, 复制给回复消息
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendRequestsReplyRtAgentChannelStatusGetSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    return transport_ptr->Send(msg);
}

MeErrorCode CloudMessageManager::SendServicesRtkCalibrationSouthbound(const std::shared_ptr<ServicesRtkCalibrationSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyRtkCalibrationNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendServicesRtkCalibrationSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendServicesRtkCalibrationSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendServicesRtkCalibrationSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendServicesRtkCalibrationSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyRtkCalibrationNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveServicesReplyRtkCalibrationNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyRtkCalibrationNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<ServicesReplyRtkCalibrationNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyRtkCalibrationNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyRtkCalibrationNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyRtkCalibrationNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendEventsRtkCalibrationSouthbound(const std::shared_ptr<EventsRtkCalibrationSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<EventsReplyRtkCalibrationNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendEventsRtkCalibrationSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendEventsRtkCalibrationSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendEventsRtkCalibrationSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendEventsRtkCalibrationSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<EventsReplyRtkCalibrationNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveEventsReplyRtkCalibrationNorthbound(const std::function<void(const std::shared_ptr<EventsReplyRtkCalibrationNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<EventsReplyRtkCalibrationNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveEventsReplyRtkCalibrationNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveEventsReplyRtkCalibrationNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<EventsReplyRtkCalibrationNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendEventsSdrDebugInfoSouthbound(const std::shared_ptr<EventsSdrDebugInfoSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<EventsReplySdrDebugInfoNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendEventsSdrDebugInfoSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendEventsSdrDebugInfoSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendEventsSdrDebugInfoSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendEventsSdrDebugInfoSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<EventsReplySdrDebugInfoNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveEventsReplySdrDebugInfoNorthbound(const std::function<void(const std::shared_ptr<EventsReplySdrDebugInfoNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<EventsReplySdrDebugInfoNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveEventsReplySdrDebugInfoNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveEventsReplySdrDebugInfoNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<EventsReplySdrDebugInfoNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendServicesSdrDebugSwitchSouthbound(const std::shared_ptr<ServicesSdrDebugSwitchSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplySdrDebugSwitchNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendServicesSdrDebugSwitchSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendServicesSdrDebugSwitchSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendServicesSdrDebugSwitchSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendServicesSdrDebugSwitchSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplySdrDebugSwitchNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveServicesReplySdrDebugSwitchNorthbound(const std::function<void(const std::shared_ptr<ServicesReplySdrDebugSwitchNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<ServicesReplySdrDebugSwitchNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplySdrDebugSwitchNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplySdrDebugSwitchNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplySdrDebugSwitchNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendServicesSdrLinkSetConfigSouthbound(const std::shared_ptr<ServicesSdrLinkSetConfigSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplySdrLinkSetConfigNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendServicesSdrLinkSetConfigSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendServicesSdrLinkSetConfigSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendServicesSdrLinkSetConfigSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendServicesSdrLinkSetConfigSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplySdrLinkSetConfigNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveServicesReplySdrLinkSetConfigNorthbound(const std::function<void(const std::shared_ptr<ServicesReplySdrLinkSetConfigNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<ServicesReplySdrLinkSetConfigNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplySdrLinkSetConfigNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplySdrLinkSetConfigNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplySdrLinkSetConfigNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendServicesSdrWorkmodeSwitchSouthbound(const std::shared_ptr<ServicesSdrWorkmodeSwitchSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplySdrWorkmodeSwitchNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendServicesSdrWorkmodeSwitchSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendServicesSdrWorkmodeSwitchSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendServicesSdrWorkmodeSwitchSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendServicesSdrWorkmodeSwitchSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplySdrWorkmodeSwitchNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveServicesReplySdrWorkmodeSwitchNorthbound(const std::function<void(const std::shared_ptr<ServicesReplySdrWorkmodeSwitchNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<ServicesReplySdrWorkmodeSwitchNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplySdrWorkmodeSwitchNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplySdrWorkmodeSwitchNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplySdrWorkmodeSwitchNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendServicesSelfDiagnosticSouthbound(const std::shared_ptr<ServicesSelfDiagnosticSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplySelfDiagnosticNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendServicesSelfDiagnosticSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendServicesSelfDiagnosticSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendServicesSelfDiagnosticSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendServicesSelfDiagnosticSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplySelfDiagnosticNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveServicesReplySelfDiagnosticNorthbound(const std::function<void(const std::shared_ptr<ServicesReplySelfDiagnosticNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<ServicesReplySelfDiagnosticNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplySelfDiagnosticNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplySelfDiagnosticNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplySelfDiagnosticNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendEventsSelfDiagnosticSouthbound(const std::shared_ptr<EventsSelfDiagnosticSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<EventsReplySelfDiagnosticNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendEventsSelfDiagnosticSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendEventsSelfDiagnosticSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendEventsSelfDiagnosticSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendEventsSelfDiagnosticSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<EventsReplySelfDiagnosticNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveEventsReplySelfDiagnosticNorthbound(const std::function<void(const std::shared_ptr<EventsReplySelfDiagnosticNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<EventsReplySelfDiagnosticNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveEventsReplySelfDiagnosticNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveEventsReplySelfDiagnosticNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<EventsReplySelfDiagnosticNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendEventsSelfDiagnosticCancelSouthbound(const std::shared_ptr<EventsSelfDiagnosticCancelSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<EventsReplySelfDiagnosticCancelNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendEventsSelfDiagnosticCancelSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendEventsSelfDiagnosticCancelSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendEventsSelfDiagnosticCancelSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendEventsSelfDiagnosticCancelSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<EventsReplySelfDiagnosticCancelNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveEventsReplySelfDiagnosticCancelNorthbound(const std::function<void(const std::shared_ptr<EventsReplySelfDiagnosticCancelNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<EventsReplySelfDiagnosticCancelNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveEventsReplySelfDiagnosticCancelNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveEventsReplySelfDiagnosticCancelNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<EventsReplySelfDiagnosticCancelNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendServicesSetRelayOccupiedStatusSouthbound(const std::shared_ptr<ServicesSetRelayOccupiedStatusSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplySetRelayOccupiedStatusNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendServicesSetRelayOccupiedStatusSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendServicesSetRelayOccupiedStatusSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendServicesSetRelayOccupiedStatusSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendServicesSetRelayOccupiedStatusSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplySetRelayOccupiedStatusNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveServicesReplySetRelayOccupiedStatusNorthbound(const std::function<void(const std::shared_ptr<ServicesReplySetRelayOccupiedStatusNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<ServicesReplySetRelayOccupiedStatusNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplySetRelayOccupiedStatusNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplySetRelayOccupiedStatusNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplySetRelayOccupiedStatusNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendServicesSimSlotSwitchSouthbound(const std::shared_ptr<ServicesSimSlotSwitchSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplySimSlotSwitchNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendServicesSimSlotSwitchSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendServicesSimSlotSwitchSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendServicesSimSlotSwitchSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendServicesSimSlotSwitchSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplySimSlotSwitchNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveServicesReplySimSlotSwitchNorthbound(const std::function<void(const std::shared_ptr<ServicesReplySimSlotSwitchNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<ServicesReplySimSlotSwitchNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplySimSlotSwitchNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplySimSlotSwitchNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplySimSlotSwitchNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendServicesSpotlightZoomCloseSouthbound(const std::shared_ptr<ServicesSpotlightZoomCloseSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplySpotlightZoomCloseNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendServicesSpotlightZoomCloseSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendServicesSpotlightZoomCloseSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendServicesSpotlightZoomCloseSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendServicesSpotlightZoomCloseSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplySpotlightZoomCloseNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveServicesReplySpotlightZoomCloseNorthbound(const std::function<void(const std::shared_ptr<ServicesReplySpotlightZoomCloseNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<ServicesReplySpotlightZoomCloseNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplySpotlightZoomCloseNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplySpotlightZoomCloseNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplySpotlightZoomCloseNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendServicesSpotlightZoomOpenSouthbound(const std::shared_ptr<ServicesSpotlightZoomOpenSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplySpotlightZoomOpenNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendServicesSpotlightZoomOpenSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendServicesSpotlightZoomOpenSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendServicesSpotlightZoomOpenSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendServicesSpotlightZoomOpenSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplySpotlightZoomOpenNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveServicesReplySpotlightZoomOpenNorthbound(const std::function<void(const std::shared_ptr<ServicesReplySpotlightZoomOpenNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<ServicesReplySpotlightZoomOpenNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplySpotlightZoomOpenNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplySpotlightZoomOpenNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplySpotlightZoomOpenNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

ListenIdCode CloudMessageManager::ObserveStatusStaticTopoNorthbound(const std::function<void(const std::shared_ptr<StatusStaticTopoNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<StatusStaticTopoNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message) -> void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveStatusStaticTopoNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveStatusStaticTopoNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<StatusStaticTopoNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

ListenIdCode CloudMessageManager::ObserveEventsStationMeasureReportNorthbound(const std::function<void(const std::shared_ptr<EventsStationMeasureReportNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<EventsStationMeasureReportNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveEventsStationMeasureReportNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveEventsStationMeasureReportNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<EventsStationMeasureReportNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendEventsReplyStationMeasureReportSouthbound(const std::shared_ptr<EventsReplyStationMeasureReportSouthbound>& msg, const std::shared_ptr<EventsStationMeasureReportNorthbound>& original_msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    if (original_msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_ORIGINAL_MESSAGE;
    }
    BuildReplyMessage(msg, original_msg); // 根据原消息的uuid, 复制给回复消息
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendEventsReplyStationMeasureReportSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    return transport_ptr->Send(msg);
}

MeErrorCode CloudMessageManager::SendDrcStickControlSouthbound(const std::shared_ptr<DrcStickControlSouthbound>& msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendDrcStickControlSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    return transport_ptr->Send(msg);
}

ListenIdCode CloudMessageManager::ObserveRequestsStorageConfigGetNorthbound(const std::function<void(const std::shared_ptr<RequestsStorageConfigGetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<RequestsStorageConfigGetNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveRequestsStorageConfigGetNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveRequestsStorageConfigGetNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<RequestsStorageConfigGetNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendRequestsReplyStorageConfigGetSouthbound(const std::shared_ptr<RequestsReplyStorageConfigGetSouthbound>& msg, const std::shared_ptr<RequestsStorageConfigGetNorthbound>& original_msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    if (original_msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_ORIGINAL_MESSAGE;
    }
    BuildReplyMessage(msg, original_msg); // 根据原消息的uuid, 复制给回复消息
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendRequestsReplyStorageConfigGetSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    return transport_ptr->Send(msg);
}

MeErrorCode CloudMessageManager::SendServicesTakeoffToPointSouthbound(const std::shared_ptr<ServicesTakeoffToPointSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyTakeoffToPointNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendServicesTakeoffToPointSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendServicesTakeoffToPointSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendServicesTakeoffToPointSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendServicesTakeoffToPointSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyTakeoffToPointNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveServicesReplyTakeoffToPointNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyTakeoffToPointNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<ServicesReplyTakeoffToPointNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyTakeoffToPointNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyTakeoffToPointNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyTakeoffToPointNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

ListenIdCode CloudMessageManager::ObserveEventsTakeoffToPointProgressNorthbound(const std::function<void(const std::shared_ptr<EventsTakeoffToPointProgressNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<EventsTakeoffToPointProgressNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveEventsTakeoffToPointProgressNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveEventsTakeoffToPointProgressNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<EventsTakeoffToPointProgressNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendEventsReplyTakeoffToPointProgressSouthbound(const std::shared_ptr<EventsReplyTakeoffToPointProgressSouthbound>& msg, const std::shared_ptr<EventsTakeoffToPointProgressNorthbound>& original_msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    if (original_msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_ORIGINAL_MESSAGE;
    }
    BuildReplyMessage(msg, original_msg); // 根据原消息的uuid, 复制给回复消息
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendEventsReplyTakeoffToPointProgressSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    return transport_ptr->Send(msg);
}

MeErrorCode CloudMessageManager::SendEventsThumbnailUploadFinishSouthbound(const std::shared_ptr<EventsThumbnailUploadFinishSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<EventsReplyThumbnailUploadFinishNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendEventsThumbnailUploadFinishSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendEventsThumbnailUploadFinishSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendEventsThumbnailUploadFinishSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendEventsThumbnailUploadFinishSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<EventsReplyThumbnailUploadFinishNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveEventsReplyThumbnailUploadFinishNorthbound(const std::function<void(const std::shared_ptr<EventsReplyThumbnailUploadFinishNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<EventsReplyThumbnailUploadFinishNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveEventsReplyThumbnailUploadFinishNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveEventsReplyThumbnailUploadFinishNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<EventsReplyThumbnailUploadFinishNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendServicesUnloadAiModelSouthbound(const std::shared_ptr<ServicesUnloadAiModelSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyUnloadAiModelNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendServicesUnloadAiModelSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendServicesUnloadAiModelSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendServicesUnloadAiModelSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendServicesUnloadAiModelSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyUnloadAiModelNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveServicesReplyUnloadAiModelNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyUnloadAiModelNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<ServicesReplyUnloadAiModelNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyUnloadAiModelNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyUnloadAiModelNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyUnloadAiModelNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendEventsUnloadAiModelProgressSouthbound(const std::shared_ptr<EventsUnloadAiModelProgressSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<EventsReplyUnloadAiModelProgressNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendEventsUnloadAiModelProgressSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendEventsUnloadAiModelProgressSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendEventsUnloadAiModelProgressSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendEventsUnloadAiModelProgressSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<EventsReplyUnloadAiModelProgressNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveEventsReplyUnloadAiModelProgressNorthbound(const std::function<void(const std::shared_ptr<EventsReplyUnloadAiModelProgressNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<EventsReplyUnloadAiModelProgressNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveEventsReplyUnloadAiModelProgressNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveEventsReplyUnloadAiModelProgressNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<EventsReplyUnloadAiModelProgressNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendServicesUnlockLicenseSwitchSouthbound(const std::shared_ptr<ServicesUnlockLicenseSwitchSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyUnlockLicenseSwitchNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendServicesUnlockLicenseSwitchSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendServicesUnlockLicenseSwitchSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendServicesUnlockLicenseSwitchSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendServicesUnlockLicenseSwitchSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyUnlockLicenseSwitchNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveServicesReplyUnlockLicenseSwitchNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyUnlockLicenseSwitchNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<ServicesReplyUnlockLicenseSwitchNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyUnlockLicenseSwitchNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyUnlockLicenseSwitchNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyUnlockLicenseSwitchNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendServicesUnlockLicenseUpdateSouthbound(const std::shared_ptr<ServicesUnlockLicenseUpdateSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyUnlockLicenseUpdateNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendServicesUnlockLicenseUpdateSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendServicesUnlockLicenseUpdateSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendServicesUnlockLicenseUpdateSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendServicesUnlockLicenseUpdateSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyUnlockLicenseUpdateNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveServicesReplyUnlockLicenseUpdateNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyUnlockLicenseUpdateNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<ServicesReplyUnlockLicenseUpdateNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyUnlockLicenseUpdateNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyUnlockLicenseUpdateNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyUnlockLicenseUpdateNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendEventsUomFlyDataInfoSouthbound(const std::shared_ptr<EventsUomFlyDataInfoSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<EventsReplyUomFlyDataInfoNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendEventsUomFlyDataInfoSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendEventsUomFlyDataInfoSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendEventsUomFlyDataInfoSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendEventsUomFlyDataInfoSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<EventsReplyUomFlyDataInfoNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveEventsReplyUomFlyDataInfoNorthbound(const std::function<void(const std::shared_ptr<EventsReplyUomFlyDataInfoNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<EventsReplyUomFlyDataInfoNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveEventsReplyUomFlyDataInfoNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveEventsReplyUomFlyDataInfoNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<EventsReplyUomFlyDataInfoNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

ListenIdCode CloudMessageManager::ObserveStatusUpdateTopoNorthbound(const std::function<void(const std::shared_ptr<StatusUpdateTopoNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<StatusUpdateTopoNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveStatusUpdateTopoNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveStatusUpdateTopoNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<StatusUpdateTopoNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendStatusReplyUpdateTopoSouthbound(const std::shared_ptr<StatusReplyUpdateTopoSouthbound>& msg, const std::shared_ptr<StatusUpdateTopoNorthbound>& original_msg, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    if (original_msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_ORIGINAL_MESSAGE;
    }
    BuildReplyMessage(msg, original_msg); // 根据原消息的uuid, 复制给回复消息
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendStatusReplyUpdateTopoSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    return transport_ptr->Send(msg);
}

MeErrorCode CloudMessageManager::SendServicesUploadFlighttaskMediaPrioritizeSouthbound(const std::shared_ptr<ServicesUploadFlighttaskMediaPrioritizeSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyUploadFlighttaskMediaPrioritizeNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendServicesUploadFlighttaskMediaPrioritizeSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendServicesUploadFlighttaskMediaPrioritizeSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendServicesUploadFlighttaskMediaPrioritizeSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendServicesUploadFlighttaskMediaPrioritizeSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyUploadFlighttaskMediaPrioritizeNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveServicesReplyUploadFlighttaskMediaPrioritizeNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyUploadFlighttaskMediaPrioritizeNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<ServicesReplyUploadFlighttaskMediaPrioritizeNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyUploadFlighttaskMediaPrioritizeNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyUploadFlighttaskMediaPrioritizeNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyUploadFlighttaskMediaPrioritizeNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendServicesVideoStorageSetSouthbound(const std::shared_ptr<ServicesVideoStorageSetSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyVideoStorageSetNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendServicesVideoStorageSetSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendServicesVideoStorageSetSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendServicesVideoStorageSetSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendServicesVideoStorageSetSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyVideoStorageSetNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveServicesReplyVideoStorageSetNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyVideoStorageSetNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<ServicesReplyVideoStorageSetNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyVideoStorageSetNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyVideoStorageSetNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyVideoStorageSetNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}

MeErrorCode CloudMessageManager::SendServicesWriteSdrTopoInfoSouthbound(const std::shared_ptr<ServicesWriteSdrTopoInfoSouthbound>& msg, const std::function<void(int error_code, const std::shared_ptr<ServicesReplyWriteSdrTopoInfoNorthbound>& msg)>& southbound_msg_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (msg == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto transport_ptr = transport_manager_->GetTransportByMethod(msg->GetMethod());
    if (transport_ptr == nullptr) {
        LOG_ERROR("[CloudMessageManager] SendServicesWriteSdrTopoInfoSouthbound, transport_ptr is nullptr");
        return MeErrorCode::ERROR_TRANSPORT_NOT_FOUND;
    }
    if (southbound_msg_callback == nullptr) {
        return transport_ptr->Send(msg);
    }
    auto error_code = transport_ptr->Send(msg, [southbound_msg_callback](MeErrorCode error_code, const std::shared_ptr<MessageWrapper>& message) -> void {
        if (error_code != MeErrorCode::ME_NO_ERROR) {
            /* Print error code */
            LOG_ERROR("[CloudMessageManager] SendServicesWriteSdrTopoInfoSouthbound transport Send() error_code: %lld.", static_cast<long long>(error_code));
            southbound_msg_callback(MeErrorCode::ERROR_TRANSPORT_ERROR, nullptr);
            return;
        }
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] SendServicesWriteSdrTopoInfoSouthbound response, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] SendServicesWriteSdrTopoInfoSouthbound response, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyWriteSdrTopoInfoNorthbound>(message_v1);
        southbound_msg_callback(MeErrorCode::ME_NO_ERROR, user_message);
    }, user_define_executor);
    return error_code;
}

ListenIdCode CloudMessageManager::ObserveServicesReplyWriteSdrTopoInfoNorthbound(const std::function<void(const std::shared_ptr<ServicesReplyWriteSdrTopoInfoNorthbound>& msg)>& observe_callback, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (observe_callback == nullptr) {
        return MeErrorCode::ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK;
    }
    auto observe_message = std::make_shared<ServicesReplyWriteSdrTopoInfoNorthbound>();
    auto error_code_or_listen_id = transport_manager_->Observe(observe_message->GetMethod(), [observe_callback](const std::shared_ptr<MessageWrapper>& message)->void {
        if (message->GetMessageProtocolType() != MessageProtocolType::V1) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyWriteSdrTopoInfoNorthbound, protocol type error, need: %d -> recv: %d", MessageProtocolType::V1, message->GetMessageProtocolType());
            return;
        }
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        if (message_v1 == nullptr) {
            LOG_ERROR("[CloudMessageManager] ObserveServicesReplyWriteSdrTopoInfoNorthbound, convert MessageWrapper to V1/V2/V3 message failed");
            return;
        }
        auto user_message = std::make_shared<ServicesReplyWriteSdrTopoInfoNorthbound>(message_v1);
        observe_callback(user_message);
    }, user_define_executor);
    return error_code_or_listen_id;
}