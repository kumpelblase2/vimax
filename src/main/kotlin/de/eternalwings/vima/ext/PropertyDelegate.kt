package de.eternalwings.vima.ext

import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.javaField

class PropertyDelegate<R>(private val prop: KProperty<R>) {
    private fun ensureDifference(prop1: KProperty<*>, prop2: KProperty<*>) {
        if (prop1.javaField == prop2.javaField) throw IllegalArgumentException(
                "Can't delegate to same property.")
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): R {
        ensureDifference(prop, property)
        return prop.getter.call()
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: R) {
        ensureDifference(prop, property)
        return (prop as? KMutableProperty<R>)?.setter?.call(value)
                ?: throw IllegalArgumentException("Cannot set val property")
    }
}
