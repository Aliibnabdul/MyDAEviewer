package com.transistorapps.mydaeviewer

import android.content.Context
import android.opengl.GLES20
import android.util.Log
import com.model3d.jcolladaloaderlib.model.AnimatedModel
import com.model3d.jcolladaloaderlib.model.Object3DData
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

class MeshShader constructor(context: Context) {
    private lateinit var mvpMatrix: FloatArray
    private var textureIndex = 0
    private val aPosition: Int
    private val aNormal: Int
    private val aColor: Int
    private val aTexCoords: Int
    private val uMMatrix: Int
    private val uMVPMatrix: Int
    private val uTexture: Int
    private var mesh: Object3DData? = null
    private lateinit var viewPos: FloatArray
    private val uViewPos: Int
    private val program: Int = GLES20.glCreateProgram()
    private val aJointIndices: Int
    private val aWeights: Int
    private val uBindShapeMatrix: Int
    private val uJointTransforms: MutableList<Int>
    private val uIsTextured: Int
    private val uIsAnimated: Int
    private val uIsColored: Int
    private val isAnimated: Boolean
        get() = (mesh as? AnimatedModel)?.vertexWeights != null && (mesh as? AnimatedModel)?.jointIds != null

    init {
        GLES20.glAttachShader(program, compile(context, GLES20.GL_VERTEX_SHADER, "vs_mesh.glsl"))
        GLES20.glAttachShader(program, compile(context, GLES20.GL_FRAGMENT_SHADER, "fs_mesh.glsl"))
        GLES20.glLinkProgram(program)
        val params = IntArray(1)
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, params, 0)
        if (params[0] == GLES20.GL_FALSE) {
            throw RuntimeException(
                """
                    Program linking failed:
                    ${GLES20.glGetProgramInfoLog(program)}
                    """.trimIndent()
            )
        }
        GLES20.glUseProgram(program)
        aPosition = getAttrib("aPosition")
        aNormal = getAttrib("aNormal")
        aColor = getAttrib("aColor")
        aTexCoords = getAttrib("aTexCoords")
        uMMatrix = getUniform("uMMatrix")
        uMVPMatrix = getUniform("uMVPMatrix")
        uTexture = getUniform("uTexture")
        uViewPos = getUniform("uViewPos")
        uIsTextured = getUniform("uIsTextured")
        uIsAnimated = getUniform("uIsAnimated")
        uIsColored = getUniform("uIsColored")
        aJointIndices = getAttrib("aJointIndices")
        aWeights = getAttrib("aWeights")
        uBindShapeMatrix = getUniform("uBindShapeMatrix")
        uJointTransforms = ArrayList()
        for (i in 0..119) {
            uJointTransforms.add(getUniform("uJointTransforms[$i]"))
        }
    }

    fun bindData() {
        GLES20.glUseProgram(program)
        GLES20.glEnableVertexAttribArray(aPosition)
        GLES20.glEnableVertexAttribArray(aNormal)
        GLES20.glEnableVertexAttribArray(aTexCoords)
        GLES20.glEnableVertexAttribArray(aWeights)
        GLES20.glEnableVertexAttribArray(aJointIndices)
        GLES20.glVertexAttribPointer(aPosition, 3, GLES20.GL_FLOAT, false, 0, mesh!!.vertexBuffer.position(0))
        GLES20.glVertexAttribPointer(aNormal, 3, GLES20.GL_FLOAT, false, 0, mesh!!.normalsBuffer.position(0))
        if (mesh!!.colorsBuffer != null) {
            GLES20.glUniform1i(uIsColored, 1)
            GLES20.glVertexAttribPointer(aColor, 4, GLES20.GL_FLOAT, false, 0, mesh!!.colorsBuffer.position(0))
        } else {
            GLES20.glUniform1i(uIsColored, 0)
        }
        GLES20.glVertexAttribPointer(aTexCoords, 2, GLES20.GL_FLOAT, false, 0, mesh!!.textureBuffer.position(0))
        GLES20.glUniformMatrix4fv(uMMatrix, 1, false, mesh!!.modelMatrix, 0)
        GLES20.glUniformMatrix4fv(uMVPMatrix, 1, false, mvpMatrix, 0)
        GLES20.glUniform3f(uViewPos, viewPos[0], viewPos[1], viewPos[2])
        textureIndex = 0
        if (mesh!!.material.textureId != -1) {
            GLES20.glUniform1i(uIsTextured, 1)
            bindTexture(uTexture, mesh!!.material.textureId)
        } else {
            GLES20.glUniform1i(uIsTextured, 0)
        }
        if (isAnimated) {
            GLES20.glUniform1i(uIsAnimated, 1)
            GLES20.glVertexAttribPointer(
                aWeights,
                3,
                GLES20.GL_FLOAT,
                false,
                0,
                (mesh as AnimatedModel?)!!.vertexWeights.position(0)
            )
            GLES20.glVertexAttribPointer(
                aJointIndices,
                3,
                GLES20.GL_FLOAT,
                false,
                0,
                (mesh as AnimatedModel?)!!.jointIds.position(0)
            )
            GLES20.glUniformMatrix4fv(uBindShapeMatrix, 1, false, (mesh as AnimatedModel?)!!.bindShapeMatrix, 0)
            for (i in (mesh as AnimatedModel?)!!.jointTransforms.indices) {
                GLES20.glUniformMatrix4fv(
                    uJointTransforms[i], 1, false,
                    (mesh as AnimatedModel?)!!.jointTransforms[i], 0
                )
            }
        } else {
            GLES20.glUniform1i(uIsAnimated, 0)
        }
    }

    fun unbindData() {
        GLES20.glDisableVertexAttribArray(aPosition)
        GLES20.glDisableVertexAttribArray(aNormal)
        GLES20.glDisableVertexAttribArray(aWeights)
        GLES20.glDisableVertexAttribArray(aJointIndices)
    }

    private fun bindTexture(uniform: Int, texture: Int) {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + textureIndex)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture) //Для подключения текстуры к слоту используется процедура:


        // Enable blending for combining colors when there is transparency
