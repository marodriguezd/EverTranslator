package tw.firemaples.onscreenocr.pages.setting

import android.content.Context
import android.util.AttributeSet
import android.widget.SeekBar
import android.widget.TextView
import androidx.preference.PreferenceViewHolder
import androidx.preference.SeekBarPreference
import java.util.Locale

class DecimalSeekBarPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.preference.R.attr.seekBarPreferenceStyle
) : SeekBarPreference(context, attrs, defStyleAttr) {

    private var valueTextView: TextView? = null

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        valueTextView = holder.findViewById(androidx.preference.R.id.seekbar_value) as? TextView

        updateText(value)

        val seekBar = holder.findViewById(androidx.preference.R.id.seekbar) as? SeekBar
        seekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val currentDeci = progress + min
                updateText(currentDeci)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let {
                    val newDeci = it.progress + min
                    value = newDeci
                }
            }
        })
    }

    private fun updateText(deciSecondsValue: Int) {
        val deciSeconds = if (deciSecondsValue in 1..4) deciSecondsValue * 10 else deciSecondsValue
        val seconds = deciSeconds / 10.0
        val formatted = String.format(Locale.getDefault(), "%.1f s", seconds)
        valueTextView?.text = formatted
        summary = formatted
    }
}
