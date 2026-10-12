// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "common.h"
#include <sstream>


namespace flight_hub_sdk {
    // 安全服务: signal-maps/search(信号地图查询) 请求参数转化为可打印信息;
    std::string to_string(const SearchSignalMapsRequest &request) {
        std::ostringstream oss;
        oss << "device_sns: [";
        int sn_sz = (int)request.device_sns.size();
        for (int i = 0; i < sn_sz; ++i) {
            oss << (request.device_sns)[i];
            if (i != sn_sz-1) {
                oss << ", ";
            }
        }
        oss << "]";

        oss << ", data_types: [";
        int data_types_sz = (int)request.data_types.size();
        for (int i = 0; i < data_types_sz; ++i) {
            oss << (request.data_types)[i];
            if (i != data_types_sz-1) {
                oss << ", ";
            }
        }
        oss << "]";

        return oss.str();
    }


    // 创建自定义飞行区域;
    std::string to_string(const CreateCustomizedFlightAreasRequest &request) {
        std::ostringstream oss;

        oss << "name: \"" << request.name << "\"";
        oss << ", type: \"" << request.type << "\"";
        oss << ", created_by: \"" << request.created_by << "\"";
        
        // content.type
        oss << ", content.type: \"" << request.content.type << "\"";
        
        // content.properties
        oss << ", content.properties.color: \"" << request.content.properties.color << "\"";
        oss << ", content.properties.width: " << request.content.properties.width;
        oss << ", content.properties.clampToGround: " << (request.content.properties.clampToGround ? "true" : "false");
        
        // content.geometry
        oss << ", content.geometry.type: \"" << request.content.geometry.type << "\"";

        // coordinates (for Polygon/Polyline)
        const auto &coordinates_ref = request.content.geometry.coordinates;
        if (!coordinates_ref.empty()) {
            oss << ", content.geometry.coordinates: [";
            for (size_t i = 0; i < coordinates_ref.size(); ++i) {
                oss << "[";
                for (size_t j = 0; j < coordinates_ref[i].size(); ++j) {
                    oss << "[";
                    for (size_t k = 0; k < coordinates_ref[i][j].size(); ++k) {
                        oss << coordinates_ref[i][j][k];
                        if (k != coordinates_ref[i][j].size()-1) {
                            oss << ", ";
                        }
                    }
                    oss << "]";
                    if (j != coordinates_ref[i].size() - 1) {
                        oss << ", ";
                    }
                }
                oss << "]";
                if (i != coordinates_ref.size() - 1) {
                    oss << ", ";
                }
            }
            oss << "]";
        }

        // for Circle;
        const auto &center_ref = request.content.geometry.center;
        if (!center_ref.empty()) {
            // center (for Circle)
            oss << ", content.geometry.center: [";
            for (size_t i = 0; i < center_ref.size(); ++i) {
                oss << center_ref[i];
                if (i != center_ref.size() - 1) {
                    oss << ", ";
                }
            }
            oss << "]";

            // radius (for Circle)
            oss << ", content.geometry.radius: " << request.content.geometry.radius;
        }

        return oss.str();
    }


