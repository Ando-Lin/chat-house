package com.ando.chathouse.domain.pojo

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlin.reflect.KProperty

open class Container<T>(open var value:T)

operator fun <T> Container<T>.getValue(thisObj: Any?, property: KProperty<*>):T = value
operator fun <T> Container<T>.setValue(thisObj: Any?, property: KProperty<*>, value: T){
    this.value = value
}

@Parcelize
class IntContainer(var value: Int):Parcelable
