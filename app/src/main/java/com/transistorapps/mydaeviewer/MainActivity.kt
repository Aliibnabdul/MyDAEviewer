package com.transistorapps.mydaeviewer

import android.graphics.PixelFormat
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<GLSurfaceView>(R.id.gLSurfaceView)
        val glSurfaceView = findViewById<GLSurfaceView>(R.id.gLSurfaceView)
//        val glSurfaceView = GLSurfaceView(this)
        //        MeshRenderer meshRenderer = new MeshRenderer(this, "Tree.dae");
//        MeshRenderer meshRenderer = new MeshRenderer(this, "got.dae");
        val meshRenderer = MeshRenderer(this, "Jerry-Running.dae")
//        glSurfaceView.systemUiVisibility = View.SYSTEM_UI_FLAG_IMMERSIVE or View.SYSTEM_UI_FLAG_FULLSCREEN
        glSurfaceView.setEGLContextClientVersion(2)


        glSurfaceView.setZOrderOnTop(true)
        glSurfaceView.alpha = 0F
        glSurfaceView.holder.setFormat(PixelFormat.TRANSLUCENT)
        glSurfaceView.setBackgroundColor(getColor(R.color.purple_200))


        glSurfaceView.setRenderer(meshRenderer)
        glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
    }
}