package com.pontimovil.hypo.modelo

class Rollo(val id : Long, val size : Int, val Usuarios : List<String>, val fotos : List<Foto>) {
    companion object{
        fun createMockRoll(): Rollo{
            val fotos = Foto.createMockOPhotos()
            val Usuarios = listOf<String>("pruebasHypo")
            return Rollo(1,10,Usuarios,fotos)
        }
    }
}