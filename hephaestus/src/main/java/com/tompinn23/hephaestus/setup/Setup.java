package com.tompinn23.hephaestus.setup;

import com.tompinn23.hephaestus.Hephaestus;
import com.tompinn23.hephaestus.block.Blocks;
import com.tompinn23.hephaestus.tile.Tiles;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = Hephaestus.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Setup {

    public static final String TAB_NAME = "hephaestus";

    @SubscribeEvent
    public static void onCreativeTab(CreativeModeTabEvent.Register event) {
        event.registerCreativeModeTab(Hephaestus.ID(TAB_NAME), builder -> {
            builder
                    .title(Component.translatable("item_group." + Hephaestus.MODID + ".tab"))
                    .icon(() -> new ItemStack(Items.DIAMOND))
                    .displayItems((enabledFeatures, output, tab) -> {
                        Blocks.blocks().map(RegistryObject::get).forEach(output::accept);
                    }).build();
        });
    }

    public static void setup() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        Blocks.init(bus);
        Tiles.init(bus);
    }

    public static void init(FMLCommonSetupEvent event) {

    }
}
