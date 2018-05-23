package br.com.medium.takeaphoto

import android.app.Application
import com.facebook.drawee.backends.pipeline.Fresco

/**
 * Classe responsável pela inicialização da lib Fresco.
 *
 * @author Augusto Santos
 * @version 1.0
 */
class Application : Application() {

    companion object {
        lateinit var instance: br.com.medium.takeaphoto.Application
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        Fresco.initialize(this)
    }
}