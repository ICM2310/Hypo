package com.pontimovil.hypo.modelo

import com.google.android.gms.maps.model.LatLng

class Foto(val id: Long, val usuario: String, val id_rollo: Long, val location : LatLng ) {

    companion object{
        fun createMockOPhotos(): List<Foto>{
            val rollo = mutableListOf<Foto>()
            rollo.add(Foto(1,"PruebasHypo",1, LatLng(4.633326770923607, -74.06774218958724)))
            //rollo.add(Foto(2,"PruebasHypo",1, LatLng(4.63988327896042, -74.06596357933073)))
            //rollo.add(Foto(3,"PruebasHypo",1, LatLng(4.645905082380165, -74.06184172051434)))
            rollo.add(Foto(4,"PruebasHypo",1, LatLng(4.658178664810315, -74.05544630293485)))
            return rollo
        }
    }
}