package de.eternalwings.vima.controller

import de.eternalwings.vima.domain.PluginInformation
import de.eternalwings.vima.plugin.PluginManager
import de.eternalwings.vima.repository.PluginInformationRepository
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/plugin")
class PluginController(
    private val pluginRepository: PluginInformationRepository,
    private val pluginManager: PluginManager,
    private val transactionTemplate: TransactionTemplate
) {

    @GetMapping
    fun getAllPlugins(): List<PluginInformation> {
        return pluginRepository.findAll()
    }

    @PostMapping("/{name}/disable")
    fun disablePlugin(@PathVariable name: String): PluginInformation? {
        transactionTemplate.execute {
            pluginManager.disablePlugin(name)
        }
        return pluginRepository.findByName(name)
    }

    @PostMapping("/{name}/enable")
    fun enablePlugin(@PathVariable name: String): PluginInformation? {
        transactionTemplate.execute {
            pluginManager.enablePlugin(name)
        }
        return pluginRepository.findByName(name)
    }

    @PostMapping("/{name}/refresh")
    fun refreshPlugin(@PathVariable name: String) {
        pluginManager.refreshPlugin(name)
    }

    @PostMapping("{name}/reload")
    fun reloadPlugin(@PathVariable name: String): PluginInformation? {
        pluginManager.reloadPlugin(name)
        return pluginRepository.findByName(name)
    }
}
