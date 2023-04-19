package com.tompinn23.hephaestus.tile;

import com.tompinn23.hephaestus.Hephaestus;
import com.tompinn23.hephaestus.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class Tiles {
    private static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Hephaestus.MODID);

    public static final RegistryObject<BlockEntityType<CableTile>> CABLE = TILES.register("cable", () -> BlockEntityType.Builder.of(CableTile::new, Blocks.CABLE.get()).build(null));

    public static void init(IEventBus bus) {
        TILES.register(bus);
    }
}
