package com.example.nammahomestay.ai.agents

import android.content.Context
import com.example.nammahomestay.ui.host.OpenRouterService

class PricingAgent(context: Context) {
    private val openRouterService = OpenRouterService(context)

    fun suggestPricing(location: String, callback: (String) -> Unit) {
        val prompt = "Suggest a competitive price range in INR for a homestay in $location. Give a short explanation why. Keep it under 30 words."
        openRouterService.generateDescription(prompt, callback)
    }
}
