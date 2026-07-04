package com.example.faunabahav.data.remote

import com.example.faunabahav.data.remote.dto.AlertDto
import com.example.faunabahav.data.remote.dto.AnalyticsSummaryDto
import com.example.faunabahav.data.remote.dto.DashboardSummaryDto
import com.example.faunabahav.data.remote.dto.DeviceDto
import com.example.faunabahav.data.remote.dto.FeedbackDto
import com.example.faunabahav.data.remote.dto.InferenceResultDto
import com.example.faunabahav.data.remote.dto.ObservationDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType

class FaunaBehavApiClient(private val client: HttpClient) {

    suspend fun getObservations(): List<ObservationDto> =
        client.get("observations/").body()

    suspend fun submitInference(
        deviceId: Int,
        fileBytes: ByteArray,
        fileName: String,
        contentType: String,
    ): InferenceResultDto =
        client.submitFormWithBinaryData(
            url = "events/inference",
            formData = formData {
                append("device_id", deviceId.toString())
                append("file", fileBytes, Headers.build {
                    append(HttpHeaders.ContentType, contentType)
                    append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                })
            },
        ).body()

    suspend fun getAlerts(): List<AlertDto> =
        client.get("alerts/").body()

    suspend fun getFeedback(): List<FeedbackDto> =
        client.get("feedback/").body()

    suspend fun submitFeedback(feedback: FeedbackDto): FeedbackDto =
        client.post("feedback/") {
            contentType(ContentType.Application.Json)
            setBody(feedback)
        }.body()

    suspend fun getAnalytics(): AnalyticsSummaryDto =
        client.get("analytics/").body()

    suspend fun getDashboardSummary(): DashboardSummaryDto =
        client.get("dashboard/summary").body()

    suspend fun getDevices(): List<DeviceDto> =
        client.get("devices/").body()
}