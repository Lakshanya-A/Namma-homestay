package com.example.nammahomestay.ai.agents

import android.content.Context
import com.example.nammahomestay.ui.host.OpenRouterService

class ReviewAgent(context: Context) {
    private val openRouterService = OpenRouterService(context)

    fun summarizeReviews(reviews: List<String>, callback: (String) -> Unit) {
        if (reviews.isEmpty()) {
            callback("No reviews yet.")
            return
        }
        val prompt = "Summarize these guest reviews into a short, positive 2-line highlight for a homestay profile: ${reviews.joinToString("; ")}"
        openRouterService.generateDescription(prompt, callback)
    }
}
