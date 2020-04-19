/**
 * TODO:
 * Use Architecture components
 * */

package com.calleb.jcoil

import android.app.AlertDialog
import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.ConnectivityManager
import android.net.NetworkInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.URLUtil
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import coil.Coil
import coil.ImageLoader
import coil.api.load
import coil.util.CoilUtils
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.OkHttpClient

class MainActivity : AppCompatActivity() {

    private val urls : Array<String> = arrayOf("https://raw.githubusercontent.com/callebdev/Files-Links/master/JCoil%20-%20JCole%20App/jcole.jpg",
        "https://raw.githubusercontent.com/callebdev/Files-Links/master/JCoil%20-%20JCole%20App/jcole1.png",
        "https://raw.githubusercontent.com/callebdev/Files-Links/master/JCoil%20-%20JCole%20App/jcole2.jpg",
        "https://raw.githubusercontent.com/callebdev/Files-Links/master/JCoil%20-%20JCole%20App/jcole3.jpg",
        "https://raw.githubusercontent.com/callebdev/Files-Links/master/JCoil%20-%20JCole%20App/jcole4.jpg",
        "https://raw.githubusercontent.com/callebdev/Files-Links/master/JCoil%20-%20JCole%20App/jcole5.jpg",
        "https://raw.githubusercontent.com/callebdev/Files-Links/master/JCoil%20-%20JCole%20App/jcole6.jpg")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Coil.setDefaultImageLoader {
            ImageLoader(this) {
                crossfade(true)
                okHttpClient {
                    OkHttpClient.Builder()
                        .cache(CoilUtils.createDefaultCache(applicationContext))
                        .build()
                }
            }
        }

        imgDownloaded.setImageDrawable(getDrawable(R.drawable.jcole))
        progressBar.visibility = View.GONE

        val randomUrl = (urls.indices).random()
        val selectedUrl = urls[randomUrl]

