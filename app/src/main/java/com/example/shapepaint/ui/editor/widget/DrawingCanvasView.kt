package com.example.shapepaint.ui.editor.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.shapepaint.R
import com.example.shapepaint.model.DrawableShape
import com.example.shapepaint.model.DrawableStroke
import com.example.shapepaint.model.ShapeType
import com.example.shapepaint.model.StrokePoint

class DrawingCanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.paint_grid)
        strokeWidth = 1f
    }
    private val shapePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private var shapes: List<DrawableShape> = emptyList()
    private var strokes: List<DrawableStroke> = emptyList()
    private var showGrid: Boolean = true
    private var backgroundImagePath: String? = null
    private var backgroundBitmap: Bitmap? = null
    private var freehandEnabled: Boolean = false
    private var eraserEnabled: Boolean = false
    private var currentStrokeColor: Int = context.getColor(R.color.paint_red)
    private var currentStrokeWidth: Float = resources.getDimension(R.dimen.default_stroke_width)
    private var activeStrokePoints: MutableList<PointF> = mutableListOf()

    var onCanvasTap: ((Float, Float) -> Unit)? = null
    var onStrokeFinished: ((List<StrokePoint>) -> Unit)? = null
    var onEraseAt: ((Float, Float) -> Unit)? = null

    fun render(
        shapes: List<DrawableShape>,
        strokes: List<DrawableStroke>,
        showGrid: Boolean,
        showLabels: Boolean,
        backgroundImagePath: String?,
        freehandEnabled: Boolean,
        eraserEnabled: Boolean,
        selectedColor: Int,
        selectedStrokeWidth: Float
    ) {
        this.shapes = shapes
        this.strokes = strokes
        this.showGrid = showGrid
        this.freehandEnabled = freehandEnabled
        this.eraserEnabled = eraserEnabled
        this.currentStrokeColor = selectedColor
        this.currentStrokeWidth = selectedStrokeWidth
        if (!freehandEnabled) {
            activeStrokePoints.clear()
        }
        if (this.backgroundImagePath != backgroundImagePath) {
            this.backgroundImagePath = backgroundImagePath
            backgroundBitmap = backgroundImagePath?.let { BitmapFactory.decodeFile(it) }
        }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(context.getColor(R.color.paint_canvas))
        backgroundBitmap?.let { bitmap ->
            canvas.drawBitmap(
                bitmap,
                null,
                Rect(0, 0, width, height),
                null
            )
        }
        if (showGrid) {
            drawGrid(canvas)
        }
        val drawSteps = buildList<Pair<Int, Any>> {
            shapes.forEach { add(it.drawOrder to it) }
            strokes.forEach { add(it.drawOrder to it) }
        }.sortedBy { it.first }

        drawSteps.forEach { (_, item) ->
            when (item) {
                is DrawableShape -> drawShape(canvas, item)
                is DrawableStroke -> drawStroke(canvas, item)
            }
        }

        if (activeStrokePoints.size > 1) {
            drawStrokePath(
                canvas = canvas,
                points = activeStrokePoints.map { StrokePoint(it.x, it.y) },
                color = currentStrokeColor,
                width = currentStrokeWidth
            )
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (eraserEnabled) {
            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE, MotionEvent.ACTION_UP -> {
                    onEraseAt?.invoke(event.x, event.y)
                }
            }
            return true
        }

        if (!freehandEnabled) {
            if (event.action == MotionEvent.ACTION_UP) {
                onCanvasTap?.invoke(event.x, event.y)
            }
            return true
        }

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                activeStrokePoints = mutableListOf(PointF(event.x, event.y))
                invalidate()
            }

            MotionEvent.ACTION_MOVE -> {
                activeStrokePoints.add(PointF(event.x, event.y))
                invalidate()
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                activeStrokePoints.add(PointF(event.x, event.y))
                val finishedStroke = activeStrokePoints.map { StrokePoint(it.x, it.y) }
                activeStrokePoints = mutableListOf()
                if (finishedStroke.size > 1) {
                    onStrokeFinished?.invoke(finishedStroke)
                }
                invalidate()
            }
        }

        return true
    }

    private fun drawShape(canvas: Canvas, shape: DrawableShape) {
        shapePaint.color = shape.color.colorInt
        when (shape.type) {
            ShapeType.SQUARE, ShapeType.RECTANGLE -> {
                canvas.drawRect(
                    shape.centerX - shape.width / 2f,
                    shape.centerY - shape.height / 2f,
                    shape.centerX + shape.width / 2f,
                    shape.centerY + shape.height / 2f,
                    shapePaint
                )
            }

            ShapeType.CIRCLE -> {
                canvas.drawCircle(shape.centerX, shape.centerY, shape.width / 2f, shapePaint)
            }

            ShapeType.OVAL -> {
                canvas.drawOval(
                    RectF(
                        shape.centerX - shape.width / 2f,
                        shape.centerY - shape.height / 2f,
                        shape.centerX + shape.width / 2f,
                        shape.centerY + shape.height / 2f
                    ),
                    shapePaint
                )
            }

            ShapeType.TRIANGLE -> {
                val path = Path().apply {
                    moveTo(shape.centerX, shape.centerY - shape.height / 2f)
                    lineTo(shape.centerX - shape.width / 2f, shape.centerY + shape.height / 2f)
                    lineTo(shape.centerX + shape.width / 2f, shape.centerY + shape.height / 2f)
                    close()
                }
                canvas.drawPath(path, shapePaint)
            }

            ShapeType.FREEHAND, ShapeType.ERASER -> Unit
        }
    }

    private fun drawStroke(canvas: Canvas, stroke: DrawableStroke) {
        drawStrokePath(canvas, stroke.points, stroke.color.colorInt, stroke.strokeWidth)
    }

    private fun drawStrokePath(canvas: Canvas, points: List<StrokePoint>, color: Int, width: Float) {
        if (points.size < 2) return
        strokePaint.color = color
        strokePaint.strokeWidth = width
        val path = Path().apply {
            moveTo(points.first().x, points.first().y)
            if (points.size == 2) {
                lineTo(points[1].x, points[1].y)
            } else {
                for (index in 1 until points.size) {
                    val previous = points[index - 1]
                    val current = points[index]
                    val midX = (previous.x + current.x) / 2f
                    val midY = (previous.y + current.y) / 2f
                    quadTo(previous.x, previous.y, midX, midY)
                }
                val last = points.last()
                lineTo(last.x, last.y)
            }
        }
        canvas.drawPath(path, strokePaint)
    }

    private fun drawGrid(canvas: Canvas) {
        val step = resources.getDimension(R.dimen.canvas_grid_step)
        var x = 0f
        while (x <= width) {
            canvas.drawLine(x, 0f, x, height.toFloat(), gridPaint)
            x += step
        }
        var y = 0f
        while (y <= height) {
            canvas.drawLine(0f, y, width.toFloat(), y, gridPaint)
            y += step
        }
    }
}
