package com.bootcamptoprod.controller;

import com.bootcamptoprod.dto.TextToSpeechRequest;
import org.springframework.ai.audio.tts.TextToSpeechPrompt;
import org.springframework.ai.audio.tts.TextToSpeechResponse;
import org.springframework.ai.elevenlabs.ElevenLabsTextToSpeechModel;
import org.springframework.ai.elevenlabs.ElevenLabsTextToSpeechOptions;
import org.springframework.ai.elevenlabs.api.ElevenLabsApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.io.UncheckedIOException;

@RestController
@RequestMapping("/api/tts")
public class TextToSpeechController {

    private final ElevenLabsTextToSpeechModel textToSpeechModel;

    @Autowired
    public TextToSpeechController(ElevenLabsTextToSpeechModel textToSpeechModel) {
        this.textToSpeechModel = textToSpeechModel;
    }

    @PostMapping(value = "/stream", produces = "audio/mpeg")
    public ResponseEntity<StreamingResponseBody> ttsStream(@RequestBody TextToSpeechRequest request) {

        var voiceSettings = new ElevenLabsApi.SpeechRequest.VoiceSettings(
                request.stability(), request.similarityBoost(), request.style(), request.useSpeakerBoost(), request.speed()
        );

        var textToSpeechOptions = ElevenLabsTextToSpeechOptions.builder()
                .model("eleven_multilingual_v2")
                .voiceSettings(voiceSettings)
                .outputFormat(ElevenLabsApi.OutputFormat.MP3_44100_128.getValue())
                .build();

        textToSpeechOptions.setEnableLogging(Boolean.TRUE);

        var textToSpeechPrompt = new TextToSpeechPrompt(request.text(), textToSpeechOptions);

        Flux<TextToSpeechResponse> responseStream = textToSpeechModel.stream(textToSpeechPrompt);

        StreamingResponseBody body = outputStream -> {
            responseStream.toStream().forEach(speechResponse -> {
                try {
                    outputStream.write(speechResponse.getResult().getOutput());
                    outputStream.flush();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        };

        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("audio/mpeg"))
                .body(body);
    }

}