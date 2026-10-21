package com.app.helper;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.MediaScannerConnection;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.util.Size;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.app.alhawanajd.BuildConfig;
import com.app.alhawanajd.R;
import com.app.alhawanajd.Utility;
import com.app.utils.Constants;
import com.app.utils.FileUtil;
import com.app.utils.Logging;
import com.app.utils.apachecommons.FilenameUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import timber.log.Timber;
import wseemann.media.FFmpegMediaMetadataRetriever;

public class StorageUtils {
    private static final String TAG = StorageUtils.class.getSimpleName();
    private static StorageUtils mInstance;
    private final Context mContext;
    private String appDirectory;

    public static final String TAG_IMAGE = "image";
    public static final String TAG_RECEIVED = "received";
    public static final String TAG_IMAGE_SENT = "image_sent";
    public static final String TAG_VIDEO_SENT = "video_sent";
    public static final String TAG_AUDIO_SENT = "audio_sent";
    public static final String TAG_AUDIO_RECEIVE = "audio_receive";
    public static final String TAG_AUDIO_DURATION = "audio_duration";
    public static final String TAG_DOCUMENT_SENT = "document_sent";
    public static final String TAG_AUDIO = "audio";
    public static final String TAG_VIDEO = "video";
    public static final String TAG_THUMB = "thumb";
    public static final String TAG_DOCUMENT = "document";

    public static final String IMG_SENT_PATH = "Images/Sent/";
    public static final String IMG_PROFILE_PATH = "Images/Profile/";
    public static final String IMG_HOME_PATH = "Images/";
    public static final String IMG_BANNER_PATH = "Images/Banner/";
    public static final String IMG_THUMBNAIL_PATH = "Images/.thumbnails/";
    public static final String VIDEO_CACHE_DIR = "video-cache";
    public static final String TEMP_CACHE_DIR = "Temp";

    public StorageUtils(Context context) {
        this.mContext = context;
        appDirectory = "/" + context.getString(R.string.app_name);

    }

