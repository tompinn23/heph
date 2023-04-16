package com.hephaestus.block;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static com.hephaestus.Hephaestus.MODID;

public class Blocks {

    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);

    //public static final RegistryObject<Block> SIMPLE = BLOCKS.register("symple", () -> new SimpleBlock());

    public static void init(IEventBus bus) {
        BLOCKS.register(bus);
    }
}
