package com.fundtracker.expenses.data

import com.fundtracker.expenses.model.ExpenseData
import com.fundtracker.expenses.model.ExpenseItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class GoogleDriveSyncService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    private val gson = Gson()

    /**
     * Converts various Google Drive link formats into a direct download URL.
     * Examples handled:
     * - https://drive.google.com/file/d/1abcXYZ_123/view?usp=sharing
     * - https://drive.google.com/open?id=1abcXYZ_123
     * - 1abcXYZ_123 (direct file ID)
     * - Direct JSON URLs (e.g. https://example.com/expenses.json)
     */
    fun normalizeGoogleDriveUrl(input: String): String {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return ""

        // Case 1: https://drive.google.com/file/d/{id}/...
        val fileDMatcher = Pattern.compile("https?://drive\\.google\\.com/file/d/([a-zA-Z0-9_-]+)").matcher(trimmed)
        if (fileDMatcher.find()) {
            val fileId = fileDMatcher.group(1)
            return "https://drive.google.com/uc?export=download&id=$fileId"
        }

        // Case 2: https://drive.google.com/open?id={id} or /uc?id={id}
        val openIdMatcher = Pattern.compile("https?://drive\\.google\\.com/.*[?&]id=([a-zA-Z0-9_-]+)").matcher(trimmed)
        if (openIdMatcher.find()) {
            val fileId = openIdMatcher.group(1)
            return "https://drive.google.com/uc?export=download&id=$fileId"
        }

        // Case 3: If it looks like a raw Google Drive File ID (alphanumeric, 25-45 chars without http)
        if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://") && trimmed.matches(Regex("^[a-zA-Z0-9_-]{20,}$"))) {
            return "https://drive.google.com/uc?export=download&id=$trimmed"
        }

        return trimmed
    }

    suspend fun fetchExpensesFromDrive(url: String): Result<ExpenseData> = withContext(Dispatchers.IO) {
        try {
            val directUrl = normalizeGoogleDriveUrl(url)
            if (directUrl.isEmpty()) {
                return@withContext Result.failure(IllegalArgumentException("Please provide a valid Google Drive link or file ID"))
            }

            val request = Request.Builder()
                .url(directUrl)
                .header("User-Agent", "Mozilla/5.0 FundTracker/1.0 (Android)")
                .header("Accept", "application/json, text/plain, */*")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP Error ${response.code}: ${response.message}"))
            }

            val bodyString = response.body?.string()
                ?: return@withContext Result.failure(Exception("Empty response body received"))

            // Verify if Google Drive returned an HTML page (such as permission error or login page)
            if (bodyString.trim().startsWith("<!DOCTYPE") || bodyString.trim().startsWith("<html")) {
                return@withContext Result.failure(
                    Exception("Google Drive returned a web page instead of JSON. Make sure the file sharing permission is set to 'Anyone with the link can view'.")
                )
            }

            // Try parsing as ExpenseData (Object with items list)
            val parsedData = try {
                gson.fromJson(bodyString, ExpenseData::class.java)
            } catch (e: Exception) {
                // If it's a raw list of items [ {...}, {...} ]
                try {
                    val listType = object : TypeToken<List<ExpenseItem>>() {}.type
                    val items: List<ExpenseItem> = gson.fromJson(bodyString, listType)
                    ExpenseData(items = items)
                } catch (e2: Exception) {
                    throw Exception("Could not parse JSON. Please verify JSON format.", e2)
                }
            }

            if (parsedData != null) {
                Result.success(parsedData)
            } else {
                Result.failure(Exception("Failed to decode JSON data"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
