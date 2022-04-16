#version 100

precision highp float;


const int MAX_JOINTS = 120;
uniform mat4 uMVPMatrix;
uniform mat4 uMMatrix;
attribute vec4 aPosition;
attribute vec3 aNormal;
attribute vec4 aColor;
attribute vec2 aTexCoords;
attribute vec3 aJointIndices;
attribute vec3 aWeights;
uniform mat4 uBindShapeMatrix;
uniform mat4 uJointTransforms[MAX_JOINTS];
uniform int uIsAnimated;
varying vec4 vPosition;
varying vec3 vNormal;
varying vec4 vColor;
varying vec2 vTexCoords;




void main() {

    vColor = aColor;
    vTexCoords = aTexCoords;

    if (uIsAnimated == 0){
        vPosition = uMMatrix * aPosition;
        vNormal = normalize(mat3(uMMatrix) * aNormal);
        gl_Position = uMVPMatrix * aPosition;
    }
    else {

        vec4 bindPos = uBindShapeMatrix * aPosition;
        vec3 bindNorm = mat3(uBindShapeMatrix) * aNormal;

        vec4 totalLocalPos = vec4(0.0);
        vec3 totalLocalNorm = vec3(0.0);

        for (int i = 0; i < 3; i++){
            int jointIndex = int(aJointIndices[i]);
            // FIXME: capire perché viene aWeights (1,0,0),(1,0,0) ecc e aJointIndices è (-1,0,0), (-1,0,0) ecc
            //if (jointIndex != -1){
            if (jointIndex > 0){
                totalLocalPos += uJointTransforms[jointIndex] * bindPos * aWeights[i];
                totalLocalNorm += mat3(uJointTransforms[jointIndex]) * bindNorm * aWeights[i];
            }
        }

        vPosition = uMMatrix * totalLocalPos;
        vNormal = normalize(mat3(uMMatrix) * totalLocalNorm);

        gl_Position = uMVPMatrix * totalLocalPos;
    }
}
