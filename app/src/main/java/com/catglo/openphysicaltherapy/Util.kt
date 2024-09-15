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
