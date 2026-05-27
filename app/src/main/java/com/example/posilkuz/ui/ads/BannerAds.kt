package com.example.posilkuz.ui.ads

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

/**
 * Komponent wyświetlający standardowy baner reklamowy Google AdMob.
 *
 * Używa rozmiaru [AdSize.BANNER] (320×50 dp). W obecnej implementacji korzysta
 * z testowego identyfikatora jednostki reklamowej AdMob.
 *
 * @param modifier modyfikator Compose stosowany do kontenera banera
 */
@Composable
fun BannerAd(modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = "ca-app-pub-3940256099942544/6300978111"
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}

/**
 * Komponent wyświetlający duży baner reklamowy Google AdMob.
 *
 * Używa rozmiaru [AdSize.LARGE_BANNER] (320×100 dp). W obecnej implementacji korzysta
 * z testowego identyfikatora jednostki reklamowej AdMob.
 *
 * @param modifier modyfikator Compose stosowany do kontenera banera
 */
@Composable
fun LargeBannerAd(modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.LARGE_BANNER)
                adUnitId = "ca-app-pub-3940256099942544/6300978111"
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}
