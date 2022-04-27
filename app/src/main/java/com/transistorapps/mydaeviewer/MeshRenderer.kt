package com.transistorapps.mydaeviewer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.opengl.Matrix
import com.model3d.jcolladaloaderlib.ColladaLoader
import com.model3d.jcolladaloaderlib.animation.Animator
import com.model3d.jcolladaloaderlib.model.Object3DData
import java.io.ByteArrayInputStream
import java.io.InputStream
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MeshRenderer(private val context: Context, private val meshFilename: String) : GLSurfaceView.Renderer {
    private var meshShader: MeshShader? = null
    private val mvpMatrix = FloatArray(16)
    private var cameraPerspective: CameraPerspective? = null
    private var meshes: List<Object3DData>? = null
    private val animator = Animator()
    val rotation = floatArrayOf(0f, 0f, 0f)
    val meshScale = floatArrayOf(0.2f, 0.2f, 0.2f)

    // Вызывается при создании экрана:
    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        GLES20.glEnable(GLES20.GL_DEPTH_TEST) // включим проверку глубины
        GLES20.glDepthFunc(GLES20.GL_LESS)
        GLES20.glClearDepthf(1.0f)
        cameraPerspective = CameraPerspective(CAMERA_EYE, CAMERA_CENTER, CAMERA_UP, 1f, 100f)
        meshes = ColladaLoader().loadFromAsset(context, meshFilename)
        for (mesh in meshes!!) {
            mesh.scale = meshScale
            if (mesh.textureData != null) {
                mesh.material.textureId = loadTexture(mesh.textureData)
            }
        }
        meshShader = MeshShader(context)
    }

    // При изменении экрана:
    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        // установим область просмотра равной размеру экрана:
        GLES20.glViewport(0, 0, width, height)

        cameraPerspective?.setWidth(width)?.setHeight(height)
    }

    // Рисование трехмерных объектов:
    override fun onDrawFrame(gl: GL10) {

//        GLES20.glClearColor(1f, 1f, 1f, 0f) // set white background color
        GLES20.glClearColor(0f, 0f, 1f, 0f) // set black background color
//        Альфа=0 - полностью прозрачный объект. Альфа=1 - полностью непрозрачный объект.

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT) // Очищаем буферы глубины и цвета


        cameraPerspective?.loadVpMatrix()
        meshShader!!.setViewPos(cameraPerspective!!.eye)
//        rotation[1] = rotation[1] + 10
        for (mesh in meshes!!) {
            mesh.scale = meshScale
            animator.update(mesh, false)
            mesh.rotation = rotation
            Matrix.multiplyMM(mvpMatrix, 0, cameraPerspective?.vpMatrix, 0, mesh.modelMatrix, 0)
            meshShader!!.setMesh(mesh)
            meshShader!!.setMvpMatrix(mvpMatrix)
            meshShader!!.bindData()
            for (i in mesh.elements.indices) {
                val indices = mesh.elements[i].indexBuffer.position(0)
                GLES20.glDrawElements(GLES20.GL_TRIANGLES, indices.capacity(), GLES20.GL_UNSIGNED_INT, indices)
            }
            meshShader!!.unbindData()


        }
    }

    private fun loadTexture(textureData: ByteArray): Int {
        val textureIs = ByteArrayInputStream(textureData)
        val textureHandle = IntArray(1)
        GLES20.glGenTextures(1, textureHandle, 0)
        if (textureHandle[0] == 0) {
            throw RuntimeException("Error loading texture.")
        }
        val bitmap = loadBitmap(textureIs)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0])
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
        bitmap.recycle()
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST)
        return textureHandle[0]
    }

    private fun loadBitmap(inputStream: InputStream): Bitmap {
        val options = BitmapFactory.Options()
        options.inScaled = false
        return BitmapFactory.decodeStream(inputStream, null, options)
            ?: throw RuntimeException("Error loading bitmap.")
    }

    companion object {
        private val CAMERA_EYE = floatArrayOf(0f, 0f, 3f)
        private val CAMERA_CENTER = floatArrayOf(0f, 0f, 0f)
        private val CAMERA_UP = floatArrayOf(0f, 1f, 0f)
    }
}