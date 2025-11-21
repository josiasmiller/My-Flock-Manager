package com.weyr_associates.animaltrakkerfarmmobile.app.core

import android.content.Context
import android.text.Annotation
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannedString
import android.text.style.BackgroundColorSpan
import androidx.annotation.StringRes
import androidx.core.text.getSpans
import com.weyr_associates.animaltrakkerfarmmobile.R
import com.weyr_associates.animaltrakkerfarmmobile.app.core.kt.takeAs

fun Context.getAnnotatedText(@StringRes textResId: Int): CharSequence {
    return AnnotatedText.applyAnnotations(this, getText(textResId))
}

object AnnotatedText {

    private const val BACKGROUND_COLOR_WARNING = "warning"

    private const val ANNOTATION_KEY_BACKGROUND_COLOR = "backgroundColor"

    private val SUPPORTED_ANNOTATIONS = setOf(
        ANNOTATION_KEY_BACKGROUND_COLOR
    )

    fun applyAnnotations(context: Context, text: CharSequence): CharSequence {
        val spannableString = text.takeAs<SpannedString>()
            ?.let { SpannableString(it) } ?: return text
        spannableString.getSpans<Annotation>()
            .filter { it.key in SUPPORTED_ANNOTATIONS }
            .forEach { annotation ->
                val spanStart = spannableString.getSpanStart(annotation)
                val spanEnd = spannableString.getSpanEnd(annotation)
                val value = annotation.value
                when (annotation.key) {
                    ANNOTATION_KEY_BACKGROUND_COLOR -> applyBackgroundColor(
                        context,
                        spannableString,
                        spanStart,
                        spanEnd,
                        value
                    )
                    else -> Unit
                }
            }
        return spannableString
    }

    private fun applyBackgroundColor(
        context: Context,
        spannableString: SpannableString,
        spanStart: Int,
        spanEnd: Int,
        value: String
    ) {
        mapAnnotationToBackgroundColorRes(value)?.let { colorResId ->
            spannableString.setSpan(
                BackgroundColorSpan(context.getColor(colorResId)),
                spanStart,
                spanEnd,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    private fun mapAnnotationToBackgroundColorRes(value: String) = when (value) {
        BACKGROUND_COLOR_WARNING -> R.color.status_error
        else -> null
    }
}
