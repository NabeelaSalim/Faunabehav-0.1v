package com.example.faunabahav.ui.screens.farms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.faunabahav.data.remote.ApiResult
import com.example.faunabahav.data.repository.DeviceRepository
import com.example.faunabahav.data.repository.FarmRepository
import com.example.faunabahav.model.Device
import com.example.faunabahav.model.Farm
import com.example.faunabahav.ui.state.UiState
import com.example.faunabahav.ui.state.toUiMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FarmWithDevices(
    val farm: Farm,
    val devices: List<Device>,
)

class FarmManagementViewModel(
    private val farmRepository: FarmRepository,
    private val deviceRepository: DeviceRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<List<FarmWithDevices>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<FarmWithDevices>>> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val farmsResult = farmRepository.getFarms()
            val devicesResult = deviceRepository.getDevices()

            _uiState.value = when {
                farmsResult is ApiResult.Failure -> UiState.Error(farmsResult.error.toUiMessage())
                devicesResult is ApiResult.Failure -> UiState.Error(devicesResult.error.toUiMessage())
                else -> {
                    val farms = (farmsResult as ApiResult.Success).data
                    val devices = (devicesResult as ApiResult.Success).data
                    val farmsWithDevices = farms.map { farm ->
                        FarmWithDevices(
                            farm = farm,
                            devices = devices.filter { it.farmId == farm.id },
                        )
                    }
                    UiState.Success(farmsWithDevices)
                }
            }
        }
    }

    fun createFarm(name: String, location: String) {
        viewModelScope.launch {
            when (val result = farmRepository.createFarm(name, location)) {
                is ApiResult.Success -> load()
                is ApiResult.Failure -> {
                    val current = _uiState.value
                    if (current is UiState.Success) {
                        _uiState.value = UiState.Error(result.error.toUiMessage())
                    }
                }
            }
        }
    }

    fun deleteFarm(farmId: Int) {
        viewModelScope.launch {
            when (val result = farmRepository.deleteFarm(farmId)) {
                is ApiResult.Success -> load()
                is ApiResult.Failure -> {
                    val current = _uiState.value
                    if (current is UiState.Success) {
                        _uiState.value = UiState.Error(result.error.toUiMessage())
                    }
                }
            }
        }
    }

    fun toggleDeterrence(deviceId: Int, currentStatus: Boolean) {
        viewModelScope.launch {
            val action = if (currentStatus) "off" else "on"
            deviceRepository.controlDeterrence(deviceId, action)
        }
    }
}
