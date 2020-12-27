package com.example.app.jasper.voicetotext

import android.Manifest
import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.app.jasper.voicetotext.databinding.ActivityMainBinding
import com.example.app.jasper.voicetotext.model.Language
import com.example.app.jasper.voicetotext.ui.RecyclerViewAdapter
import com.example.app.jasper.voicetotext.ui.hideSoftKeyboard
import com.example.app.jasper.voicetotext.ui.showSnackBarWithText
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), RecyclerViewAdapter.UserActionClickListener {
    private lateinit var binding: ActivityMainBinding

    var isRecording = false
    private val REQ_CODE_SPEECH_INPUT = 100
    var settings_clicked_menu_dialog: CardView? = null
    var settings_clicked_menu_overlay: View? = null
    private lateinit var supportedLanguages: ArrayList<Language>
    var currentLanguageCode = "de_DE"
    var currentLanguage = ""
    private lateinit var viewModel: ViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerViewAdapter: RecyclerViewAdapter
    private lateinit var userActionClickListener: RecyclerViewAdapter.UserActionClickListener


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showPermission()
        requestPermission()
        getSupportedLanguages()

        viewModel = ViewModelProvider(this).get(ViewModel::class.java)
        viewModel.setInitialList(supportedLanguages)
        setInitialLanguage()

        initView()

        observeLiveData()
    } // onCreate()


    private fun initView() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        current_language.text = currentLanguage

        replace_append_switch.isChecked = false
        replace_append_switch.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(p0: CompoundButton?, isChecked: Boolean) {
                if (isChecked) {
                    append.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.colorAccent))
                    append.setTypeface(null, Typeface.BOLD)
                    replace.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.grey))
                    replace.setTypeface(null, Typeface.NORMAL)
                } else {
                    append.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.grey))
                    append.setTypeface(null, Typeface.NORMAL)
                    replace.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.colorAccent))
                    replace.setTypeface(null, Typeface.BOLD)
                }
            }
        })

        initRecyclerView()
        initSearchView()
    }

    private fun extractLanguage(lang: String): String {
        return lang.split("(")[0].replace("\\s".toRegex(), "")
    }

    private fun initSearchView() {
        search_view.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(textEntered: String): Boolean {
                viewModel.sortListBy(textEntered.toLowerCase())
                return false
            }

            override fun onQueryTextChange(textEntered: String): Boolean {
                if (textEntered == getString(R.string.empty_text)) {
                    return false
                } else {
                    viewModel.sortListBy(textEntered.toLowerCase())
                    return true
                }
            }
        })
        search_view.setOnSearchClickListener { hideBottomUi() }



        search_view.setOnCloseListener {
            hideSoftKeyboard()
            showBottomUi()
            recyclerView.visibility = View.GONE
            true
        }
    }
    private fun hideBottomUi() {
        bottom_group.visibility = View.GONE
    }

    private fun showBottomUi() {
        bottom_group.visibility = View.VISIBLE
    }

    private fun initRecyclerView() {
        userActionClickListener = object : RecyclerViewAdapter.UserActionClickListener {
            override fun onItemClick(position: Int) {
                viewModel.setCurrentLocale(viewModel.resultList.value!![position])
                val text: String = getString((R.string.current_language), extractLanguage(viewModel.currentLocale.value!!.displayName))
                showSnackBarWithText(binding.root,text)
                hideSoftKeyboard()
                if (!search_view.isIconified) {
                    search_view.isIconified = true
                }
//                search_view.setQuery(getString(R.string.empty_text),false)
                showBottomUi()
                search_view.clearFocus()
                recyclerView.visibility = View.GONE
            }
        }
        recyclerViewAdapter = RecyclerViewAdapter(userActionClickListener, viewModel.filteredList, viewModel)
        recyclerView = findViewById(R.id.recycler_view_data)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = recyclerViewAdapter
    }

    private fun observeLiveData() {
        viewModel.resultList.observe(this, Observer { resultList: List<Language> ->
            recyclerView.visibility = View.VISIBLE
            recyclerViewAdapter.setList(resultList)
        })
        viewModel.currentLocale.observe(this, Observer { currentLanguage: Language ->
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
                if (replace_append_switch.isChecked) {
                    val defaultText = resources.getString(R.string.default_output_text)
                    var oldText = output_container.text.toString()
                    var newText = ". ${result[0].toString().capitalize()}"
                    if (oldText == defaultText) {
                        oldText = ""
                        newText = result[0].toString().capitalize()
                    }
                    val text = "$oldText$newText"
                    output_container.setText(text as CharSequence)
                } else {
                    output_container.setText(result[0] as CharSequence)
                }
            }
        }
    }



    fun onClickCopy(view: View) {
        val clipboard = this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val input = output_container.text.toString()
        val clipData = ClipData.newPlainText("input", input)
        clipboard.setPrimaryClip(clipData)
        showSnackBarWithText(binding.root,getString(R.string.text_copied_to_clipboard))
    }

    fun onClickRecord(view: View) {
        if (!isRecording) {
            try {
                val recordIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                // Art und Weise, welchen Text man als Rückgabe erhällt
                recordIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH)
                //recordIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                recordIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                recordIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 100)
                recordIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, currentLanguageCode)

                recordIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "${viewModel.currentLocale.value!!.displayName}" +
                        "\n" +
                        getString(R.string.speak_now))
                startActivityForResult(recordIntent, REQ_CODE_SPEECH_INPUT)
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQ_CODE_SPEECH_INPUT -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showSnackBarWithText(binding.root,getString(R.string.permission_granted))
            } else {
                showSnackBarWithText(binding.root,getString(R.string.permission_denied))
            }
        }
    }

    private fun requestPermission() {
        val permissionCheck = ContextCompat.checkSelfPermission(this, recordAudioPermission)
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            return
        }
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, recordAudioPermission)) {
            Toast.makeText(this, R.string.grant_rights_to_microphone, Toast.LENGTH_LONG).show()
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

    override fun onItemClick(position: Int) {
        currentLanguage = recyclerViewAdapter.getItem(position).displayName
    }

    private fun setInitialLanguage() {
        val initialLang = Language()
        val locale = Locale.getDefault()
        initialLang.code = locale.language + "_" + locale.country
        initialLang.displayName = extractLanguage(locale.displayName)
        viewModel.setCurrentLocale(initialLang)
    }

    private fun getSupportedLanguages() {
        supportedLanguages = ArrayList()
        val langList = ArrayList<String>()

        for (locale in Locale.getAvailableLocales()) {
            val language = Language()
            language.code = locale.language + "_" + locale.country

            val displayName = extractLanguage(locale.displayName)
            if (displayName == "Niederdeutsch") {
                language.displayName = "Deutsch"
            } else {
                language.displayName = displayName
            }

            for (lang in supportedLanguages) {
                langList.add(lang.displayName)
            }
            if (!langList.contains(language.displayName)) {
                supportedLanguages.add(language)
            }
        }
    }

    fun shareText(view: View) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, output_container.text.toString())
        startActivity(Intent.createChooser(intent, getString(R.string.share_subject)))
    }


    companion object {
        const val recordAudioPermission = Manifest.permission.RECORD_AUDIO
        const val internetPermission = Manifest.permission.INTERNET
    }
}