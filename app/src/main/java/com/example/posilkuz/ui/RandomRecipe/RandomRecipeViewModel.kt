package com.example.posilkuz.ui.RandomRecipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.posilkuz.data.model.Recipe
import com.example.posilkuz.data.repository.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RandomRecipeViewModel(private val recipieRepository: RecipeRepository = RecipeRepository()) : ViewModel() {
    private val _recipe = MutableStateFlow<Recipe?>(null)
    val recipe: StateFlow<Recipe?> = _recipe.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _offsetX = MutableStateFlow(0f)
    val offsetX: StateFlow<Float> = _offsetX.asStateFlow()

    private val _offsetY = MutableStateFlow(0f)
    val offsetY: StateFlow<Float> = _offsetY.asStateFlow()

    var velocityX = 0f
    var velocityY = 0f
    var accelX = 0f
    var accelY = 0f

    fun onAcceleration(x: Float, y: Float) {
        if (_recipe.value != null || _isLoading.value) return
        accelX = x
        accelY = y
    }

    fun updatePhysics(maxX: Float, maxY: Float) {
        if (_recipe.value != null || _isLoading.value) return

        velocityX += accelX * 2f
        velocityY -= accelY * 2f

        velocityX *= 0.9f
        velocityY *= 0.9f

        var newX = _offsetX.value + velocityX
        var newY = _offsetY.value + velocityY

        if (newX > maxX) {
            newX = maxX
            velocityX = -velocityX * 0.7f
        }
        if (newX < -maxX) {
            newX = -maxX
            velocityX = -velocityX * 0.7f
        }
        if (newY > maxY) {
            newY = maxY
            velocityY = -velocityY * 0.7f
        }
        if (newY < -maxY) {
            newY = -maxY
            velocityY = -velocityY * 0.7f
        }

        _offsetX.value = newX
        _offsetY.value = newY
    }

    fun onShake() {
        if (_isLoading.value || _recipe.value != null) return
        viewModelScope.launch {
            _isLoading.value = true
            val all = recipieRepository.getAllRecipes()
            _recipe.value = all.randomOrNull()
            _isLoading.value = false
        }
    }

    fun dismissRecipe() {
        _recipe.value = null
    }
}