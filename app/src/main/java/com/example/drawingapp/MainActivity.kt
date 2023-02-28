package com.example.drawingapp

import android.app.Dialog
import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.get

class MainActivity : AppCompatActivity() {

    private var drawingView : DrawingView ?= null
    private var mImageButtonCurrentPaint : ImageButton ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        drawingView = findViewById(R.id.drawing_view)

        /** Linear layout ek array ke trh behave krta h
         * to hm isme element ko accese uske index se kr skte h*/
        val linearLayoutPaintColor  : LinearLayout = findViewById(R.id.ll_paint_colors)
        mImageButtonCurrentPaint = linearLayoutPaintColor[1] as ImageButton
        mImageButtonCurrentPaint!!.setImageDrawable(
            ContextCompat.getDrawable(this,R.drawable.pallet_select)
        )

        val ib_brush : ImageButton = findViewById(R.id.ib_brush)
        ib_brush.setOnClickListener{
            showBrushSizeChooserDialog()
        }
    }
    /** hm brush select krne ke lie bss ek dialog popup kra rhe h pura ka pura
     * nya screen nhi display kra rhe
     * jo Hmara bursh select krne ka dialog box h ya option h open hoga isse
     * aur usme jo option hm select krege uske according brush ki size set ho jaegi */
    private fun showBrushSizeChooserDialog(){
        val brushDialog = Dialog(this)

        /** JO hmara dialog box hoga uska layout given layout(.xml) ke according ho jaega*/
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

    fun paintClicked(view : View){
        /** Hm check kr rhe ki jo hmara selected color h aur jo current color h
         * kya wo dono same.. Agr to wo same h to hme kch krna hi nhi h
         * age hm ye check nhi lagate to same color hone pr bi wo phr se
         * sara process run krta*/
        if(view !== mImageButtonCurrentPaint){

            /** Jo hmra view h use hm ImageButtton type ka bna ke ek variable me store kr rhe*/
            val imageButton = view as ImageButton

            /** imageButton se hm tag extract kr rhe h
             * tag use krna ka advantage ye rha ki
             * agr hm id lete to wo ek strign pass krta
             * pr tag me string form me original location hi store h color ki*/
            val colorTag = imageButton.tag.toString()

            /** Setting the color of our draw with user selected color*/
            drawingView?.setColor(colorTag)

            /** Changing the layout of selected color pallet*/
            imageButton.setImageDrawable(
                ContextCompat.getDrawable(this,R.drawable.pallet_select)
            )

            /** Changing the layout of previously color pallet with normal color pallet*/
            mImageButtonCurrentPaint?.setImageDrawable(
                ContextCompat.getDrawable(this,R.drawable.pallet_normal)
            )

            /** updating the current paint with selected color
             * So that it will work next thing again
             * agr hm isse update nhi krte to */
            mImageButtonCurrentPaint = view
        }
    }
}