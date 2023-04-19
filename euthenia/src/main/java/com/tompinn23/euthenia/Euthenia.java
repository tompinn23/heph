package com.tompinn23.euthenia;

import com.mojang.logging.LogUtils;
import com.tompinn23.euthenia.lib.network.Network;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Euthenia.MODID)
public class Euthenia
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "euthenia";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final Network NET = new Network(MODID);

    public static @NotNull ResourceLocation ID(String id) {
        return new ResourceLocation(MODID, id);
    }


    public Euthenia() {
    }

}
