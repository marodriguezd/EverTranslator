package tw.firemaples.onscreenocr.pages.setting

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.widget.TextView
import androidx.preference.PreferenceViewHolder
import androidx.preference.SeekBarPreference
import java.util.Locale

class DecimalSeekBarPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.preference.R.attr.seekBarPreferenceStyle
) : SeekBarPreference(context, attrs, defStyleAttr) {

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        val valueTextView = holder.findViewById(androidx.preference.R.id.seekbar_value) as? TextView
        if (valueTextView != null) {
            (valueTextView.tag as? TextWatcher)?.let { oldWatcher ->
                valueTextView.removeTextChangedListener(oldWatcher)
            }

            val textWatcher = object : TextWatcher {
                private var isFormatting = false

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    if (isFormatting || s == null) return
                    val text = s.toString()
                    if (text.endsWith(" s")) return

                    val deciValue = text.toIntOrNull() ?: return
                    val formatted = formatValue(deciValue)

                    isFormatting = true
                    valueTextView.text = formatted
                    isFormatting = false
                }
            }

            valueTextView.tag = textWatcher
            valueTextView.addTextChangedListener(textWatcher)

            val currentText = valueTextView.text.toString()
            if (!currentText.endsWith(" s")) {
                valueTextView.text = formatValue(value)
            }
        }
    }

    companion object {
        fun formatValue(deciSecondsValue: Int): String {
            val deciSeconds = if (deciSecondsValue in 1..4) deciSecondsValue * 10 else deciSecondsValue
            val seconds = deciSeconds / 10.0
            return String.format(Locale.getDefault(), "%.1f s", seconds)
        }
    }
}
