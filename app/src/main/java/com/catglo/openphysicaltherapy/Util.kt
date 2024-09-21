package com.catglo.openphysicaltherapy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.util.Locale
import java.util.concurrent.TimeUnit

fun String.toValidFileName():String{
    return replace(Regex("[\\\\/:\",.|\\s]+"),"_")
}

inline fun <VM : ViewModel> viewModelFactory(crossinline f: () -> VM) =
    object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>):T = f() as T
    }


fun Int.secondsToInterval(): String {
    val hr: Long = TimeUnit.SECONDS.toHours(this.toLong())
    val min: Long = TimeUnit.SECONDS.toMinutes(this - TimeUnit.HOURS.toSeconds(hr))
    val sec: Long = this - TimeUnit.HOURS.toSeconds(hr) - TimeUnit.MINUTES.toSeconds(min)
    if (hr == 0L && min == 0L){
        return String.format(Locale.ROOT, "%2ds", sec)
    }
    if (hr == 0L){
        return String.format(Locale.ROOT, "%2dm %2ds", min, sec)
    }
    return String.format(Locale.ROOT, "%02d:%02d:%02d", hr, min, sec)
}

fun findNextNameNumber(baseNameParam:String, size:()->Int, name:(index:Int)->String):String{
    var baseName = baseNameParam
    val findNameNumber = Regex("\\((\\d+)\\)$")
    findNameNumber.find(baseName)?.groups?.get(1)?.range?.start?.let {
        baseName = baseName.substring(0,it-1).trim() //TODO: Use trimCounter extension
    }
    var isDuplicate = false
    var count = 0
    for (i in 0..<size()){
        val thisName = name(i)
        if (thisName == baseName){
            isDuplicate = true
        }
        else if (thisName.startsWith(baseName)){
            val nameCounter = findNameNumber.find(thisName)?.groups?.get(1)?.value?.toIntOrNull()
            if (nameCounter!=null){
                if (nameCounter > count) {
                    count = nameCounter
                }
            }
        }
    }
    if (count == 0 && isDuplicate){
        return "$baseName(2)"
    }
    if (count > 0){
        return "$baseName(${count+1})"
    }
    return baseName
}