package com.example.companion.avatar

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.*

/**
 * 3D Procedural Vector Renderer for Avatar 3: "Authentic Cosmic Shiv".
 * Built with full 3D perspective projection, faceted polyhedron geometry,
 * dynamic normal-vector lighting, and dual gyro-stabilized 3D diamond halos.
 * Zero static 2D cutouts — 100% mathematical, responsive, and stable.
 */
@Composable
fun CosmicShivRenderer(
    context: AvatarRenderContext,
    modifier: Modifier = Modifier
) {
    val isSlumber = context.sleepStage == InactivitySleepStage.DEEP_SLUMBER_0FPS

    val infiniteTransition = rememberInfiniteTransition(label = "cosmic_3d_physics")

    val levitationBob by if (!isSlumber) {
        infiniteTransition.animateFloat(
            initialValue = -12f,
            targetValue = 12f,
            animationSpec = infiniteRepeatable(
                animation = tween(2600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "levitation"
        )
    } else {
        remember { mutableFloatStateOf(0f) }
    }

    val rotationAngleY by if (!isSlumber) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = (2 * PI).toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(12000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "rotation_y"
        )
    } else {
        remember { mutableFloatStateOf(0.35f) }
    }

    val haloSpinAngle by if (!isSlumber) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = (2 * PI).toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(6000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "halo_spin"
        )
    } else {
        remember { mutableFloatStateOf(0.7f) }
    }

    val breathPulse by if (!isSlumber) {
        infiniteTransition.animateFloat(
            initialValue = 0.94f,
            targetValue = 1.06f,
            animationSpec = infiniteRepeatable(
                animation = tween(2100, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "breath_pulse"
        )
    } else {
        remember { mutableFloatStateOf(1f) }
    }

    val waveRipple by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "listening_ripple"
    )

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f + levitationBob
            val scale = (size.minDimension * 0.42f) * breathPulse

            // 3D Perspective parameters
            val cameraDist = 480f

            fun project3D(x: Float, y: Float, z: Float): Pair<Offset, Float> {
                val f = cameraDist / (cameraDist + z)
                return Pair(Offset(cx + x * f, cy + y * f), f)
            }

            // A. Deep Cosmic Nebula Background
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x7700E5FF),
                        Color(0x447C4DFF),
                        Color(0x11020617),
                        Color.Transparent
                    ),
                    center = Offset(cx, cy),
                    radius = scale * 1.55f
                ),
                radius = scale * 1.55f,
                center = Offset(cx, cy)
            )

            // B. Sound Wave Ripples when listening or speaking
            if ((context.state == AvatarState.LISTENING || context.state == AvatarState.SPEAKING) && !isSlumber) {
                drawCircle(
                    color = Color(0x6600E5FF),
                    radius = scale * waveRipple,
                    center = Offset(cx, cy),
                    style = Stroke(width = 3f)
                )
                drawCircle(
                    color = Color(0x33A855F7),
                    radius = scale * (waveRipple * 1.25f),
                    center = Offset(cx, cy),
                    style = Stroke(width = 2f)
                )
            }

            // C. 3D Diamond Crown Halo (Behind-head pass: z < 0)
            val haloPointsCount = 12
            val haloRadius = scale * 1.05f
            val haloTiltX = 0.42f // 24 degrees forward tilt
            val haloRot = haloSpinAngle

            for (i in 0 until haloPointsCount) {
                val phi = (2 * PI * i / haloPointsCount + haloRot).toFloat()
                val rx = haloRadius * cos(phi)
                val ry = haloRadius * sin(phi) * sin(haloTiltX) - scale * 0.55f
                val rz = haloRadius * sin(phi) * cos(haloTiltX)

                if (rz < 0) { // Render behind avatar
                    val (proj, f) = project3D(rx, ry, rz)
                    val diamondSize = 7f * f
                    val alpha = (0.35f + 0.35f * ((rz + haloRadius) / (2 * haloRadius))).coerceIn(0.1f, 1f)

                    drawCircle(
                        color = Color(0xFF00E5FF).copy(alpha = alpha * 0.7f),
                        radius = diamondSize * 1.5f,
                        center = proj
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = alpha),
                        radius = diamondSize,
                        center = proj
                    )
                }
            }

            // D. 3D Faceted Octahedron / Polyhedron Celestial Head & Torso
            // Polyhedron vertices in 3D object space
            val rotY = rotationAngleY
            val cosY = cos(rotY)
            val sinY = sin(rotY)

            fun rotateY(x: Float, y: Float, z: Float): Triple<Float, Float, Float> {
                val nx = x * cosY + z * sinY
                val nz = -x * sinY + z * cosY
                return Triple(nx, y, nz)
            }

            // Head 3D Octahedron vertices
            val headH = scale * 0.55f
            val headW = scale * 0.44f
            val headD = scale * 0.44f

            val vTop = rotateY(0f, -headH - scale * 0.1f, 0f)
            val vBottom = rotateY(0f, scale * 0.25f, 0f)
            val vFront = rotateY(0f, -headH * 0.4f, headD)
            val vBack = rotateY(0f, -headH * 0.4f, -headD)
            val vLeft = rotateY(-headW, -headH * 0.4f, 0f)
            val vRight = rotateY(headW, -headH * 0.4f, 0f)

            data class Face(val v1: Triple<Float, Float, Float>, val v2: Triple<Float, Float, Float>, val v3: Triple<Float, Float, Float>, val baseColor: Color)

            val headFaces = listOf(
                // Top pyramid
                Face(vTop, vFront, vRight, Color(0xFF00E5FF)),
                Face(vTop, vRight, vBack, Color(0xFF1E40AF)),
                Face(vTop, vBack, vLeft, Color(0xFF4338CA)),
                Face(vTop, vLeft, vFront, Color(0xFF38BDF8)),
                // Bottom pyramid
                Face(vBottom, vRight, vFront, Color(0xFF0284C7)),
                Face(vBottom, vBack, vRight, Color(0xFF1D4ED8)),
                Face(vBottom, vLeft, vBack, Color(0xFF6D28D9)),
                Face(vBottom, vFront, vLeft, Color(0xFF06B6D4))
            )

            // Light vector for dynamic 3D Lambertian shading
            val lx = 0.4f
            val ly = -0.7f
            val lz = 0.58f

            // Sort faces by average Z-depth (Painter's algorithm)
            val sortedFaces = headFaces.map { face ->
                val avgZ = (face.v1.third + face.v2.third + face.v3.third) / 3f
                Pair(face, avgZ)
            }.sortedBy { it.second }

            for ((face, _) in sortedFaces) {
                // Calculate normal vector
                val ax = face.v2.first - face.v1.first
                val ay = face.v2.second - face.v1.second
                val az = face.v2.third - face.v1.third

                val bx = face.v3.first - face.v1.first
                val by = face.v3.second - face.v1.second
                val bz = face.v3.third - face.v1.third

                val nx = ay * bz - az * by
                val ny = az * bx - ax * bz
                val nz = ax * by - ay * bx
                val len = sqrt(nx * nx + ny * ny + nz * nz).coerceAtLeast(0.001f)

                val nnx = nx / len
                val nny = ny / len
                val nnz = nz / len

                // Backface culling: only draw faces facing the viewer (nz > -0.05f)
                if (nnz > -0.15f) {
                    val dot = (nnx * lx + nny * ly + nnz * lz).coerceIn(-1f, 1f)
                    val lightIntensity = (0.35f + 0.65f * max(0f, dot)).coerceIn(0.2f, 1f)

                    val p1 = project3D(face.v1.first, face.v1.second, face.v1.third).first
                    val p2 = project3D(face.v2.first, face.v2.second, face.v2.third).first
                    val p3 = project3D(face.v3.first, face.v3.second, face.v3.third).first

                    val path = Path().apply {
                        moveTo(p1.x, p1.y)
                        lineTo(p2.x, p2.y)
                        lineTo(p3.x, p3.y)
                        close()
                    }

                    val faceCol = Color(
                        red = (face.baseColor.red * lightIntensity).coerceIn(0f, 1f),
                        green = (face.baseColor.green * lightIntensity).coerceIn(0f, 1f),
                        blue = (face.baseColor.blue * lightIntensity).coerceIn(0f, 1f),
                        alpha = 0.95f
                    )

                    drawPath(path = path, color = faceCol)
                    drawPath(path = path, color = Color(0xFFE0F2FE).copy(alpha = 0.65f * lightIntensity), style = Stroke(width = 1.5f))
                }
            }

            // E. Starry Galaxy Eyes in 3D Space
            val eyeZ = headD * 0.75f
            val eyeY = -headH * 0.38f
            val eyeSpacing = headW * 0.35f

            val leftEyeRot = rotateY(-eyeSpacing, eyeY, eyeZ)
            val rightEyeRot = rotateY(eyeSpacing, eyeY, eyeZ)

            if (leftEyeRot.third > 0) {
                val (eyeP, f) = project3D(leftEyeRot.first, leftEyeRot.second, leftEyeRot.third)
                val eyeR = 6.5f * f
                drawCircle(Color(0xFFE0F2FE), eyeR, eyeP)
                drawCircle(Color(0xFF00E5FF), eyeR * 0.65f, eyeP)
                drawCircle(Color.White, eyeR * 0.28f, Offset(eyeP.x - eyeR * 0.25f, eyeP.y - eyeR * 0.25f))
            }
            if (rightEyeRot.third > 0) {
                val (eyeP, f) = project3D(rightEyeRot.first, rightEyeRot.second, rightEyeRot.third)
                val eyeR = 6.5f * f
                drawCircle(Color(0xFFE0F2FE), eyeR, eyeP)
                drawCircle(Color(0xFF00E5FF), eyeR * 0.65f, eyeP)
                drawCircle(Color.White, eyeR * 0.28f, Offset(eyeP.x - eyeR * 0.25f, eyeP.y - eyeR * 0.25f))
            }

            // F. Sacred Third Eye / Cyan Celestial Core
            val coreRot = rotateY(0f, -headH * 0.62f, headD * 0.55f)
            if (coreRot.third > 0) {
                val (coreP, f) = project3D(coreRot.first, coreRot.second, coreRot.third)
                val coreRadius = (8.5f + context.audioAmplitude * 12f) * f
                drawCircle(Color(0x8800E5FF), coreRadius * 1.8f, coreP)
                drawCircle(Color(0xFF38BDF8), coreRadius, coreP)
                drawCircle(Color.White, coreRadius * 0.45f, coreP)
            }

            // G. 3D Diamond Crown Halo (Front-head pass: z >= 0)
            for (i in 0 until haloPointsCount) {
                val phi = (2 * PI * i / haloPointsCount + haloRot).toFloat()
                val rx = haloRadius * cos(phi)
                val ry = haloRadius * sin(phi) * sin(haloTiltX) - scale * 0.55f
                val rz = haloRadius * sin(phi) * cos(haloTiltX)

                if (rz >= 0) { // Render in front of avatar
                    val (proj, f) = project3D(rx, ry, rz)
                    val diamondSize = (9.5f + if (i % 2 == 0) 3.5f else 0f) * f
                    val alpha = (0.55f + 0.45f * (rz / haloRadius)).coerceIn(0.5f, 1f)

                    // Glow halo node
                    drawCircle(
                        color = Color(0xFF00E5FF).copy(alpha = alpha * 0.85f),
                        radius = diamondSize * 1.6f,
                        center = proj
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = alpha),
                        radius = diamondSize,
                        center = proj
                    )
                }
            }

            // H. 3D Concentric Orbiting Ribbons
            val ribbonPoints = 36
            val ribbonRadius = scale * 1.25f
            val ribbonTilt1 = 0.65f

            for (pass in 0..1) { // 0 = back (z < 0), 1 = front (z >= 0)
                for (i in 0 until ribbonPoints) {
                    val angle1 = (2 * PI * i / ribbonPoints + haloRot * 1.5f).toFloat()
                    val pbx = ribbonRadius * cos(angle1)
                    val pby = ribbonRadius * sin(angle1) * sin(ribbonTilt1) + scale * 0.15f
                    val pbz = ribbonRadius * sin(angle1) * cos(ribbonTilt1)

                    if ((pass == 0 && pbz < 0) || (pass == 1 && pbz >= 0)) {
                        val (p, f) = project3D(pbx, pby, pbz)
                        val pSize = (3.2f + 1.8f * (pbz / ribbonRadius)) * f
                        val col = if (i % 3 == 0) Color(0xFF00E5FF) else Color(0xFFA855F7)
                        drawCircle(col.copy(alpha = 0.75f), pSize, p)
                    }
                }
            }
        }
    }
}
