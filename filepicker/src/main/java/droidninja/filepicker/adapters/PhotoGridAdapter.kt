package droidninja.filepicker.adapters

import android.content.Context
import android.database.Cursor
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import droidninja.filepicker.FilePickerConst
import droidninja.filepicker.PickerManager
import droidninja.filepicker.R
import droidninja.filepicker.models.Media
import droidninja.filepicker.utils.AndroidLifecycleUtils
import droidninja.filepicker.views.SmoothCheckBox
import java.lang.NumberFormatException

class PhotoGridAdapter(
    private val context: Context,
    private val glide: RequestManager,
    medias: List<Media>,
    selectedPaths: MutableList<Uri>,
    private val showCamera: Boolean,
    private val mListener: FileAdapterListener?
) : SelectableAdapter<PhotoGridAdapter.PhotoViewHolder, Media>(medias, selectedPaths) {
    private var imageSize: Int = 0
    private var imageSize_value: Int = 0
    private var cameraOnClickListener: View.OnClickListener? = null

    init {
        setColumnNumber(context, 3)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val itemView =
            LayoutInflater.from(context).inflate(R.layout.item_photo_layout, parent, false)

        return PhotoViewHolder(itemView)
    }

    override fun getItemViewType(position: Int): Int {
        return if (showCamera)
            if (position == 0) ITEM_TYPE_CAMERA else ITEM_TYPE_PHOTO
        else
            ITEM_TYPE_PHOTO
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        if (getItemViewType(position) == ITEM_TYPE_PHOTO) {

            val media = items[if (showCamera) position - 1 else position]

            if (AndroidLifecycleUtils.canLoadImage(holder.imageView.context)) {
                glide.load(media.path)
                    .apply(
                        RequestOptions
                            .centerCropTransform()
                            .override(imageSize, imageSize)
                            .placeholder(R.drawable.image_placeholder)
                    )
                    .thumbnail(0.5f)
                    .into(holder.imageView)
            }


            if (media.mediaType == FilePickerConst.MEDIA_TYPE_VIDEO)
                holder.videoIcon.visibility = View.VISIBLE
            else
                holder.videoIcon.visibility = View.GONE

            holder.itemView.setOnClickListener { onItemClicked(holder, media) }

            //in some cases, it will prevent unwanted situations
            holder.checkBox.visibility = View.VISIBLE
            holder.checkBox.setOnCheckedChangeListener(null)
            holder.checkBox.setOnClickListener { /*null*/onItemClicked(holder, media) }

            //if true, your checkbox will be selected, else unselected
//            holder.checkBox.isChecked = isSelected(media)
//
//            holder.selectBg.visibility = if (isSelected(media)) View.VISIBLE else View.GONE
//            holder.checkBox.visibility = if (isSelected(media)) View.VISIBLE else View.GONE

       /*     holder.checkBox.setOnCheckedChangeListener(object :
                SmoothCheckBox.OnCheckedChangeListener {
                override fun onCheckedChanged(checkBox: SmoothCheckBox, isChecked: Boolean) {
                    toggleSelection(media)
                    holder.selectBg.visibility = if (isChecked) View.VISIBLE else View.GONE

                    if (isChecked) {
                        holder.checkBox.visibility = View.VISIBLE
                        PickerManager.add(media.path, FilePickerConst.FILE_TYPE_MEDIA)
                    } else {
                        holder.checkBox.visibility = View.GONE
                        PickerManager.remove(media.path, FilePickerConst.FILE_TYPE_MEDIA)
                    }

                    mListener?.onItemSelected()
                }
            })*/

        } else {
            holder.imageView.setImageResource(PickerManager.cameraDrawable)
            holder.checkBox.visibility = View.GONE
            holder.itemView.setOnClickListener(cameraOnClickListener)
            holder.videoIcon.visibility = View.GONE
        }
    }

    private fun onItemClicked(holder: PhotoViewHolder, media: Media) {
//        if (PickerManager.getMaxCount() == 1) {
//        if (imageSize_value<5) {
//            mmmm
            Log.d("PickerManager1234", imageSize.toString());

            if (holder.checkBox.isChecked) {
                imageSize_value--
                holder.checkBox.setChecked(false);

                Log.d(TAG, "onItemClicked123: " + media.name + ", " + media.duration)
                if (getFileSizeInMB(media.path) <= FilePickerConst.MAX_FILE_SIZE) {
                    if (media.mediaType == FilePickerConst.MEDIA_TYPE_VIDEO) {
                        if (getMediaDuration(context, media.path) > 0) {
                            if (media.duration > FilePickerConst.DEFAULT_VIDEO_DURATION) {
//                              PickerManager.add(media.path, FilePickerConst.FILE_TYPE_MEDIA)
                                PickerManager.remove(media.path, FilePickerConst.FILE_TYPE_MEDIA);
                                mListener?.onItemSelected()
                            } else {
                                Toast.makeText(
                                    context,
                                    String.format(
                                        context.getString(R.string.minimum_video_length),
                                        (FilePickerConst.DEFAULT_VIDEO_DURATION / 1000L)
                                    ),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            Toast.makeText(
                                context,
                                context.getString(R.string.invalid_video),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
//                      PickerManager.add(media.path, FilePickerConst.FILE_TYPE_MEDIA)
                        PickerManager.remove(media.path, FilePickerConst.FILE_TYPE_MEDIA)
                        mListener?.onItemSelected()
                    }
                } else {
                    Toast.makeText(
                        context,
                        String.format(
                            context.getString(R.string.maximum_file_size),
                            FilePickerConst.MAX_FILE_SIZE
                        ),
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } else {
                if (imageSize_value < 4) {


                    imageSize_value++
                    holder.checkBox.setChecked(true);

                    Log.d(TAG, "onItemClicked123: " + media.name + ", " + media.duration)
                    if (getFileSizeInMB(media.path) <= FilePickerConst.MAX_FILE_SIZE) {
                        if (media.mediaType == FilePickerConst.MEDIA_TYPE_VIDEO) {
                            if (getMediaDuration(context, media.path) > 0) {
                                if (media.duration > FilePickerConst.DEFAULT_VIDEO_DURATION) {
                                    PickerManager.add(media.path, FilePickerConst.FILE_TYPE_MEDIA)
                                    mListener?.onItemSelected()
                                } else {
                                    Toast.makeText(
                                        context,
                                        String.format(
                                            context.getString(R.string.minimum_video_length),
                                            (FilePickerConst.DEFAULT_VIDEO_DURATION / 1000L)
                                        ),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.invalid_video),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            PickerManager.add(media.path, FilePickerConst.FILE_TYPE_MEDIA)
                            mListener?.onItemSelected()
                        }
                    } else {
                        Toast.makeText(
                            context,
                            String.format(
                                context.getString(R.string.maximum_file_size),
                                FilePickerConst.MAX_FILE_SIZE
                            ),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        context, "Only 4 images are allowed to add",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
//        }

    }

    private fun getFileSizeInMB(fileUri: Uri): Long {
        val returnCursor: Cursor? =
            context.contentResolver.query(fileUri, null, null, null, null)
        /*
         * Get the column indexes of the data in the Cursor,
         * move to the first row in the Cursor, get the data,
         * and display it.
         */
        val nameIndex = returnCursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        val sizeIndex = returnCursor?.getColumnIndex(OpenableColumns.SIZE)
        returnCursor?.moveToFirst()
        if (returnCursor != null && sizeIndex != null) {
            val fileSizeInBytes: Long = returnCursor.getLong(sizeIndex)
            // Convert the bytes to Kilobytes (1 KB = 1024 Bytes)
            val fileSizeInKB = fileSizeInBytes / 1024
            // Convert the KB to MegaBytes (1 MB = 1024 KBytes)
            val fileSizeInMB = fileSizeInKB / 1024
            Log.d(TAG, "getFileSizeInMB: " + fileSizeInMB)
            return fileSizeInMB
        } else {
            return 0
        }
    }

    fun getMediaDuration(context: Context?, fileUri: Uri?): Int {
        val mmr = MediaMetadataRetriever()
        try {
            mmr.setDataSource(context, fileUri)
            val durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            return durationStr!!.toInt()
        } catch (e: NumberFormatException) {
            e.printStackTrace()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
        return 0
    }

    private fun setColumnNumber(context: Context, columnNum: Int) {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        wm.defaultDisplay.getMetrics(metrics)
        val widthPixels = metrics.widthPixels
        imageSize = widthPixels / columnNum
    }

    override fun getItemCount(): Int {
        return if (showCamera) items.size + 1 else items.size
    }

    fun setCameraListener(onClickListener: View.OnClickListener) {
        this.cameraOnClickListener = onClickListener
    }

    class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var checkBox: SmoothCheckBox

        var imageView: ImageView

        var videoIcon: ImageView

        var selectBg: View

        init {
            checkBox = itemView.findViewById<View>(R.id.checkbox) as SmoothCheckBox
            imageView = itemView.findViewById<View>(R.id.iv_photo) as ImageView
            videoIcon = itemView.findViewById<View>(R.id.video_icon) as ImageView
            selectBg = itemView.findViewById(R.id.transparent_bg)
        }
    }

    companion object {
        val TAG = PhotoGridAdapter::class.java.simpleName
        val ITEM_TYPE_CAMERA = 100
        val ITEM_TYPE_PHOTO = 101
    }
}
