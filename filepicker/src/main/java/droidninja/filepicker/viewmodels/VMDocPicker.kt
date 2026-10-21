package droidninja.filepicker.viewmodels

import android.app.Application
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.BaseColumns
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.annotation.WorkerThread
import androidx.core.content.FileProvider
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import droidninja.filepicker.PickerManager
import droidninja.filepicker.models.Document
import droidninja.filepicker.models.FileType
import droidninja.filepicker.utils.FilePickerUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*
import android.R.bool
import java.io.InputStream
import java.lang.Exception


class VMDocPicker(application: Application) : BaseViewModel(application) {
    companion object {
        val TAG = VMDocPicker::class.java.simpleName
    }

    val app = application
    private val _lvDocData = MutableLiveData<HashMap<FileType, List<Document>>>()
    val lvDocData: LiveData<HashMap<FileType, List<Document>>>
        get() = _lvDocData


    fun getDocs(fileTypes: List<FileType>, comparator: Comparator<Document>?) {
        launchDataLoad {
            val dirs = queryDocs(fileTypes, comparator)
            _lvDocData.postValue(dirs)
        }
    }

    @WorkerThread
    suspend fun queryDocs(
        fileTypes: List<FileType>,
        comparator: Comparator<Document>?
    ): HashMap<FileType, List<Document>> {
        var data = HashMap<FileType, List<Document>>()
        withContext(Dispatchers.IO) {
            /*Above 9 works for Audio only*/
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                /*val selection =
                    ("${MediaStore.Files.FileColumns.MEDIA_TYPE}!=${MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE}" +
                            " AND ${MediaStore.Files.FileColumns.MEDIA_TYPE}!=${MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO}" +
                            " AND ${MediaStore.Files.FileColumns.MEDIA_TYPE}!=${MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO}")*/

//                val selection = MediaStore.Audio.Media.IS_MUSIC + " != 0"

                var selection = (MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                        + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE)

                val DOC_PROJECTION = arrayOf(
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.MIME_TYPE,
                    MediaStore.Audio.Media.SIZE,
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.IS_PENDING
                )

                val cursor = getApplication<Application>().contentResolver.query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    DOC_PROJECTION,
                    null,
                    null,
                    MediaStore.Audio.Media.DATE_ADDED + " DESC"
                )

                if (cursor != null) {
                    data = createDocumentType(fileTypes, comparator, getDocumentFromCursor(cursor))
                    cursor.close()
                }
            } else {
                val selection =
                    ("${MediaStore.Files.FileColumns.MEDIA_TYPE}!=${MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE}" +
                            " AND ${MediaStore.Files.FileColumns.MEDIA_TYPE}!=${MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO}")
                val DOC_PROJECTION = arrayOf(
                    MediaStore.Files.FileColumns._ID,
                    MediaStore.Files.FileColumns.DATA,
                    MediaStore.Files.FileColumns.MIME_TYPE,
                    MediaStore.Files.FileColumns.SIZE,
                    MediaStore.Files.FileColumns.DATE_ADDED,
                    MediaStore.Files.FileColumns.TITLE
                )

                val cursor = getApplication<Application>().contentResolver.query(
                    MediaStore.Files.getContentUri("external"),
                    DOC_PROJECTION,
                    selection,
                    null,
                    MediaStore.Files.FileColumns.DATE_ADDED + " DESC"
                )

                if (cursor != null) {
                    data = createDocumentType(fileTypes, comparator, getDocumentFromCursor(cursor))
                    cursor.close()
                }
            }
        }
        return data
    }

    @WorkerThread
    private fun createDocumentType(
        fileTypes: List<FileType>,
        comparator: Comparator<Document>?,
        documents: MutableList<Document>
    ): HashMap<FileType, List<Document>> {
        val documentMap = HashMap<FileType, List<Document>>()

        for (fileType in fileTypes) {
            val documentListFilteredByType = documents.filter { document ->
                FilePickerUtils.contains(
                    fileType.extensions,
                    document.mimeType
                )
            }

            comparator?.let {
                documentListFilteredByType.sortedWith(comparator)
            }

            documentMap[fileType] = documentListFilteredByType
        }

        return documentMap
    }

    @WorkerThread
    private fun getDocumentFromCursor(data: Cursor): MutableList<Document> {
        val documents = mutableListOf<Document>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            while (data.moveToNext()) {
                val imageId = data.getLong(data.getColumnIndexOrThrow(BaseColumns._ID))
                val title =
                    data.getString(data.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))

                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, imageId
                )
                try {
                    val inputStream: InputStream? =
                        app.applicationContext.contentResolver.openInputStream(contentUri)
                    inputStream?.close()
                    val fileType = getFileType(PickerManager.getFileTypes(), contentUri)
                    val document = Document(imageId, title, contentUri)
                    document.fileType = fileType
                    val mimeType =
                        data.getString(data.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE))
                    if (!TextUtils.isEmpty(mimeType)) {
                        document.mimeType = mimeType
                    } else {
                        document.mimeType = ""
                    }

                    document.size =
                        data.getString(data.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE))

                    if (!documents.contains(document)) documents.add(document)
                } catch (e: Exception) {
                    Log.d(TAG, "getDocumentFromCursor: $title")
                    Log.e(TAG, "getDocumentFromCursor: " + e.message)
                }
            }
        } else {
            while (data.moveToNext()) {
                val imageId = data.getLong(data.getColumnIndexOrThrow(BaseColumns._ID))
                val path = data.getString(data.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA))
                val title =
                    data.getString(data.getColumnIndexOrThrow(MediaStore.Files.FileColumns.TITLE))

                if (path != null) {

                    val fileType = getFileType(PickerManager.getFileTypes(), path)
                    val file = File(path)
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Files.getContentUri("external"),
                        imageId
                    )
                    if (fileType != null && !file.isDirectory && file.exists()) {

                        val document = Document(imageId, title, contentUri)
                        document.fileType = fileType

                        val mimeType =
                            data.getString(data.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE))
                        if (mimeType != null && !TextUtils.isEmpty(mimeType)) {
                            document.mimeType = mimeType
                        } else {
                            document.mimeType = ""
                        }

                        document.size =
                            data.getString(data.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE))

                        if (!documents.contains(document)) documents.add(document)
                    }
                }
            }
        }

        return documents
    }

    private fun getFileType(types: ArrayList<FileType>, path: String): FileType? {
        for (index in types.indices) {
            for (string in types[index].extensions) {
                if (path.endsWith(string)) return types[index]
            }
        }
        return null
    }

    private fun getFileType(types: ArrayList<FileType>, contentUri: Uri): FileType? {
        val extension = getExtension(contentUri)
        for (index in types.indices) {
            for (string in types[index].extensions) {
                if (extension.toString().endsWith(string)) return types[index]
            }
        }
        return null
    }

    fun getExtension(uri: Uri): String? {
        val extension: String?

        //Check uri format to avoid null
        extension = if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            //If scheme is a content
            val mime = MimeTypeMap.getSingleton()
            mime.getExtensionFromMimeType(app.applicationContext.contentResolver.getType(uri))
        } else {
            //If scheme is a File
            //This will replace white spaces with %20 and also other special characters. This will avoid returning null values on file name with spaces and special characters.
            MimeTypeMap.getFileExtensionFromUrl(
                getUriFromFile(
                    app.applicationContext,
                    File(uri.path)
                ).toString()
            )
        }
        Log.d(TAG, "getExtension: $extension")
        return extension
    }

    fun getUriFromFile(context: Context, dir: File?): Uri? {
        return if (dir != null && dir.exists()) {
            FileProvider.getUriForFile(
                context,
                context.packageName + ".droidninja.filepicker" + ".provider",
                dir
            )
        } else {
            null
        }
    }
}