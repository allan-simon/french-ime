package dot.dot

import android.inputmethodservice.InputMethodService
import android.inputmethodservice.Keyboard  // Deprecated but still needed for Android 12 IME
import android.inputmethodservice.KeyboardView  // Deprecated but still needed for Android 12 IME
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputConnection
import android.os.Vibrator
import android.content.Context
import android.media.AudioManager
import android.os.VibrationEffect
import android.os.Build
import android.view.inputmethod.EditorInfo

class FrenchIMEService : InputMethodService(), KeyboardView.OnKeyboardActionListener {
    // KeyboardView is deprecated but is still the main way to implement IME in Android 12
    private lateinit var keyboardView: KeyboardView
    // Keyboard class is deprecated but required for basic IME functionality in Android 12
    private var keyboard: Keyboard? = null
    private var isSymbolsKeyboard = false
    
    private var caps = false
    private var lastShiftTime: Long = 0
    private val DOUBLE_CLICK_TIMEOUT = 300L
    
    private var vibrator: Vibrator? = null
    private var audioManager: AudioManager? = null
    
    // French specific characters mapping for long press
    private val accentMap = mapOf(
        'e' to listOf('é', 'è', 'ê', 'ë'),
        'a' to listOf('à', 'â', 'æ'),
        'i' to listOf('î', 'ï'),
        'o' to listOf('ô', 'œ'),
        'u' to listOf('ù', 'û', 'ü'),
        'y' to listOf('ÿ'),
        'c' to listOf('ç')
    )

    override fun onCreate() {
        super.onCreate()
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    override fun onCreateInputView(): View {
        // Using deprecated KeyboardView as it's still the standard way to create IMEs in Android 12
        keyboardView = layoutInflater.inflate(R.layout.keyboard_view, null) as KeyboardView
        keyboard = Keyboard(this, R.xml.french_azerty)  // Deprecated constructor but needed for XML layouts
        keyboardView.keyboard = keyboard  // Deprecated setter but required for keyboard functionality
        keyboardView.setOnKeyboardActionListener(this)
        return keyboardView
    }

    override fun onStartInputView(info: EditorInfo, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        // Reset keyboard state
        caps = false
        updateShiftKey()
    }

    private fun playClick(keyCode: Int) {
        val prefs = getSharedPreferences("ime_settings", Context.MODE_PRIVATE)
        val audioEnabled = prefs.getBoolean("settings_sound", false)
        
        if (audioEnabled) {
            audioManager?.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD, -1f)
        }

        val vibrationEnabled = prefs.getBoolean("settings_vibration", true)
            
        if (vibrationEnabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                // Deprecated vibrate call but needed for older versions
                vibrator?.vibrate(20)
            }
        }
    }

    override fun onKey(primaryCode: Int, keyCodes: IntArray?) {
        val ic = currentInputConnection ?: return
        playClick(primaryCode)

        when (primaryCode) {
            // These keycodes are deprecated but still standard for IME implementation
            Keyboard.KEYCODE_DELETE -> handleBackspace(ic)
            Keyboard.KEYCODE_SHIFT -> handleShift()
            Keyboard.KEYCODE_DONE -> handleEnter(ic)
            Keyboard.KEYCODE_MODE_CHANGE -> handleSymbolsSwitch()  // Deprecated but still used for keyboard switching
            Keyboard.KEYCODE_ALT -> handleAccents(ic)  // Deprecated but needed for accent handling
            else -> handleCharacter(primaryCode, ic)
        }
    }

    private fun handleBackspace(ic: InputConnection) {
        val selectedText = ic.getSelectedText(0)
        if (TextUtils.isEmpty(selectedText)) {
            ic.deleteSurroundingText(1, 0)
        } else {
            ic.commitText("", 1)
        }
    }

    private fun handleShift() {
        val now = System.currentTimeMillis()
        if (lastShiftTime + DOUBLE_CLICK_TIMEOUT > now) {
            caps = true
            // setShifted and invalidateAllKeys are deprecated but required for shift functionality
            keyboard?.setShifted(true)
            keyboardView.invalidateAllKeys()
        } else {
            caps = !caps
            keyboard?.setShifted(caps)
            keyboardView.invalidateAllKeys()
        }
        lastShiftTime = now
    }

    private fun handleEnter(ic: InputConnection) {
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
    }

    private fun handleSymbolsSwitch() {
        isSymbolsKeyboard = !isSymbolsKeyboard
        // Using deprecated Keyboard constructor but needed for layout switching
        keyboard = Keyboard(this, if (isSymbolsKeyboard) {
            R.xml.french_symbols
        } else {
            R.xml.french_azerty
        })
        keyboardView.keyboard = keyboard
        updateShiftKey()
    }

    private fun handleAccents(ic: InputConnection) {
        // Get the character before cursor
        val beforeCursor = ic.getTextBeforeCursor(1, 0)
        if (beforeCursor != null && beforeCursor.length == 1) {
            val char = beforeCursor[0].lowercaseChar()
            accentMap[char]?.let { accents ->
                // Show accent picker dialog or handle accent selection
                showAccentPickerDialog(accents) { selectedAccent ->
                    ic.deleteSurroundingText(1, 0)
                    ic.commitText(selectedAccent.toString(), 1)
                }
            }
        }
    }

    private fun handleCharacter(primaryCode: Int, ic: InputConnection) {
        var code = primaryCode.toChar()
        if (Character.isLetter(code) && caps) {
            code = code.uppercaseChar()
        }
        ic.commitText(code.toString(), 1)
    }

    private fun updateShiftKey() {
        // These methods are deprecated but still needed for shift state updates
        keyboard?.setShifted(caps)
        keyboardView.invalidateAllKeys()
    }

    // Method stub - implement later
    private fun showAccentPickerDialog(
        accents: List<Char>,
        onAccentSelected: (Char) -> Unit
    ) {
        // TODO: Implement accent picker dialog
    }

    // Required interface implementations
    override fun onPress(primaryCode: Int) {}
    override fun onRelease(primaryCode: Int) {}
    override fun onText(text: CharSequence?) {}
    override fun swipeLeft() {}
    override fun swipeRight() {}
    override fun swipeDown() {}
    override fun swipeUp() {}
}
