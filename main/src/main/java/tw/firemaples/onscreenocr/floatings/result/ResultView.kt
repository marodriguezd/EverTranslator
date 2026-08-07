package tw.firemaples.onscreenocr.floatings.result

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import android.text.method.ScrollingMovementMethod
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import java.util.Locale
import tw.firemaples.onscreenocr.R
import androidx.core.graphics.ColorUtils
import com.google.android.material.card.MaterialCardView
import tw.firemaples.onscreenocr.databinding.FloatingResultViewBinding
import tw.firemaples.onscreenocr.databinding.ViewResultPanelBinding
import tw.firemaples.onscreenocr.floatings.base.FloatingView
import tw.firemaples.onscreenocr.floatings.manager.Result
import tw.firemaples.onscreenocr.floatings.recognizedTextEditor.RecognizedTextEditor
import tw.firemaples.onscreenocr.floatings.textInfoSearch.TextInfoSearchView
import tw.firemaples.onscreenocr.pages.setting.SettingManager
import tw.firemaples.onscreenocr.recognition.RecognitionResult
import tw.firemaples.onscreenocr.translator.TranslationProviderType
import tw.firemaples.onscreenocr.translator.utils.GoogleTranslateUtils
import tw.firemaples.onscreenocr.utils.Logger
import tw.firemaples.onscreenocr.utils.UIUtils
import tw.firemaples.onscreenocr.utils.Utils
import tw.firemaples.onscreenocr.utils.clickOnce
import tw.firemaples.onscreenocr.utils.dpToPx
import tw.firemaples.onscreenocr.utils.getViewRect
import tw.firemaples.onscreenocr.utils.setReusable
import tw.firemaples.onscreenocr.utils.setTextOrGone
import tw.firemaples.onscreenocr.utils.showOrHide

class ResultView(context: Context) : FloatingView(context) {
    companion object {
        private const val LABEL_RECOGNIZED_TEXT = "Recognized text"
        private const val LABEL_TRANSLATED_TEXT = "Translated text"
    }

    private val logger: Logger by lazy { Logger(ResultView::class) }

    override val layoutId: Int
        get() = R.layout.floating_result_view

    override val layoutWidth: Int
        get() = WindowManager.LayoutParams.MATCH_PARENT

    override val layoutHeight: Int
        get() = WindowManager.LayoutParams.MATCH_PARENT

    override val enableHomeButtonWatcher: Boolean
        get() = true

    // Touch pass-through is only wanted while subtitles are being refreshed continuously:
    // there the overlay is purely informative and must not block the app underneath.
    // For a one-shot translation the panel has to stay interactive so the user can
    // scroll a long translation, so the window keeps receiving touches.
    override val passThroughTouches: Boolean
        get() = SettingManager.translationOnlyMode &&
                SettingManager.enableContinuousTranslation

    private val viewModel: ResultViewModel by lazy { ResultViewModel(viewScope) }

    private val binding: FloatingResultViewBinding = FloatingResultViewBinding.bind(rootLayout)

    private val viewRoot: RelativeLayout = binding.viewRoot

    var onUserDismiss: (() -> Unit)? = null

    private val viewResultWindow: View = binding.viewResultWindow

    private var unionRect: Rect = Rect()

    private var croppedBitmap: Bitmap? = null

    init {
        binding.resultPanel.setViews()
        applyTranslationOnlyMode()
    }

    /**
     * Bubble-Translate-like minimal overlay: only the translated text stays visible.
     * The OCR block, both icon bars, the provider attribution and the dimmed background
     * are all removed, leaving just the semi-transparent card with the translation.
     */
    private fun applyTranslationOnlyMode() {
        applyTranslationTextStyle()

        if (!SettingManager.translationOnlyMode) return

        // No full-screen dimming: the user keeps seeing the app underneath.
        viewRoot.setBackgroundColor(Color.TRANSPARENT)

        with(binding.resultPanel) {
            wrapperOcrButtons.visibility = View.GONE
            wrapperOcrResult.visibility = View.GONE
            wrapperTranslatedButtons.visibility = View.GONE
            tvTranslationProvider.visibility = View.GONE
            ivTranslatedByGoogle.visibility = View.GONE
        }

        // Bounding boxes over the source text are noise in this mode.
        binding.viewTextBoundingBoxView.visibility = View.GONE
    }

