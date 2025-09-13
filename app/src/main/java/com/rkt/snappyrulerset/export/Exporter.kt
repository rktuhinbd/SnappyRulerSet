package com.rkt.snappyrulerset.export

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.core.content.FileProvider
import com.rkt.snappyrulerset.model.DrawingState
import com.rkt.snappyrulerset.model.Shape
import com.rkt.snappyrulerset.model.Vec2
import com.rkt.snappyrulerset.model.Viewport
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream


object Exporter {
    fun createBitmap(state: DrawingState, viewport: Viewport, widthPx: Int, heightPx: Int): Bitmap {
        val bmp = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        canvas.drawColor(Color.WHITE)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            strokeWidth = 3f
            style = Paint.Style.STROKE
        }

        fun w2s(x: Float, y: Float) =
            Pair((x - viewport.pan.x) * viewport.zoom, (y - viewport.pan.y) * viewport.zoom)

        fun drawLine(a: Vec2, b: Vec2) {
            val ax = w2s(a.x, a.y);
            val bx = w2s(b.x, b.y)
            canvas.drawLine(ax.first, ax.second, bx.first, bx.second, paint)
        }
        state.shapes.forEach { shape ->
            when (shape) {
                is Shape.Line -> drawLine(shape.a, shape.b)
                is Shape.Circle -> {
                    val c = w2s(shape.center.x, shape.center.y)
                    val r = shape.r * viewport.zoom
                    canvas.drawCircle(c.first, c.second, r, paint)
                }

                is Shape.Arc -> {
                    val c = w2s(shape.center.x, shape.center.y)
                    val r = shape.r * viewport.zoom
                    canvas.drawArc(
                        c.first - r,
                        c.second - r,
                        c.first + r,
                        c.second + r,
                        Math.toDegrees(shape.startRad.toDouble()).toFloat(),
                        Math.toDegrees(shape.sweepRad.toDouble()).toFloat(),
                        false,
                        paint
                    )
                }

                is Shape.Point -> {
                    val p = w2s(shape.p.x, shape.p.y)
                    canvas.drawPoint(p.first, p.second, paint)
                }

                is Shape.Path -> {
                    val pts = shape.points
                    for (i in 1 until pts.size) {
                        val a = w2s(pts[i - 1].x, pts[i - 1].y)
                        val b = w2s(pts[i].x, pts[i].y)
                        canvas.drawLine(a.first, a.second, b.first, b.second, paint)
                    }
                }
            }
        }
        return bmp
    }

    fun exportPng(state: DrawingState, widthPx: Int, heightPx: Int, file: File) {
        val bmp = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        canvas.drawColor(Color.WHITE)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            strokeWidth = 3f
            style = Paint.Style.STROKE
        }

        fun drawLine(a: Vec2, b: Vec2) = canvas.drawLine(a.x, a.y, b.x, b.y, paint)
        state.shapes.forEach { shape ->
            when (shape) {
                is Shape.Line -> drawLine(shape.a, shape.b)
                is Shape.Circle -> canvas.drawCircle(shape.center.x, shape.center.y, shape.r, paint)
                is Shape.Arc -> canvas.drawArc(
                    shape.center.x - shape.r,
                    shape.center.y - shape.r,
                    shape.center.x + shape.r,
                    shape.center.y + shape.r,
                    Math.toDegrees(shape.startRad.toDouble()).toFloat(),
                    Math.toDegrees(shape.sweepRad.toDouble()).toFloat(),
                    false,
                    paint
                )

                is Shape.Point -> canvas.drawPoint(shape.p.x, shape.p.y, paint)
                is Shape.Path -> {
                    val pts = shape.points
                    for (i in 1 until pts.size) drawLine(pts[i - 1], pts[i])
                }
            }
        }
        FileOutputStream(file).use { out -> bmp.compress(Bitmap.CompressFormat.PNG, 100, out) }
    }

    // Viewport-aware export: map world -> screen so the output matches what user sees
    fun exportPng(
        state: DrawingState,
        viewport: Viewport,
        widthPx: Int,
        heightPx: Int,
        file: File
    ) {
        val bmp = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        canvas.drawColor(Color.WHITE)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            strokeWidth = 3f
            style = Paint.Style.STROKE
        }

        fun w2s(x: Float, y: Float) =
            Pair((x - viewport.pan.x) * viewport.zoom, (y - viewport.pan.y) * viewport.zoom)

        fun drawLine(a: Vec2, b: Vec2) {
            val ax = w2s(a.x, a.y);
            val bx = w2s(b.x, b.y)
            canvas.drawLine(ax.first, ax.second, bx.first, bx.second, paint)
        }
        state.shapes.forEach { shape ->
            when (shape) {
                is Shape.Line -> drawLine(shape.a, shape.b)
                is Shape.Circle -> {
                    val c = w2s(shape.center.x, shape.center.y)
                    val r = shape.r * viewport.zoom
                    canvas.drawCircle(c.first, c.second, r, paint)
                }

                is Shape.Arc -> {
                    val c = w2s(shape.center.x, shape.center.y)
                    val r = shape.r * viewport.zoom
                    canvas.drawArc(
                        c.first - r,
                        c.second - r,
                        c.first + r,
                        c.second + r,
                        Math.toDegrees(shape.startRad.toDouble()).toFloat(),
                        Math.toDegrees(shape.sweepRad.toDouble()).toFloat(),
                        false,
                        paint
                    )
                }

                is Shape.Point -> {
                    val p = w2s(shape.p.x, shape.p.y)
                    canvas.drawPoint(p.first, p.second, paint)
                }

                is Shape.Path -> {
                    val pts = shape.points
                    for (i in 1 until pts.size) {
                        val a = w2s(pts[i - 1].x, pts[i - 1].y)
                        val b = w2s(pts[i].x, pts[i].y)
                        canvas.drawLine(a.first, a.second, b.first, b.second, paint)
                    }
                }
            }
        }
        FileOutputStream(file).use { out -> bmp.compress(Bitmap.CompressFormat.PNG, 100, out) }
    }

    // JPEG export
    fun exportJpeg(
        state: DrawingState,
        viewport: Viewport,
        widthPx: Int,
        heightPx: Int,
        file: File
    ) {
        val bmp = createBitmap(state, viewport, widthPx, heightPx)
        FileOutputStream(file).use { out -> bmp.compress(Bitmap.CompressFormat.JPEG, 95, out) }
    }

    // Create share intent for PNG
    fun createShareIntentPng(
        context: Context,
        state: DrawingState,
        viewport: Viewport,
        widthPx: Int,
        heightPx: Int
    ): Intent {
        val file = File(context.cacheDir, "snappy_export_${System.currentTimeMillis()}.png")
        exportPng(state, viewport, widthPx, heightPx, file)

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        return Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    // Create share intent for JPEG
    fun createShareIntentJpeg(
        context: Context,
        state: DrawingState,
        viewport: Viewport,
        widthPx: Int,
        heightPx: Int
    ): Intent {
        val file = File(context.cacheDir, "snappy_export_${System.currentTimeMillis()}.jpg")
        exportJpeg(state, viewport, widthPx, heightPx, file)

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        return Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    // Create bitmap as byte array for sharing
    fun createBitmapBytes(
        state: DrawingState,
        viewport: Viewport,
        widthPx: Int,
        heightPx: Int,
        format: Bitmap.CompressFormat
    ): ByteArray {
        val bmp = createBitmap(state, viewport, widthPx, heightPx)
        val stream = ByteArrayOutputStream()
        bmp.compress(format, if (format == Bitmap.CompressFormat.JPEG) 95 else 100, stream)
        return stream.toByteArray()
    }
}