    public static StorageUtils getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new StorageUtils(context);
        }
        return mInstance;
    }

    private String getFileName(String url) {
        String imgSplit = url;
        int endIndex = imgSplit.lastIndexOf("/");
        if (endIndex != -1) {
            imgSplit = imgSplit.substring(endIndex + 1);
        }
        return imgSplit;
    }

    public boolean checkIfFileExists(String from, String imageName) {
        File file = getImage(from, imageName);
        Log.d(TAG, "checkIfFileExists: " + file.exists());
        return file != null && file.exists();
    }

    public String saveThumbNail(Bitmap bitmap, String filename) {
        String stored = "";
        String path = "/" + mContext.getString(R.string.app_name) + "/" + mContext.getString(R.string.app_name) + "Images/.thumbnails";
        File sdcard = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File folder = new File(sdcard.getAbsoluteFile(), path);
        folder.mkdirs();

        File nomediaFile = new File(folder.getAbsoluteFile(), ".nomedia");
        try {
            if (!nomediaFile.exists()) {
                nomediaFile.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        File file = new File(folder.getAbsoluteFile(), filename);
        if (file.exists())
            return file.getPath();

        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
            out.flush();
            out.close();
            stored = "success";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file.getPath();
    }


    public File getcheckAudioFile(String from, String imageName) {
        File mediaImage = null;
        try {
//            File root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File root = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                root = getRootDir(String.valueOf(mContext));
            } else {
                root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
            }

            //    File myDir = new File(root);
            if (!root.exists())
                return null;

            String path = "";
            path = getPath(from);
            mediaImage = new File(root.getPath() + path + FilenameUtils.getName(imageName));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mediaImage;
    }

    public Uri saveToSDCard(Bitmap bitmap, String from, String fileName) {
        fileName = getFileName(fileName);

        String path = getPath(from);

        ContentResolver resolver = mContext.getContentResolver();
        ContentValues contentValues = new ContentValues();
        Uri outUri;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            contentValues.put(MediaStore.MediaColumns.TITLE, fileName);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + path);
            contentValues.put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis());
            contentValues.put(MediaStore.MediaColumns.DATE_TAKEN, System.currentTimeMillis());
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q)
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 1);

            /*if (Objects.equals(from, Constants.TAG_SENT) && !containsNoMedia(Environment.DIRECTORY_PICTURES + path)) {
                makeNoMedia(Environment.DIRECTORY_PICTURES + path);
            }*/

            Uri collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);

            outUri = resolver.insert(collection, contentValues);

            try {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
                InputStream inputStream = new ByteArrayInputStream(stream.toByteArray());

                ParcelFileDescriptor pfd = resolver.openFileDescriptor(outUri, "w");
                boolean saved = FileUtil.copyFile(inputStream, new FileOutputStream(pfd.getFileDescriptor()));

                Timber.i("Save: %s", outUri.getPath());
            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
                Timber.e("Save: failed %s", e.getMessage());
            }

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                contentValues.clear();
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0);
                resolver.update(outUri, contentValues, null, null);
            }
            return outUri;
        } else {
            // for API 28 and below (in progress)
           /* final String directory = Environment.getExternalStorageDirectory().getAbsolutePath();
            File targetDir = new File(directory);

            File folder = new File(targetDir, path);
            if (from.equals(Constants.TAG_SENT)) {
                if (!folder.exists()) {
                    if (folder.mkdirs()) {
                        Timber.i("Directory created: %s", targetDir.getAbsolutePath());
                    }
                    File noMediaFile = new File(folder.getAbsoluteFile(), ".nomedia");
                    try {
                        if (!noMediaFile.exists()) {
                            noMediaFile.createNewFile();
                        }
                    } catch (IOException e) {
                        Logging.e(TAG, "saveToSDCard: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            } else {
                folder.mkdirs();
            }

            File createdMedia = new File(folder.getAbsoluteFile(), fileName);
            if (createdMedia.exists()) createdMedia.delete();*/

            File sdcard = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

            File folder = new File(sdcard.getAbsoluteFile(), path);
            if (from.equals(Constants.TAG_SENT)) {
                if (!folder.exists()) folder.mkdirs();
                File noMediaFile = new File(folder.getAbsoluteFile(), ".nomedia");
                try {
                    if (!noMediaFile.exists()) {
                        noMediaFile.createNewFile();
                    }
                } catch (IOException e) {
                    Logging.e(TAG, "saveToSDCard: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                folder.mkdirs();
            }

            File file = new File(folder.getAbsoluteFile(), fileName);
            if (file.exists())
                file.delete();

            try {
                FileOutputStream out = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                out.flush();
                out.close();
                refreshGallery(file);
                return createImageUri(from, file.getAbsolutePath());
            } catch (Exception e) {
                Logging.e(TAG, "saveToSDCard: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return null;
    }

    public File saveImageInStorage(Bitmap bitmap, String fileName, String from) {
        File savedFile = saveBitmap(bitmap, fileName, from);
        if (savedFile != null) {
            Timber.tag(TAG).d("saveImageInStorage: " + savedFile.getAbsolutePath());
        }
        return savedFile;
    }

    public File saveBitmap(Bitmap bitmap, String fileName, String from) {
        File mDestDir = getDirectory(from, true);
        File mDestFile = new File(mDestDir, fileName);
        try {
            FileOutputStream out = new FileOutputStream(mDestFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            return mDestFile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public File saveThumbBitmap(Bitmap bitmap, File outputFile) {
        if (outputFile != null && outputFile.exists()) outputFile.delete();
        try {
            FileOutputStream out = new FileOutputStream(outputFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            Timber.tag(TAG).d("saveThumbBitmap: %s", outputFile.getAbsolutePath());
            return outputFile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Bitmap getFrameAtTime(File mSrcFile, long time, int width, int height) {
//        Bitmap thumb = null;
//        FFmpegMediaMetadataRetriever retriever = new FFmpegMediaMetadataRetriever();
//        retriever.setDataSource(mSrcFile.getAbsolutePath());
////        thumb = retriever.getScaledFrameAtTime(time, MediaMetadataRetriever.OPTION_CLOSEST_SYNC, width, height);
//        thumb = retriever.getFrameAtTime(time, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
////        Timber.d("getFrameAtTime: %s, %s, %s, %s", width, height, time, (thumb == null));
//        return thumb;

        Bitmap thumb = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(mSrcFile.getAbsolutePath());
//        thumb = retriever.getScaledFrameAtTime(time, MediaMetadataRetriever.OPTION_CLOSEST_SYNC, width, height);
        thumb = retriever.getFrameAtTime(time, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
//        Timber.d("getFrameAtTime: %s, %s, %s, %s", width, height, time, (thumb == null));
        return thumb;
    }

    public Bitmap getFrameAtTime(Uri videoUri, long time) {
        Bitmap thumb = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            retriever.setDataSource(mContext, videoUri);
        } else {
            retriever.setDataSource(videoUri.getPath());
        }
        thumb = retriever.getFrameAtTime(time, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
        Log.d(TAG, "getFrameAtTime: " + (thumb == null));
        return thumb;
    }

    public static Uri getImageContentUri(Context context, File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID},
                MediaStore.Images.Media.DATA + "=? ",
                new String[]{filePath}, null);

        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor
                    .getColumnIndexOrThrow(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }

    public Uri getUriFromFile(File dir) {
        if (dir != null && dir.exists()) {
            return FileProvider.getUriForFile(mContext, BuildConfig.APPLICATION_ID + ".fileprovider", dir);
        } else {
            return null;
        }
    }

    public Uri getUriFromFileLivza(File file) {
        return FileProvider.getUriForFile(mContext, BuildConfig.APPLICATION_ID + ".provider", file);
    }

    public Uri getUriFromFileReceive(File file) {
        return FileProvider.getUriForFile(mContext, BuildConfig.APPLICATION_ID + ".provider", file);
    }

    public void refreshGallery(File file) {
        try {
            final Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            final Uri contentUri = Uri.fromFile(file);
            scanIntent.setData(contentUri);
            mContext.sendBroadcast(scanIntent);
            MediaScannerConnection.scanFile(mContext, new String[]{file.getAbsolutePath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                @Override
                public void onScanCompleted(String path, Uri uri) {
                    Timber.tag(TAG).d("Finished scanning: %s", file.getAbsolutePath());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public File getCacheDirLIvza(Context context) {
        File mDataDir = null;
        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            mDataDir = ContextCompat.getExternalCacheDirs(context)[0];

            if (!mDataDir.exists()) {
                mDataDir.mkdirs();
            }
        } else {
            mDataDir = context.getCacheDir();
            if (!mDataDir.exists()) {
                mDataDir.mkdirs();
            }
        }
//        Log.i(TAG, "getAppDataDir: " + mDataDir);
        return new File(mDataDir.getAbsolutePath());
    }

    public File getFileelivza(String from, String fileName) {
        File file = null;
        try {
            String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).toString();
            File myDir = new File(root);
            if (!myDir.exists())
                return null;

            String path = "";
            switch (from) {
                case Constants.TAG_SENT:
                    path = "/" + mContext.getString(R.string.app_name) + "/" + mContext.getString(R.string.app_name) + "Images/Sent/";
                    break;
                case "profile":
                    path = "/" + mContext.getString(R.string.app_name) + "/" + mContext.getString(R.string.app_name) + "Images/Profile/";
                    break;
                case Constants.TAG_THUMBNAIL:
                    path = "/" + mContext.getString(R.string.app_name) + "/" + mContext.getString(R.string.app_name) + "Images/.thumbnails/";
                    break;
                case TAG_AUDIO_SENT:
                    path = "/" + mContext.getString(R.string.app_name) + "/" + mContext.getString(R.string.app_name).replaceAll(" ", "") + "_Audio/Sent/";
                    break;
                case Constants.TAG_AUDIO_RECEIVE:
                    path = "/" + mContext.getString(R.string.app_name) + "/" + mContext.getString(R.string.app_name).replaceAll(" ", "") + "_Audio/";
                    break;
                default:
                    path = "/" + mContext.getString(R.string.app_name) + "/" + mContext.getString(R.string.app_name) + "Images/";
                    break;
            }
            file = new File(myDir.getPath() + path + fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    public File getSrcFile(String from, String fileName, String fileType) {
        File root = getDirectory(from);
        if (root != null && !root.exists())
            return null;
        File dir = new File(root, fileName);
        if (dir.exists()) {
            Log.d(TAG, "getSrcFile: " + dir.getAbsolutePath());
            return dir;
        }
        return null;
    }

    public File getSrcFile(String from, String fileName) {
        File root = getDirectory(from, false);
        if (root != null && !root.exists())
            return null;
        File file = new File(root, fileName);
        if (file.exists()) {
            Timber.tag(TAG).d("getSrcFile: %s", file.getAbsolutePath());
            return file;
        }
        return null;
    }

    public File getDirectory(String from) {
        File folder = getRootDir(from);
        Timber.d("getDirectory: %s, Exists: %b", folder, folder.exists());
        if (folder.exists()) {
            return folder;
        }
        return null;
    }

    public File getDirectory(String from, boolean createDir) {
        File folder = getRootDir(from);
        if (createDir && !folder.exists()) {
            folder.mkdirs();
        }
        if (createDir && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (from.equals(Constants.TAG_SENT) || from.equals(StorageUtils.TAG_IMAGE_SENT) || from.equals(Constants.TAG_PROFILE) ||
                    from.equals(StorageUtils.TAG_AUDIO_SENT)) {
                File noMediaFile = new File(folder.getAbsoluteFile(), ".nomedia");
                try {
                    if (!noMediaFile.exists()) {
                        noMediaFile.createNewFile();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (folder.exists()) {
            return folder;
        }
        return null;
    }

    public File getRootDir(String from) {
        File folder = null;
        switch (from) {
            case StorageUtils.TAG_IMAGE_SENT:
            case Constants.TAG_SENT: {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    folder = new File(getExtFilesDir() + getFolderPath(from));
                } else {
                    folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + getFolderPath(from));
                }
            }
            break;
            case StorageUtils.TAG_IMAGE: {
                folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + getFolderPath(from));
            }
            break;
            case TAG_AUDIO_SENT: {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    folder = new File(getExtFilesDir() + getFolderPath(from));
                } else {
                    folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC) + getFolderPath(from));
                }
            }
            break;
            case TAG_AUDIO:
                folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC) + getFolderPath(from));
                break;
            case TAG_VIDEO_SENT: {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    folder = new File(getExtFilesDir() + getFolderPath(from));
                } else {
                    folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) + getFolderPath(from));
                }
            }
            break;
            case TAG_VIDEO:
                folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) + getFolderPath(from));
                break;
            case TAG_DOCUMENT_SENT:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    folder = new File(getExtFilesDir() + getFolderPath(from));
                } else {
                    folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + getFolderPath(from));
                }
                break;
            case TAG_DOCUMENT:
                folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + getFolderPath(from));
                break;
            case TAG_THUMB:
            case Constants.TAG_PROFILE:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    folder = new File(getExtFilesDir() + getFolderPath(from));
                } else {
                    folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + getFolderPath(from));
                }
                break;
            default:
                break;
        }
        return folder;
    }

    public String getFolderPath(String from) {
        String path = "";
        switch (from) {
            case TAG_IMAGE:
            default:
                path = File.separator + mContext.getString(R.string.app_name) + File.separator + mContext.getString(R.string.app_name) + "Images" + File.separator;
                break;
            case Constants.TAG_SENT:
            case TAG_IMAGE_SENT:
                path = File.separator + mContext.getString(R.string.app_name) + File.separator + mContext.getString(R.string.app_name) + "Images" + File.separator + "Sent" + File.separator;
                break;
            case "profile":
                path = File.separator + mContext.getString(R.string.app_name) + File.separator + mContext.getString(R.string.app_name) + "Images" + File.separator + "Profile" + File.separator;
                break;
            case Constants.TAG_THUMBNAIL:
                path = File.separator + mContext.getString(R.string.app_name) + File.separator + mContext.getString(R.string.app_name) + "Images" + File.separator + ".thumbnails" + File.separator;
                break;
            case TAG_VIDEO:
                path = File.separator + mContext.getString(R.string.app_name) + File.separator + mContext.getString(R.string.app_name) + "Videos" + File.separator;
                break;
            case TAG_AUDIO:
                path = File.separator + mContext.getString(R.string.app_name) + File.separator + mContext.getString(R.string.app_name) + "Audios" + File.separator;
                break;
            case TAG_AUDIO_SENT:
                path = File.separator + mContext.getString(R.string.app_name) + File.separator + mContext.getString(R.string.app_name) + "Audios" + File.separator + "Sent" + File.separator;
                break;
            case TAG_AUDIO_RECEIVE:
                path = File.separator + mContext.getString(R.string.app_name) + File.separator + mContext.getString(R.string.app_name) + "Audios" + File.separator;
                break;
        }
        Timber.tag(TAG).d("getFolderPath: %s", path);
        return path;
    }

    public String getPath(@NonNull String from) {
        String path = getFolderPath(from);
        if (from.equals(StorageUtils.TAG_AUDIO_SENT) || from.equals("audio_receive")) {
            File sdcard = Environment.getExternalStorageDirectory();
            File folder = new File(sdcard.getAbsoluteFile(), path);
            if (!folder.exists()) {
                folder.mkdirs();
            }
            File noMediaFile = new File(folder.getAbsoluteFile(), ".nomedia");
            try {
                if (!noMediaFile.exists()) {
                    noMediaFile.createNewFile();
                }
            } catch (IOException e) {
                Logging.e(TAG, "getPath: " + e.getMessage());
                e.printStackTrace();
            }
            return folder.getPath();
        }
        return path;
    }


    public String getPathLivza(String from) {
        String path = "";
        switch (from) {
            case Constants.TAG_SENT:
                path = "/" + mContext.getString(R.string.app_name) + "/" + mContext.getString(R.string.app_name) + "Images/Sent/";
                break;
            case "profile":
                path = "/" + mContext.getString(R.string.app_name) + "/" + mContext.getString(R.string.app_name) + "Images/Profile/";
                break;
            case Constants.TAG_THUMBNAIL:
                path = "/" + mContext.getString(R.string.app_name) + "/" + mContext.getString(R.string.app_name) + "Images/.thumbnails/";
                break;
            case TAG_AUDIO_SENT:
                path = "/" + mContext.getString(R.string.app_name) + "/" + mContext.getString(R.string.app_name).replaceAll(" ", "") + "_Audio/Sent/";
                break;
            case Constants.TAG_AUDIO_RECEIVE:
                path = "/" + mContext.getString(R.string.app_name) + "/" + mContext.getString(R.string.app_name).replaceAll(" ", "") + "_Audio/";
                break;
            default:
                path = "/" + mContext.getString(R.string.app_name) + "/" + mContext.getString(R.string.app_name) + "Images/";
                break;
        }


        return path;
    }


    public Uri createImageUri(String from, String filePath) {
        Uri imageUri = getImageUri(from, getFileName(filePath));
        if (imageUri == null) {
            // create a new uri
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            } else {
                ContentResolver resolver = mContext.getContentResolver();
                ContentValues values = new ContentValues();

                final String fileName = getFileName(filePath);
                final String path = getPath(from);
                Uri collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

                values.put(MediaStore.MediaColumns.TITLE, fileName);
                values.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
                values.put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis());
                values.put(MediaStore.MediaColumns.DATA, filePath);

                imageUri = resolver.insert(collection, values);
            }
        }
        return imageUri;
    }

    public Uri getImageUri(String from, String fileName) {
        fileName = getFileName(fileName);
        Uri imageUri = null;
        String path = getPath(from);

        ContentResolver resolver = mContext.getContentResolver();

        Uri collection = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                ? MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                : MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        final String selection;
        final String[] selectionArgs;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            selection = MediaStore.MediaColumns.DISPLAY_NAME + "=? AND " +
                    MediaStore.MediaColumns.RELATIVE_PATH + "=?";
            selectionArgs = new String[]{fileName, Environment.DIRECTORY_PICTURES + path};
        } else {
            path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + path;
            selection = MediaStore.MediaColumns.DATA + "=?";
            selectionArgs = new String[]{path + fileName};
        }
        try (Cursor c = resolver.query(collection, null, selection, selectionArgs, null)) {
            if (c.moveToFirst()) {
                imageUri = ContentUris.withAppendedId(collection, c.getLong(c.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)));
                Timber.d("Check file: exists %s %s", fileName, imageUri);
            } else {
                Timber.d("Check file: not found %s", (Object) selectionArgs);
            }
        } catch (Exception e) {
            Timber.e(e);
        }
        return imageUri;
    }

    public File getImage(String from, String imageName) {
        File mediaImage = null;
        try {
            String root = Environment.getExternalStorageDirectory().toString();
            File myDir = new File(root);
            if (!myDir.exists())
                return null;

            String path = "";
            switch (from) {
                case Constants.TAG_SENT:
                    path = "/" + mContext.getString(R.string.app_name) + "/" + mContext.getString(R.string.app_name) + "Images/Sent/";
                    break;
                case "profile":
                    path = "/" + mContext.getString(R.string.app_name) + "/" + mContext.getString(R.string.app_name) + "Images/Profile/";
                    break;
                case Constants.TAG_THUMBNAIL:
                    path = "/" + mContext.getString(R.string.app_name) + "/" + mContext.getString(R.string.app_name) + "Images/.thumbnails/";
                    break;
                case StorageUtils.TAG_AUDIO_SENT:
                    //path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name)+ "_Audio/Sent/";
                    path = "/" + "Android" + "/" + "data/" + "com.app./" + "files/" + "Fundoo/FundooAudios/Sent/";
                    break;
                case Constants.TAG_AUDIO_RECEIVE:
                    //path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name)+ "_Audio/";
                    path = "/" + "Android" + "/" + "data/" + "com.app./" + "files/" + "_Audio/";
                    break;
                default:
                    path = "/" + mContext.getString(R.string.app_name) + "/" + mContext.getString(R.string.app_name) + "Images/";
                    break;
            }


            mediaImage = new File(myDir.getPath() + path + FilenameUtils.getName(imageName));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return mediaImage;
    }

    public File getTempFile(Context context, String fileName) {
        File mDataDir = null;
        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            mDataDir = ContextCompat.getExternalFilesDirs(context, "Temp")[0];
            if (!mDataDir.exists()) {
                mDataDir.mkdirs();
            }
        } else {
            mDataDir = context.getFilesDir();
            if (!mDataDir.exists()) {
                mDataDir.mkdirs();
            }
        }
        Timber.tag(TAG).i("getTempFile: %s", (mDataDir.getPath() + File.separator + fileName));
        return new File(mDataDir.getPath(), fileName);
    }

    /*public Uri SaveVideoToGallery(Context context, File videoFile) {
        ContentValues values = new ContentValues(3);
        values.put(MediaStore.Video.Media.TITLE, "My video title");
        values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
        values.put(MediaStore.Video.Media.DATA, videoFile.getAbsolutePath());
        return context.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
    }*/

    public static void exportMp4ToGallery(Context context, String filePath) {

        final ContentValues values = new ContentValues(2);
        values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
        values.put(MediaStore.Video.Media.DATA, filePath);

        context.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                values);
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.parse("file://" + filePath)));
    }

    public void clearTempCachee() {
        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            File mDataDir = ContextCompat.getExternalFilesDirs(mContext, "Temp")[0];

            if (mDataDir.listFiles() != null) {
                for (File listFile : mDataDir.listFiles()) {
                    listFile.delete();
                }
            }

        } else {
            File mDataDir = null;
            mDataDir = mContext.getCacheDir();
            if (mDataDir.listFiles() != null) {
                for (File listFile : mDataDir.listFiles()) {
                    listFile.delete();
                }
            }
        }
    }

    public File TrimmerPath(Context context, String fileName, String audioVideo) {
        File mDataDir = null;
        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            mDataDir = ContextCompat.getExternalFilesDirs(context, audioVideo)[0];
            if (!mDataDir.exists()) {
                mDataDir.mkdirs();
            }
        } else {
            mDataDir = context.getFilesDir();
            if (!mDataDir.exists()) {
                mDataDir.mkdirs();
            }
        }
        Logging.i(TAG, "getTempFile: " + mDataDir);
        return new File(mDataDir.getPath() + File.separator + fileName);
    }

    public File saveToCacheDir(Bitmap bitmap, String filename) {
        boolean stored = false;
        File mDataDir = getExtCacheDir();
        File file = new File(mDataDir.getAbsoluteFile(), filename);
        if (file.exists())
            stored = true;

        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            stored = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return stored ? file : null;
    }

    public File saveUri(Uri source, File destDir, String fileName) {
        File destFile = new File(destDir, fileName);
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
            bis = new BufferedInputStream(mContext.getContentResolver().openInputStream(source));
            bos = new BufferedOutputStream(new FileOutputStream(destFile, false));
            byte[] buf = new byte[1024];
            bis.read(buf);
            do {
                bos.write(buf);
            } while (bis.read(buf) != -1);
            Timber.tag(TAG).d("saveUriToCache: %s", destFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (bis != null) bis.close();
                if (bos != null) bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return destFile.exists() ? destFile : null;
    }

    public File saveFile(File source, File destDir, String fileName) {
        File destFile = new File(destDir, fileName);
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
            bis = new BufferedInputStream(new FileInputStream(source.getAbsolutePath()));
            bos = new BufferedOutputStream(new FileOutputStream(destFile, false));
            byte[] buf = new byte[1024];
            bis.read(buf);
            do {
                bos.write(buf);
            } while (bis.read(buf) != -1);
            Timber.tag(TAG).d("saveUriToCache: %s", destFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (bis != null) bis.close();
                if (bos != null) bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return destFile.exists() ? destFile : null;
    }

    public static String getMimeType(String url, String fileType) {
        String type = null;
        if (fileType.equals(StorageUtils.TAG_AUDIO)) {
            if (!url.endsWith(Utility.AUDIO_FORMAT)) {
                url = url + Utility.AUDIO_FORMAT;
            }
        } else if (fileType.equals(StorageUtils.TAG_VIDEO)) {
            if (!url.endsWith(Utility.VIDEO_FORMAT)) {
                url = url + Utility.VIDEO_FORMAT;
            }
        } else if (fileType.equals(StorageUtils.TAG_IMAGE)) {
            if (!url.endsWith(Utility.IMAGE_FORMAT)) {
                url = url + Utility.IMAGE_FORMAT;
            }
        }
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        Timber.d("getMimeType: %s", type);
        return type;
    }

    public static String getMimeType_player(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    public String getMimeTypeOfUri(Uri uri) {
        String mimeType = null;
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            ContentResolver cr = mContext.getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
                    .toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.toLowerCase());
        }
        return mimeType;
    }

    public String getExtension(Uri uri) {
        String extension;

        //Check uri format to avoid null
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            //If scheme is a content
            final MimeTypeMap mime = MimeTypeMap.getSingleton();
            extension = mime.getExtensionFromMimeType(mContext.getContentResolver().getType(uri));
        } else {
            //If scheme is a File
            //This will replace white spaces with %20 and also other special characters. This will avoid returning null values on file name with spaces and special characters.
            extension = MimeTypeMap.getFileExtensionFromUrl(getUriFromFile(new File(uri.getPath())).toString());

        }
        Log.d(TAG, "getExtension: " + extension);
        return extension;
    }

    public String getExtension(File file) {
        String extension = FilenameUtils.getExtension(file.getName());
        Log.d(TAG, "getExtension: " + extension);
        return extension;
    }

    public File getExtFilesDir() {
        File mDataDir = null;
        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            mDataDir = ContextCompat.getExternalFilesDirs(mContext, null)[0];
            if (!mDataDir.exists()) {
                mDataDir.mkdirs();
            }
        } else {
            mDataDir = mContext.getExternalFilesDir(null);
            if (!mDataDir.exists()) {
                mDataDir.mkdirs();
            }
        }
        if (mDataDir.exists()) return mDataDir;
        else return null;
    }

    public File getExtCacheDir() {
        File mDataDir = null;
        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            mDataDir = ContextCompat.getExternalCacheDirs(mContext)[0];
            if (!mDataDir.exists()) {
                mDataDir.mkdirs();
            }
        } else {
            mDataDir = mContext.getCacheDir();
            if (!mDataDir.exists()) {
                mDataDir.mkdirs();
            }
        }
        if (mDataDir.exists()) return mDataDir;
        else return null;
    }

    public void deleteCacheDir() {
        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            for (File externalCacheDir : ContextCompat.getExternalCacheDirs(mContext)) {
                if (externalCacheDir.listFiles() != null) {
                    for (File file : externalCacheDir.listFiles()) {
                        if (file.isDirectory()) {
                            if (!file.getName().equals(VIDEO_CACHE_DIR)) {
                                for (File listFile : file.listFiles()) {
                                    Timber.d("deleteCacheDir: %s", listFile);
                                    listFile.delete();
                                }
                            }
                        } else {
                            file.delete();
                        }
                    }
                }
            }
        } else {
            File mDataDir = null;
            mDataDir = mContext.getCacheDir();
            if (mDataDir.exists() && mDataDir.listFiles() != null)
                for (File file : mDataDir.listFiles()) {
                    if (file.isDirectory()) {
                        if (!file.getName().equals(VIDEO_CACHE_DIR)) {
                            for (File listFile : file.listFiles()) {
                                Timber.d("deleteCacheDir: %s", listFile);
                                listFile.delete();
                            }
                        }
                    } else {
                        file.delete();
                    }
                }
        }
    }

    public void deleteExternalDir() {
        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            for (File externalCacheDir : ContextCompat.getExternalFilesDirs(mContext, null)) {
                if (externalCacheDir.listFiles() != null) {
                    for (File file : externalCacheDir.listFiles()) {
                        file.delete();
                    }
                }
            }
        } else {
            File mDataDir = null;
            mDataDir = mContext.getExternalFilesDir(null);
            if (mDataDir.exists() && mDataDir.listFiles() != null)
                for (File file : mDataDir.listFiles()) {
                    file.delete();
                }
        }
    }

    public File getVideoCacheDir(boolean createDir) {
        File rootDir = getExtCacheDir();
        File videoDir = new File(rootDir, VIDEO_CACHE_DIR);
        if (createDir) {
            if (!videoDir.exists()) videoDir.mkdirs();
        }
        return videoDir.exists() ? videoDir : null;
    }

    public void clearVideoCache() {
        File videoDir = getVideoCacheDir(false);
        if (videoDir != null && videoDir.exists()) {
            if (videoDir.isDirectory()) {
                for (File file : videoDir.listFiles()) {
                    Timber.d("clearVideoCache: %s", file);
                    file.delete();
                }
            } else {
                videoDir.delete();
            }
        }
    }

    public byte[] getFileBytes(String filePath) {
        File file = new File(filePath);
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes, 0, bytes.length);
            buf.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return bytes;
    }


    public boolean ifFolderExists(String from) {
        String path = "";
        switch (from) {
            case Constants.TAG_SENT:
                path = "/" + mContext.getString(R.string.app_name) + "/" + mContext.getString(R.string.app_name) + "Images/Sent/";
                break;
            case "profile":
                path = "/" + mContext.getString(R.string.app_name) + "/" + mContext.getString(R.string.app_name) + "Images/Profile/";
                break;
            case Constants.TAG_THUMBNAIL:
                path = "/" + mContext.getString(R.string.app_name) + "/" + mContext.getString(R.string.app_name) + "Images/.thumbnails/";
                break;
            case StorageUtils.TAG_AUDIO_SENT:
                //path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name).replaceAll(" ", "") + "_Audio/Sent/";
                path = "/" + "Android" + "/" + "data/" + "com.app./" + "files/" + "_Audio/Sent/";
                break;
            case Constants.TAG_AUDIO_RECEIVE:
                //path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name).replaceAll(" ", "") + "_Audio/";
                path = "/" + "Android" + "/" + "data/" + "com.app./" + "files/" + "_Audio/";
                break;
            default:
                path = "/" + mContext.getString(R.string.app_name) + "/" + mContext.getString(R.string.app_name) + "Images/";
                break;
        }


        File sdcard = Environment.getExternalStorageDirectory();
        File folder = new File(sdcard.getAbsoluteFile(), path);
        if (!folder.exists()) {
            folder.mkdirs();
            return true;
        }
        return true;
    }

    public Uri getFileUri(String from, String fileName, String fileType) {
        Uri fileUri = null;

        switch (fileType) {
            case TAG_AUDIO:
                fileUri = getAudioUriFromName(fileName, from);
                break;
            case TAG_VIDEO:
                fileUri = getVideoUriFromName(fileName, from);
                break;
            case TAG_IMAGE:
                fileUri = getImageUriFromName(fileName, from);
                break;
        }
        if (fileUri != null) {
            Timber.d("getFileUri: %s, %s, %s, %s", fileUri.getPath(), fileName, fileType, from);
        }
        return fileUri;
    }

    public Uri getAudioUriReceive(String from, String fileName) {
        fileName = getFileName(fileName);
        if (!fileName.endsWith(".mp3")) {
            fileName = fileName + ".mp3";
        }
        Log.d(TAG, "getAudioUri: " + fileName);
        Uri audioUri = null;
        String path = getPathLivza(from);
        ContentResolver resolver = mContext.getContentResolver();

        if (from.equals(Constants.TAG_AUDIO_RECEIVE) && Build.VERSION.SDK_INT != Build.VERSION_CODES.Q) {
            Uri collection = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                    ? MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
                    : MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            final String selection;
            final String[] selectionArgs;
            // which image properties are we querying
            String[] projection = new String[]{
                    MediaStore.Audio.Media._ID
            };
            selection = MediaStore.Audio.Media.DISPLAY_NAME + " =? AND " +
                    MediaStore.Audio.Media.RELATIVE_PATH + "=?";
            selectionArgs = new String[]{fileName, Environment.DIRECTORY_MUSIC + path};
            int columnIndexID;
            Cursor cursor = resolver.query(collection, projection, selection, selectionArgs, null);
            if (cursor != null) {
                columnIndexID = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
                if (cursor.moveToFirst()) {
                    long imageId = cursor.getLong(columnIndexID);
                    audioUri = ContentUris.withAppendedId(
                            collection, imageId);
                }
                cursor.close();
            }
            audioUri = checkValidUri(audioUri) ? audioUri : null;
            Log.d(TAG, "getAudioUri: " + audioUri);
        } else {
            File audioFile = getAudioFilelivza(from, fileName);
            if (audioFile != null && audioFile.exists()) {
                audioUri = getUriFromFile(audioFile);
            }
        }
        return audioUri;
    }

    private boolean checkValidUri(Uri uri) {
        if (uri == null) return false;
        try {
            InputStream inputStream = mContext.getContentResolver().openInputStream(uri);
            inputStream.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public File getFileReceive(String from, String fileName) {
        File file = null;
        try {
            String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).toString();
            File myDir = new File(root);
            if (!myDir.exists())
                return null;

            String path = "";
            switch (from) {
                case Constants.TAG_SENT:
                    path = "/" + mContext.getString(R.string.app_name) + "/" + mContext.getString(R.string.app_name) + "Images/Sent/";
                    break;
                case "profile":
                    path = "/" + mContext.getString(R.string.app_name) + "/" + mContext.getString(R.string.app_name) + "Images/Profile/";
                    break;
                case Constants.TAG_THUMBNAIL:
                    path = "/" + mContext.getString(R.string.app_name) + "/" + mContext.getString(R.string.app_name) + "Images/.thumbnails/";
                    break;
                case TAG_AUDIO_SENT:
                    path = "/" + mContext.getString(R.string.app_name) + "/" + mContext.getString(R.string.app_name).replaceAll(" ", "") + "_Audio/Sent/";
                    break;
                case Constants.TAG_AUDIO_RECEIVE:
                    path = "/" + mContext.getString(R.string.app_name) + "/" + mContext.getString(R.string.app_name).replaceAll(" ", "") + "_Audio/";
                    break;
                default:
                    path = "/" + mContext.getString(R.string.app_name) + "/" + mContext.getString(R.string.app_name) + "Images/";
                    break;
            }
            file = new File(myDir.getPath() + path + fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }


    /**
     * Returns the Uri which can be used to delete/work with images in the photo gallery.
     *
     * @param displayName Path to IMAGE on SD card
     * @return Uri in the format of... content://media/external/images/media/[NUMBER]
     */
    private Uri getImageUriFromName(String displayName, String from) {
        String relativePath;
        relativePath = Environment.DIRECTORY_PICTURES + getFolderPath(from);
        String extension = null;
        try {
            extension = displayName.substring(displayName.lastIndexOf("."));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(extension)) {
            displayName = displayName + Utility.IMAGE_FORMAT;
        }

        Uri returnUri = null;
        // which image properties are we querying
        String[] projection = new String[]{
                MediaStore.Images.Media._ID
        };

        Uri collection;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        } else {
            collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }
        String selection = MediaStore.Images.Media.RELATIVE_PATH + " == ? AND " +
                MediaStore.Images.Media.DISPLAY_NAME + " == ?" + " or " +
                MediaStore.MediaColumns.TITLE + " == ?";
        String[] selectionArgs = new String[]{
                relativePath, displayName
        };
        ContentResolver resolver = mContext.getContentResolver();
        Cursor imageCursor = resolver.query(collection, projection, selection, selectionArgs, null);
        int columnIndexID;
        if (imageCursor != null) {
            columnIndexID = imageCursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
            if (imageCursor.moveToFirst()) {
                long imageId = imageCursor.getLong(columnIndexID);
                returnUri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageId);
            }
            imageCursor.close();
        }
        if (returnUri != null) {
            Log.d(TAG, "getImageUriFromName: " + returnUri.getPath());
        }
        return returnUri;
    }

    private Uri getVideoUriFromName(String displayName, String from) {
        String relativePath;
        relativePath = Environment.DIRECTORY_MOVIES + getFolderPath(from);
        String extension = null;
        try {
            extension = displayName.substring(displayName.lastIndexOf("."));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(extension)) {
            displayName = displayName + Utility.IMAGE_FORMAT;
        }
        Log.d(TAG, "getVideoUriFromName: " + relativePath + ", " + displayName + ", " + from);
        Uri returnUri = null;
        // which image properties are we querying
        String[] projection = new String[]{
                MediaStore.Video.Media._ID
        };
        Uri collection;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            collection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);

            String selection = MediaStore.Video.Media.RELATIVE_PATH + " ==? AND " +
                    MediaStore.Video.Media.DISPLAY_NAME + " == ?";
            String[] selectionArgs = new String[]{relativePath, displayName};
            ContentResolver resolver = mContext.getContentResolver();
            Cursor cursor = resolver.query(collection, projection, selection, selectionArgs, null);
            int columnIndexID;
            if (cursor != null) {
                columnIndexID = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
                if (cursor.moveToFirst()) {
                    long imageId = cursor.getLong(columnIndexID);
                    returnUri = ContentUris.withAppendedId(
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI, imageId);
                }
                cursor.close();
            }
            if (returnUri != null) {
                Log.d(TAG, "getVideoUriFromName: " + returnUri.getPath());
            }
        }
        return returnUri;
    }

    private Uri getAudioUriFromName(String displayName, String from) {
        String relativePath = Environment.DIRECTORY_MUSIC + getFolderPath(from);
        Uri returnUri = null;
        if (!displayName.endsWith(".mp3") || !displayName.endsWith(".MP3")) {
            displayName = displayName + ".mp3";
        }
        // which image properties are we querying
        String[] projection = new String[]{
                MediaStore.Audio.Media._ID
        };
        Uri collection;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            collection = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        } else {
            collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        }
