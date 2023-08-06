package com.example.drawingapp

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.MediaScannerConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    // Defining variable to access our design drawing view
    private var drawingView : DrawingView ?= null
    // A variable for current color is picked from color pallet.
    private var mImageButtonCurrentPaint : ImageButton ?= null
    private var customProgressDialog : Dialog? = null

    /** Function to access gallery and pick the image from that and set it as background image */
    private val openGalleryLauncher : ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result ->
            if(result.resultCode == RESULT_OK && result.data!=null){
                val imageBackground : ImageView = findViewById(R.id.iv_background)
                imageBackground.setImageURI(result.data?.data)
            }
        }

    /** Create an ActivityResultLauncher with MultiplePermissions
     * since we are requesting both read and write
     * Function to requests for permission */
    private val requestPermission : ActivityResultLauncher<Array<String>> =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
                permissions ->
                permissions.entries.forEach{
                    val permissionName = it.key
                    val isGranted = it.value
                    if(permissionName==Manifest.permission.READ_EXTERNAL_STORAGE){
                        // if permission is granted show a toast and perform operation
                        if(isGranted){
                            Toast.makeText(this,
                                "Permission Granted now ypu can read Storage",
                                    Toast.LENGTH_SHORT).show()

                            val pickIntent = Intent(Intent.ACTION_PICK,
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                            openGalleryLauncher.launch(pickIntent)
                        }
                        /** Displaying another toast if permission is not granted and
                        * this time focus on Read external storage */
                        else{
                            Toast.makeText(this,
                                "Oops just Denied the Permission", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        drawingView = findViewById(R.id.drawing_view)

        /** Linear layout ek array ke trh behave krta h
         * to hm isme element ko access uske index se kr skte h*/
        val linearLayoutPaintColor  : LinearLayout = findViewById(R.id.ll_paint_colors)
        mImageButtonCurrentPaint = linearLayoutPaintColor[1] as ImageButton
        mImageButtonCurrentPaint!!.setImageDrawable(
            ContextCompat.getDrawable(this,R.drawable.pallet_select)
        )

        val ibBrush : ImageButton = findViewById(R.id.ib_brush)
        ibBrush.setOnClickListener{
            showBrushSizeChooserDialog()
        }

        val ibGallery : ImageButton =findViewById(R.id.ib_gallery)
        ibGallery.setOnClickListener{
            requestStoragePermission()
        }

        // This is for undo recent stroke.
        val ibUndo : ImageButton =findViewById(R.id.ib_undo)
        ibUndo.setOnClickListener{
            drawingView?.onClickUndo()
        }

        val ibRedo : ImageButton =findViewById(R.id.ib_redo)
        ibRedo.setOnClickListener{
            drawingView?.onClickRedo()
        }

        // Reference the save button from the layout
        val ibSave : ImageButton =findViewById(R.id.ib_save)
        ibSave.setOnClickListener{
            //check if permission is allowed
            if(isReadStorageAllowed()){
                showProgressDialog()
                //launch a coroutine block
                lifecycleScope.launch {
                    //reference the frame layout
                    val flDrawingView : FrameLayout = findViewById(R.id.fl_drawing_view_container)
                    //Save the image to the device
                    saveBitmapFile(getBitFromView(flDrawingView))
                }
            }
        }
    }

    /** Method is used to launch the dialog to select different brush sizes.
     * hm brush select krne ke lie bss ek dialog popup kra rhe h pura ka pura
     * nya screen nhi display kra rhe
     * jo Hmara bursh select krne ka dialog box h ya option h open hoga isse
     * aur usme jo option hm select krege uske according brush ki size set ho jaegi */
    private fun showBrushSizeChooserDialog(){
        val brushDialog = Dialog(this)
        /** Jo hmara dialog box hoga uska layout given layout(.xml) ke according ho jaega*/
        brushDialog.setContentView(R.layout.dialog_brush_size)
        brushDialog.setTitle("Brush Size: ")

        /** Thino jo brush size select krne ke icons h uhne functioning bna rhe
         * aur define kr rhe ki click hone pe kya hona chahie */
        val smallBtn : ImageButton = brushDialog.findViewById(R.id.ib_small_brush)
        smallBtn.setOnClickListener{
            drawingView?.setSizeForBrush(3.toFloat())
            brushDialog.dismiss()
        }

        val mediumBtn : ImageButton = brushDialog.findViewById(R.id.ib_medium_brush)
        mediumBtn.setOnClickListener {
            drawingView?.setSizeForBrush(5.toFloat())
            brushDialog.dismiss()
        }

        val largeBtn : ImageButton = brushDialog.findViewById(R.id.ib_large_brush)
        largeBtn.setOnClickListener {
            drawingView?.setSizeForBrush(7.toFloat())
            brushDialog.dismiss()
        }
            brushDialog.show()
    }

    /** Method is called when color is clicked from pallet_normal. */
    fun paintClicked(view : View){
        /** Hm check kr rhe ki jo hmara selected color h aur jo current color h
         * kya wo dono same.. Agr to wo same h to hme kch krna hi nhi h
         * age hm ye check nhi lagate to same color hone pr bi wo phr se
         * sara process run krta*/
        if(view !== mImageButtonCurrentPaint){

            /** Update the color
             * Jo hmra view h use hm ImageButtton type ka bna ke ek variable me store kr rhe*/
            val imageButton = view as ImageButton

            /** Here the tag is used for swaping the current color with previous color.
             * The tag stores the selected view
             * imageButton se hm tag extract kr rhe h
             * tag use krna ka advantage ye rha ki
             * agr hm id lete to wo ek strign pass krta
             * pr tag me string form me original location hi store h color ki*/
            val colorTag = imageButton.tag.toString()

            /** Setting the color of our draw with user selected color*/
            drawingView?.setColor(colorTag)

            /** Swap the backgrounds for last active and currently active image button.
             * Changing the layout of selected color pallet */
            imageButton.setImageDrawable(
                ContextCompat.getDrawable(this,R.drawable.pallet_select)
            )
            /** Changing the layout of previously color pallet with normal color pallet*/
            mImageButtonCurrentPaint?.setImageDrawable(
                ContextCompat.getDrawable(this,R.drawable.pallet_normal)
            )

            /** Current view is updated with selected view in the form of ImageButton.
             * updating the current paint with selected color
             * So that it will work next thing again
             * agr hm isse update nhi krte to */
            mImageButtonCurrentPaint = view
        }
    }

    /** We are calling this method to check the permission status */
    private fun isReadStorageAllowed():Boolean{

        /** Getting the permission status
         * Here the checkSelfPermission is
         * Determine whether <em>you</em> have been granted a particular permission.
         * @param permission The name of the permission being checked. */
        val result = ContextCompat.checkSelfPermission(this,
            Manifest.permission.READ_EXTERNAL_STORAGE)

        /** @return {@link android.content.pm.PackageManager#PERMISSION_GRANTED} if you have the
         * permission, or {@link android.content.pm.PackageManager#PERMISSION_DENIED} if not. */
        // If permission is granted returning true and If permission is not granted returning false
        return result  == PackageManager.PERMISSION_GRANTED
    }

    /** create a method to requestStorage permission
     * For Checking whether we have request for that permission earlier or not.
     * if yes then show why we needed that and you have denied
     * if no then ask for permission */
     private fun requestStoragePermission(){

        // Check if the permission was denied and show rationale
         if(ActivityCompat.shouldShowRequestPermissionRationale(this
                 ,Manifest.permission.READ_EXTERNAL_STORAGE)){
             //call the rationale dialog to tell the user why they need to allow permission request
             showRationaleDialog()
         }else{
             /** You can directly ask for the permission.
              * if it has not been denied then request for permission
              * The registered ActivityResultCallback gets the result of this request.
              * Passing array of permission for requesting multiple permissions */
             requestPermission.launch(arrayOf(
                 Manifest.permission.READ_EXTERNAL_STORAGE,
                 Manifest.permission.WRITE_EXTERNAL_STORAGE)
             )
         }
     }

    /** Create rationale dialog
     * Shows rationale dialog for displaying why the app needs permission
     * Only shown if the user has denied the permission request previously
     * Dialog Box to show our message to user */
    private fun showRationaleDialog(){
        val builder:AlertDialog.Builder=AlertDialog.Builder(this)
        builder.setTitle("Kids Drawing App").setMessage("Kids Drawing App needs to"
                +"Access your External Storage")
            .setPositiveButton("Cancel"){dialog,_ -> dialog.dismiss() }
            builder.create().show()
    }

    /** Create bitmap from view and returns it */
    private fun getBitFromView(view: View):Bitmap{

        /** Define a bitmap with the same size as the view.
        * CreateBitmap : Returns a mutable bitmap with the specified width and height */
        val returnedBitmap=Bitmap.createBitmap(view.width,view.height
            ,Bitmap.Config.ARGB_8888)

        // Bind a canvas to it
        val canvas = Canvas(returnedBitmap)

        // Get the view's background
        val bgDrawable = view.background

        if(bgDrawable!=null){
            // has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas)
        }
        else{
            // does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE)
        }

        // draw the view on the canvas
        view.draw(canvas)
        // return bitmap
        return returnedBitmap
    }

    private suspend fun saveBitmapFile(mBitmap: Bitmap?):String{
        var result=""
        withContext(Dispatchers.IO){
            if(mBitmap!=null){
                try {
                    /** Creates a new byte array output stream.
                     * The buffer capacity is initially 32 bytes,
                     * though its size increases if necessary. */
                    val bytes = ByteArrayOutputStream()
                    mBitmap.compress(Bitmap.CompressFormat.PNG,90,bytes)

                    /** Write a compressed version of the bitmap to the specified outputstream.
                     * If this returns true, the bitmap can be reconstructed by passing a
                     * corresponding inputstream to BitmapFactory.decodeStream(). Note: not
                     * all Formats support all bitmap configs directly, so it is possible that
                     * the returned bitmap from BitmapFactory could be in a different bitdepth,
                     * and/or may have lost per-pixel alpha (e.g. JPEG only supports opaque
                     * pixels).
                     * @param format   The format of the compressed image
                     * @param quality  Hint to the compressor, 0-100. 0 meaning compress for
                     *                 small size, 100 meaning compress for max quality. Some
                     *                 formats, like PNG which is lossless, will ignore the
                     *                 quality setting
                     * @param stream   The outputstream to write the compressed data.
                     * @return true if successfully compressed to the specified stream. */
                    val f =File(externalCacheDir?.absoluteFile.toString() + File.separator
                            + "KidsDrawingApp_" + System.currentTimeMillis()/1000 + ".png")

                    /** Here the Environment : Provides access to environment variables.
                     * getExternalStorageDirectory : returns the primary shared/external storage directory.
                     * absoluteFile : Returns the absolute form of this abstract pathname.
                     * File.separator : The system-dependent default name-separator character.
                     * This string contains a single character. */

                    // Creates a file output stream to write to the file represented by the specified object.
                    val fo = FileOutputStream(f)
                    // Writes bytes from the specified byte array to this file output stream.
                    fo.write(bytes.toByteArray())
                    // Closes this file output stream and releases any system resources associated with this stream.
                    // This file output stream may no longer be used for writing bytes.
                    fo.close()

                    // The file absolute path is return as a result.
                    result=f.absolutePath

                    //We switch from io to ui thread to show a toast
                    runOnUiThread{
                        cancelProgressDialog()
                        if(result.isNotEmpty()){
                            shareImage(result)
                            Toast.makeText(this@MainActivity,
                                "File saved Successfully : $result",
                                Toast.LENGTH_SHORT).show()
                        }
                        else{
                            Toast.makeText(this@MainActivity,
                                "Something went wrong while saving the file",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                catch (e:Exception){
                    result=""
                    e.printStackTrace()
                }
            }
        }
        return result
    }

    /** Method is used to show the Custom Progress Dialog. */
    private fun showProgressDialog() {
        customProgressDialog = Dialog(this)

        /** Set the screen content from a layout resource.
         * The resource will be inflated, adding all top-level views to the screen.*/
        customProgressDialog?.setContentView(R.layout.dialog_custom_layout)

        // Start the dialog and display it on screen.
        customProgressDialog?.show()
    }

    /** This function is used to dismiss the progress dialog if it is visible to user. */
    private fun cancelProgressDialog(){
        if(customProgressDialog!=null){
            customProgressDialog?.dismiss()
            customProgressDialog=null
        }
    }

    private fun shareImage(result: String){
        /**MediaScannerConnection provides a way for applications to pass a
         * newly created or downloaded media file to the media scanner service.
         * The media scanner service will read metadata from the file and add
         * the file to the media content provider.
         * The MediaScannerConnectionClient provides an interface for the
         * media scanner service to return the Uri for a newly scanned file
         * to the client of the MediaScannerConnection class.*/

        // scanFile is used to scan the file when the connection is established with MediaScanner
        MediaScannerConnection.scanFile(this, arrayOf(result),null){
            path,uri ->

            // This is used for sharing the image after it has being stored in the storage.
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND

            /** A content: URI holding a stream of data associated with the Intent,
             * used to supply the data being sent. */
            shareIntent.putExtra(Intent.EXTRA_STREAM,uri)

            shareIntent.type="image/png"  // The MIME type of the data being handled by this intent.

            startActivity(Intent.createChooser(shareIntent,"Share"))
            /** Activity Action: Display an activity chooser,
             * allowing the user to pick what they want to before proceeding.
             * This can be used as an alternative to the standard activity picker
             * that is displayed by the system when you try to start an activity with
             * multiple possible matches, with these differences in behavior: */
        }  // End
    }


}