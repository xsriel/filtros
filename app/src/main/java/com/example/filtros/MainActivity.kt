package com.example.filtros

import android.R.attr.bitmap
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.filtros.Adapter.ViewPagerAdapter
import com.example.filtros.Interface.EditImageFragmentListener
import com.example.filtros.Interface.FilterListFragmentListener
import com.example.filtros.Utils.BitmapUtils
import com.example.filtros.Utils.NonSwipeableViewPager
import com.google.android.material.snackbar.Snackbar
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.zomato.photofilters.imageprocessors.Filter
import com.zomato.photofilters.imageprocessors.subfilters.BrightnessSubFilter
import com.zomato.photofilters.imageprocessors.subfilters.ContrastSubFilter
import com.zomato.photofilters.imageprocessors.subfilters.SaturationSubfilter
import ja.burhanrashid52.photoeditor.OnSaveBitmap
import ja.burhanrashid52.photoeditor.PhotoEditor
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity(), FilterListFragmentListener, EditImageFragmentListener {

    internal var originalImage: Bitmap? = null
    internal lateinit var filteredImage: Bitmap
    internal lateinit var finalImage:Bitmap

    internal lateinit var filterListFragment: FilterListFragment
    internal lateinit var editImageFragment: EditImageFragment

    //Oliver Ordaz 09/12/2020 se agrega photo editor
    lateinit var photoEditor:PhotoEditor

    internal var brightnessFinal = 0
    internal var saturationFinal = 1.0f
    internal var contrastFinal = 1.0f

    internal var image_uri:Uri?=null
    internal val CAMARA_REQUEST:Int=9999

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
        photoEditor = PhotoEditor.Builder(this@MainActivity, image_preview).setPinchTextScalable(
            true
        ).build()

        loadImage()

         filterListFragment = FilterListFragment.getInstance(null)
         editImageFragment = EditImageFragment.getInstance()
        btn_filter.setOnClickListener{
            if (filterListFragment != null)
            {
                filterListFragment.show(supportFragmentManager, filterListFragment.tag)
            }else
            {
                filterListFragment.setListener(this@MainActivity)
                filterListFragment.show(supportFragmentManager, filterListFragment.tag)
            }
        }
        btn_edit.setOnClickListener{
            if (editImageFragment != null) {
                editImageFragment.setListener(this@MainActivity)
                editImageFragment.show(supportFragmentManager, editImageFragment.tag)
            }
        }
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
        image_preview.source.setImageBitmap(originalImage)
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
        else if (id == R.id.action_camara) {
            openCamara()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun openCamara() {
        Dexter.withActivity(this)
                .withPermissions(
                    android.Manifest.permission.CAMERA,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        if (report!!.areAllPermissionsGranted()) {
                            val values = ContentValues()
                            values.put(MediaStore.Images.Media.TITLE, "Nueva imagen")
                            values.put(MediaStore.Images.Media.DESCRIPTION, "desde la camara")
                            image_uri = contentResolver.insert(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                values
                            )
                            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri)
                            startActivityForResult(cameraIntent, CAMARA_REQUEST)

                        } else {
                            Toast.makeText(
                                applicationContext,
                                "Permission denied",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permissions: MutableList<PermissionRequest>?,
                        token: PermissionToken?
                    ) {
                        token!!.continuePermissionRequest()
                    }

                }).check()
    }

    private fun openImageFromGallery() {
        val withListener: Unit = Dexter.withActivity(this)
                .withPermissions(
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        if (report!!.areAllPermissionsGranted()) {
                            val intent = Intent(Intent.ACTION_PICK)
                            intent.type = "image/*"

                            startActivityForResult(intent, SELECT_GALLERY_PERMISSION)
                        } else {
                            Toast.makeText(
                                applicationContext,
                                "Permission denied",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permissions: MutableList<PermissionRequest>?,
                        token: PermissionToken?
                    ) {
                        token!!.continuePermissionRequest()
                    }
                }).check()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_GALLERY_PERMISSION) {
            val bitmap = BitmapUtils.getBitmapFromGallery(this, data?.data!!, 800, 800)

            originalImage!!.recycle()
            finalImage!!.recycle()
            filteredImage!!.recycle()

            originalImage = bitmap.copy(Bitmap.Config.ARGB_8888, true)
            filteredImage = originalImage!!.copy(Bitmap.Config.ARGB_8888, true)
            finalImage = originalImage!!.copy(Bitmap.Config.ARGB_8888, true)

            image_preview.source.setImageBitmap(originalImage)

            bitmap.recycle()

            filterListFragment = FilterListFragment.getInstance(originalImage!!)
            filterListFragment.setListener(this)
        }
            else if (requestCode == CAMARA_REQUEST) {
                val bitmap = BitmapUtils.getBitmapFromGallery(this, image_uri!!, 800, 800)

                originalImage!!.recycle()
                finalImage!!.recycle()
                filteredImage!!.recycle()

                originalImage = bitmap.copy(Bitmap.Config.ARGB_8888, true)
                filteredImage = originalImage!!.copy(Bitmap.Config.ARGB_8888, true)
                finalImage = originalImage!!.copy(Bitmap.Config.ARGB_8888, true)

                image_preview.source.setImageBitmap(originalImage)

                bitmap.recycle()

                filterListFragment = FilterListFragment.getInstance(originalImage!!)
                filterListFragment.setListener(this)
            }
    }
    }

    private fun saveImageToGallery() {
        Dexter.withActivity(this)
                .withPermissions(
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        if (report!!.areAllPermissionsGranted()) {
                            photoEditor.saveAsBitmap(object : OnSaveBitmap {
                                override fun onBitmapReady(saveBitmap: Bitmap?) {
                                    //val path = BitmapUtils.insertImage(contentResolver, saveBitmap, System.currentTimeMillis().toString() + "_profile.jpg", "")
                                    val filename = "firma-" + System.currentTimeMillis() + ".png"
                                    val url: String = MediaStore.Images.Media.insertImage(
                                        contentResolver,
                                        saveBitmap,
                                        filename,
                                        "Foto con filtro"
                                    )
                                    if (!TextUtils.isEmpty(url)) {
                                        val snackBar: Snackbar = Snackbar.make(
                                            coordinator,
                                            "Image saved to gallery",
                                            Snackbar.LENGTH_LONG
                                        )
                                            .setAction(
                                                "OPEN",
                                            ) {
                                                openImage(url)
                                            }
                                        snackBar.show()
                                    } else {
                                        val snackBar: Snackbar = Snackbar.make(
                                            coordinator,
                                            "unable to save image",
                                            Snackbar.LENGTH_LONG
                                        )
                                        snackBar.show()
                                    }

                                }

                                override fun onFailure(e: Exception?) {
                                    val snackBar: Snackbar = Snackbar.make(
                                        coordinator,
                                        e!!.message.toString(),
                                        Snackbar.LENGTH_LONG
                                    )
                                    snackBar.show()
                                }

                            })
                        } else {
                            Toast.makeText(
                                applicationContext,
                                "Permission denied",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permissions: MutableList<PermissionRequest>?,
                        token: PermissionToken?
                    ) {
                        token!!.continuePermissionRequest()
                    }

                }).check()
    }

    private fun openImage(path: String?) {
        val intent =  Intent()
        intent.action = Intent.ACTION_VIEW
        intent.setDataAndType(Uri.parse(path), "image/*")
        startActivity(intent)
    }

    override fun OnFilterSelected(filter: Filter) {
        //resetControls()
        filteredImage = originalImage!!.copy(Bitmap.Config.ARGB_8888, true)
        image_preview.source.setImageBitmap(filter.processFilter(filteredImage))
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
        image_preview.source.setImageBitmap(
            myFilter.processFilter(
                finalImage.copy(
                    Bitmap.Config.ARGB_8888,
                    true
                )
            )
        )
    }

    override fun onSaturationChanged(saturation: Float) {
        saturationFinal = saturation
        val myFilter = Filter()
        myFilter.addSubFilter(SaturationSubfilter(saturation))
        image_preview.source.setImageBitmap(
            myFilter.processFilter(
                finalImage.copy(
                    Bitmap.Config.ARGB_8888,
                    true
                )
            )
        )
    }

    override fun onConstrantChanged(constrant: Float) {
        contrastFinal = constrant
        val myFilter = Filter()
        myFilter.addSubFilter(ContrastSubFilter(constrant))
        image_preview.source.setImageBitmap(
            myFilter.processFilter(
                finalImage.copy(
                    Bitmap.Config.ARGB_8888,
                    true
                )
            )
        )
    }

    override fun onEditStarted() {
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