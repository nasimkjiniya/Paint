package com.example.shapepaint.model

import androidx.annotation.StringRes
import com.example.shapepaint.R

enum class ShapeType(
    @StringRes val labelResId: Int,
    val lockAspectRatio: Boolean
) {
    SQUARE(labelResId = R.string.square, lockAspectRatio = true),
    RECTANGLE(labelResId = R.string.rectangle, lockAspectRatio = false),
    CIRCLE(labelResId = R.string.circle, lockAspectRatio = true),
    OVAL(labelResId = R.string.oval, lockAspectRatio = false),
    TRIANGLE(labelResId = R.string.triangle, lockAspectRatio = false),
    FREEHAND(labelResId = R.string.freehand, lockAspectRatio = false),
    ERASER(labelResId = R.string.eraser, lockAspectRatio = false)
}
