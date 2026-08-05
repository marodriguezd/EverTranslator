package tw.firemaples.onscreenocr.translator.googlemlkit

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.floatings.dialog.DialogView
import tw.firemaples.onscreenocr.log.FirebaseEvent
import tw.firemaples.onscreenocr.pref.AppPref
import tw.firemaples.onscreenocr.translator.TranslationLanguage
import tw.firemaples.onscreenocr.translator.TranslationProviderType
import tw.firemaples.onscreenocr.translator.TranslationResult
import tw.firemaples.onscreenocr.translator.Translator
import tw.firemaples.onscreenocr.utils.firstPart

object GoogleMLKitTranslator : Translator {
    private const val DOWNLOAD_SITE = "GoogleMLKit"

    private val remoteModelManager: RemoteModelManager by lazy { RemoteModelManager.getInstance() }

    override val type: TranslationProviderType
        get() = TranslationProviderType.GoogleMLKit

    private var lastTranslatorLangKey: String? = null
    private var lastTranslator: com.google.mlkit.nl.translate.Translator? = null

    override suspend fun supportedLanguages(): List<TranslationLanguage> {
        val langCodeList =
            context.resources.getStringArray(R.array.google_MLKit_translationLangCode_iso6391)
        val langNameList =
            context.resources.getStringArray(R.array.google_MLKit_translationLangName)

        val selectedLangCode = selectedLangCode(langCodeList)

        return (langCodeList.indices).map { i ->
            val code = langCodeList[i]
            val name = langNameList[i]

            TranslationLanguage(
                code = code,
                displayName = name,
                selected = code == selectedLangCode
            )
        }
    }

    override suspend fun checkEnvironment(coroutineScope: CoroutineScope): Boolean =
        checkTranslationResources(coroutineScope)

    override suspend fun translate(text: String, sourceLangCode: String): TranslationResult {
        if (!isLangSupport()) {
            return TranslationResult.SourceLangNotSupport(type)
        }

        val targetLangCode = supportedLanguages().firstOrNull { it.selected }?.code
            ?: return TranslationResult.TranslationFailed(IllegalArgumentException("Selected language code is not found"))
        val sourceLang = TranslateLanguage.fromLanguageTag(sourceLangCode.firstPart())
            ?: return TranslationResult.TranslationFailed(IllegalArgumentException("Parsing language tag failed, sourceLangCode: $sourceLangCode"))
        val targetLang = TranslateLanguage.fromLanguageTag(targetLangCode)
            ?: return TranslationResult.TranslationFailed(IllegalArgumentException("Parsing language tag failed, targetLangCode: $targetLangCode"))

        val langKey = "${sourceLang}_$targetLang"

        val lastTranslatorLangKey = lastTranslatorLangKey
        val lastTranslator = lastTranslator

        val client =
            if (lastTranslatorLangKey == langKey && lastTranslator != null) lastTranslator
            else {
                if (lastTranslator != null) {
                    lastTranslator.close()
                    GoogleMLKitTranslator.lastTranslator = null
                }

                Translation.getClient(
                    TranslatorOptions.Builder()
                        .setSourceLanguage(sourceLang)
                        .setTargetLanguage(targetLang)
                        .build()
                ).also {
                    GoogleMLKitTranslator.lastTranslatorLangKey = langKey
                    GoogleMLKitTranslator.lastTranslator = it
                }
            }

        return suspendCoroutine { c ->
            client.translate(text)
                .addOnSuccessListener {
                    c.resume(
                        TranslationResult.TranslatedResult(
                            it, type
                        )
                    )
                }
                .addOnFailureListener {
                    c.resumeWithException(it)
                }
        }
    }

    private suspend fun checkTranslationResources(coroutineScope: CoroutineScope): Boolean {
        val langList = supportedLanguages().filter {
            it.code.firstPart() == AppPref.selectedOCRLang.firstPart()
                    || it.code.firstPart() == AppPref.selectedTranslationLang.firstPart()
        }.map { it.code }.toList()

        val langToDownload = try {
            checkResources(langList)
        } catch (e: Exception) {
            FirebaseEvent.logException(e)

            DialogView(context).apply {
                setTitle(context.getString(R.string.title_failed_to_check_resources))
                setMessage(e.localizedMessage ?: context.getString(R.string.error_unknown))
                setDialogType(DialogView.DialogType.CONFIRM_ONLY)
            }.attachToScreen()
            return false
        }

        if (langToDownload.isNotEmpty()) {
            DialogView(context).apply {
                setTitle(context.getString(R.string.title_download))
                setMessage(
                    context.getString(R.string.msg_models_to_download) +
                            "\n\n${displayNamesOf(langToDownload)}"
                )
                setDialogType(DialogView.DialogType.CONFIRM_CANCEL)

                onButtonOkClicked = {
                    coroutineScope.launch {
                        downloadTranslationResources(langToDownload)
                    }
                }
            }.attachToScreen()

            FirebaseEvent.logShowOCRFilesNotFoundAlert()

            return false
        }
        return true
    }

