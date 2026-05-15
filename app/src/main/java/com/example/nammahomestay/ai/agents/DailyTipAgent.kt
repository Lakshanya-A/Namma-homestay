package com.example.nammahomestay.ai.agents

import android.content.Context
import com.example.nammahomestay.ui.host.OpenRouterService

class DailyTipAgent(context: Context) {

    private val openRouterService = OpenRouterService(context)
    private val fallbackTips = listOf(
        "Greet every guest with a warm smile and local hospitality.",
        "Keep your homestay sparkling clean – first impressions last!",
        "Suggest a local hidden gem to your guests to make their stay special.",
        "A small welcome treat can turn a guest into a regular.",
        "Respond to inquiries quickly to show guests you value them.",
        "Share stories about your town to give guests a true local experience."
    )

    fun generateDailyTip(callback: (String) -> Unit) {
        val prompt = """
            You are a 'Host Success Coach' for a homestay business. 
            Provide one short, professional, and inspiring tip for a homestay host today.
            The tip could be about hospitality, cleanliness, local food, or guest communication.
            Keep it under 20 words.
        """.trimIndent()

        openRouterService.generateDescription(prompt) { result ->
            if (result.startsWith("Error") || result.startsWith("AI Error") || result.contains("failed", ignoreCase = true)) {
                callback(fallbackTips.random())
            } else {
                callback(result.trim().replace("\"", ""))
            }
        }
    }
}
