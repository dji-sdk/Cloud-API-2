/*
 * Copyright 2025-2026 DJI. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dji.flighthub.mcp.tools.ai;

import com.dji.flighthubsdk.bean.AiDetectionTarget;
import com.dji.flighthubsdk.bean.AiSeiInfo;
import com.dji.flighthubsdk.bean.ArSeiInfo;
import com.dji.flighthubsdk.bean.AssistantSeiInfo;
import com.dji.flighthubsdk.bean.CamCropPos;
import com.dji.flighthubsdk.bean.EisSeiInfo;
import com.dji.flighthubsdk.bean.FovSeiInfo;
import com.dji.flighthubsdk.generated.AiAlarmStateInfo;
import com.dji.flighthubsdk.generated.AiDataInfo;
import com.dji.flighthubsdk.generated.AiModelLabel;
import com.dji.flighthubsdk.generated.AiModelLabelVector;
import com.dji.flighthubsdk.generated.AiModelsConfigSet;
import com.dji.flighthubsdk.generated.AiModelsConfigSetVector;
import com.dji.flighthubsdk.generated.AiModelsInfoItem;
import com.dji.flighthubsdk.generated.AiModelsInfoItemVector;
import com.dji.flighthubsdk.generated.AiModelsStatusItem;
import com.dji.flighthubsdk.generated.AiModelsStatusItemVector;
import com.dji.flighthubsdk.generated.IntVector;
import com.dji.flighthubsdk.generated.SelectedAiModel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * SDK 回调对象逐字段映射为 Map，用于 MCP JSON 响应。
 * 包内工具类，无状态，所有方法为 static。
 *
 * @author DJI FlightHub SDK Team
 */
final class AiCallbackMapper {

    private AiCallbackMapper() {
    }

    static Map<String, Object> mapAiDataInfo(AiDataInfo info) {
        Map<String, Object> m = new LinkedHashMap<>(8);
        m.put("identify_on", info.getIdentify_on());
        m.put("ai_model_list", mapModelList(info.getAi_model_list()));
        m.put("selected_ai_model", mapSelectedModel(info.getSelected_ai_model()));
        m.put("ai_models_config", mapConfigVector(info.getAi_models_config()));
        m.put("selected_ai_models_status", mapStatusVector(info.getSelected_ai_models_status()));
        m.put("ai_models_infos", mapInfosVector(info.getAi_models_infos()));
        return m;
    }

    static Map<String, Object> mapAiSeiInfo(AiSeiInfo sei) {
        Map<String, Object> m = new LinkedHashMap<>(2);
        List<Map<String, Object>> targets = new ArrayList<>();
        if (sei.getTargets() != null) {
            for (AiDetectionTarget t : sei.getTargets()) {
                targets.add(mapDetectionTarget(t));
            }
        }
        m.put("targets", targets);
        return m;
    }

    static Map<String, Object> mapAlarmState(AiAlarmStateInfo alarm) {
        Map<String, Object> m = new LinkedHashMap<>(2);
        m.put("alarm_on", alarm.getAlarm_on());
        return m;
    }

    static Map<String, Object> mapArSeiInfo(ArSeiInfo ar) {
        Map<String, Object> m = new LinkedHashMap<>(10);
        m.put("camera_yaw", ar.getCameraYaw());
        m.put("camera_pitch", ar.getCameraPitch());
        m.put("camera_roll", ar.getCameraRoll());
        m.put("camera_pos_x", ar.getCameraPosX());
        m.put("camera_pos_y", ar.getCameraPosY());
        m.put("camera_pos_z", ar.getCameraPosZ());
        m.put("latitude", ar.getLatitude());
        m.put("longitude", ar.getLongitude());
        m.put("gnd_origin_z", ar.getGndOriginZ());
        m.put("altitude", ar.getAltitude());
        return m;
    }

    static Map<String, Object> mapFovSeiInfo(FovSeiInfo fov) {
        Map<String, Object> m = new LinkedHashMap<>(4);
        m.put("fov_horizontal", fov.getFovHorizontal());
        m.put("fov_vertical", fov.getFovVertical());
        m.put("cur_actual_focal_length", fov.getCurActualFocalLength());
        return m;
    }

    static Map<String, Object> mapAssistantSeiInfo(AssistantSeiInfo a) {
        Map<String, Object> m = new LinkedHashMap<>(4);
        m.put("auto_direction", a.isAutoDirection());
        m.put("direction", a.getDirection());
        return m;
    }

