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
import com.dji.flighthubsdk.generated.AiAlarmStateInfo;
import com.dji.flighthubsdk.generated.AiDataInfo;
import com.dji.flighthubsdk.generated.AiModelItem;
import com.dji.flighthubsdk.generated.AiModelItemVector;
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
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * AiCallbackMapper 单元测试。
 * generated.* 类型通过 Mockito mock 绕过 JNI 构造限制。
 * bean.* 类型直接构造。
 *
 * @author DJI FlightHub SDK Team
 */
class AiCallbackMapperTest {

    @Test
    @SuppressWarnings("unchecked")
    void mapAiDataInfo_shouldMapAllFields_whenFullyPopulated() {
        AiModelItem item = mock(AiModelItem.class);
        when(item.getIndex()).thenReturn(0);
        when(item.getSigned_name()).thenReturn("person");

        AiModelItemVector modelList = mock(AiModelItemVector.class);
        when(modelList.size()).thenReturn(1);
        when(modelList.get(0)).thenReturn(item);

        SelectedAiModel selected = mock(SelectedAiModel.class);
        when(selected.getIndex()).thenReturn(0);
        when(selected.getScore()).thenReturn(80);
        when(selected.getScore_mode()).thenReturn(1);
        IntVector emptyVec = mock(IntVector.class);
        when(emptyVec.size()).thenReturn(0);
        when(selected.getImage_source()).thenReturn(emptyVec);
        when(selected.getDigital_effect()).thenReturn(emptyVec);
        when(selected.getFilters()).thenReturn(emptyVec);
        AiModelLabelVector labelVec = mock(AiModelLabelVector.class);
        when(labelVec.size()).thenReturn(0);
        when(selected.getLabels()).thenReturn(labelVec);

        AiModelsConfigSetVector configVec = mock(AiModelsConfigSetVector.class);
        when(configVec.size()).thenReturn(0);
        AiModelsStatusItemVector statusVec = mock(AiModelsStatusItemVector.class);
        when(statusVec.size()).thenReturn(0);
        AiModelsInfoItemVector infosVec = mock(AiModelsInfoItemVector.class);
        when(infosVec.size()).thenReturn(0);

        AiDataInfo info = mock(AiDataInfo.class);
        when(info.getIdentify_on()).thenReturn(1);
        when(info.getAi_model_list()).thenReturn(modelList);
        when(info.getSelected_ai_model()).thenReturn(selected);
        when(info.getAi_models_config()).thenReturn(configVec);
        when(info.getSelected_ai_models_status()).thenReturn(statusVec);
        when(info.getAi_models_infos()).thenReturn(infosVec);

        Map<String, Object> result = AiCallbackMapper.mapAiDataInfo(info);

        assertEquals(1, result.get("identify_on"));
        List<Map<String, Object>> models = (List<Map<String, Object>>) result.get("ai_model_list");
        assertEquals(1, models.size());
        assertEquals(0, models.get(0).get("index"));
        assertEquals("person", models.get(0).get("signed_name"));
    }

    @Test
    void mapAiSeiInfo_shouldMapTargets_whenSingleTarget() {
        AiDetectionTarget t = new AiDetectionTarget();
        t.setTargetId(7);
        t.setTargetType(2);
        t.setTargetDistance(120L);
        AiSeiInfo sei = new AiSeiInfo(List.of(t));

        Map<String, Object> result = AiCallbackMapper.mapAiSeiInfo(sei);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> targets = (List<Map<String, Object>>) result.get("targets");
        assertEquals(1, targets.size());
        assertEquals(7, targets.get(0).get("target_id"));
        assertEquals(2, targets.get(0).get("target_type"));
        assertEquals(120L, targets.get(0).get("target_distance"));
    }

    @Test
    void mapAlarmState_shouldMapAlarmOn() {
        AiAlarmStateInfo alarm = mock(AiAlarmStateInfo.class);
        when(alarm.getAlarm_on()).thenReturn(true);

        Map<String, Object> result = AiCallbackMapper.mapAlarmState(alarm);

        assertEquals(true, result.get("alarm_on"));
    }

    @Test
    void mapAiDataInfo_shouldHandleEmptyVectors() {
        AiModelItemVector emptyModels = mock(AiModelItemVector.class);
        when(emptyModels.size()).thenReturn(0);
        SelectedAiModel selected = mock(SelectedAiModel.class);
        when(selected.getIndex()).thenReturn(0);
        when(selected.getScore()).thenReturn(0);
        when(selected.getScore_mode()).thenReturn(0);
        IntVector ev = mock(IntVector.class);
        when(ev.size()).thenReturn(0);
        when(selected.getImage_source()).thenReturn(ev);
        when(selected.getDigital_effect()).thenReturn(ev);
        when(selected.getFilters()).thenReturn(ev);
        AiModelLabelVector lv = mock(AiModelLabelVector.class);
        when(lv.size()).thenReturn(0);
        when(selected.getLabels()).thenReturn(lv);
        AiModelsConfigSetVector cv = mock(AiModelsConfigSetVector.class);
        when(cv.size()).thenReturn(0);
        AiModelsStatusItemVector sv = mock(AiModelsStatusItemVector.class);
        when(sv.size()).thenReturn(0);
        AiModelsInfoItemVector iv = mock(AiModelsInfoItemVector.class);
        when(iv.size()).thenReturn(0);

        AiDataInfo info = mock(AiDataInfo.class);
        when(info.getIdentify_on()).thenReturn(0);
        when(info.getAi_model_list()).thenReturn(emptyModels);
        when(info.getSelected_ai_model()).thenReturn(selected);
        when(info.getAi_models_config()).thenReturn(cv);
        when(info.getSelected_ai_models_status()).thenReturn(sv);
        when(info.getAi_models_infos()).thenReturn(iv);

        Map<String, Object> result = AiCallbackMapper.mapAiDataInfo(info);

        assertEquals(0, result.get("identify_on"));
        @SuppressWarnings("unchecked")
        List<?> models = (List<?>) result.get("ai_model_list");
        assertTrue(models.isEmpty());
    }
}
