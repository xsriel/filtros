package com.example.filtros

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.DragAndDropPermissions
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.example.filtros.Adapter.ViewPagerAdapter
import com.example.filtros.Interface.EditImageFragmentListener
import com.example.filtros.Interface.FilterListFragmentListener
import com.example.filtros.Utils.BitmapUtils
import com.example.filtros.Utils.NonSwipeableViewPager
import com.google.android.material.snackbar.Snackbar
import com.karumi.dexter.Dexter
import com.karumi.dexter.DexterBuilder
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.zomato.photofilters.imageprocessors.Filter
import com.zomato.photofilters.imageprocessors.subfilters.BrightnessSubFilter
import com.zomato.photofilters.imageprocessors.subfilters.ContrastSubFilter
import com.zomato.photofilters.imageprocessors.subfilters.SaturationSubfilter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import com.karumi.dexter.MultiplePermissionsReport as MultiplePermissionsReport

class MainActivity : AppCompatActivity(), FilterListFragmentListener, EditImageFragmentListener {

    internal var originalImage: Bitmap? = null
    internal lateinit var filteredImage: Bitmap
    internal lateinit var finalImage:Bitmap

    internal lateinit var filterListFragment: FilterListFragment
    internal lateinit var editImageFragment: EditImageFragment

    internal var brightnessFinal = 0
    internal var saturationFinal = 1.0f
    internal var contrastFinal = 1.0f

    val SELECT_GALLERY_PERMISSION = 1000

    init {
        System.loadLibrary("NativeImageProcessor")
    }


    object Main {
        val IMAGE_NAME = "tona.jpg"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //toolbar :u
        setSupportActionBar(toolBar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.title="Filtros"

        loadImage()
        setupViewPager(viewPager)
        tabs.setupWithViewPager(viewPager)
    }

    private fun setupViewPager(viewPager: NonSwipeableViewPager?) {
        val adapter = ViewPagerAdapter(supportFragmentManager)

        //lista de filtros
        filterListFragment = FilterListFragment()
        filterListFragment.setListener(this)

        //fragment editarimagen
        editImageFragment = EditImageFragment()
        editImageFragment.setListener(this)

        adapter.addFragment(filterListFragment, "FILTROS")
        adapter.addFragment(editImageFragment, "EDIT")

        viewPager!!.adapter = adapter
    }

    private fun loadImage() {
        originalImage = BitmapUtils.getBitmapFromAssets(this, Main.IMAGE_NAME, 300, 300)
        filteredImage = originalImage!!.copy(Bitmap.Config.ARGB_8888, true)
        finalImage = originalImage!!.copy(Bitmap.Config.ARGB_8888, true)
        image_preview.setImageBitmap(originalImage)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_open) {
            openImageFromGallery()
            return  true
        }
        else if (id == R.id.action_save) {
            saveImageToGallery()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun openImageFromGallery() {
        val withListener: Unit = Dexter.withActivity(this)
                .withPermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        if (report!!.areAllPermissionsGranted()) {
                            val intent = Intent(Intent.ACTION_PICK)
                            intent.type = "image/*"

                            startActivityForResult(intent, SELECT_GALLERY_PERMISSION)
                        } else {
                            Toast.makeText(applicationContext, "Permission denied", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?, token: PermissionToken?) {
                        token!!.continuePermissionRequest()
                    }
                }).check()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == SELECT_GALLERY_PERMISSION) {
            val bitmap = BitmapUtils.getBitmapFromGallery(this, data?.data!!, 800, 800)

            originalImage!!.recycle()
            finalImage!!.recycle()
            filteredImage!!.recycle()

            originalImage = bitmap.copy(Bitmap.Config.ARGB_8888,true)
            filteredImage = originalImage!!.copy(Bitmap.Config.ARGB_8888,true)
            finalImage = originalImage!!.copy(Bitmap.Config.ARGB_8888,true)

            bitmap.recycle()

            filterListFragment.displayImage(bitmap)

        }
    }

    private fun saveImageToGallery() = Dexter.withActivity(this)
            .withPermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .withListener(object: MultiplePermissionsListener{
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report!!.areAllPermissionsGranted()) {
                        val path = BitmapUtils.insertImage(contentResolver,
                        finalImage,
                        System.currentTimeMillis().toString() + "_profile.jpg",
                        "")
                        if (!TextUtils.isEmpty(path)) {
                            val snackBar: Snackbar = Snackbar.make(coordinator, "Image saved to gallery", Snackbar.LENGTH_LONG)
                                    .setAction("OPEN", {
                                        openImage(path)
                            })
                            snackBar.show()
                        }
                        else {
                            val snackBar: Snackbar = Snackbar.make(coordinator, "Unable to save image", Snackbar.LENGTH_LONG)
                            snackBar.show()
                        }
                    }
                    else {
                        Toast.makeText(applicationContext, "Permission denied", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?, token: PermissionToken?) {
                    token!!.continuePermissionRequest()
                }

            }).check()

    private fun openImage(path: String?) {
        val intent =  Intent()
        intent.action = Intent.ACTION_VIEW
        intent.setDataAndType(Uri.parse(path), "image/*")
        startActivity(intent)
    }

    override fun OnFilterSelected(filter: Filter) {
        resetControls()
        filteredImage = originalImage!!.copy(Bitmap.Config.ARGB_8888, true)
        image_preview.setImageBitmap(filter.processFilter(filteredImage))
        finalImage = filteredImage.copy(Bitmap.Config.ARGB_8888, true)

    }

    private fun resetControls() {
        if (editImageFragment  != null) {
            editImageFragment.resetControls()
        }
        brightnessFinal = 0
        saturationFinal = 1.0f
        contrastFinal = 1.0f
    }

    override fun onBrightnessChanged(brightness: Int) {
        brightnessFinal = brightness
        val myFilter = Filter()
        myFilter.addSubFilter(BrightnessSubFilter(brightness))
        image_preview.setImageBitmap(myFilter.processFilter(finalImage.copy(Bitmap.Config.ARGB_8888, true)))
    }

    override fun onSaturationChanged(saturation: Float) {
        saturationFinal = saturation
        val myFilter = Filter()
        myFilter.addSubFilter(SaturationSubfilter(saturation))
        image_preview.setImageBitmap(myFilter.processFilter(finalImage.copy(Bitmap.Config.ARGB_8888, true)))
    }

    override fun onConstrantChanged(constrant: Float) {
        contrastFinal = constrant
        val myFilter = Filter()
        myFilter.addSubFilter(ContrastSubFilter(constrant))
        image_preview.setImageBitmap(myFilter.processFilter(finalImage.copy(Bitmap.Config.ARGB_8888, true)))
    }

    override fun onEditStarted() {
        TODO("Not yet implemented")
    }

    override fun onEditCompleted() {
        val bitmap = filteredImage.copy(Bitmap.Config.ARGB_8888, true)
        val myFilter = Filter()
        myFilter.addSubFilter(BrightnessSubFilter(brightnessFinal))
        myFilter.addSubFilter(SaturationSubfilter(saturationFinal))
        myFilter.addSubFilter(ContrastSubFilter(contrastFinal))
        finalImage = myFilter.processFilter(bitmap)
    }
}