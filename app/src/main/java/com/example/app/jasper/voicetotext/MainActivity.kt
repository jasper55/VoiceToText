package com.example.app.jasper.voicetotext

import android.Manifest
import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.app.jasper.voicetotext.model.Language
import com.example.app.jasper.voicetotext.ui.RecyclerViewAdapter
import java.lang.Thread.sleep
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), RecyclerViewAdapter.UserActionClickListener {
    private lateinit var textField: EditText
    private lateinit var copyTextButton: ImageView
    private lateinit var current_language: TextView
    private lateinit var search_view: SearchView
    var isRecording = false
    private val REQ_CODE_SPEECH_INPUT = 100
    private val REQ_LANGUAGE_LIST = 200
    var settings_clicked_menu_dialog: CardView? = null
    var settings_clicked_menu_overlay: View? = null
    private lateinit var supportedLanguages: ArrayList<Language>
    var currentLanguageCode = "de_DE"
    var currentLanguage = "German"
    private lateinit var viewModel: ViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerViewAdapter: RecyclerViewAdapter
    private lateinit var userActionClickListener: RecyclerViewAdapter.UserActionClickListener


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_main)

        showPermission()
        requestPermission()

        getSupportedLanguages()

        viewModel = ViewModelProvider(this).get(ViewModel::class.java)
        viewModel.setInitialList(supportedLanguages)
        setInitialLanguage()

        initView()

        observeLiveData()
    } // onCreate()

    private fun setInitialLanguage() {
        val initialLang = Language()
        val locale = Locale.getDefault()
        initialLang.code = locale.language + "_" + locale.country
        initialLang.displayName = extractLanguage(locale.displayName)
        viewModel.setCurrentLanguage(initialLang)
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }

    private fun hideKeyboard(view: View) {
        val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }


    private fun initView() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        textField = findViewById(R.id.textContainer)
        copyTextButton = findViewById(R.id.copyButton)
        current_language = findViewById(R.id.current_language)
        current_language.text = currentLanguage
        initRecyclerView()
        initSearchView()
    }

    private fun extractLanguage(lang: String): String {
        return lang.split("(")[0].replace("\\s".toRegex(),"")
    }

    private fun initSearchView() {
        search_view = findViewById(R.id.search_view)
        search_view.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(textEntered: String): Boolean {
                viewModel.sortListBy(textEntered.toLowerCase())
//                hideKeyboard()
                return false
            }

            override fun onQueryTextChange(textEntered: String): Boolean {
                viewModel.sortListBy(textEntered.toLowerCase())
                return true
            }
        })

        search_view.setOnCloseListener {
//            hideKeyboard()
            recyclerView.visibility = View.GONE
            true
        }

//        search_view.onFocusChangeListener = View.OnFocusChangeListener { view, hasFocus ->
//            if (!hasFocus) {
//                hideKeyboard(view)
//            }
//        }
    }

    private fun initRecyclerView() {
        userActionClickListener = object : RecyclerViewAdapter.UserActionClickListener {
            override fun onItemClick(position: Int) {
                viewModel.setCurrentLanguage(viewModel.resultList.value!![position])
                recyclerView.visibility = View.GONE
            }
        }
        recyclerViewAdapter = RecyclerViewAdapter(userActionClickListener, viewModel.filteredList, viewModel)
        recyclerView = findViewById(R.id.recycler_view_data)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = recyclerViewAdapter
    }

    private fun getSupportedLanguages() {
        supportedLanguages = ArrayList()
        val langList = ArrayList<String>()

        for (locale in Locale.getAvailableLocales()) {
//            Log.d("LOCALES", "code: " + locale.language + "_" + locale.country)
            val language = Language()
            language.code = locale.language + "_" + locale.country

            val displayName = extractLanguage(locale.displayName)
//                    .toRegex()).toTypedArray()
//            Log.d("LOCALES", "${displayName[0]}")
            if (displayName == "Niederdeutsch") {
                language.displayName = "Deutsch"
            } else {
                language.displayName = displayName
            }

            for (lang in supportedLanguages) {
                langList.add(lang.displayName)
            }
            sleep(10)
            if (!langList.contains(language.displayName)) {
                Log.d("LOCALES", "$displayName")
                supportedLanguages.add(language)
            }
        }
    }

    private fun observeLiveData() {
        viewModel.resultList.observe(this, Observer { resultList: List<Language> ->
            recyclerView.visibility = View.VISIBLE
            recyclerViewAdapter.setList(resultList)
        })
        viewModel.currentLanguage.observe(this, Observer { currentLanguage: Language ->
            current_language.text = currentLanguage.displayName
            currentLanguage.code?.let {
                currentLanguageCode = it
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_CODE_SPEECH_INPUT) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                val result: ArrayList<*> = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                textField.setText(result[0] as CharSequence)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQ_CODE_SPEECH_INPUT -> if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "permission just granted", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "permission just denied", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun onClickCopy(view: View?) {
        val clipboard = this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val input = textField.text.toString()
        val clipData = ClipData.newPlainText("input", input)
        clipboard.setPrimaryClip(clipData)
        Toast.makeText(this, "text copied to clipboard!", Toast.LENGTH_LONG).show()
    }

    fun onClickRecord(view: View?) {
        if (!isRecording) {
            try {
                val recordIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                // Art und Weise, welchen Text man als Rückgabe erhällt
                recordIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH)
                //recordIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                recordIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                recordIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 100)
                recordIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, currentLanguageCode)
                recordIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "${viewModel.currentLanguage.value!!.displayName}\nSpeak now")
                startActivityForResult(recordIntent, REQ_CODE_SPEECH_INPUT)
                Toast.makeText(this, "Activity started", Toast.LENGTH_SHORT).show()
            } catch (e: ActivityNotFoundException) {
                val appPackageName = "com.google.android.googlequicksearchbox"
                Toast.makeText(this, "Activity not found", Toast.LENGTH_LONG).show()
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")))
                    Toast.makeText(this, "Action_View", Toast.LENGTH_LONG).show()
                } catch (answer: ActivityNotFoundException) {
                    Toast.makeText(this, "$answer -- Activity not found", Toast.LENGTH_LONG).show()
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")))
                }
            }
        } else {
            isRecording = false
        }
    }

    private fun requestPermission() {
        val permissionCheck = ContextCompat.checkSelfPermission(this, recordAudioPermission)
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            return
        }
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, recordAudioPermission)) {
            Toast.makeText(this, "Falls du die App richtig verwenden möchtest, musst die ihr die Rechte für das Benutzen des Mikrofons erteilen", Toast.LENGTH_LONG).show()
        }
        ActivityCompat.requestPermissions(this, arrayOf(recordAudioPermission, internetPermission), REQ_CODE_SPEECH_INPUT)
    }

    private fun showPermission() {
        val permissionCheck = ContextCompat.checkSelfPermission(this, recordAudioPermission)
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            //Toast.makeText(this, "permission granted", Toast.LENGTH_LONG).show();
        }
        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            //Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_settings) {
            return true
        }
        return if (id == R.id.action_language) {
            true
        } else super.onOptionsItemSelected(item)
    }

    companion object {
        const val recordAudioPermission = Manifest.permission.RECORD_AUDIO
        const val internetPermission = Manifest.permission.INTERNET
    }

    override fun onItemClick(position: Int) {
        currentLanguage = recyclerViewAdapter.getItem(position).displayName
    }
}