package com.dusrel.shrinkit.presentation.util

import android.app.Activity
import android.content.Context
import com.google.android.play.core.review.ReviewManagerFactory

object InAppReviewManager {

    /**
     * Prompts the Google Play In-App Review dialog after user successfully completes operations.
     */
    fun requestReview(activity: Activity) {
        val manager = ReviewManagerFactory.create(activity)
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val reviewInfo = task.result
                manager.launchReviewFlow(activity, reviewInfo)
            }
        }
    }
}
