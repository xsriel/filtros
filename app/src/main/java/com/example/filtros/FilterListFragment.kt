package com.example.filtros

import android.graphics.Bitmap
import android.os.Bundle
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentPagerAdapter
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.filtros.Adapter.ThumbnailAdapter
import com.example.filtros.Interface.FilterListFragmentListener
import com.example.filtros.Utils.BitmapUtils
import com.example.filtros.Utils.SpaceItemDecoration
import com.zomato.photofilters.FilterPack
import com.zomato.photofilters.imageprocessors.Filter
import com.zomato.photofilters.utils.ThumbnailItem
import com.zomato.photofilters.utils.ThumbnailsManager
import kotlinx.android.synthetic.main.fragment_filter_list.*


class FilterListFragment : Fragment(), FilterListFragmentListener {

    internal lateinit var recycler_view:RecyclerView
    internal var listener: FilterListFragmentListener? = null
    internal lateinit var adapter: ThumbnailAdapter
    internal lateinit var thumbnailItemList: MutableList<ThumbnailItem>

    fun setListener(listFragmentListener: FilterListFragmentListener) {
        this.listener = listFragmentListener
    }

    override fun OnFilterSelected(filter: Filter) {
        if (listener != null) {
            listener!!.OnFilterSelected(filter)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val itemView: View = inflater.inflate(R.layout.fragment_filter_list, container, false)

        thumbnailItemList = ArrayList()
        adapter = ThumbnailAdapter(activity!!,thumbnailItemList, this)

        recycler_view = itemView.findViewById<RecyclerView>(R.id.recycler_view)

        recycler_view.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        recycler_view.itemAnimator = DefaultItemAnimator()
        val space: Int = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, resources.displayMetrics).toInt()
        recycler_view.addItemDecoration(SpaceItemDecoration(space))
        recycler_view.adapter = adapter

        displayImage(null)

        return  itemView
    }

    fun displayImage(bitmap: Bitmap?) {
        val r = Runnable {
            val thumbImage : Bitmap?
            if (bitmap == null) {
                thumbImage = BitmapUtils.getBitmapFromAssets(activity!!, MainActivity.Main.IMAGE_NAME,100, 100)
            }
            else {
                thumbImage = Bitmap.createScaledBitmap(bitmap, 100, 100, false)
            }
            if (thumbImage == null) {
                return@Runnable
            }
            ThumbnailsManager.clearThumbs()
            thumbnailItemList.clear()

            val thumbnailItem = ThumbnailItem()
            thumbnailItem.image = thumbImage
            thumbnailItem.filterName = "Normal"
            ThumbnailsManager.addThumb(thumbnailItem)

            //aqui se agregan los filtros
            val filters = FilterPack.getFilterPack(activity!!)

            for (filter: Filter in filters) {
                val item = ThumbnailItem()
                item.image = thumbImage
                item.filter = filter
                item.filterName = filter.name
                ThumbnailsManager.addThumb(item)
            }

            thumbnailItemList.addAll(ThumbnailsManager.processThumbs(activity))
            activity!!.runOnUiThread{
                adapter.notifyDataSetChanged()
            }
        }
        Thread(r).start()
    }


}