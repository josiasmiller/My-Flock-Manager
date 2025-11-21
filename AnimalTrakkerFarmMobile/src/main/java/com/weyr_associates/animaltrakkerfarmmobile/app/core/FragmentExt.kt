package com.weyr_associates.animaltrakkerfarmmobile.app.core

import android.view.View
import androidx.fragment.app.Fragment
import com.weyr_associates.animaltrakkerfarmmobile.R
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

fun Fragment.hideKeyboard() {
    requireActivity().hideKeyboard()
}

fun Fragment.checkDatabaseValidityThen(onValid: () -> Unit) {
    requireActivity().checkDatabaseValidityThen(onValid)
}

inline fun <reified B> viewBinding(): ReadOnlyProperty<Fragment, B> {
    val bindMethod = B::class.java.getMethod("bind", View::class.java)
    return ViewBindingPropertyDelegate(B::class.java) {
        bindMethod.invoke(null, it) as B
    }
}

class ViewBindingPropertyDelegate<B>(private val clazz: Class<B>, private val bindingFactory: (View) -> B) : ReadOnlyProperty<Fragment, B> {
    override fun getValue(thisRef: Fragment, property: KProperty<*>): B {
        val view = thisRef.view ?: throw IllegalStateException("Fragment does not currently have a view.")
        var binding = view.getTag(R.id.tag_view_binding)
        if (binding == null) {
            binding = bindingFactory.invoke(view)
            view.setTag(R.id.tag_view_binding, binding)
        } else if (!clazz.isInstance(binding)) {
            throw IllegalStateException("Fragment view already has a view binding that is not of type ${clazz.simpleName}")
        }
        return requireNotNull(clazz.cast(binding))
    }
}
