package de.eternalwings.vima.controller

import de.eternalwings.vima.domain.PluginInformation
import de.eternalwings.vima.plugin.PluginManager
import de.eternalwings.vima.repository.PluginInformationRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.transaction.Transactional

@RestController
@RequestMapping("/api/plugin")
class PluginController(private val pluginRepository: PluginInformationRepository, private val pluginManager: PluginManager) {

    @GetMapping
    fun getAllPlugins(): List<PluginInformation> {
        return pluginRepository.findAll()
    }

    @Transactional
    @PostMapping("/{name}/disable")
    fun disablePlugin(@PathVariable name: String): PluginInformation? {
        pluginManager.disablePlugin(name)
        return pluginRepository.findByName(name)
    }

    @Transactional
    @PostMapping("{name}/enable")
    fun enablePlugin(@PathVariable name: String): PluginInformation? {
        pluginManager.enablePlugin(name)
        return pluginRepository.findByName(name)
    }

}