    static Map<String, Object> mapEisSeiInfo(EisSeiInfo eis) {
        Map<String, Object> m = new LinkedHashMap<>(6);
        m.put("version", eis.getVersion());
        m.put("left_top", mapCamCropPos(eis.getLeftTop()));
        m.put("right_top", mapCamCropPos(eis.getRightTop()));
        m.put("right_bottom", mapCamCropPos(eis.getRightBottom()));
        m.put("left_bottom", mapCamCropPos(eis.getLeftBottom()));
        return m;
    }

    // ==================== 内部映射方法 ====================

    private static Map<String, Object> mapDetectionTarget(AiDetectionTarget t) {
        Map<String, Object> m = new LinkedHashMap<>(10);
        m.put("target_id", t.getTargetId());
        m.put("target_type", t.getTargetType());
        m.put("target_state", t.getTargetState());
        m.put("target_cx", t.getTargetCx());
        m.put("target_cy", t.getTargetCy());
        m.put("target_w", t.getTargetW());
        m.put("target_h", t.getTargetH());
        m.put("target_distance", t.getTargetDistance());
        return m;
    }

    private static List<Map<String, Object>> mapModelList(
            com.dji.flighthubsdk.generated.AiModelItemVector vec) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < vec.size(); i++) {
            var item = vec.get(i);
            Map<String, Object> m = new LinkedHashMap<>(4);
            m.put("index", item.getIndex());
            m.put("signed_name", item.getSigned_name());
            result.add(m);
        }
        return result;
    }

    private static Map<String, Object> mapSelectedModel(SelectedAiModel s) {
        Map<String, Object> m = new LinkedHashMap<>(8);
        m.put("index", s.getIndex());
        m.put("score", s.getScore());
        m.put("score_mode", s.getScore_mode());
        m.put("image_source", intVectorToList(s.getImage_source()));
        m.put("digital_effect", intVectorToList(s.getDigital_effect()));
        m.put("filters", intVectorToList(s.getFilters()));
        m.put("labels", mapLabelVector(s.getLabels()));
        return m;
    }

    private static List<Map<String, Object>> mapConfigVector(AiModelsConfigSetVector vec) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < vec.size(); i++) {
            AiModelsConfigSet item = vec.get(i);
            Map<String, Object> m = new LinkedHashMap<>(4);
            m.put("stream_type", item.getStream_type());
            m.put("models", intVectorToList(item.getModels()));
            result.add(m);
        }
        return result;
    }

    private static List<Map<String, Object>> mapStatusVector(AiModelsStatusItemVector vec) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < vec.size(); i++) {
            AiModelsStatusItem item = vec.get(i);
            Map<String, Object> m = new LinkedHashMap<>(6);
            m.put("index", item.getIndex());
            m.put("light_type", item.getLight_type());
            m.put("score", item.getScore());
            m.put("score_mode", item.getScore_mode());
            m.put("filters", intVectorToList(item.getFilters()));
            result.add(m);
        }
        return result;
    }

    private static List<Map<String, Object>> mapInfosVector(AiModelsInfoItemVector vec) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < vec.size(); i++) {
            AiModelsInfoItem item = vec.get(i);
            Map<String, Object> m = new LinkedHashMap<>(4);
            m.put("index", item.getIndex());
            m.put("image_source", intVectorToList(item.getImage_source()));
            m.put("digital_effect", intVectorToList(item.getDigital_effect()));
            m.put("labels", mapLabelVector(item.getLabels()));
            result.add(m);
        }
        return result;
    }

    private static List<Map<String, Object>> mapLabelVector(AiModelLabelVector vec) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < vec.size(); i++) {
            AiModelLabel label = vec.get(i);
            Map<String, Object> m = new LinkedHashMap<>(4);
            m.put("index", label.getIndex());
            m.put("name", label.getName());
            result.add(m);
        }
        return result;
    }

    private static List<Integer> intVectorToList(IntVector vec) {
        return IntStream.range(0, vec.size()).map(vec::get).boxed().toList();
    }

    private static Map<String, Object> mapCamCropPos(CamCropPos pos) {
        Map<String, Object> m = new LinkedHashMap<>(4);
        m.put("x", pos.getX());
        m.put("y", pos.getY());
        return m;
    }
}