    /**
     * Applies the user-configurable appearance of the translated text: fully opaque
     * colour and font size for the text, and a separate translucency for the panel
     * behind it, so the text never becomes hard to read.
     */
    private fun applyTranslationTextStyle() {
        val textColor = SettingManager.translationTextColor
        val textSize = SettingManager.translationTextSize

        binding.resultPanel.tvTranslatedText.apply {
            setTextColor(textColor)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize)
        }

        // Panel translucency is applied to the card only, never to the text.
        val alpha = (SettingManager.translationPanelOpacity * 255 / 100)
            .coerceIn(0, 255)
        (viewResultWindow as? MaterialCardView)?.apply {
            setCardBackgroundColor(
                ColorUtils.setAlphaComponent(cardBackgroundColor.defaultColor, alpha)
            )
        }
    }

    private fun ViewResultPanelBinding.setViews() {
        viewModel.displayOCROperationProgress.observe(lifecycleOwner) {
            pbOcrOperating.showOrHide(it)
        }
        viewModel.displayTranslationProgress.observe(lifecycleOwner) {
            pbTranslationOperating.showOrHide(it)
        }
        viewModel.displaySelectableText.observe(lifecycleOwner) {
            textSelectable.isChecked = it
            tvOcrText.showOrHide(!it)
            tvWordBreakOcrText.showOrHide(it)
        }
        viewModel.ocrText.observe(lifecycleOwner) {
            tvWordBreakOcrText.setContent(it?.text(), it?.locale() ?: Locale.getDefault())
            tvOcrText.text = it?.text()
        }
        viewModel.translatedText.observe(lifecycleOwner) {
            if (it == null) {
                tvTranslatedText.text = null
            } else {
                val (text, color) = it
                tvTranslatedText.text = text
                // The user-chosen colour wins over the default result colour, except for
                // error/notice colours which must stay recognisable.
                if (color == R.color.foregroundSecond) {
                    tvTranslatedText.setTextColor(SettingManager.translationTextColor)
                } else {
                    tvTranslatedText.setTextColor(ContextCompat.getColor(context, color))
                }
            }

            reposition()
        }

        viewModel.displayRecognitionBlock.observe(lifecycleOwner) {
            // In translation-only mode the OCR block must stay hidden regardless of state.
            groupRecognitionViews.showOrHide(it && !SettingManager.translationOnlyMode)
        }
        viewModel.displayTranslatedBlock.observe(lifecycleOwner) {
            groupTranslationViews.showOrHide(it)
            if (SettingManager.translationOnlyMode) {
                // The group also references the icon bar and attribution; re-hide them.
                wrapperTranslatedButtons.visibility = View.GONE
                tvTranslationProvider.visibility = View.GONE
                ivTranslatedByGoogle.visibility = View.GONE
            }
        }

        viewModel.translationProviderText.observe(lifecycleOwner) {
            if (!SettingManager.translationOnlyMode) tvTranslationProvider.setTextOrGone(it)
        }
        viewModel.displayTranslatedByGoogle.observe(lifecycleOwner) {
            ivTranslatedByGoogle.showOrHide(it && !SettingManager.translationOnlyMode)
        }

        viewModel.displayRecognizedTextAreas.observe(lifecycleOwner) {
            val (boundingBoxes, unionRect) = it
            binding.viewTextBoundingBoxView.boundingBoxes = boundingBoxes
            updateSelectedAreas(unionRect)
        }

        viewModel.copyRecognizedText.observe(lifecycleOwner) {
            Utils.copyToClipboard(LABEL_RECOGNIZED_TEXT, it)
        }

        viewModel.fontSize.observe(lifecycleOwner) {
            tvOcrText.setTextSize(TypedValue.COMPLEX_UNIT_SP, it)
            tvWordBreakOcrText.setTextSize(TypedValue.COMPLEX_UNIT_SP, it)
            // The translated text has its own dedicated size setting.
            tvTranslatedText.setTextSize(
                TypedValue.COMPLEX_UNIT_SP, SettingManager.translationTextSize
            )
        }

        viewModel.displayTextInfoSearchView.observe(lifecycleOwner) {
            TextInfoSearchView(context, it.text, it.sourceLang, it.targetLang)
                .attachToScreen()
        }

        textSelectable.setOnCheckedChangeListener { _, checked ->
            viewModel.onTextSelectableChecked(checked)
        }
        tvWordBreakOcrText.onWordClicked = { word ->
            if (word != null) {
                viewModel.onWordSelected(word)
                tvWordBreakOcrText.clearSelection()
            }
        }
        tvOcrText.movementMethod = ScrollingMovementMethod()
        tvTranslatedText.movementMethod = ScrollingMovementMethod()
        viewRoot.clickOnce { onUserDismiss?.invoke() }
        btEditOCRText.clickOnce {
            showRecognizedTextEditor(viewModel.ocrText.value?.text() ?: "")
        }
        btCopyOCRText.clickOnce {
            Utils.copyToClipboard(LABEL_RECOGNIZED_TEXT, viewModel.ocrText.value?.text() ?: "")
        }
        btCopyTranslatedText.clickOnce {
            Utils.copyToClipboard(LABEL_TRANSLATED_TEXT, tvTranslatedText.text.toString())
        }
        btTranslateOCRTextWithGoogleTranslate.clickOnce {
            GoogleTranslateUtils.launchTranslator(viewModel.ocrText.value?.text() ?: "")
            onUserDismiss?.invoke()
        }
        btTranslateTranslatedTextWithGoogleTranslate.clickOnce {
            GoogleTranslateUtils.launchTranslator(tvTranslatedText.text.toString())
            onUserDismiss?.invoke()
        }
        btShareOCRText.clickOnce {
            val ocrText = viewModel.ocrText.value?.text() ?: return@clickOnce
            Utils.shareText(ocrText)
            onUserDismiss?.invoke()
        }
        btAdjustFontSize.clickOnce {
            FontSizeAdjuster(context).attachToScreen()
        }
    }

    private fun showRecognizedTextEditor(recognizedText: String) {
        RecognizedTextEditor(
            context = context,
            review = croppedBitmap,
            text = recognizedText,
            onSubmit = {
                if (it.isNotBlank() && it.trim() != recognizedText) {
                    viewModel.onOCRTextEdited(it.trim())
                }
            },
        ).attachToScreen()
    }

    override fun onAttachedToScreen() {
        super.onAttachedToScreen()
        viewResultWindow.visibility = View.INVISIBLE
    }

    override fun onDetachedFromScreen() {
        super.onDetachedFromScreen()
        this.croppedBitmap?.setReusable()
        this.croppedBitmap = null
    }

    override fun onHomeButtonPressed() {
        super.onHomeButtonPressed()
        onUserDismiss?.invoke()
    }

    fun startRecognition() {
        attachToScreen()
        viewModel.startRecognition()
    }

    fun textRecognized(
        result: RecognitionResult,
        parent: Rect,
        selected: Rect,
        croppedBitmap: Bitmap
    ) {
        this.croppedBitmap = croppedBitmap
        viewModel.textRecognized(result, parent, selected, rootView.getViewRect())
    }

    fun startTranslation(translationProviderType: TranslationProviderType) {
        viewModel.startTranslation(translationProviderType)
    }

    fun textTranslated(result: Result) {
        viewModel.textTranslated(result)
    }

    fun backToIdle() {
        detachFromScreen()
    }

    private fun updateSelectedAreas(unionRect: Rect) {
        this.unionRect = unionRect
        reposition()
    }

    private fun reposition() {
        rootView.post {
            val parentRect = viewRoot.getViewRect()
            val anchorRect = Rect(unionRect).apply {
                top += parentRect.top
                left += parentRect.left
                bottom += parentRect.top
                right += parentRect.left
            }
            val windowRect = viewResultWindow.getViewRect()

            val (leftMargin, topMargin) = UIUtils.countViewPosition(
                anchorRect, parentRect,
                windowRect.width(), windowRect.height(), 2.dpToPx(),
            )

            val layoutParams =
                (viewResultWindow.layoutParams as RelativeLayout.LayoutParams).apply {
                    this.leftMargin = leftMargin
                    this.topMargin = topMargin
                }

            viewRoot.updateViewLayout(viewResultWindow, layoutParams)

            viewRoot.post {
                val hasTranslatedText = !binding.resultPanel.tvTranslatedText.text.isNullOrBlank()
                val shouldShow = if (SettingManager.translationOnlyMode) {
                    hasTranslatedText
                } else if (SettingManager.hideRecognizedResultAfterTranslated) {
                    hasTranslatedText || viewModel.displayRecognitionBlock.value == true
                } else {
                    true
                }
                viewResultWindow.visibility = if (shouldShow) View.VISIBLE else View.INVISIBLE
            }
        }
    }
}
