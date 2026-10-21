package droidninja.filepicker.viewmodels

import android.app.Application
import android.content.ContentUris
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.BaseColumns
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.annotation.WorkerThread
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import droidninja.filepicker.FilePickerConst
import droidninja.filepicker.PickerManager
import droidninja.filepicker.R
import droidninja.filepicker.models.Media
import droidninja.filepicker.models.PhotoDirectory
import droidninja.filepicker.utils.registerObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.lang.Exception
import java.util.*

class VMMediaPicker(application: Application) : BaseViewModel(application) {

    companion object {
        val TAG = VMMediaPicker::javaClass.name
    }

    private val _lvMediaData = MutableLiveData<List<Media>>()
    private val app = application
    val lvMediaData: LiveData<List<Media>>
        get() = _lvMediaData

    private val _lvPhotoDirsData = MutableLiveData<List<PhotoDirectory>>()
    val lvPhotoDirsData: LiveData<List<PhotoDirectory>>
        get() = _lvPhotoDirsData

    private val _lvDataChanged = MutableLiveData<Boolean>()
    val lvDataChanged: LiveData<Boolean>
        get() = _lvDataChanged

    private var contentObserver: ContentObserver? = null

    private fun registerContentObserver() {
        if (contentObserver == null) {
            contentObserver = getApplication<Application>().contentResolver.registerObserver(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            ) {
                _lvDataChanged.value = true
            }
        }
    }

    fun getMedia(
        bucketId: String? = null,
        mediaType: Int = FilePickerConst.MEDIA_TYPE_IMAGE,
        imageFileSize: Int = FilePickerConst.DEFAULT_FILE_SIZE,
        videoFileSize: Int = FilePickerConst.DEFAULT_FILE_SIZE
    ) {
        launchDataLoad {
            val medias = mutableListOf<Media>()
            queryImages(bucketId, mediaType, imageFileSize, videoFileSize).map { dir ->
                medias.addAll(dir.medias)
            }
            medias.sortWith(Comparator { a, b -> (b.id - a.id).toInt() })

            _lvMediaData.postValue(medias)
            registerContentObserver()
        }
    }

    fun getPhotoDirs(
        bucketId: String? = null,
        mediaType: Int = FilePickerConst.MEDIA_TYPE_IMAGE,
        imageFileSize: Int = FilePickerConst.DEFAULT_FILE_SIZE,
        videoFileSize: Int = FilePickerConst.DEFAULT_FILE_SIZE
    ) {
        launchDataLoad {
            val dirs = queryImages(bucketId, mediaType, imageFileSize, videoFileSize)
            val photoDirectory = PhotoDirectory()
            photoDirectory.bucketId = null

            when (mediaType) {
                FilePickerConst.MEDIA_TYPE_VIDEO -> {
                    photoDirectory.name =
                        getApplication<Application>().applicationContext.getString(R.string.all_videos)
                }
                FilePickerConst.MEDIA_TYPE_IMAGE -> {
                    photoDirectory.name =
                        getApplication<Application>().applicationContext.getString(R.string.all_photos)
                }
                else -> {
                    photoDirectory.name =
                        getApplication<Application>().applicationContext.getString(R.string.all_files)
                }
            }

            if (dirs.isNotEmpty() && dirs[0].medias.size > 0) {
                photoDirectory.dateAdded = dirs[0].dateAdded
                photoDirectory.setCoverPath(dirs[0].medias[0].path)
            }

            for (i in dirs.indices) {
                photoDirectory.medias.addAll(dirs[i].medias)
            }

            dirs.add(0, photoDirectory)
            _lvPhotoDirsData.postValue(dirs)
            registerContentObserver()
        }
    }

