package com.example.app.jasper.voicetotext;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;

import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.app.jasper.voicetotext.model.Language;

import java.util.ArrayList;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    EditText textField;
    ImageView copyTextButton;
    TextView current_language;
    boolean isRecording = false;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private final int REQ_LANGUAGE_LIST = 200;
    CardView settings_clicked_menu_dialog;
    View settings_clicked_menu_overlay;
    private ArrayList<Language> supportedLanguages;


    public static final String recordAudioPermission = Manifest.permission.RECORD_AUDIO;
    public static final String internetPermission = Manifest.permission.INTERNET;

    String currentLanguageCode = "de_DE";
    String currentLanguage = "German";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        textField = findViewById(R.id.textContainer);
        copyTextButton = findViewById(R.id.copyButton);
        current_language = findViewById(R.id.current_language);
        current_language.setText(currentLanguage);

        showPermission();
        requestPermission();
        int i = 1;
        supportedLanguages = new ArrayList<>();
        for (Locale locale : Locale.getAvailableLocales()) {
            Log.d("LOCALES", "code: " + locale.getLanguage() + "_" + locale.getCountry());
            Language language = new Language();
            language.setId(i);
            language.setCode(locale.getLanguage() + "_" + locale.getCountry());
            language.setLanguage(locale.getDisplayName());
            supportedLanguages.add(language);
            i ++;
        }
        Log.d("LOCALES", "List : " + supportedLanguages.get(12).getLanguage());
    }   // onCreate()





    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_CODE_SPEECH_INPUT) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                textField.setText((CharSequence) result.get(0));
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "permission just granted", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "permission just denied", Toast.LENGTH_LONG).show();
                }
        }
    }

    public void onClickCopy(View view) {
        ClipboardManager clipboard = (ClipboardManager) this.getSystemService(CLIPBOARD_SERVICE);
        String input = textField.getText().toString();
        ClipData clipData = ClipData.newPlainText("input", input);
        clipboard.setPrimaryClip(clipData);
        Toast.makeText(this, "text copied to clipboard!", Toast.LENGTH_LONG).show();
    }

    public void onClickRecord(View view) {

        if (!isRecording) {
            try {
                Intent recordIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                // Art und Weise, welchen Text man als Rückgabe erhällt
                recordIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
                //recordIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                recordIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

                recordIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 100);
                recordIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, currentLanguageCode);
                recordIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, currentLanguage + "\nSpeak now");

                startActivityForResult(recordIntent, REQ_CODE_SPEECH_INPUT);
                Toast.makeText(this, "Activity started", Toast.LENGTH_SHORT).show();


            } catch (ActivityNotFoundException e) {
                String appPackageName = "com.google.android.googlequicksearchbox";
                Toast.makeText(this, "Activity not found", Toast.LENGTH_LONG).show();
                try {

                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                    Toast.makeText(this, "Action_View", Toast.LENGTH_LONG).show();

                } catch (android.content.ActivityNotFoundException answer) {
                    Toast.makeText(this, answer + " -- Activity not found", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }
            }
        } else {
            isRecording = false;
        }
    }

    private void requestPermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, recordAudioPermission);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, recordAudioPermission)) {
            Toast.makeText(this, "Falls du die App richtig verwenden möchtest, musst die ihr die Rechte für das Benutzen des Mikrofons erteilen", Toast.LENGTH_LONG).show();
        }
        ActivityCompat.requestPermissions(this, new String[]{recordAudioPermission, internetPermission}, REQ_CODE_SPEECH_INPUT);
    }

    private void showPermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, recordAudioPermission);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            //Toast.makeText(this, "permission granted", Toast.LENGTH_LONG).show();
        }
        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            //Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.action_language) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
