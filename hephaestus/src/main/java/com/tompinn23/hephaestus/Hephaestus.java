package com.tompinn23.hephaestus;

import com.mojang.logging.LogUtils;
import com.tompinn23.hephaestus.config.Config;
import com.tompinn23.hephaestus.setup.Setup;
import me.shedaniel.autoconfig.ConfigHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Hephaestus.MODID)
public class Hephaestus
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "hephaestus";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    private static final ConfigHolder<Config> CONFIG = Config.register();

    public static ResourceLocation ID(String id) {
        return new ResourceLocation(MODID, id);
    }
    public static Config getConfig() { return CONFIG.getConfig(); }

    public Hephaestus() {
        Setup.setup();
    }

}
