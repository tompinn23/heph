package com.tompinn23.hephaestus.block;

import com.tompinn23.euthenia.lib.block.IBlock;
import com.tompinn23.euthenia.lib.block.SimpleBlock;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.util.stream.Stream;

import static com.tompinn23.hephaestus.Hephaestus.MODID;

public class Blocks {

    private static final BlockBehaviour.Properties SIMPLE_PROPS = BlockBehaviour.Properties.of(Material.STONE);

    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);

    public static final RegistryObject<Block> SIMPLE = BLOCKS.register("symple", () -> new SimpleBlock(SIMPLE_PROPS));
    public static final RegistryObject<Block> CABLE = BLOCKS.register("cable", () -> new CableBlock(SIMPLE_PROPS));

    public static Stream<RegistryObject<Block>> blocks() {
        return BLOCKS.getEntries().stream();
    }

    public static void init(IEventBus bus) {
        BLOCKS.register(bus);
        bus.addListener((RegisterEvent event) -> {
            event.register(ForgeRegistries.Keys.ITEMS, helper -> {
                for (var block : ForgeRegistries.BLOCKS.getEntries()) {
                    if (block.getValue() instanceof IBlock<?, ?> iBlock) {
                        var blockItem = iBlock.getBlockItem(new Item.Properties());
                        helper.register(block.getKey().location(), blockItem);
//                    if(!event.getRegistry().containsKey(block.getRegistryName())) {
//                        Balnor.LOGGER.debug("Creating automatic ItemBlock for {}", block.getRegistryName());
//                        blockItem.setRegistryName(block.getRegistryName());
//                        event.getRegistry().register(blockItem);
//                    }
                    }
                }
            });
        });
    }
}
