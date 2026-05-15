package com.example.nammahomestay.ai.agents

import android.content.Context
import com.example.nammahomestay.data.model.NearbySpot
import com.example.nammahomestay.ui.host.OpenRouterService
import org.json.JSONArray
import org.json.JSONObject

class TourismAgent(context: Context) {

    private val openRouterService = OpenRouterService(context)

    fun getLocalRecommendations(location: String, userQuery: String, callback: (String) -> Unit) {
        val prompt = """
            You are 'Namma Guide', a friendly local AI assistant for a homestay in $location.
            A traveler is asking: "$userQuery"
            
            Your job:
            1. Suggest specific REAL hidden gems, waterfalls, viewpoints, or local food that actually exist in or very near $location.
            2. Give practical tips (best time to visit, what to wear).
            3. Be warm, welcoming, and act like a local who loves their town.
            4. Keep the response concise but very helpful.
        """.trimIndent()

        openRouterService.generateDescription(prompt, callback)
    }

    fun autoGenerateNearbySpots(location: String, callback: (List<NearbySpot>) -> Unit) {
        val prompt = """
            Find 3 real tourist spots in $location.
            Format your response as a JSON array ONLY:
            [
              {"name": "Spot Name", "type": "Type", "description": "Short description"}
            ]
        """.trimIndent()

        openRouterService.generateDescription(prompt) { result ->
            try {
                val start = result.indexOf("[")
                val end = result.lastIndexOf("]")
                if (start != -1 && end != -1) {
                    val jsonArray = JSONArray(result.substring(start, end + 1))
                    val spots = mutableListOf<NearbySpot>()
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        spots.add(NearbySpot(
                            obj.optString("name", "Unknown Spot"),
                            obj.optString("type", "Attraction"),
                            obj.optString("description", "A local spot to visit.")
                        ))
                    }
                    callback(spots)
                } else {
                    callback(emptyList())
                }
            } catch (e: Exception) {
                callback(emptyList())
            }
        }
    }
}
