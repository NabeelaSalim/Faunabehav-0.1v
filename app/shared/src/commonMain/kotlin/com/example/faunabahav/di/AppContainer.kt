package com.example.faunabahav.di

import com.example.faunabahav.data.remote.FaunaBehavApiClient
import com.example.faunabahav.data.remote.createHttpClient
import com.example.faunabahav.data.remote.defaultBaseUrl
import com.example.faunabahav.data.repository.AlertRepository
import com.example.faunabahav.data.repository.AlertRepositoryImpl
import com.example.faunabahav.data.repository.AnalyticsRepository
import com.example.faunabahav.data.repository.AnalyticsRepositoryImpl
import com.example.faunabahav.data.repository.AuthRepository
import com.example.faunabahav.data.repository.AuthRepositoryImpl
import com.example.faunabahav.data.repository.sessionTokenFrom
import com.example.faunabahav.data.repository.DashboardRepository
import com.example.faunabahav.data.repository.DashboardRepositoryImpl
import com.example.faunabahav.data.repository.DeviceRepository
import com.example.faunabahav.data.repository.DeviceRepositoryImpl
import com.example.faunabahav.data.repository.FeedbackRepository
import com.example.faunabahav.data.repository.FeedbackRepositoryImpl
import com.example.faunabahav.data.repository.ObservationRepository
import com.example.faunabahav.data.repository.ObservationRepositoryImpl
import com.example.faunabahav.data.session.SessionStorage

class AppContainer(
    sessionStorage: SessionStorage,
    val baseUrl: String = defaultBaseUrl(),
) {
    val httpClient = createHttpClient(baseUrl, tokenProvider = { sessionTokenFrom(sessionStorage.readRaw()) })
    private val apiClient = FaunaBehavApiClient(httpClient)

    val observationRepository: ObservationRepository = ObservationRepositoryImpl(apiClient)
    val alertRepository: AlertRepository = AlertRepositoryImpl(apiClient)
    val analyticsRepository: AnalyticsRepository = AnalyticsRepositoryImpl(apiClient)
    val dashboardRepository: DashboardRepository = DashboardRepositoryImpl(apiClient)
    val deviceRepository: DeviceRepository = DeviceRepositoryImpl(apiClient)
    val feedbackRepository: FeedbackRepository = FeedbackRepositoryImpl(apiClient)

    val authRepository: AuthRepository = AuthRepositoryImpl(apiClient, sessionStorage)
}