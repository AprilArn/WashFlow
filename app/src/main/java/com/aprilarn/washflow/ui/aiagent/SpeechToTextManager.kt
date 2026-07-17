package com.aprilarn.washflow.ui.aiagent

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log

class SpeechToTextManager(
    private val context: Context,
    private val onSpeechPartialResults: (String) -> Unit,
    private val onSpeechFinalResults: (String) -> Unit,
    private val onSpeechError: (Int) -> Unit
) {
    private val TAG = "ai agent button"
    private var speechRecognizer: SpeechRecognizer? = null
    private val recognizerIntent: Intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "id-ID")
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
    }

    private val recognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}

        override fun onError(error: Int) {
            Log.e(TAG, "RecognitionListener onError: $error")
            onSpeechError(error)
        }

        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            Log.d(TAG, "RecognitionListener onResults: Captured ${matches?.size ?: 0} matches")
            if (!matches.isNullOrEmpty()) {
                onSpeechFinalResults(matches[0])
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                onSpeechPartialResults(matches[0])
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    fun startListening() {
        Log.d(TAG, "startListening: Initiating SpeechRecognizer")
        if (speechRecognizer == null) {
            Log.d(TAG, "startListening: Creating new SpeechRecognizer instance")
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(recognitionListener)
            }
        }
        speechRecognizer?.startListening(recognizerIntent)
    }

    fun stopListening() {
        Log.d(TAG, "stopListening: Stopping SpeechRecognizer")
        speechRecognizer?.stopListening()
    }

    fun destroy() {
        Log.d(TAG, "destroy: Destroying SpeechRecognizer")
        speechRecognizer?.destroy()
        speechRecognizer = null
    }
}
