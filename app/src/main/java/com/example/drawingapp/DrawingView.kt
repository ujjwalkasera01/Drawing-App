package com.example.drawingapp

/** import android.graphics.*
 * import all files present in graphic
 * we don,t need to separately import things under it
 */
import android.annotation.SuppressLint
import android.graphics.*
import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View


class DrawingView(context: Context, attrs:AttributeSet) : View(context,attrs) {

    private var mDrawPath : CustomPath ?= null
    private var mCanvasBitmap : Bitmap ?= null
    private var mDrawPaint : Paint ?= null
    private var mCanvasPaint : Paint ?= null
    private var mBrushSize : Float = 3.toFloat()
    private var color : Int = Color.BLACK
    private var canvas : Canvas ?= null

    /** Ye hmara ek trike se jo jo hm path track krege hm uska list bna ke
     * rkhe ga taki phr se us path ko bna ke use persist kr skte */
    private val mPath = ArrayList<CustomPath>()

    /** We are storing the paths that we are removing as we can similarly implement Redo button
     * with path that have been removed */
    private val mUndoPath = ArrayList<CustomPath>()

    init{
        setUpDrawing()
    }

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
            /** As our onDraw function is overridden function
             * "invalidate" - will internally call onDraw Function and rewrite the whole screen or
             * canvas once again */
            invalidate()
        }
    }

    /** Initialising the variables we have created earlier */
    private fun setUpDrawing(){
        mDrawPaint = Paint()
        mDrawPath = CustomPath(color,mBrushSize)
        mDrawPaint!!.color = color
        mDrawPaint!!.style = Paint.Style.STROKE
        mDrawPaint!!.strokeJoin = Paint.Join.ROUND
        mDrawPaint!!.strokeCap = Paint.Cap.ROUND
        mCanvasPaint = Paint(Paint.DITHER_FLAG)
        // Kyuki hm mainActivity me hi brushSize ko define kr de rhe to
        // is lie hm ise hta skte h
        // mBrushSize = 10.toFloat()

    }

    /** Jb bi Drawing view ki size change hogi to ye function call hoga
     * Aur to jitne bi variables iske andar h wo phr se assigned ho jaege
     * Ya phr ye bi kh skte h ki jb hmra drawing view kulega
     * to ye function call ho ga us us time pe
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mCanvasBitmap = Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888)
        canvas = Canvas(mCanvasBitmap!!)
    }

    /** Jo hme draw krna h canvas me wo ye function krega
     * ye kotlin me inbuilt function h jo ki arguments lega h
     * aur draw krta h
     * Ye Function ye bta rha ki kya aur kaise draw hona chahie
     * Change Canvas to Canvas? if fails
     * */
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
         * jaise ki jb hm pura bna lege tb wo hme bnke dikhega
         */
        for(path in mPath){
            mDrawPaint!!.strokeWidth = path.brushThickness
            mDrawPaint!!.color = path.colour
            canvas.drawPath(path, mDrawPaint!!)
        }
    }

    /** ye Function btae ga ki kb draw krna h kch bi */
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touchX = event?.x
        val touchY = event?.y

        /** When we touch screen there are multiple actions that can be happens like
         * 1. jb hmm screen ko touch krege to use instance se kya hoga
         * 2. jb hm apne finger ko drag krege screen me to kya hoga
         * 3 jb hm fingers ko release krge to kya hoga
         */
        when(event?.action){
            MotionEvent.ACTION_DOWN -> {
                mDrawPath!!.colour = color
                mDrawPath!!.brushThickness = mBrushSize
                mDrawPath!!.reset()
                mDrawPath!!.moveTo(touchX!!, touchY!!)
            }

            MotionEvent.ACTION_MOVE -> {
                mDrawPath!!.lineTo(touchX!!, touchY!!)
            }

            MotionEvent.ACTION_UP -> {
                /** Hm list me add krte ja rhe ki kaun kaun se path hmne track kie h */
                mPath.add(mDrawPath!!)
                mDrawPath = CustomPath(color, mBrushSize)
            }

            else -> return false
        }
        /** ye hmara sayad se draw function ko call krega */
        invalidate()
        return true
    }

    /** Isme hm brush Size ko chnage krege jo user ne select kiya h uske according
     * Hm mBrushSize ko newSize ke equal nhi kr skte kyu nhi hme screen
     * dimension ko bi consider krna hoga
     * Same size agr agr screen pe alg alg size ke view ho skte h */
    fun setSizeForBrush(newSize : Float){
        mBrushSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
            newSize, resources.displayMetrics)
        mDrawPaint!!.strokeWidth = mBrushSize
    }

    /** Jo color user ne select kiya h wo ye set krega */
    fun setColor(newColor : String){
        /** firstly setting our colr variable */
        color = Color.parseColor(newColor)
        /** then setting our drawpaint color  which will draw in our canvas*/
        mDrawPaint!!.color = color
    }

    internal inner class CustomPath(var colour : Int,var brushThickness : Float) : Path() {

    }
}