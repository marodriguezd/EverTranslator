package tw.firemaples.onscreenocr.repo

import android.content.Context
import androidx.lifecycle.asFlow
import com.chibatching.kotpref.livedata.asLiveData
import java.io.File
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Call
import tw.firemaples.onscreenocr.api.ApiHub
import tw.firemaples.onscreenocr.pref.AppPref
import tw.firemaples.onscreenocr.recognition.RecognitionLanguage
import tw.firemaples.onscreenocr.recognition.TesseractTextRecognizer
import tw.firemaples.onscreenocr.recognition.TextRecognitionProviderType
import tw.firemaples.onscreenocr.recognition.TextRecognizer
import tw.firemaples.onscreenocr.utils.Logger
import tw.firemaples.onscreenocr.utils.Utils

class OCRRepository {
    private val logger: Logger by lazy { Logger(OCRRepository::class) }
    private val context: Context by lazy { Utils.context }

    private var downloadingTessDataCall: Call<ResponseBody>? = null

    val selectedOCRLangFlow: Flow<String>
        get() = AppPref.asLiveData(AppPref::selectedOCRLang).asFlow()
            .flowOn(Dispatchers.Default)

    fun getAllOCRLanguages(): Flow<List<RecognitionLanguage>> = flow {
        val supportedLangList = TextRecognizer.allSupportedLanguages(
            AppPref.selectedOCRLang,
            AppPref.selectedOCRProvider
        )

        emit(supportedLangList)
    }.flowOn(Dispatchers.Default)

    suspend fun setSelectedOCRLanguage(
        langCode: String,
        ocrProviderType: TextRecognitionProviderType,
    ) {
        withContext(Dispatchers.Default) {
            AppPref.selectedOCRLang = langCode
            AppPref.selectedOCRProvider = ocrProviderType
        }
    }

    @Throws(Exception::class)
    suspend fun downloadTessData(
        langCode: String,
        destFile: File = TesseractTextRecognizer.getTessDataFile(langCode),
        onProgress: (downloadedBytes: Long, totalBytes: Long) -> Unit = { _, _ -> },
    ): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val call = ApiHub.tessDataDownloader.downloadFromGithub(langCode)
                downloadingTessDataCall = call
                try {
                    val response = call.execute()
                    if (response.isSuccessful) {
                        return@withContext response.body()?.saveToFile(destFile, onProgress) == true
                    } else {
                        val status = response.code()
                        val error = response.errorBody()?.string()
                        val msg = "Download Tesseract data failed, status: $status, error: $error"
                        logger.warn(msg)

                        throw Exception(msg)
                    }
                } finally {
                    if (downloadingTessDataCall === call) {
                        downloadingTessDataCall = null
                    }
                }
            } catch (e: Exception) {
                logger.warn("Download Tesseract data failed", e)

                throw e
            }
        }

    fun cancelDownloadingTessData() {
        downloadingTessDataCall?.cancel()
    }

    @Throws(IOException::class)
    suspend fun ResponseBody.saveToFile(
        destFile: File,
        onProgress: (downloadedBytes: Long, totalBytes: Long) -> Unit = { _, _ -> },
    ): Boolean =
        withContext(Dispatchers.IO) {
            val parent = destFile.parentFile ?: run {
                logger.error("Tesseract data destination has no parent, path: $destFile")
                return@withContext false
            }
            if (!parent.exists() && !parent.mkdirs()) {
                logger.error("Creating tesseract data folder failed, path: $parent")
                return@withContext false
            }
            val temp = File.createTempFile(destFile.name, ".tmp", parent)
            val totalBytes = contentLength()
            var downloadedBytes = 0L
            var lastProgress = -1
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var moved = false

            try {
                byteStream().use { input ->
                    temp.outputStream().use { output ->
                        while (true) {
                            val bytesRead = input.read(buffer)
                            if (bytesRead == -1) break

                            output.write(buffer, 0, bytesRead)
                            downloadedBytes += bytesRead
                            if (totalBytes > 0) {
                                val progress = (downloadedBytes * 100 / totalBytes).toInt()
                                if (progress != lastProgress) {
                                    onProgress(downloadedBytes, totalBytes)
                                    lastProgress = progress
                                }
                            } else if (downloadedBytes == bytesRead.toLong()) {
                                onProgress(downloadedBytes, totalBytes)
                            }
                        }
                    }
                }

                if (destFile.exists() && !destFile.delete()) {
                    logger.error("Deleting dest tesseract data failed, path: $destFile")
                    return@withContext false
                }
                if (!temp.renameTo(destFile)) {
                    logger.error("Moving downloaded tesseract data failed, path: $destFile")
                    return@withContext false
                }
                moved = true
            } finally {
                if (!moved) temp.delete()
            }

            return@withContext true
        }
}
