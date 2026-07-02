package com.example.uniklplanner

import android.content.Context
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

/**
 * Loads the trained neural network (course_recommender.tflite from assets) and
 * runs on-device inference to score how suitable each candidate course is.
 *
 * The 8 input features (must EXACTLY match the training script order):
 *   0 prereq_met          1.0 if prerequisites satisfied else 0.0
 *   1 semester_distance   |course_sem - current_sem| / 8  (electives -> 0.5)
 *   2 category_deficit    remaining credits in category / requirement (0..1)
 *   3 is_elective         1.0 / 0.0
 *   4 is_optional         1.0 / 0.0
 *   5 credits_norm        course credits / 6
 *   6 current_load_ratio  current-semester credits / 20
 *   7 credits_would_fit   1.0 if (load + credits) <= 20 else 0.0
 *
 * Output: a single suitability score in [0,1].
 */
class CourseRecommender private constructor(private val interpreter: Interpreter) {

    /** Run the NN on one feature vector and return the suitability score. */
    fun score(features: FloatArray): Float {
        require(features.size == 8) { "Expected 8 features, got ${features.size}" }
        // Input buffer: 1 row x 8 floats
        val input = ByteBuffer.allocateDirect(8 * 4).order(ByteOrder.nativeOrder())
        for (f in features) input.putFloat(f)
        input.rewind()
        // Output buffer: 1 row x 1 float
        val output = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder())
        interpreter.run(input, output)
        output.rewind()
        return output.float
    }

    companion object {
        private const val MODEL_FILE = "course_recommender.tflite"
        private const val CREDIT_CAP = 20

        /**
         * Try to load the model from assets. Returns null if anything fails
         * (so the caller can fall back to the rule-based recommender).
         */
        fun tryLoad(context: Context): CourseRecommender? {
            return try {
                val afd = context.assets.openFd(MODEL_FILE)
                val inputStream = afd.createInputStream()
                val channel = inputStream.channel
                val mapped = channel.map(
                    FileChannel.MapMode.READ_ONLY,
                    afd.startOffset,
                    afd.declaredLength
                )
                val options = Interpreter.Options().apply { setNumThreads(2) }
                android.util.Log.e("UNIKL_NN", "✅ TFLite model loaded successfully")
                CourseRecommender(Interpreter(mapped, options))
            } catch (e: Exception) {
                android.util.Log.e("UNIKL_NN", "❌ TFLite load FAILED: ${e.message}", e)
                null
            }
        }

        /**
         * Build the 8-feature vector for a candidate course, mirroring the
         * training script's feature extraction EXACTLY.
         */
        fun buildFeatures(
            course: Course,
            currentSemester: Int,
            prereqMet: Boolean,
            categoryDeficitCredits: Int,
            categoryRequirement: Int,
            currentLoadCredits: Int
        ): FloatArray {
            val prereq = if (prereqMet) 1f else 0f

            val isElective = course.category == "ELECTIVE"
            val semDistance = if (isElective) {
                0.5f
            } else {
                (Math.abs(course.semester - currentSemester).toFloat() / 8f).coerceIn(0f, 1f)
            }

            val catDeficit = if (categoryRequirement <= 0) 0f
            else (categoryDeficitCredits.toFloat() / categoryRequirement).coerceIn(0f, 1f)

            val isElec = if (isElective) 1f else 0f
            val isOpt = if (course.isOptional) 1f else 0f
            val creditsNorm = (course.credits.toFloat() / 6f).coerceIn(0f, 1f)
            val loadRatio = (currentLoadCredits.toFloat() / CREDIT_CAP).coerceIn(0f, 1f)
            val fits = if (currentLoadCredits + course.credits <= CREDIT_CAP) 1f else 0f

            return floatArrayOf(
                prereq, semDistance, catDeficit, isElec, isOpt, creditsNorm, loadRatio, fits
            )
        }
    }
}