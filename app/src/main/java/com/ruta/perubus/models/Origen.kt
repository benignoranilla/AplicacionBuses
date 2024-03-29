package com.ruta.perubus.models

import com.google.gson.annotations.SerializedName

data class Origen(
    @SerializedName("codigoOrigen") var codigoOrigen: String,
    @SerializedName("descripcionOrigen") var descripcionOrigen: String
) {

    override fun toString(): String {
        return descripcionOrigen
    }

}