package com.example.drawingapp

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton

class MainActivity : AppCompatActivity() {

    private var drawingView : DrawingView ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        drawingView = findViewById(R.id.drawing_view)
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
}