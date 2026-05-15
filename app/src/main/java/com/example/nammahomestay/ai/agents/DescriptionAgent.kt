package com.example.nammahomestay.ai.agents

import android.content.Context
import com.example.nammahomestay.ui.host.OpenRouterService

class DescriptionAgent(context: Context) {
    private val openRouterService = OpenRouterService(context)

    fun generateDescription(name: String, location: String, price: String, callback: (String) -> Unit) {
        val prompt = "Write an attractive homestay description for '$name' in $location at ₹$price per night. Keep it professional and inviting, under 60 words."
        openRouterService.generateDescription(prompt, callback)
    }
}
