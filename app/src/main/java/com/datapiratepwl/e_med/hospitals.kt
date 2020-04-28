package com.datapiratepwl.e_med



class hospitals {

    var huic : String? =null


    constructor() {

    }

    constructor(huic: String?) {
        this.huic = huic
    }

    fun get_huic():String? = huic

}
