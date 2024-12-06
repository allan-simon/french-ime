package dot.dot
import android.os.Bundle
import android.widget.Switch
import android.content.SharedPreferences
import android.content.Context
import android.widget.TextView
import android.widget.LinearLayout
import android.app.Activity
import android.content.Intent
import android.provider.Settings
import android.view.MenuItem

class IMESettingsActivity : Activity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var containerLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable back button in action bar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        setTitle(getString(R.string.settings_name))

        // Initialize SharedPreferences
        prefs = getSharedPreferences("ime_settings", Context.MODE_PRIVATE)
        
        // Create the layout programmatically
        containerLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 16, 32, 16)
        }
        setContentView(containerLayout)

        // Check if IME is enabled
        if (!isImeEnabled()) {
            addEnableImeButton()
        }

        // Add settings switches
        addSettingSwitch(
            "settings_vibration",
            getString(R.string.settings_vibration),
            getString(R.string.settings_vibration_summary),
            true
        )

        addSettingSwitch(
            "settings_sound",
            getString(R.string.settings_sound),
            getString(R.string.settings_sound_summary),
            false
        )

        addSettingSwitch(
            "settings_auto_caps",
            getString(R.string.settings_auto_caps),
            getString(R.string.settings_auto_caps_summary),
            true
        )

        addSettingSwitch(
            "settings_accents",
            getString(R.string.settings_accents),
            getString(R.string.settings_accents_summary),
            true
        )

        addSettingSwitch(
            "settings_suggestions",
            getString(R.string.settings_suggestions),
            getString(R.string.settings_suggestions_summary),
            true
        )
    }

    private fun addSettingSwitch(
        prefKey: String,
        title: String,
        summary: String,
        defaultValue: Boolean
    ) {
        // Create container for this setting
        val settingContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 16, 0, 16)
        }

        // Create and configure the switch
        val switch = Switch(this).apply {
            text = title
            isChecked = prefs.getBoolean(prefKey, defaultValue)
            setOnCheckedChangeListener { _, isChecked ->
                prefs.edit().putBoolean(prefKey, isChecked).apply()
            }
        }

        // Create and configure the summary text
        val summaryText = TextView(this).apply {
            text = summary
            setTextColor(resources.getColor(android.R.color.darker_gray))
            setPadding(0, 8, 0, 0)
        }

        // Add views to container
        settingContainer.addView(switch)
        settingContainer.addView(summaryText)

        // Add setting container to main layout
        containerLayout.addView(settingContainer)
    }

    private fun addEnableImeButton() {
        val warningText = TextView(this).apply {
            text = getString(R.string.error_ime_not_enabled)
            setTextColor(resources.getColor(android.R.color.holo_red_dark))
            setPadding(0, 16, 0, 32)
        }

        val enableButton = android.widget.Button(this).apply {
            text = "Enable Keyboard"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
            }
        }

        containerLayout.addView(warningText)
        containerLayout.addView(enableButton)
    }

    private fun isImeEnabled(): Boolean {
        val imeManager = getSystemService(Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        val packageLocal = packageName
        
        for (id in imeManager.enabledInputMethodList) {
            if (id.packageName == packageLocal) return true
        }
        return false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