//        Log.d(TAG, "getAudioUriFromName: " + relativePath);
        String selection = MediaStore.Audio.Media.RELATIVE_PATH + " ==? AND " +
                MediaStore.Audio.Media.DISPLAY_NAME + " == ?";
        String[] selectionArgs = new String[]{relativePath, displayName};
        ContentResolver resolver = mContext.getContentResolver();
        Cursor cursor = resolver.query(collection, projection, selection, selectionArgs, null);
        int columnIndexID;
        if (cursor != null) {
            columnIndexID = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
            if (cursor.moveToFirst()) {
                long imageId = cursor.getLong(columnIndexID);
                returnUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, imageId);
            }
            cursor.close();
        }
        if (returnUri != null) {
            Log.d(TAG, "getAudioUriFromName: " + returnUri.getPath());
        }
        return returnUri;
    }

    public Bitmap getThumbnailFromVideo(File mSrcFile) {
        Bitmap thumb = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                thumb = ThumbnailUtils.createVideoThumbnail(mSrcFile,
                        getBitmapSize(mSrcFile, mContext), null);
                Log.d(TAG, "getThumbnailFromVideo: Android Q+ thumb: " + thumb);
            } catch (IOException e) {
                Log.e(TAG, "getThumbnailFromVideo: Android Q+ failed: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            // Try the deprecated method first
            try {
                thumb = ThumbnailUtils.createVideoThumbnail(mSrcFile.getAbsolutePath(),
                        MediaStore.Video.Thumbnails.MINI_KIND);
                Log.d(TAG, "getThumbnailFromVideo: deprecated method thumb: " + thumb);
            } catch (Exception e) {
                Log.e(TAG, "getThumbnailFromVideo: deprecated method failed: " + e.getMessage());
            }
        }

        // If both methods failed, try MediaMetadataRetriever as fallback
        if (thumb == null) {
            Log.w(TAG, "Primary methods failed, trying MediaMetadataRetriever fallback");
            thumb = getThumbnailWithMediaRetriever(mSrcFile.getAbsolutePath());
        }

        return thumb;
    }

    private Bitmap getThumbnailWithMediaRetriever(String videoPath) {
        MediaMetadataRetriever retriever = null;
        try {
            retriever = new MediaMetadataRetriever();

            // Check if file exists and is readable
            File file = new File(videoPath);
            if (!file.exists() || !file.canRead()) {
                Log.e(TAG, "Video file not accessible: " + videoPath);
                return null;
            }

            retriever.setDataSource(videoPath);

            // Get video duration to ensure we don't exceed it
            String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            long durationMs = duration != null ? Long.parseLong(duration) : 0;

            Bitmap bitmap = null;

            // Try multiple time positions if the first fails
            long[] timePositions = {1000000, 500000, 2000000, 0}; // 1s, 0.5s, 2s, start

            for (long timeUs : timePositions) {
                if (durationMs > 0 && timeUs > durationMs * 1000) {
                    Log.d(TAG, "getThumbnailWithMediaRetriever: time "+timeUs);
                    continue; // Skip if time exceeds video duration
                }

                try {
                    bitmap = retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                    if (bitmap != null) {
                        Log.d(TAG, "Successfully extracted frame at " + timeUs + "μs");
                        break;
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Failed to get frame at " + timeUs + "μs: " + e.getMessage());
                }
            }

            // Last resort: try getting any frame
            if (bitmap == null) {
                try {
                    bitmap = retriever.getFrameAtTime();
                    Log.d(TAG, "Got frame using getFrameAtTime() without parameters");
                    Log.d(TAG,"bitmap size height"+bitmap.getHeight() +" width "+bitmap.getWidth() );
                } catch (Exception e) {
                    Log.e(TAG, "Failed to get any frame: " + e.getMessage());
                }
            }

            return bitmap;

        } catch (Exception e) {
            Log.e(TAG, "MediaMetadataRetriever failed: " + e.getMessage());
            return null;
        } finally {
            if (retriever != null) {
                try {
                    retriever.release();
                } catch (Exception e) {
                    Log.e(TAG, "Error releasing MediaMetadataRetriever: " + e.getMessage());
                }
            }
        }
    }

    public static Size getBitmapSize(File mSrcFile, Context mContext) {
        MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
        metaRetriever.setDataSource(mSrcFile.getAbsolutePath());
        String height = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
        String width = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
        return new Size(Integer.parseInt(width), Integer.parseInt(height));
    }

    public Uri saveFileInStorageV10(File srcFile, String fileName, String fileType) {
        Uri savedUri = null;
        Uri mediaContentUri = null;
        ContentValues values = new ContentValues();
        if (fileType.equals(StorageUtils.TAG_VIDEO)) {
            mediaContentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            String directory = Environment.DIRECTORY_MOVIES + getFolderPath(StorageUtils.TAG_VIDEO);
            values.put(MediaStore.Video.Media.TITLE, fileName);
            values.put(MediaStore.Video.Media.DISPLAY_NAME, fileName);
            values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
            values.put(MediaStore.Video.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
            values.put(MediaStore.Video.Media.RELATIVE_PATH, directory);
            values.put(MediaStore.Video.Media.IS_PENDING, 1);
        } else if (fileType.equals(StorageUtils.TAG_AUDIO)) {
            mediaContentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            String directory = Environment.DIRECTORY_MUSIC + getFolderPath(StorageUtils.TAG_AUDIO);
            values.put(MediaStore.Audio.Media.TITLE, fileName);
            values.put(MediaStore.Audio.Media.DISPLAY_NAME, fileName);
            values.put(MediaStore.Audio.Media.MIME_TYPE, getMimeType(srcFile.getAbsolutePath(), fileType));
            values.put(MediaStore.Audio.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
            values.put(MediaStore.Audio.Media.RELATIVE_PATH, directory);
            values.put(MediaStore.Audio.Media.IS_PENDING, 1);
        } else if (fileType.equals(StorageUtils.TAG_IMAGE)) {
            mediaContentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            String directory = Environment.DIRECTORY_PICTURES + getFolderPath(StorageUtils.TAG_IMAGE);
            values.put(MediaStore.Images.Media.TITLE, fileName);
            values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
            values.put(MediaStore.Images.Media.MIME_TYPE, getMimeType(srcFile.getAbsolutePath(), fileType));
            values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
            values.put(MediaStore.Images.Media.RELATIVE_PATH, directory);
            values.put(MediaStore.Images.Media.IS_PENDING, 1);
        }
        savedUri = mContext.getContentResolver().insert(mediaContentUri, values);
        if (savedUri != null) {
            ParcelFileDescriptor pfd;
            try {
                pfd = mContext.getContentResolver().openFileDescriptor(savedUri, "w");
                FileOutputStream out = new FileOutputStream(pfd.getFileDescriptor());
                InputStream in = new FileInputStream(srcFile);
                byte[] buf = new byte[8192];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.close();
                in.close();
                pfd.close();
                switch (fileType) {
                    case Constants.TAG_IMAGE: {
                        values.put(MediaStore.Images.Media.IS_PENDING, 0);
                    }
                    break;
                    case Constants.TAG_AUDIO: {
                        values.put(MediaStore.Audio.Media.IS_PENDING, 0);
                    }
                    break;
                    case Constants.TAG_VIDEO: {
                        values.put(MediaStore.Video.Media.IS_PENDING, 0);
                    }
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            mContext.getContentResolver().update(savedUri, values, null, null);
            Log.d(TAG, "saveFileInStorageV10: " + savedUri);
            return savedUri;
        }
        return null;
    }


    public Uri saveFileInStorageV10livza(File srcFile, String fileName, String from, String fileType) {
        Uri savedUri = null;
        Uri mediaContentUri = null;
        ContentValues values = null;
        Log.d(TAG, "saveFileInStorageV10: " + fileName);
        switch (fileType) {
            case Constants.TAG_IMAGE:
                mediaContentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                values = getContentValues(mContext, from, fileName, "image/*");
                break;
            case TAG_AUDIO:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    mediaContentUri = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
                } else {
                    mediaContentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                values = getContentValues(mContext, from, fileName, "audio/*");
                break;
            case Constants.TAG_VIDEO:
                mediaContentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                values = getContentValues(mContext, from, fileName, "video/mp4");
                break;

            default:
                break;
        }

        savedUri = mContext.getContentResolver().insert(mediaContentUri, values);
        ParcelFileDescriptor pfd = null;
        try {
            pfd = mContext.getContentResolver().openFileDescriptor(savedUri, "w");
            FileInputStream inputStream = new FileInputStream(srcFile);
            boolean saved = FileUtil.copyFile(inputStream, new FileOutputStream(pfd.getFileDescriptor()));
            Log.d(TAG, "saveFileInStorageV10: " + savedUri);
            switch (fileType) {
                case Constants.TAG_VIDEO: {
                    values.put(MediaStore.Video.Media.IS_PENDING, 0);
                }
                break;
                case TAG_AUDIO: {
                    values.put(MediaStore.Audio.Media.IS_PENDING, 0);
                }
                break;

                default:
                    values.put(MediaStore.Images.Media.IS_PENDING, 0);
                    break;
            }
            mContext.getContentResolver().update(savedUri, values, null, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return savedUri;
    }


    public ContentValues getContentValues(Context mContext, String from, String
            fileName, String mimeType) {
        String directory = "";
        ContentValues values = new ContentValues();
        switch (from) {
            case Constants.TAG_VIDEO: {
                directory = Environment.DIRECTORY_MOVIES + getPath(from);
                values.put(MediaStore.Video.Media.TITLE, fileName);
                values.put(MediaStore.Video.Media.DISPLAY_NAME, fileName);
                values.put(MediaStore.Video.Media.MIME_TYPE, mimeType);
                values.put(MediaStore.Video.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
                values.put(MediaStore.Video.Media.RELATIVE_PATH, directory);
                values.put(MediaStore.Video.Media.IS_PENDING, 1);
            }
            break;
            case Constants.TAG_AUDIO_RECEIVE: {
                directory = Environment.DIRECTORY_MUSIC + getPath(from);
                values.put(MediaStore.Audio.Media.TITLE, fileName);
                values.put(MediaStore.Audio.Media.DISPLAY_NAME, fileName);
                values.put(MediaStore.Audio.Media.MIME_TYPE, mimeType);
                values.put(MediaStore.Audio.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
                values.put(MediaStore.Audio.Media.RELATIVE_PATH, directory);
                values.put(MediaStore.Audio.Media.IS_PENDING, 1);
            }
            break;
            case TAG_AUDIO_SENT: {
                directory = Environment.DIRECTORY_MUSIC + getPath(from);
                values.put(MediaStore.Audio.Media.TITLE, fileName);
                values.put(MediaStore.Audio.Media.DISPLAY_NAME, fileName);
                values.put(MediaStore.Audio.Media.MIME_TYPE, mimeType);
                values.put(MediaStore.Audio.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
                values.put(MediaStore.Audio.Media.RELATIVE_PATH, directory);
                values.put(MediaStore.Audio.Media.IS_PENDING, 1);
            }
            break;

            default:
                directory = Environment.DIRECTORY_PICTURES + getPath(from);
                values.put(MediaStore.Files.FileColumns.TITLE, fileName);
                values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
                values.put(MediaStore.Images.Media.MIME_TYPE, mimeType);
                values.put(MediaStore.Files.FileColumns.DATE_ADDED, System.currentTimeMillis() / 1000);
                values.put(MediaStore.Images.Media.RELATIVE_PATH, directory);
                values.put(MediaStore.Images.Media.IS_PENDING, 1);
                break;
        }
        return values;
    }


    public File saveFileInStorage(File src, String from, String fileType) {
        File savedFile = null;
        String fileName = src.getName();
        File root;
        if (fileType.equals(Constants.TAG_VIDEO)) {
            root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        } else if (fileType.equals(Constants.TAG_AUDIO)) {
            root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
        } else {
            root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        }
        File destDir = new File(root, getFolderPath(from));
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        if (src.exists()) {
            try {
                File newFile = new File(destDir.getAbsoluteFile(), fileName);
                InputStream inputStream = new FileInputStream(src);
                OutputStream outputStream = new FileOutputStream(newFile);
                byte[] buf = new byte[1024];
                int bytesRead;

                while ((bytesRead = inputStream.read(buf)) > 0) {
                    outputStream.write(buf, 0, bytesRead);
                }

                outputStream.flush();
                inputStream.close();
                outputStream.close();
                savedFile = newFile;
                refreshGallery(savedFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (savedFile != null) {
            Log.d(TAG, "saveFileInStorage: " + savedFile.getAbsolutePath());
            return savedFile;
        } else {
            return null;
        }
    }


    public String saveFileInStorageLivza(File src, String from) {
        File savedFile = null;
        String fileName = src.getName();
        File destDir = createDirectory(from);

        if (src != null && src.exists()) {
            try {
                File newFile = new File(destDir.getAbsoluteFile(), fileName);
                InputStream inputStream = new FileInputStream(src);
                OutputStream outputStream = new FileOutputStream(newFile);
                byte[] buf = new byte[1024];
                int bytesRead;

                while ((bytesRead = inputStream.read(buf)) > 0) {
                    outputStream.write(buf, 0, bytesRead);
                }

                outputStream.flush();
                inputStream.close();
                outputStream.close();
                savedFile = newFile;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (savedFile != null) {
            Log.d(TAG, "saveFileInStorage: " + savedFile.getAbsolutePath());
            return savedFile.getAbsolutePath();
        } else {
            return null;
        }
    }


    public File createDirectory(String from) {
        File folder = null;
        switch (from) {

            case TAG_AUDIO_RECEIVE: {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    folder = new File(getExtFilesDir() + getSentPathQ(from));
                } else {
                    folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC) + getFolderPath(from));
                }
            }
            break;
            case TAG_AUDIO:
                folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC) + getFolderPath(from));
                break;
            default:
                break;
        }
        if (!folder.exists()) {
            folder.mkdirs();
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (from.equals("sent") || from.equals(Constants.TAG_STATUS)) {
                File noMediaFile = new File(folder.getAbsoluteFile(), ".nomedia");
                try {
                    if (!noMediaFile.exists()) {
                        noMediaFile.createNewFile();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return folder;
    }

    public String getSentPathQ(String from) {
        String path = "";
        switch (from) {
            case Constants.TAG_IMAGE:
                path = "/" + "Images/";
                break;
            case "sent":

            case "profile":
                path = "/" + "Images/.Profile/";
                break;

            case "audio":
                path = "/" + "Audios/";
                break;
            case TAG_AUDIO_SENT:
                path = "/" + "Audios/Sent/";
                break;
            case TAG_AUDIO_RECEIVE:
                path = "/" + mContext.getString(R.string.app_name) + "/" + mContext.getString(R.string.app_name).replaceAll(" ", "") + "_Audio/";
                break;
            default:
                path = "/" + "Files/";
                break;
        }
        Log.d(TAG, "getSentPathQ: " + path);
        return path;
    }


    public int getMediaDuration(Context context, Uri fileUri) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(context, fileUri);
        String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        try {
            int duration = Integer.parseInt(durationStr);
            return duration;
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getMediaDuration(File srcFile) {
        int duration = 0;
        if (srcFile != null && srcFile.length() > 0) {
            try {
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                mmr.setDataSource(srcFile.getAbsolutePath());
                String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                duration = Integer.parseInt(durationStr);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "getMediaDuration: " + duration);
        return duration;
    }

    /*public long getMediaDuration(Context mContext, String filePath) {
        long duration = 0;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(filePath);
        if (retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION) != null) {
            duration = Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
            retriever.release();
        } else {
            IsoFile isoFile = null;
            try {
                isoFile = new IsoFile(filePath);
                double lengthInSeconds = (double)
                        isoFile.getMovieBox().getMovieHeaderBox().getDuration() /
                        isoFile.getMovieBox().getMovieHeaderBox().getTimescale();
                duration = (Math.round(lengthInSeconds * 1000));
            } catch (IOException e) {
                e.printStackTrace();
                duration = 0;
            }
        }
        Log.d(TAG, "getMediaDuration: " + duration);
        return duration;
    }*/

    public long getAudioDuration(Context context, Uri fileUri, String fileType) {
        long duration = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (fileType.equals(Constants.TAG_AUDIO)) {
                String[] projection = new String[]{
                        MediaStore.Audio.Media._ID,
                        MediaStore.Audio.Media.DISPLAY_NAME,
                        MediaStore.Audio.Media.DURATION,
                        MediaStore.Audio.Media.SIZE
                };
                Cursor cursor = context.getContentResolver().query(fileUri, projection, null, null, null);
                if (cursor != null && cursor.getCount() != 0) {
                    int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
                    cursor.moveToFirst();
                    duration = cursor.getLong(columnIndex);
                }
                if (cursor != null) {
                    cursor.close();
                }
            } else {
                String[] projection = new String[]{
                        MediaStore.Video.Media._ID,
                        MediaStore.Video.Media.DISPLAY_NAME,
                        MediaStore.Video.Media.DURATION,
                        MediaStore.Video.Media.SIZE
                };
                Cursor cursor = context.getContentResolver().query(fileUri, projection, null, null, null);
                if (cursor != null && cursor.getCount() != 0) {
                    int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION);
                    cursor.moveToFirst();
                    duration = cursor.getLong(columnIndex);
                }
                if (cursor != null) {
                    cursor.close();
                }
            }
        } else {
            duration = getMediaDuration(context, fileUri);
        }
        Log.d(TAG, "getAudioDuration: " + duration);
        return duration;
    }

    public static boolean isVideoHaveAudioTrack(String path) {
        boolean audioTrack = false;

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(path);
        String hasAudioStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO);
        Timber.d("isVideoHaveAudioTrack: %s", hasAudioStr);
        if (hasAudioStr != null && hasAudioStr.equals("yes")) {
            audioTrack = true;
        } else {
            audioTrack = false;
        }

        return audioTrack;
    }


    public File getAudioFilelivza(String from, String imageName) {
        File mediaImage = null;
        try {
//            File root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File root = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                root = getRootDir(String.valueOf(mContext));
            } else {
                root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
            }

            //    File myDir = new File(root);
            if (!root.exists())
                return null;

            String path = "";
            path = getPathLivza(from);
            mediaImage = new File(root.getPath() + path + FilenameUtils.getName(imageName));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mediaImage;
    }

    public boolean checkIfAudioExists(String from, String imageName) {
        File file = getAudioFilelivza(from, imageName);
        return file != null && file.exists();
    }

}
