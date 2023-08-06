package com.example.drawingapp

/** import android.graphics.*
 * import all files present in graphic
 * we don,t need to separately import things under it */
import android.annotation.SuppressLint
import android.graphics.*
import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View

class DrawingView(context: Context, attrs:AttributeSet) : View(context,attrs) {

    // An variable of CustomPath inner class to use it further.
    private var mDrawPath : CustomPath ?= null

    // An instance of the Bitmap.
    private var mCanvasBitmap : Bitmap ?= null

    /** The Paint class holds the style and color information
    * about how to draw geometries, text and bitmaps.*/
    private var mDrawPaint : Paint ?= null

    // Instance of canvas paint view.
    private var mCanvasPaint : Paint ?= null

    // A variable for stroke/brush size to draw on the canvas.
    private var mBrushSize : Float = 3.toFloat()

    // A variable to hold a color of the stroke.
    private var color : Int = Color.BLACK

    /** A variable for canvas which will be initialized later and used.
     * The Canvas class holds the "draw" calls. To draw something, you need 4 basic components:
     * A Bitmap to hold the pixels, a Canvas to host the draw calls (writing into the bitmap),
     * a drawing primitive (e.g. Rect, Path, text, Bitmap), and
     * a paint (to describe the colors and styles for the drawing) */
    private var canvas : Canvas ?= null

    /** ArrayList for Paths
     * Ye hmara ek trike se jo jo hm path track krege hm uska list bna ke
     * rkhe ga taki phr se us path ko bna ke use persist kr skte */
    private val mPath = ArrayList<CustomPath>()

    /** We are storing the paths that we are removing as we can similarly implement Redo button
     * with path that have been removed */
    private val mUndoPath = ArrayList<CustomPath>()

    init{
        setUpDrawing()
    }

    /** This method initializes the attributes of the ViewForDrawing class.
     * Initialising the variables we have created earlier */
    private fun setUpDrawing(){

        mDrawPaint = Paint()
        mDrawPath = CustomPath(color,mBrushSize)
        mDrawPaint!!.color = color
        mDrawPaint!!.style = Paint.Style.STROKE // This is to draw a STROKE style
        mDrawPaint!!.strokeJoin = Paint.Join.ROUND // This is for store join
        mDrawPaint!!.strokeCap = Paint.Cap.ROUND // This is for stroke Cap
        mCanvasPaint = Paint(Paint.DITHER_FLAG) // Paint flag that enables dithering when blitting.
        // Kyuki hm mainActivity me hi brushSize ko define kr de rhe to
        // is lie hm ise hta skte h
        // mBrushSize = 10.toFloat()
    }

    /** Jb bi Drawing view ki size change hogi to ye function call hoga
     * Aur to jitne bi variables iske andar h wo phr se assigned ho jaege
     * Ya phr ye bi kh skte h ki jb hmra drawing view kulega
     * to ye function call ho ga us us time pe */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mCanvasBitmap = Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888)
        canvas = Canvas(mCanvasBitmap!!)
    }

    /** This method is called when a stroke is drawn on the canvas as a part of the painting.
     * Jo hme draw krna h canvas me wo ye function krega
     * ye kotlin me inbuilt function h jo ki arguments lega h
     * aur draw krta h
     * Ye Function ye bta rha ki kya aur kaise draw hona chahie
     * Change Canvas to Canvas? if fails */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(mCanvasBitmap!!,0f,0f,mCanvasPaint)

        /** ye hmara current runtime me path draw krega */
        if(!mDrawPath!!.isEmpty){
            mDrawPaint!!.strokeWidth = mDrawPath!!.brushThickness
            mDrawPaint!!.color = mDrawPath!!.colour
            canvas.drawPath(mDrawPath!!, mDrawPaint!!)
        }

        /** Jo jo path hmre mpath list me pdhe hue h use track/draw krega aur persist krega
         * agr hm upr wla if block nhi likhe h to path to draw hoga pr wo delayed draw hoga
         * jaise ki jb hm pura bna lege tb wo hme bnke dikhega */
        for(path in mPath){
            mDrawPaint!!.strokeWidth = path.brushThickness
            mDrawPaint!!.color = path.colour
            canvas.drawPath(path, mDrawPaint!!)
        }
    }

    /** This method acts as an event listener when a touch
     * event is detected on the device.
     * ye Function btae ga ki kb draw krna h kch bi */
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touchX = event?.x
        val touchY = event?.y

        /** When we touch screen there are multiple actions that can be happens like
         * 1. jb hmm screen ko touch krege to use instance se kya hoga
         * 2. jb hm apne finger ko drag krege screen me to kya hoga
         * 3 jb hm fingers ko release krge to kya hoga */
        when(event?.action){
            MotionEvent.ACTION_DOWN -> {
                mDrawPath!!.colour = color
                mDrawPath!!.brushThickness = mBrushSize
                mDrawPath!!.reset() // Clear any lines and curves from the path, making it empty.
                mDrawPath!!.moveTo(touchX!!, touchY!!)
            } // Set the beginning of the next contour to the point (x,y).

            MotionEvent.ACTION_MOVE -> {
                mDrawPath!!.lineTo(touchX!!, touchY!!)
            } // Add a line from the last point to the specified point (x,y).

            MotionEvent.ACTION_UP -> {
                /** Add when to stroke is drawn to canvas and added in the path arraylist
                 * Hm list me add krte ja rhe ki kaun kaun se path hmne track kie h */
                mPath.add(mDrawPath!!)
                mDrawPath = CustomPath(color, mBrushSize)
            }
            else -> return false
        }
        /** It will call draw function internally again */
        invalidate()
        return true
    }

    /**This method is called when either the brush or the eraser
     * sizes are to be changed. This method sets the brush/eraser
     * sizes to the new values depending on user selection.
     * Isme hm brush Size ko chnage krege jo user ne select kiya h uske according
     * Hm mBrushSize ko newSize ke equal nhi kr skte kyu nhi hme screen
     * dimension ko bi consider krna hoga
     * Same size agr agr screen pe alg alg size ke view ho skte h */
    fun setSizeForBrush(newSize : Float){
        mBrushSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
            newSize, resources.displayMetrics)
        mDrawPaint!!.strokeWidth = mBrushSize
    }

    /**This function is called when the user desires a color change.
     * This functions sets the color of a store to selected color and able to draw on view using that color.
     * Jo color user ne select kiya h wo ye set krega */
    fun setColor(newColor : String){
        /** firstly setting our colr variable */
        color = Color.parseColor(newColor)
        /** then setting our drawpaint color  which will draw in our canvas*/
        mDrawPaint!!.color = color
    }

    /** This function is called when the user selects the undo
     * command from the application. This function removes the
     * last stroke input by the user depending on the
     * number of times undo has been activated. */
    fun onClickUndo(){
        if(mPath.size > 0){
            mUndoPath.add(mPath.removeAt(mPath.size-1))
            /** As our onDraw function is overridden function
             * "invalidate" - will internally call onDraw Function and rewrite the whole screen or
             * canvas once again */
            invalidate()
        }
    }

    fun onClickRedo(){
        if(mUndoPath.size > 0){
            mPath.add(mUndoPath.removeAt(mUndoPath.size-1))
            invalidate()
        }
    }

    internal inner class CustomPath(var colour : Int,var brushThickness : Float) : Path()
}