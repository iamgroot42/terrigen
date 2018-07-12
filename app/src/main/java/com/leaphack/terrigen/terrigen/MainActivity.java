package com.leaphack.terrigen.terrigen;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    private LinearLayout messageScrollView;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private final String TAG = "Terrigen";
    private TextToSpeech textToSpeechEngine;
    private EditText userInputTextbox;

    private void displaySenderMessageInChatWindow(String message) {
        Log.i(TAG, "Displaying the sender's message: " + message);
        RelativeLayout senderMessageLayout = (RelativeLayout) LayoutInflater.from(this)
                .inflate(R.layout.sender_message, null);
        TextView senderMessageView = (TextView) ((LinearLayout) senderMessageLayout
                .getChildAt(0)).getChildAt(1);
        final ScrollView scrollWindow =  findViewById(R.id.scroll_window);
        senderMessageView.setText(message);
        messageScrollView.addView(senderMessageLayout);
        scrollWindow.post(new Runnable() {
            public void run() {
                scrollWindow.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    private void displayReceiverMessageInChatWindow(String message) {
        Log.i(TAG, "Displaying the receiver's message: " + message);
        RelativeLayout senderMessageLayout = (RelativeLayout) LayoutInflater.from(this)
                .inflate(R.layout.receiver_message, null);
        TextView senderMessageView = (TextView) ((LinearLayout) senderMessageLayout
                .getChildAt(0)).getChildAt(0);
        senderMessageView.setText(message);
        messageScrollView.addView(senderMessageLayout);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "Initialising the app");
        Log.d(TAG, "Saved instance state" + savedInstanceState);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        messageScrollView = findViewById(R.id.messageWindow);
        textToSpeechEngine = new TextToSpeech(this, this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "Speech-to-text: Fetched text data");
        Log.d(TAG, "Request code: " + requestCode);
        Log.d(TAG, "Result code: " + resultCode);
        Log.d(TAG, "Data: " + data);
        String receiverMessage;
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && data != null) {
                    ArrayList<String> messageList = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    Log.d(TAG, "Message list received");
                    Log.d(TAG, messageList.toString());
                    receiverMessage = messageList.get(0);
                    this.displayReceiverMessageInChatWindow(receiverMessage);
                }

                break;
            }
        }
    }

    public void obtainSpeechInput(View button) {
        Log.i(TAG, "Obtaining speech input");
        String displayMessage = "Hi speak something";
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, displayMessage);
        try {
            Log.d(TAG, "Attempting to process speech input");
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
            Log.d(TAG, "Successfully processed speech input");
        } catch (ActivityNotFoundException activityNotFound) {
            Log.e(TAG, "Unable to process speech input", activityNotFound);
        }
    }

    @Override
    public void onInit(int status) {
        Log.d(TAG, "Status code: " + status);
        userInputTextbox = findViewById(R.id.textBox);
        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeechEngine.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "Text-to-speech: This language is not supported.");
            } else {
                Log.i(TAG, "About to convert text to speech");
                View dummyView = new View(this);
                convertTextToSpeech(dummyView);
            }
        } else {
            Log.e(TAG, "Text-to-speech: Initialisation Failed!");
        }
    }

    public void convertTextToSpeech(View view) {
        Log.i(TAG, "Inside convertTextToSpeech()");
        Log.d(TAG, view.toString());
        CharSequence text = userInputTextbox.getText();
        textToSpeechEngine.speak(text, TextToSpeech.QUEUE_FLUSH, null,"id1");
        userInputTextbox.setText(null);
        if (text.length() > 0) {
            this.displaySenderMessageInChatWindow(text.toString());
        }
    }
}
