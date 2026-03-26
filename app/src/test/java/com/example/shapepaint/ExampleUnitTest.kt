package com.example.shapepaint

import com.example.shapepaint.model.PaintColor
import com.example.shapepaint.model.ShapeType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun paintColor_resolvesFromHex() {
        assertEquals(PaintColor.BLUE, PaintColor.fromHex("#2E86AB"))
        assertEquals(PaintColor.RED, PaintColor.fromHex("#does-not-exist"))
    }

    @Test
    fun shapeType_reportsAspectLocking() {
        assertTrue(ShapeType.SQUARE.lockAspectRatio)
        assertTrue(ShapeType.CIRCLE.lockAspectRatio)
        assertEquals(false, ShapeType.RECTANGLE.lockAspectRatio)
    }
}
