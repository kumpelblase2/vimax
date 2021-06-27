package de.eternalwings.vima.plugin

import java.io.Reader
import javax.script.ScriptEngineManager

class ScriptExecutor {

    private val engine = ScriptEngineManager().getEngineByExtension("kts")!!

    fun execute(reader: Reader) {
        engine.eval(reader)
    }

}
