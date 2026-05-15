package com.example.nammahomestay.ai.agents

import android.content.Context
import com.example.nammahomestay.data.model.Inquiry
import com.example.nammahomestay.data.model.NearbySpot

class AIAgentManager(context: Context) {

    private val tourismAgent = TourismAgent(context)
    private val descriptionAgent = DescriptionAgent(context)
    private val pricingAgent = PricingAgent(context)
    private val reviewAgent = ReviewAgent(context)
    private val inquiryAgent = InquiryAgent(context)

    /**
     * Ask Namma Guide about local tips and hidden gems.
     */
    fun askNammaGuide(location: String, query: String, callback: (String) -> Unit) {
        tourismAgent.getLocalRecommendations(location, query, callback)
    }

    /**
     * Generate attractive homestay descriptions.
     */
    fun generateDescription(name: String, location: String, price: String, callback: (String) -> Unit) {
        descriptionAgent.generateDescription(name, location, price, callback)
    }

    /**
     * Suggest nearby spots to visit.
     */
    fun suggestNearbySpots(location: String, callback: (List<NearbySpot>) -> Unit) {
        tourismAgent.autoGenerateNearbySpots(location, callback)
    }

    /**
     * Suggest pricing for a location.
     */
    fun suggestPricing(location: String, callback: (String) -> Unit) {
        pricingAgent.suggestPricing(location, callback)
    }

    /**
     * Summarize guest reviews into a highlight.
     */
    fun summarizeReviews(reviews: List<String>, callback: (String) -> Unit) {
        reviewAgent.summarizeReviews(reviews, callback)
    }

    /**
     * Analyze inquiry trends to provide host insights.
     */
    fun analyzeInquiryTrends(inquiries: List<Inquiry>, callback: (String?) -> Unit) {
        inquiryAgent.analyzeTrends(inquiries, callback)
    }

    fun getTags(location: String): List<String> {
        return listOf(
            "Nature Stay",
            "Eco Tourism",
            "Family Friendly",
            location
        )
    }
}
