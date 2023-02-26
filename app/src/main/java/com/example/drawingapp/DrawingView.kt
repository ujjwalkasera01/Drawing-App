package com.example.drawingapp

/** import android.graphics.*
 * import all files present in graphic
 * we don,t need to separately import things under it
 */
import android.graphics.*
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View


class DrawingView(context: Context, attrs:AttributeSet) : View(context,attrs) {

    private var mDrawPath : CustomPath ?= null
    private var mCanvasBitmap : Bitmap ?= null
    private var mDrawPaint : Paint ?= null
    private var mCanvasPaint : Paint ?= null
    private var mBrushSize : Float = 0.toFloat()
    private var color : Int = Color.BLACK
    private var canvas : Canvas ?= null
    /** Ye hmara ek trike se jo jo hmre path track krege hm uska list bna ke
     * rkhe ga taki phr se us path ko bna ke use persist kr skte */
    private val mPath = ArrayList<CustomPath>()

    init{
        setUpDrawing()
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
        mBrushSize = 10.toFloat()

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

    internal inner class CustomPath(var colour : Int,var brushThickness : Float) : Path() {

    }
}