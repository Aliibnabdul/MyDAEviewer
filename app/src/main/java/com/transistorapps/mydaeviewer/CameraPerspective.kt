package com.transistorapps.mydaeviewer

import android.opengl.Matrix


class CameraPerspective constructor(
    eye: FloatArray,
    center: FloatArray,
    up: FloatArray,
    near: Float,
    far: Float
) {
    val vpMatrix: FloatArray
    var projectionMatrix: FloatArray
    val viewMatrix: FloatArray
    var eye: FloatArray
    var center: FloatArray
    var up: FloatArray
    val near: Float
    val far: Float
    var width = 0
    var height = 0

    fun loadVpMatrix() {
        val ratio = width.toFloat() / height.toFloat()
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, near, far)
        createVpMatrix()
    }

    fun createVpMatrix() {
        // Set the camera position (View matrix)
        Matrix.setLookAtM(
            viewMatrix, 0,
            eye[0], eye[1], eye[2], center[0], center[1], center[2], up[0], up[1], up[2]
        )
        // Calculate the projection and view transformation
        Matrix.multiplyMM(vpMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
    }

    fun setWidth(width: Int): CameraPerspective {
        this.width = width
        return this
    }

    fun setHeight(height: Int): CameraPerspective {
        this.height = height
        return this
    }

    init {
        vpMatrix = FloatArray(16)
        projectionMatrix = FloatArray(16)
        viewMatrix = FloatArray(16)
        this.eye = eye
        this.center = center
        this.up = up
        this.near = near
        this.far = far
    }
}