    @WorkerThread
    suspend fun queryImages(
        bucketId: String?,
        mediaType: Int,
        imageFileSize: Int,
        videoFileSize: Int
    ): MutableList<PhotoDirectory> {
        var data = mutableListOf<PhotoDirectory>()
        withContext(Dispatchers.IO) {
            var projection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                arrayOf(
                    MediaStore.Files.FileColumns._ID,
                    MediaStore.Files.FileColumns.BUCKET_ID,
                    MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME,
                    MediaStore.Files.FileColumns.DURATION,
                    MediaStore.Files.FileColumns.MIME_TYPE,
                    MediaStore.Files.FileColumns.MEDIA_TYPE,
                    MediaStore.Files.FileColumns.SIZE,
                    MediaStore.Files.FileColumns.TITLE,
                    MediaStore.Files.FileColumns.DATE_ADDED,
                    MediaStore.Files.FileColumns.RELATIVE_PATH
                )
            } else {
                if (mediaType == FilePickerConst.MEDIA_TYPE_VIDEO) {
                    arrayOf(
                        MediaStore.Files.FileColumns._ID,
                        MediaStore.Video.Media.BUCKET_ID,
                        MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
                        MediaStore.Video.Media.DURATION,
                        MediaStore.Files.FileColumns.MIME_TYPE,
                        MediaStore.Files.FileColumns.MEDIA_TYPE,
                        MediaStore.Files.FileColumns.SIZE,
                        MediaStore.Files.FileColumns.TITLE,
                        MediaStore.Files.FileColumns.DATE_ADDED
                    )
                } else {
                    arrayOf(
                        MediaStore.Files.FileColumns._ID,
                        MediaStore.Images.Media.BUCKET_ID,
                        MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                        MediaStore.Files.FileColumns.MIME_TYPE,
                        MediaStore.Files.FileColumns.MEDIA_TYPE,
                        MediaStore.Files.FileColumns.SIZE,
                        MediaStore.Files.FileColumns.TITLE,
                        MediaStore.Files.FileColumns.DATE_ADDED
                    )
                }
            }
            val uri = MediaStore.Files.getContentUri("external")
            val sortOrder = MediaStore.Files.FileColumns._ID + " DESC"
            val selectionArgs = mutableListOf<String>()

            var selection = (MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                    + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE)

            if (mediaType == FilePickerConst.MEDIA_TYPE_VIDEO) {
                selection = (MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                        + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO)

                if (videoFileSize != FilePickerConst.DEFAULT_FILE_SIZE) {
                    selection += " AND ${MediaStore.Video.Media.SIZE}>0?"
                    selection += " AND ${MediaStore.Video.Media.SIZE}<=?"
                    selectionArgs.add("${videoFileSize * 1024 * 1024}")
                }
            }

            if (mediaType == FilePickerConst.MEDIA_TYPE_IMAGE && imageFileSize != FilePickerConst.DEFAULT_FILE_SIZE) {
                selection += " AND ${MediaStore.Images.Media.SIZE}<=?"
                selectionArgs.add("${imageFileSize * 1024 * 1024}")
            }

            if (!PickerManager.isShowGif) {
                selection += " AND " + MediaStore.Images.Media.MIME_TYPE + "!='" + MimeTypeMap.getSingleton()
                    .getMimeTypeFromExtension("gif") + "'"
            }

            if (bucketId != null)
                selection += " AND " + MediaStore.Images.Media.BUCKET_ID + "='" + bucketId + "'"

            val cursor = getApplication<Application>().contentResolver.query(
                uri,
                projection,
                selection,
                selectionArgs.toTypedArray(),
                sortOrder
            )

            if (cursor != null) {
                data = getPhotoDirectories(mediaType, cursor)
                cursor.close()
            }
        }
        return data
    }

    @WorkerThread
    private fun getPhotoDirectories(fileType: Int, data: Cursor): MutableList<PhotoDirectory> {
        val directories = mutableListOf<PhotoDirectory>()
        val idList = mutableListOf<Long>()

        while (data.moveToNext()) {
            val imageId = data.getLong(data.getColumnIndexOrThrow(BaseColumns._ID))
            val fileName = data.getString(data.getColumnIndexOrThrow(MediaStore.MediaColumns.TITLE))
            val mediaType =
                data.getInt(data.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE))
            var duration = 0L
            val isVideo = (mediaType == FilePickerConst.MEDIA_TYPE_VIDEO)
            var bucketId: String
            var name: String
            if (isVideo) {
                bucketId =
                    data.getString(data.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_ID))
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    name =
                        if (data.getString(data.getColumnIndexOrThrow(MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME)) != null)
                            data.getString(data.getColumnIndexOrThrow(MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME))
                        else ""
                    if (TextUtils.isEmpty(name)) {
                        Log.d(
                            TAG,
                            "getPhotoDirectoriesVideo: $name, $bucketId, " + data.getString(
                                data.getColumnIndexOrThrow(MediaStore.Files.FileColumns.RELATIVE_PATH)
                            )
                        )
                    }
                    duration =
                        data.getInt(data.getColumnIndexOrThrow(MediaStore.MediaColumns.DURATION))
                            .toLong()
                } else {
                    name =
                        data.getString(data.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME))
                    duration =
                        data.getInt(data.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION))
                            .toLong()
                }
            } else {
                bucketId =
                    data.getString(data.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID))
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    name =
                        if (data.getString(data.getColumnIndexOrThrow(MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME)) != null)
                            data.getString(data.getColumnIndexOrThrow(MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME))
                        else ""
