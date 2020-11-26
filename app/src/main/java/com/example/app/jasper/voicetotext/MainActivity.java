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
import android.widget.Spinner;
import android.widget.Toast;

import com.example.app.jasper.voicetotext.model.Language;

import java.util.ArrayList;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    EditText textField;
    Button copyTextButton;
    boolean isRecording = false;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private final int REQ_LANGUAGE_LIST = 200;
    CardView settings_clicked_menu_dialog;
    View settings_clicked_menu_overlay;
    private ArrayList<Language> supportedLanguages;


    public static final String recordAudioPermission = Manifest.permission.RECORD_AUDIO;
    public static final String internetPermission = Manifest.permission.INTERNET;

    String[] langList = {"GERMAN", "ENGLISH", "FRENCH"};
    String language = "GERMAN";
    Locale lang;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        textField = findViewById(R.id.textContainer);
        copyTextButton = findViewById(R.id.copyButton);

        showPermission();
        requestPermission();
        initLanguageSpinner();
        int i = 1;
        supportedLanguages = new ArrayList<>();
        for (Locale locale : Locale.getAvailableLocales()) {
//            Log.d("LOCALES", "code: " + locale.getLanguage() + "_" + locale.getCountry());
//            Log.d("LOCALES", "language : " + locale.getDisplayName());
//            Log.d("LOCALES", "i : " + i);
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
    }

    public void onClickRecord(View view) {

        if (!isRecording) {

            //da vom System etwas zurück an die App gegeben werden muss,
            // muss die Methode startActivityFOrResult aufgerufen werden
            // requestCode kann frei ausgesucht werden
            try {
                Intent recordIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                // Art und Weise, welchen Text man als Rückgabe erhällt
                recordIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
                //recordIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                recordIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

                recordIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 100);
                recordIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, setLanguage(language));
                recordIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Jetzt Sprechen");

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

    private void initLanguageSpinner() {
        //get the spinner from the xml.
        settings_clicked_menu_overlay = findViewById(R.id.settings_clicked_menu_overlay);
        settings_clicked_menu_dialog = findViewById(R.id.settings_clicked_menu_dialog);
        Spinner dropdown = findViewById(R.id.language_spinner);
//create a list of items for the spinner.
//create an adapter to describe how the items are displayed, adapters are used in several places in android.
//There are multiple variations of this, but this is the basic variant.
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, langList);
//set the spinners adapter to the previously created one.
        dropdown.setAdapter(adapter);
        dropdown.setOnItemSelectedListener(this);
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

    private Locale setLanguage(String language) {
        return new Locale(language);
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
            showLanguageSpinner();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showLanguageSpinner() {
        settings_clicked_menu_overlay.setVisibility(View.VISIBLE);
        settings_clicked_menu_dialog.setVisibility(View.VISIBLE);
    }

    private void hideLanguageSpinner() {
        settings_clicked_menu_overlay.setVisibility(View.GONE);
        settings_clicked_menu_dialog.setVisibility(View.GONE);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        int position = adapterView.getCount();
        language = langList[position-1];
        setLanguage(language);
        hideLanguageSpinner();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        hideLanguageSpinner();

    }

}
