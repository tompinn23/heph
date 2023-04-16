package com.tompinn23.hephaestus;

import com.mojang.logging.LogUtils;
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
    private static final Logger LOGGER = LogUtils.getLogger();

    public static ResourceLocation ID(String id) {
        return new ResourceLocation(MODID, id);
    }


    public Hephaestus() {
    }

}
