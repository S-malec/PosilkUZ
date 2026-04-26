package com.example.posilkuz.ui.RandomRecipe

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.posilkuz.ui.recipe.RecipeCard
import kotlinx.coroutines.delay
import kotlin.math.sqrt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RandomRecipeScreen(
    onBack: () -> Unit,
    viewModel: RandomRecipeViewModel = viewModel()
) {
    val context = LocalContext.current
    val recipe by viewModel.recipe.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val offsetX by viewModel.offsetX.collectAsState()
    val offsetY by viewModel.offsetY.collectAsState()

    val density = LocalDensity.current
    val configuration = LocalConfiguration.current

    val maxX = with(density) {
        (configuration.screenWidthDp / 2 - 50).dp.toPx()
    }
    val maxY = with(density) {
        (configuration.screenHeightDp / 2 - 100).dp.toPx()
    }

    val sensorManager = remember {
        context.getSystemService(SensorManager::class.java)
    }

    val accelerometr = remember {
        sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }


    DisposableEffect(Unit) {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if(event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]

                    val acceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()

                    viewModel.onAcceleration(x, y)

                    if(acceleration > 20 && recipe == null && !isLoading) {
                        viewModel.onShake()
                    }
                }
            }


            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
            }
        accelerometr?.let {
            sensorManager?.registerListener(
                listener,
                it,
                SensorManager.SENSOR_DELAY_UI
            )
        }

        onDispose {
            sensorManager?.unregisterListener(listener)
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            viewModel.updatePhysics(maxX, maxY)
            delay(16L)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Losuj przepis") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Wróć")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> CircularProgressIndicator()
                recipe != null -> Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    RecipeCard(
                        recipe = recipe!!,
                        userPantryIds = emptySet()
                    )
                    Button(
                        onClick = { viewModel.dismissRecipe() },
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text("Losuj ponownie")
                    }
                }
                else -> Box(Modifier.fillMaxSize()) {
                    Text(
                        text = "Potrząścnij telefonem!",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.align(Alignment.Center)
                    )
                    Text(
                        text = "🍎",
                        fontSize = 64.sp,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset {
                                IntOffset(offsetX.toInt(), offsetY.toInt())
                            }
                            .padding(top = 120.dp)
                    )
                }
            }
        }
    }
}