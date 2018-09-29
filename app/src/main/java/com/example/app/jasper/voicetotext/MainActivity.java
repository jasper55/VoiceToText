package com.example.app.jasper.voicetotext;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private EditText textField;
    private Button copyTextButton;
    private boolean isRecording = false;
    private final int REQ_CODE_SPEECH_INPUT = 100;

    public static final String recordAudioPermission = Manifest.permission.RECORD_AUDIO;
    public static final String internetPermission = Manifest.permission.INTERNET;

    private ArrayAdapter<String> languageListAdapter;
    private String language = "GERMAN";
    private AlertDialog AlertDialogActions;
    private Spinner languageSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        textField = (EditText) findViewById(R.id.textContainer);
        copyTextButton = (Button)findViewById(R.id.copyButton);

        showPermission();
        requestPermission();

    }   // onCreate()

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==REQ_CODE_SPEECH_INPUT)
        {
            if(resultCode == RESULT_OK && data != null){
                ArrayList result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                textField.setText((CharSequence) result.get(0));
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        switch (requestCode){
            case REQ_CODE_SPEECH_INPUT:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    Toast.makeText(this, "permission just granted", Toast.LENGTH_LONG).show();
                }
                else
                {
                    Toast.makeText(this, "permission just denied", Toast.LENGTH_LONG).show();
                }
        }
    }

    public void onClickCopy(View view){
        ClipboardManager clipboard = (ClipboardManager)this.getSystemService(this.CLIPBOARD_SERVICE);
        String input = textField.getText().toString();
        ClipData clipData = ClipData.newPlainText("input",input);
        clipboard.setPrimaryClip(clipData);
    }
    public void onClickRecord(View view){

        if(!isRecording) {

            //da vom System etwas zurück an die App gegeben werden muss,
            // muss die Methode startActivityFOrResult aufgerufen werden
            // requestCode kann frei ausgesucht werden
            try {
                Intent recordIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                // Art und Weise, welchen Text man als Rückgabe erhällt
                //recordIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
                recordIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                //recordIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

                recordIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, REQ_CODE_SPEECH_INPUT);
                recordIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language);
                Log.i("Language", language);
                recordIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now");

                startActivityForResult(recordIntent, REQ_CODE_SPEECH_INPUT);


            } catch(ActivityNotFoundException e) {
                String appPackageName = "com.google.android.googlequicksearchbox";
                Toast.makeText(this, "Activity not found", Toast.LENGTH_LONG).show();
                try {

                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                    Toast.makeText(this, "Action_View", Toast.LENGTH_LONG).show();

                } catch (android.content.ActivityNotFoundException anfe) {
                    Toast.makeText(this, "anfe -- Activity not found", Toast.LENGTH_LONG).show();

                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }
            }
        } else {
            isRecording = false;
        }
    }

    private void requestPermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, recordAudioPermission);
        if(permissionCheck == PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, recordAudioPermission)){
            Toast.makeText(this, "Falls du die App richtig verwenden möchtest, musst die ihr die Rechte für das Benutzen des Mikrofons erteilen", Toast.LENGTH_LONG).show();
        }
        ActivityCompat.requestPermissions(this, new String[]{recordAudioPermission,internetPermission}, REQ_CODE_SPEECH_INPUT);
    }
    private void showPermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, recordAudioPermission);
        if(permissionCheck == PackageManager.PERMISSION_GRANTED)
        {
            //Toast.makeText(this, "permission granted", Toast.LENGTH_LONG).show();
        }
        if(permissionCheck == PackageManager.PERMISSION_DENIED)
        {
            //Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
        }
    }

    private String chooseLanguage (){

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        View spinnerView = getLayoutInflater().inflate(R.layout.language_spinner, null);
        builder.setTitle("Choose your language");
        languageSpinner = (Spinner)spinnerView.findViewById(R.id.language_spinner_id);

        languageListAdapter = new ArrayAdapter<String>(MainActivity.this,
                android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.languageList));

        languageListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSpinner.setAdapter(languageListAdapter);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                language = languageSpinner.getSelectedItem().toString();
                Toast.makeText(MainActivity.this, language,Toast.LENGTH_SHORT).show();
                dialogInterface.dismiss();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        builder.setView(spinnerView);
        AlertDialogActions = builder.create();
        AlertDialogActions.show();

        return language;
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
            chooseLanguage();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
