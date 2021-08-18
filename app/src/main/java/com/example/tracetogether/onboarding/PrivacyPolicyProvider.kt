package com.example.tracetogether.onboarding

object PrivacyPolicyProvider {
    fun localAppPrivacyPolicyUrl(): String? {
        return "file:///android_asset/privacypolicy.html"
    }
}