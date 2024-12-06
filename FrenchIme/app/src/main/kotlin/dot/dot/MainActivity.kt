package dot.dot

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.Toast

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val button = Button(this).apply {
            text = if (isImeEnabled()) "Keyboard Enabled" else "Enable Keyboard"
            setOnClickListener {
                if (!isImeEnabled()) {
                    // Open IME settings if not enabled
                    startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
                } else {
                    // Show status if already enabled
                    Toast.makeText(context, "Keyboard is already enabled!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        setContentView(button)
    }

    private fun isImeEnabled(): Boolean {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        val enabledImis = imm.enabledInputMethodList
        val imeId = packageName + "/.FrenchIME"
        return enabledImis.any { it.id == imeId }
    }
}
