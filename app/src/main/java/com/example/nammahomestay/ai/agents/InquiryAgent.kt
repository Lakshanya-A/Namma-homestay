package com.example.nammahomestay.ai.agents

import android.content.Context
import com.example.nammahomestay.data.model.Inquiry
import com.example.nammahomestay.ui.host.OpenRouterService
import org.json.JSONObject

class InquiryAgent(context: Context) {

    private val openRouterService = OpenRouterService(context)

    /**
     * Proactive Suggestions: Analyzes guest inquiries to find common trends
     * and suggests improvements to the host.
     */
    fun analyzeTrends(inquiries: List<Inquiry>, callback: (String?) -> Unit) {
        if (inquiries.isEmpty()) {
            callback(null)
            return
        }

        val messages = inquiries.joinToString("\n") { "- ${it.message}" }
        val prompt = """
            You are an 'Insights Agent' for a homestay owner. 
            Analyze the following guest inquiries and identify if there is a common theme (e.g., people asking about food, waterfalls, trekking, or check-in times).
            
            Inquiries:
            $messages
            
            If you find a strong trend, suggest a specific action for the host in one short sentence.
            Example response: "3 guests asked about waterfalls this morning; consider updating your Local Guide with nearby falls."
            If no clear trend is found, respond with "NONE".
        """.trimIndent()

        openRouterService.generateDescription(prompt) { result ->
            if (result.trim().uppercase() == "NONE") {
                callback(null)
            } else {
                callback(result.trim().replace("\"", ""))
            }
        }
    }
}