//                    Log.d(TAG, "getPhotoDirectoriesImage: $name")
                } else {
                    name =
                        if (data.getString(data.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME)) != null)
                            data.getString(data.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME))
                        else ""

                }
            }

            val photoDirectory = PhotoDirectory()
            photoDirectory.id = imageId
            photoDirectory.bucketId = bucketId
            photoDirectory.name = name
            photoDirectory.duration = duration

            var contentUri = ContentUris.withAppendedId(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                imageId
            )
            if (fileType == FilePickerConst.MEDIA_TYPE_VIDEO) {
                contentUri = ContentUris.withAppendedId(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        MediaStore.Video.Media.getContentUri(
                            MediaStore.VOLUME_EXTERNAL
                        )
                    } else {
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    },
                    imageId
                )
            }

            if (!directories.contains(photoDirectory)) {
                if (fileType == FilePickerConst.MEDIA_TYPE_VIDEO) {
                    if (duration >= FilePickerConst.DEFAULT_VIDEO_DURATION && checkValidUri(
                            contentUri,
                            fileName
                        )
                    ) {
                        idList.add(imageId)
                        photoDirectory.addPhoto(imageId, fileName, contentUri, mediaType, duration)
                    }
                } else {
                    idList.add(imageId)
                    photoDirectory.addPhoto(imageId, fileName, contentUri, mediaType, duration)
                }
                photoDirectory.dateAdded =
                    data.getLong(data.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED))
                directories.add(photoDirectory)
            } else {
                if (fileType == FilePickerConst.MEDIA_TYPE_VIDEO) {
                    if (duration >= FilePickerConst.DEFAULT_VIDEO_DURATION) {
                        if (!idList.contains(imageId) && checkValidUri(
                                contentUri,
                                fileName
                            )
                        ) {
                            directories[directories.indexOf(photoDirectory)]
                                .addPhoto(imageId, fileName, contentUri, mediaType, duration)
                        }
                    }
                } else {
                    if (!idList.contains(imageId)) {
                        directories[directories.indexOf(photoDirectory)]
                            .addPhoto(imageId, fileName, contentUri, mediaType, duration)
                    }
                }
            }
        }

        return directories
    }

    private fun checkValidUri(contentUri: Uri, fileName: String): Boolean {
        try {
            val inputStream: InputStream? =
                app.applicationContext.contentResolver.openInputStream(contentUri)
            inputStream?.close()
            return true
        } catch (e: Exception) {
            Log.e(VMDocPicker.TAG, "checkValidUri: " + fileName + ", " + e.message)
        }
        return false
    }

    override fun onCleared() {
        contentObserver?.let {
            getApplication<Application>().contentResolver.unregisterContentObserver(it)
        }
    }
}