    // 更新自定义飞行区域;
    std::string to_string(const UpdateCustomizedFlightAreasRequest &request) {
        std::ostringstream oss;

        oss << "name: \"" << request.name << "\"";
        oss << ", update_by: \"" << request.update_by << "\"";
        
        // content.type
        oss << ", content.type: \"" << request.content.type << "\"";
        
        // content.properties
        oss << ", content.properties.color: \"" << request.content.properties.color << "\"";
        oss << ", content.properties.width: " << request.content.properties.width;
        oss << ", content.properties.clampToGround: " << (request.content.properties.clampToGround ? "true" : "false");
        
        // content.geometry
        oss << ", content.geometry.type: \"" << request.content.geometry.type << "\"";

        // coordinates (for Polygon/Polyline)
        const auto &coordinates_ref = request.content.geometry.coordinates;
        if (!coordinates_ref.empty()) {
            oss << ", content.geometry.coordinates: [";
            for (size_t i = 0; i < coordinates_ref.size(); ++i) {
                oss << "[";
                for (size_t j = 0; j < coordinates_ref[i].size(); ++j) {
                    oss << "[";
                    for (size_t k = 0; k < coordinates_ref[i][j].size(); ++k) {
                        oss << coordinates_ref[i][j][k];
                        if (k != coordinates_ref[i][j].size()-1) {
                            oss << ", ";
                        }
                    }
                    oss << "]";
                    if (j != coordinates_ref[i].size() - 1) {
                        oss << ", ";
                    }
                }
                oss << "]";
                if (i != coordinates_ref.size() - 1) {
                    oss << ", ";
                }
            }
            oss << "]";
        }
        
        // for Circle;
        const auto &center_ref = request.content.geometry.center;
        if (!center_ref.empty()) {
            // center (for Circle)
            oss << ", content.geometry.center: [";
            for (size_t i = 0; i < center_ref.size(); ++i) {
                oss << center_ref[i];
                if (i != center_ref.size() - 1) {
                    oss << ", ";
                }
            }
            oss << "]";

            // radius (for Circle)
            oss << ", content.geometry.radius: " << request.content.geometry.radius;
        }

        return oss.str();
    }


    // Common request with Device SNs;
    std::string to_string(const CommonRequestWithDeviceSNs &request) {
        std::ostringstream oss;

        int device_sz = (int)request.devices.size();

        oss << "devices: [";
        for (int i = 0; i < device_sz; ++i) {
            oss << (request.devices)[i];
            if (i != device_sz-1) {
                oss << ", ";
            }
        }
        oss << "]";

        return oss.str();
    }


    // 更新离线地图模型;
    std::string to_string(const UpdateOfflineMapsRequest &request) {
        std::ostringstream oss;

        int model_3d_items_size = (int)request.model_3d_items.size();
        oss << "model_3d_items: [";
        for (int i = 0; i < model_3d_items_size; ++i) {
            oss << "{";
            oss << "uuid: " << (request.model_3d_items)[i].uuid << ", ";
            oss << "object_key: " << (request.model_3d_items)[i].object_key << ", ";
            oss << "file_type: " << (request.model_3d_items)[i].file_type;
            oss << "}";

            if (i != model_3d_items_size-1) {
                oss << ", ";
            }
        }
        oss << "]";

        oss << ", ";
        int model_2d_items_size = (int)request.model_2d_items.size();
        oss << "model_2d_items: [";
        for (int i = 0; i < model_2d_items_size; ++i) {
            oss << "{";
            oss << "uuid: " << (request.model_2d_items)[i].uuid << ", ";
            oss << "object_key: " << (request.model_2d_items)[i].object_key << ", ";
            oss << "file_type: " << (request.model_2d_items)[i].file_type;
            oss << "}";

            if (i != model_2d_items_size-1) {
                oss << ", ";
            }
        }
        oss << "]";

        return oss.str();
    }


    // Common media service request with file IDs;
    std::string to_string(const CommonMediaServiceRequestWithFileIDs &request) {
        std::ostringstream oss;

        int file_ids_size = (int)request.file_ids.size();
        oss << "file_ids: [";
        for (int i = 0; i < file_ids_size; ++i) {
            oss << request.file_ids[i];
            if (i != file_ids_size - 1) {
                oss << ", ";
            }
        }
        oss << "]";

        return oss.str();
    }


    // 飞行记录: 查询导出进度;
    std::string to_string(const GetExportFlightRecordsProgressParams &request) {
        std::ostringstream oss;

        oss << "content_type: \"" << request.content_type << "\"";
        oss << ", status: " << request.status;
        oss << ", sort: \"" << request.sort << "\"";
        oss << ", page: " << request.page;
        oss << ", page_size: " << request.page_size;
        oss << ", export_id: \"" << request.export_id << "\"";

        return oss.str();
    }

}  // End of namespace flight_hub_sdk;