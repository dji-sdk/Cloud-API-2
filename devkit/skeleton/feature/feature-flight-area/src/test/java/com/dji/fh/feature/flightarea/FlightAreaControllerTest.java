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

package com.dji.fh.feature.flightarea;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class FlightAreaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Configuration
    @EnableAutoConfiguration
    @Import(FlightAreaAutoConfiguration.class)
    static class TestConfig {
    }

    @Test
    void listFlightAreas_shouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/api/v1/flight-areas"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void createFlightArea_shouldReturn201() throws Exception {
        String body = """
                {"name":"test","type":"nfz","content":{"type":"Feature","geometry":{"type":"Polygon","coordinates":[[[0,0],[1,0],[1,1],[0,0]]]}}}
                """;

        mockMvc.perform(post("/api/v1/flight-areas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("test"))
                .andExpect(jsonPath("$.type").value("nfz"));
    }

    @Test
    void listFlightAreas_shouldFilterByType() throws Exception {
        String nfzBody = """
                {"name":"限飞区","type":"nfz","content":{"type":"Feature"}}
                """;
        String dfenceBody = """
                {"name":"作业区","type":"dfence","content":{"type":"Feature"}}
                """;

        mockMvc.perform(post("/api/v1/flight-areas")
                        .contentType(MediaType.APPLICATION_JSON).content(nfzBody))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/v1/flight-areas")
                        .contentType(MediaType.APPLICATION_JSON).content(dfenceBody))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/flight-areas").param("type", "nfz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("限飞区"));
    }

    @Test
    void deleteFlightArea_shouldReturn404_whenNotExists() throws Exception {
        mockMvc.perform(delete("/api/v1/flight-areas/nonexistent"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getOfflineMapModelInfo_shouldReturnData() throws Exception {
        mockMvc.perform(get("/api/v1/offline-maps/model-info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value("1.0.0"));
    }

    @Test
    void getTcasConfig_shouldReturnDefaultThresholds() throws Exception {
        mockMvc.perform(get("/api/v1/tcas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.thresholds").isArray())
                .andExpect(jsonPath("$.thresholds.length()").value(3));
    }
}
