#!/bin/bash

# MentorService updates
sed -i 's/val apiKey = BuildConfig.NVIDIA_API_KEY.takeIf/val apiKey = BuildConfig.NVIDIA_API_KEY.takeIf/g' app/src/main/java/com/example/domain/ai/MentorService.kt
# DocumentAnalyzerService updates - inject try-catch-retry-cache
# Actually, it's easier to just overwrite them completely using cat or sed. Let's create a Helper method in NvidiaApiService or similar for Retry.