        edtLink.setText(selectedUrl)
    }

    /**
     * Menu Stuffs
     **/

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_options, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.actionWallpaper -> setWallpaper()
            R.id.actionAnalyze -> analyze()
            R.id.actionShare -> shareLink()
            R.id.actionAbout -> {
                val alertDialog = AlertDialog.Builder(this)
                alertDialog.setTitle("About the app")
                alertDialog.setMessage("JCoil is an Android application that uses the library Coil to download images from URLs, and Machine Learning (ML Kit) for image labeling.\n\n" +
                        "App Functions:\n" +
                        "   Download an image from an url (Requires internet connection);\n" +
                        "   Image labeling (Doesn't require internet connection);\n" +
                        "   Set the downloaded image as wallpaper;\n")
                alertDialog.setPositiveButton("Got it", null)
                alertDialog.create()
                alertDialog.show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Download Stuffs
     ** TODO:
     ** Verify the download state (onSuccess/onFailed)
     ** Show a progress bar when the image is being downloaded
     */

    // Downloads image from an url if data is on
    fun downloadImage(view: View) {
        if (isConnected()){
            val url = edtLink.text.toString()
            if (urlIsValid(url)){
                Toast.makeText(this, "Downloading...", Toast.LENGTH_LONG).show()

                imgDownloaded.load(url){
                    placeholder(getDrawable(R.drawable.undraw_download))
                    crossfade(true)
                }
                edtLink.setText("")
            } else{
                urlIsValid(url)
            }

        }else{
            imgDownloaded.setImageDrawable(getDrawable(R.drawable.undraw_page_not_found_su7k))
            val alertDialogBuilder = AlertDialog.Builder(this@MainActivity)
            alertDialogBuilder.setTitle("Check your connection!")
            alertDialogBuilder.setIcon(R.drawable.ic_signal_wifi_off)
            alertDialogBuilder.setMessage("Hmmm, seems that you are not connected to any network..." +
                    "\nVerify your connection and try again.")
            alertDialogBuilder.setPositiveButton("Retry"){_,_ -> downloadImage()}
            alertDialogBuilder.create()
            alertDialogBuilder.show()
        }
    }

    // Downloads image if data was previously off
    private fun downloadImage() {
        if (isConnected()){
            val url = edtLink.text.toString()
            Toast.makeText(this, "Downloading...", Toast.LENGTH_LONG).show()
            imgDownloaded.load(url){
                placeholder(getDrawable(R.drawable.undraw_download))
                crossfade(true)
            }
            edtLink.setText("")
        }else{
            imgDownloaded.setImageDrawable(getDrawable(R.drawable.undraw_page_not_found_su7k))
            val alertDialogBuilder = AlertDialog.Builder(this@MainActivity)
            alertDialogBuilder.setTitle("You are not connected!")
            alertDialogBuilder.setIcon(R.drawable.ic_signal_wifi_off)
            alertDialogBuilder.setMessage("Please, turn on you Wi-fi or mobile data to and try again!")
            alertDialogBuilder.setPositiveButton("Try Again"){_,_ -> downloadImage()}
            alertDialogBuilder.create()
            alertDialogBuilder.show()
        }
    }

    /**
     * Wallpaper Stuffs
     * */

    //Sets the downloaded image as phone wallpaper
    private fun setWallpaper() {
        /**
         * TODO:
         * Restrict this function for downloaded images
         * Resize/Crop image
        **/
        val myWallpaperManager = WallpaperManager.getInstance(applicationContext)
        try {
            myWallpaperManager.setBitmap(getBitmap2())
            myWallpaperManager.suggestDesiredDimensions(getScreenWidth(), getScreenHeight())
            Toast.makeText(this, "Wallpaper set successfully!", Toast.LENGTH_LONG).show()
        }catch (e : Exception){
            if (imgDownloaded == null) {
                Toast.makeText(this, "Download an image first!", Toast.LENGTH_LONG).show()
            }
            Toast.makeText(this, "No Image!", Toast.LENGTH_LONG).show()
        }

    }

    private fun getBitmapDrawable(): BitmapDrawable {
        return imgDownloaded.drawable as BitmapDrawable
    }

    private fun getBitmap1(): Bitmap {
        return getBitmapDrawable().bitmap
    }

    private fun getBitmap2(): Bitmap {
        return setBitmapSize()
    }

 private fun getScreenWidth(): Int {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.widthPixels
    }

    private fun getScreenHeight(): Int {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.heightPixels
    }

    private fun setBitmapSize(): Bitmap {
        return Bitmap.createScaledBitmap(getBitmap1(), getScreenWidth(), getScreenHeight(), false)
    }


    /**
    * Link Stuffs
    * */

    // Clears the url field
    fun clearLink(view: View) {
        edtLink.setText("")
    }

    // Chooses a random link (from randomUrl[])
    fun randomLink(view: View) {
        val randomUrl = (urls.indices).random()
        val selectedUrl = urls[randomUrl]
        edtLink.setText(selectedUrl)
    }

    // Shares link via WhatsApp
    private fun shareLink() {
        /**
         * TODO:
         * Share link via Message, Twitter (Direct Message, Tweet)
         * Share downloaded image
        **/
        val link = edtLink.text.toString()

        try{
            if (URLUtil.isValidUrl(link)){
                val message = "Hey! Checkout this link: $link \nDon't forget to follow callebdev on Github! [https://github.com/callebdev]"
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "text/plain"
                intent.putExtra(Intent.EXTRA_TEXT, message)
                intent.`package` = "com.whatsapp"
                startActivity(intent)
            }else{
                Toast.makeText(applicationContext, "The link cannot be shared!", Toast.LENGTH_LONG).show()
                edtLink.setText("")
            }
        }catch (ex: Exception){
            Toast.makeText(applicationContext, "WhatsApp is not installed...", Toast.LENGTH_LONG).show()
        }
    }

    // Returns true if Url is valid
    private fun urlIsValid(url : String?): Boolean {
        return when (!TextUtils.isEmpty(url) && URLUtil.isValidUrl(url)) {

            true -> true

            false ->{
                edtLink.error = "Enter a valid URL"
                edtLink.setText("")
                false
            }
        }
    }

    /**
    * Internet Stuffs
    * */

    // Returns true if mobile data is on
    private fun isConnected(): Boolean {
        // TODO: Remove depreciated usages and verify mobile data
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).state == NetworkInfo.State.CONNECTED || connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).state == NetworkInfo.State.CONNECTED
    }

    /**
     * Machine Learning Stuffs
     **/

    // Turns the downloaded image into a Bitmap
    private fun toBitmap(): Bitmap {
        return imgDownloaded.drawable.toBitmap()
    }

    // Shows image labels in Toasts
    private fun analyze() {
        // TODO: Restrict this function only for downloaded images.
        val image = FirebaseVisionImage.fromBitmap(toBitmap())
        val labeler = FirebaseVision.getInstance().onDeviceImageLabeler
        Toast.makeText(this, "Analyzing...", Toast.LENGTH_LONG).show()
        labeler.processImage(image)
            .addOnSuccessListener { labels ->
                        for (label in labels) {
                            Toast.makeText(this, "Detected: ${label.text}\nConfidence: ${label.confidence}", Toast.LENGTH_LONG).show()
                        }
            }
            .addOnFailureListener { e ->
                val alertDialog = AlertDialog.Builder(this)
                alertDialog.setTitle("Nothing Found")
                alertDialog.setMessage("Sorry, we couldn't find any recognizable element in you photo!\n${e.stackTrace}")
                alertDialog.setPositiveButton("Ok", null)
                alertDialog.create()
                alertDialog.show()
            }
    }
}