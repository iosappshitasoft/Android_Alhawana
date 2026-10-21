package com.app.Zego.Adapter

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.app.Zego.Additional.VerticalSlidePageTransformer
import com.app.Zego.Fragment.PkLiveStreamingFragment
import com.app.Zego.internal.sdk.ZEGOSDKManager
import com.app.Zego.pkbattle.activity.SwipeLiveActivity
import com.app.addon.livestream.external.model.StreamDetails
import com.app.alhawanajd.R
import com.app.alhawanajd.databinding.ContentSwipeLiveBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

import com.google.gson.Gson
import jp.wasabeef.glide.transformations.BlurTransformation

class SwipeLiveAdapter(
    private val activity: FragmentActivity, // ✅ use valid FragmentActivity
    private val viewPager: ViewPager2
) : RecyclerView.Adapter<SwipeLiveAdapter.SwipeLiveViewHolder>() {

    private var list: MutableList<StreamDetails> = mutableListOf()
    private var fragment: Fragment? = null
    //newly added
    private val fragmentMap = mutableMapOf<Int, PkLiveStreamingFragment>()

    companion object {
        const val TAG = "SwipeLiveAdapter"
        var selectedControlScreenPosition = 1
        val userInfo = ZEGOSDKManager.getInstance().expressService.currentUser
    }

    fun updateLiveStream(newList: MutableList<StreamDetails>) {
        Log.e(TAG, "updateLiveStream: ::::::${Gson().toJson(newList)}")
        val size = list.size
        list.addAll(newList)
        notifyItemRangeInserted(size, newList.size)
    }

    //newly added
    fun getFragmentAt(position: Int): PkLiveStreamingFragment? {
        return fragmentMap[position]
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SwipeLiveViewHolder {
        val binding =
            ContentSwipeLiveBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SwipeLiveViewHolder(binding)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: SwipeLiveViewHolder, position: Int) {
   /*     if (fragment != null && position == viewPager.currentItem) {
            Log.e(TAG, "onBindViewHolder: ")
        } else {
            holder.onBind(list[position])
        }*/
        //newly added
        holder.onBind(list[position], position)
    }

    inner class SwipeLiveViewHolder(
        private val binding: ContentSwipeLiveBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun onBind(item: StreamDetails, position: Int) {
            setIsRecyclable(false)

            val blurTransformation = BlurTransformation(50)
            Glide.with(activity)
                .load(item.publisherImage)
                .error(R.drawable.alhawana)
                .apply(RequestOptions.bitmapTransform(blurTransformation))
                .into(binding.userImage)

            SwipeLiveActivity.hostId = item.publisherId


            /*val adapter = SwipeControlsViewPagerAdapter(
                activity,
                item// Pass true or false
            )*/
            //newly added
            val adapter = object : FragmentStateAdapter(activity) {
                override fun getItemCount(): Int = 1
                override fun createFragment(index: Int): Fragment {
                    val bundle = Bundle().apply {
                        putString("stream_name", item.name)
                        putString("publisherName", item.postedBy)
                        putBoolean("host", false)
                        putString("type", item.streamType)
                        putString("title", item.title)
                        putString("invite_link", item.shareStreamLink)
                        putString("token", "")
                        putString("hostId", item.publisherId)
                        putString("hostName", item.postedBy)
                        putString("hostImage", item.publisherImage)
                        putString("liveID", item.name)
                        putString("follow_sts", item.follow)
                        putString("host_level", item.user_level)
                        putString("last_gifted_date", item.last_gifted_date)
                        putString("profile_verified", item.profile_verified)
                        putString("main_stream_name", item.mainStreamName)
                        putString("from", "viewer")
                        putInt("fragment_index", 1)
                    }
                    Log.e(TAG, item.name +"teststest")
                        val frag = PkLiveStreamingFragment.newInstance(bundle)
                    fragmentMap[position] = frag
                    return frag
                }
            }

            binding.controlsPager.adapter = adapter

            binding.controlsPager.orientation = ViewPager2.ORIENTATION_VERTICAL

            binding.controlsPager.setPageTransformer(VerticalSlidePageTransformer())
            binding.controlsPager.setCurrentItem(selectedControlScreenPosition, false)

          /*  binding.controlsPager.registerOnPageChangeCallback(object :
                ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    selectedControlScreenPosition = position
                }
            })*/
        }
    }

    inner class SwipeControlsViewPagerAdapter(
        fa: FragmentActivity,
        stream: StreamDetails
    ) : FragmentStateAdapter(fa) {
        private val bundle: Bundle = Bundle().apply {
            putString("stream_name", stream.name)
            putString("publisherName", stream.postedBy)
            putBoolean("host", false)
            putString("type", stream.streamType)
            putString("title", stream.title)
           /* putString("invite_link", stream.inviteLink)*/
            putString("invite_link", stream.shareStreamLink)
            putString("token", "")
            putString("hostId", stream.publisherId)
            putString("hostName", stream.postedBy)
            putString("hostImage", stream.publisherImage)
            putString("liveID", stream.name)
            putString("follow_sts", stream.follow)
            putString("host_level", stream.user_level)
            putString("last_gifted_date", stream.last_gifted_date)
            putString("profile_verified", stream.profile_verified)
            putString("main_stream_name", stream.mainStreamName)
            putString("from", "viewer")
            putInt("fragment_index", 1)
        }


        private val fragments = listOf(
            PkLiveStreamingFragment.newInstance(bundle)
        )

        override fun getItemCount(): Int = fragments.size

        override fun createFragment(position: Int): Fragment = fragments[position]
    }
}