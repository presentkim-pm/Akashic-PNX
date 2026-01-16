package kim.present.pnx.akashic

import cn.nukkit.Server
import cn.nukkit.lang.PluginI18n
import cn.nukkit.lang.PluginI18nManager
import cn.nukkit.plugin.PluginBase

class Akashic : PluginBase() {
    companion object {
        lateinit var I18N: PluginI18n

        fun i18n(key: String, vararg args: Any?): String {
            return I18N.tr(Server.getInstance().languageCode, key, *args)
        }
    }

    override fun onLoad() {
        I18N = PluginI18nManager.register(this);
    }

    override fun onEnable() {
        logger.info(i18n("template.on_enable", Server.getInstance().version))
    }

}