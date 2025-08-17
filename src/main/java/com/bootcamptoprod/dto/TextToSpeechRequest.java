package com.bootcamptoprod.dto;

public record TextToSpeechRequest(
        String text,
        String voiceId,
        Double stability,
        Double similarityBoost,
        Double style,
        Boolean useSpeakerBoost,
        Double speed
) {
}
