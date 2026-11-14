package com.exanthiax.ecobattlepass.config

import wiki.ecohub.config.ConfigType
import wiki.ecohub.config.file.BaseConfig
import wiki.ecohub.core.plugin.EcoPlugin

class CategoriesYml(plugin: EcoPlugin) : BaseConfig("categories", plugin, false, ConfigType.YAML)
