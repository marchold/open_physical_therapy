package com.catglo.openphysicaltherapy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.util.Locale
import java.util.concurrent.TimeUnit


inline fun <VM : ViewModel> viewModelFactory(crossinline f: () -> VM) =
    object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>):T = f() as T
    }


fun Int.secondsToInterval(): String {
    val hr: Long = TimeUnit.SECONDS.toHours(this.toLong())
    val min: Long = TimeUnit.SECONDS.toMinutes(this - TimeUnit.HOURS.toSeconds(hr))
    val sec: Long = this - TimeUnit.HOURS.toSeconds(hr) - TimeUnit.MINUTES.toSeconds(min)
    return String.format(Locale.ROOT, "%02d:%02d:%02d", hr, min, sec)
}
