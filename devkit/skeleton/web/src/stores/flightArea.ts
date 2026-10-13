/**
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

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { FlightArea, FlightAreaType, FlightAreaStatus, CreateFlightAreaRequest, UpdateFlightAreaRequest } from '@/types/flightArea'
import { fetchFlightAreas, createFlightArea, updateFlightArea, deleteFlightArea } from '@/api/flightArea'

export const useFlightAreaStore = defineStore('flightArea', () => {
  const areas = ref<FlightArea[]>([])
  const selectedAreaId = ref<string | null>(null)
  const filterType = ref<FlightAreaType | 'all'>('all')
  const filterStatus = ref<FlightAreaStatus | 'all'>('all')
  const searchName = ref('')
  const loading = ref(false)

  const selectedArea = computed(() =>
    areas.value.find(a => a.id === selectedAreaId.value) ?? null
  )

  async function loadAreas(): Promise<void> {
    loading.value = true
    try {
      areas.value = await fetchFlightAreas({
        type: filterType.value === 'all' ? undefined : filterType.value,
        status: filterStatus.value === 'all' ? undefined : filterStatus.value,
        name: searchName.value || undefined,
      })
    } finally {
      loading.value = false
    }
  }

  async function addArea(req: CreateFlightAreaRequest): Promise<FlightArea> {
    const created = await createFlightArea(req)
    areas.value.unshift(created)
    return created
  }

  async function editArea(id: string, req: UpdateFlightAreaRequest): Promise<void> {
    const updated = await updateFlightArea(id, req)
    const idx = areas.value.findIndex(a => a.id === id)
    if (idx >= 0) {
      areas.value[idx] = updated
    }
  }

  async function removeArea(id: string): Promise<void> {
    await deleteFlightArea(id)
    areas.value = areas.value.filter(a => a.id !== id)
    if (selectedAreaId.value === id) {
      selectedAreaId.value = null
    }
  }

  function selectArea(id: string | null): void {
    selectedAreaId.value = id
  }

  return {
    areas, selectedAreaId, selectedArea, filterType, filterStatus, searchName, loading,
    loadAreas, addArea, editArea, removeArea, selectArea,
  }
})