//        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
//        GLES20.glEnable(GLES20.GL_BLEND)


        GLES20.glUniform1i(uniform, textureIndex++)
    }

    fun setMvpMatrix(mvpMatrix: FloatArray): MeshShader {
        this.mvpMatrix = mvpMatrix
        return this
    }

    fun setMesh(mesh: Object3DData?): MeshShader {
        this.mesh = mesh
        return this
    }

    fun setViewPos(viewPos: FloatArray): MeshShader {
        this.viewPos = viewPos
        return this
    }

    private fun compile(context: Context, type: Int, filename: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, read(context, filename))
        GLES20.glCompileShader(shader)
        val params = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, params, 0)
        if (params[0] == GLES20.GL_FALSE) {
            throw RuntimeException(
                "Compilation of shader $filename failed:\n" + GLES20.glGetShaderInfoLog(shader)
            )
        }
        return shader
    }

    private fun getAttrib(name: String): Int {
        val location = GLES20.glGetAttribLocation(program, name)
        checkLocation(location, name)
        return location
    }

    private fun getUniform(name: String): Int {
        val location = GLES20.glGetUniformLocation(program, name)
        checkLocation(location, name)
        return location
    }

    companion object {
        private fun read(context: Context, filename: String): String {
            val buf = StringBuilder()
            val text: InputStream
            val bufferedReader: BufferedReader
            var str: String?
            return try {
                text = context.assets.open(filename)
                bufferedReader = BufferedReader(InputStreamReader(text, StandardCharsets.UTF_8))
                while (bufferedReader.readLine().also { str = it } != null) {
                    str += "\n"
                    buf.append(str)
                }
                bufferedReader.close()
                buf.toString()
            } catch (e: Exception) {
                Log.e(
                    "MeshShader_TAG", "read(filename: $filename)\n" +
                            "Exception: ${e.stackTraceToString()}"
                )
                ""
            }
        }

        private fun checkLocation(location: Int, label: String) {
            if (location < 0) {
                throw RuntimeException("Unable to locate '$label' in program")
            }
        }
    }

}