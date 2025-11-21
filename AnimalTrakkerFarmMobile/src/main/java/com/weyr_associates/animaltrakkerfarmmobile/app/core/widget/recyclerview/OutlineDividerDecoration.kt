package com.weyr_associates.animaltrakkerfarmmobile.app.core.widget.recyclerview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.weyr_associates.animaltrakkerfarmmobile.R

class OutlineDividerDecoration(
    context: Context,
    private val insetDimenRes: Int = R.dimen.interior_spacing_margin,
    private val sectionPositionProvider: SectionPositionProvider? = null
) : RecyclerView.ItemDecoration() {

    enum class SectionPosition {
        SOLITARY,
        BEGINNING,
        MIDDLE,
        END
    }

    interface SectionPositionProvider {
        fun positionToSectionPosition(position: Int): SectionPosition
    }

    private val inset = context.resources.getDimensionPixelOffset(insetDimenRes)
    private val insetInterior = inset / 2
    private val insetPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.background_widget)
    }
    private val childBounds = Rect()
    private val decorationRect = RectF()

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val itemPosition = parent.getChildAdapterPosition(view)
        val itemCount = requireNotNull(parent.adapter).itemCount

        val sectionPos = sectionPositionProvider?.positionToSectionPosition(itemPosition)
            ?: SectionPosition.SOLITARY

        when (itemPosition) {
            0 -> {
                val bottomInset = when (sectionPos) {
                    SectionPosition.BEGINNING -> 0
                    else -> insetInterior
                }
                outRect.set(inset, inset, inset, bottomInset)
            }
            itemCount - 1 -> {
                val topInset = when (sectionPos) {
                    SectionPosition.END -> 0
                    else -> insetInterior
                }
                outRect.set(inset, topInset, inset, inset)
            }
            else -> {
                val topInset = when (sectionPos) {
                    SectionPosition.BEGINNING,
                    SectionPosition.SOLITARY -> insetInterior
                    SectionPosition.MIDDLE,
                    SectionPosition.END -> 0
                }
                val bottomInset = when (sectionPos) {
                    SectionPosition.BEGINNING,
                    SectionPosition.MIDDLE -> 0
                    SectionPosition.END,
                    SectionPosition.SOLITARY -> insetInterior
                }
                outRect.set(inset, topInset, inset, bottomInset)
            }
        }
    }

    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        canvas.save()

        var left = 0
        var right = parent.width
        var top = 0
        var bottom = parent.height

        if (parent.clipToPadding) {
            left = parent.paddingLeft
            right = parent.width - parent.paddingRight
            top = parent.paddingTop
            bottom = parent.height - parent.paddingBottom
            canvas.clipRect(left, top, right, bottom)
        }

        val childCount = parent.childCount
        for (index: Int in 0 until childCount) {
            val child = parent.getChildAt(index)
            val itemPosition = parent.getChildAdapterPosition(child)
            val sectionPosition = sectionPositionProvider?.positionToSectionPosition(itemPosition)
                ?: SectionPosition.SOLITARY
            parent.getDecoratedBoundsWithMargins(child, childBounds)
            val translationX = child.translationX
            val translationY = child.translationY

            //Draw left decoration
            decorationRect.set(
                left.toFloat(),
                childBounds.top + translationY,
                childBounds.left + translationX + inset,
                childBounds.bottom + translationY
            )
            drawCurrentDecoration(canvas)

            //Draw right decoration
            decorationRect.set(
                childBounds.right + translationX - inset,
                childBounds.top + translationY,
                right.toFloat(),
                childBounds.bottom + translationY
            )
            drawCurrentDecoration(canvas)

            //Draw top decoration
            when (itemPosition) {
                0 -> decorationRect.set(
                    left.toFloat(),
                    top.toFloat(),
                    right.toFloat(),
                    childBounds.top + translationY + inset
                )
                else -> decorationRect.set(
                    left.toFloat(),
                    childBounds.top + translationY,
                    right.toFloat(),
                    childBounds.top + translationY + when (sectionPosition) {
                        SectionPosition.BEGINNING,
                        SectionPosition.SOLITARY -> insetInterior
                        else -> 0
                    }
                )
            }
            drawCurrentDecoration(canvas)

            //Draw bottom decoration
            when (itemPosition) {
                state.itemCount - 1 -> decorationRect.set(
                    left.toFloat(),
                    childBounds.bottom + translationY - inset,
                    right.toFloat(),
                    bottom.toFloat()
                )
                else -> decorationRect.set(
                    left.toFloat(),
                    childBounds.bottom + translationY - when (sectionPosition) {
                        SectionPosition.END,
                        SectionPosition.SOLITARY -> insetInterior
                        else -> 0
                    },
                    right.toFloat(),
                    childBounds.bottom + translationY
                )
            }
            drawCurrentDecoration(canvas)
        }
        canvas.restore()
    }

    private fun drawCurrentDecoration(canvas: Canvas) {
        canvas.drawRect(
            decorationRect.left,
            decorationRect.top,
            decorationRect.right,
            decorationRect.bottom,
            insetPaint
        )
    }
}
