package com.example.marikitiapp.data.repository

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull

class SupabaseUploader(
    private val supabaseUrl: String,
    private val supabaseApiKey: String
) {
    private val client = OkHttpClient()

    suspend fun uploadImageToSupabase(context: Context, imageBytes: ByteArray): String? {
        return withContext(Dispatchers.IO) {
            val bucketName = "product-images"
            val fileName = "product_${System.currentTimeMillis()}.jpg"

            val requestBody = RequestBody.create(
                "image/jpeg".toMediaTypeOrNull(),
                imageBytes        // 💡 ByteArray is directly used
            )

            val request = Request.Builder()
                .url("$supabaseUrl/storage/v1/object/$bucketName/$fileName")
                .addHeader("apikey", supabaseApiKey)
                .addHeader("Authorization", "Bearer $supabaseApiKey")
                .put(requestBody)
                .build()

            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                "$supabaseUrl/storage/v1/object/public/$bucketName/$fileName"
            } else {
                val errorBody = response.body?.string() ?: "Unknown Supabase error"
                throw Exception("Upload failed: $errorBody")
            }
        }
    }
}