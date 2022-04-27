package com.transistorapps.mydaeviewer

import android.graphics.PixelFormat
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var meshRenderer: MeshRenderer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val glSurfaceView = findViewById<GLSurfaceView>(R.id.gLSurfaceView)
//        val glSurfaceView = GLSurfaceView(this)

// !!! Важно !!! Обязательные 2 файла для всех моделей !!! fs_mesh.glsl и vs_mesh.glsl

        meshRenderer = MeshRenderer(this, "Jerry-Running.dae")
        glSurfaceView.setEGLContextClientVersion(2)

// Transparent background:
        glSurfaceView.setZOrderOnTop(true) // Displays GLSurfaceView on top of other views, this is required.
//        Configures OpenGL and the surface to allow transparent drawing:
        glSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        glSurfaceView.holder.setFormat(PixelFormat.RGBA_8888)


        glSurfaceView.setRenderer(meshRenderer)
        glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY

        setupButtons()
    }

    private fun setupButtons(){
        // Zoom:
        findViewById<Button>(R.id.btnZoomIn).setOnClickListener {
            meshRenderer.meshScale[0] = meshRenderer.meshScale[0] + 0.1F
            meshRenderer.meshScale[1] = meshRenderer.meshScale[1] + 0.1F
            meshRenderer.meshScale[2] = meshRenderer.meshScale[2] + 0.1F
        }
        findViewById<Button>(R.id.btnZoomOut).setOnClickListener {
            meshRenderer.meshScale[0] = meshRenderer.meshScale[0] - 0.1F
            meshRenderer.meshScale[1] = meshRenderer.meshScale[1] - 0.1F
            meshRenderer.meshScale[2] = meshRenderer.meshScale[2] - 0.1F
        }

        // Rotation:
        findViewById<Button>(R.id.btnRotateLeft).setOnClickListener { meshRenderer.rotation[1] = meshRenderer.rotation[1] - 6 }
        findViewById<Button>(R.id.btnRotateRight).setOnClickListener { meshRenderer.rotation[1] = meshRenderer.rotation[1] + 6 }
        findViewById<Button>(R.id.btnLeft).setOnClickListener { meshRenderer.rotation[2] = meshRenderer.rotation[2] + 6 }
        findViewById<Button>(R.id.btnRight).setOnClickListener { meshRenderer.rotation[2] = meshRenderer.rotation[2] - 6 }
        findViewById<Button>(R.id.btnForward).setOnClickListener { meshRenderer.rotation[0] = meshRenderer.rotation[0] - 6 }
        findViewById<Button>(R.id.btnReverse).setOnClickListener { meshRenderer.rotation[0] = meshRenderer.rotation[0] + 6 }
    }
}