    private suspend fun checkResources(langList: List<String>): List<String> =
        suspendCoroutine {
            remoteModelManager.getDownloadedModels(TranslateRemoteModel::class.java)
                .addOnSuccessListener { modelList ->
                    it.resume(langList - modelList.map { it.language }.toSet())
                }
                .addOnFailureListener { e ->
                    it.resumeWithException(e)
                }
        }

    /**
     * Maps ISO-639-1 codes to the human readable language names declared in resources,
     * so the download dialog shows "Spanish" instead of "es".
     */
    private fun displayNameOf(code: String): String {
        val langCodeList =
            context.resources.getStringArray(R.array.google_MLKit_translationLangCode_iso6391)
        val langNameList =
            context.resources.getStringArray(R.array.google_MLKit_translationLangName)
        val index = langCodeList.indexOfFirst { it.firstPart() == code.firstPart() }
        return if (index >= 0 && index < langNameList.size) {
            "${langNameList[index]} ($code)"
        } else {
            code
        }
    }

    private fun displayNamesOf(codes: List<String>): String =
        codes.joinToString(", ") { displayNameOf(it) }

    private suspend fun downloadResources(
        langList: List<String>,
        onStartDownloading: (index: Int, lang: String) -> Unit = { _, _ -> },
        onProgress: (completed: Int, total: Int) -> Unit,
    ) {
        for ((index, lang) in langList.withIndex()) {
            kotlinx.coroutines.currentCoroutineContext().ensureActive()
            onStartDownloading(index, lang)
            suspendCancellableCoroutine { c ->
                remoteModelManager.download(
                    TranslateRemoteModel.Builder(lang).build(),
                    DownloadConditions.Builder().build()
                ).addOnSuccessListener {
                    if (c.isActive) c.resume(Any())
                }.addOnFailureListener {
                    if (c.isActive) c.resumeWithException(it)
                }
            }
            onProgress(index + 1, langList.size)
        }
    }

    private fun buildDownloadMessage(
        langList: List<String>,
        completed: Int,
        currentIndex: Int,
    ): String = buildString {
        append(context.getString(R.string.msg_downloading_language_models))
        append("\n\n")
        append(
            context.getString(
                R.string.msg_download_progress_count,
                (currentIndex + 1).coerceAtMost(langList.size),
                langList.size,
            )
        )
        langList.getOrNull(currentIndex)?.let {
            append("\n")
            append(context.getString(R.string.msg_downloading_current_model, displayNameOf(it)))
        }
        if (completed > 0) {
            append("\n")
            append(
                context.getString(
                    R.string.msg_download_completed_models,
                    displayNamesOf(langList.take(completed)),
                )
            )
        }
        val pending = langList.drop(maxOf(completed, currentIndex + 1))
        if (pending.isNotEmpty()) {
            append("\n")
            append(
                context.getString(R.string.msg_download_pending_models, displayNamesOf(pending))
            )
        }
    }

    private suspend fun downloadTranslationResources(langList: List<String>) {
        val downloadJob = kotlinx.coroutines.currentCoroutineContext()[Job]
        val dialog = DialogView(context).apply {
            setTitle(context.getString(R.string.title_resources_downloading))
            setMessage(buildDownloadMessage(langList, completed = 0, currentIndex = 0))
            setDownloadProgress(0)
            setDialogType(DialogView.DialogType.CANCEL_ONLY)
            setCancelByClickingOutside(false)
            onButtonCancelClicked = {
                downloadJob?.cancel()
            }

            attachToScreen()
        }

        FirebaseEvent.logStartDownloadOCRFile(langList.joinToString(","), DOWNLOAD_SITE)

        try {
            downloadResources(
                langList,
                onStartDownloading = { index, _ ->
                    dialog.setMessageAndProgress(
                        buildDownloadMessage(langList, completed = index, currentIndex = index),
                        index * 100 / langList.size,
                    )
                },
            ) { completed, total ->
                dialog.setMessageAndProgress(
                    buildDownloadMessage(
                        langList,
                        completed = completed,
                        currentIndex = completed.coerceAtMost(total - 1),
                    ),
                    completed * 100 / total,
                )
            }

            dialog.detachFromScreen()
            DialogView(context).apply {
                setTitle(context.getString(R.string.title_resouces_downloaded))
                setMessage(context.getString(R.string.msg_resouces_downloaded))
                setDialogType(DialogView.DialogType.CONFIRM_ONLY)
            }.attachToScreen()

            FirebaseEvent.logOCRFileDownloadFinished()
        } catch (e: CancellationException) {
            dialog.detachFromScreen()
        } catch (e: Exception) {
            FirebaseEvent.logException(e)

            dialog.detachFromScreen()
            DialogView(context).apply {
                setTitle(context.getString(R.string.title_downloading_resouces_failed))
                setMessage(e.localizedMessage ?: context.getString(R.string.error_unknown))
                setDialogType(DialogView.DialogType.CONFIRM_ONLY)
            }.attachToScreen()

            FirebaseEvent.logOCRFileDownloadFailed(
                langList.joinToString(","), DOWNLOAD_SITE,
                e.localizedMessage ?: e.message
            )
        }
    }
}
