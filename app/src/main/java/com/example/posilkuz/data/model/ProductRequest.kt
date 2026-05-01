package com.example.posilkuz.data.model

    data class ProductRequest(
        val id: String = "",
        val name: String = "",
        val barcode: String = "",
        val requestedBy: String = "",
        val status: String = "pending"
    )
