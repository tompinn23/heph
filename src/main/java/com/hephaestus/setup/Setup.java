package com.hephaestus.setup;

import com.hephaestus.Hephaestus;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(modid = Hephaestus.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Setup {

    public static final String TAB_NAME = "hephaestus";

    @SubscribeEvent
    public static void onCreativeTab(CreativeModeTabEvent.Register event) {
        event.registerCreativeModeTab(Hephaestus.ID(TAB_NAME), builder -> {
            builder
                    .title(Component.translatable("item_group." + Hephaestus.MODID + ".tab"))
                    .icon(() -> new ItemStack(Items.DIAMOND));
        });
    }

    public static void setup() {
        IEventBus bus = MinecraftForge.EVENT_BUS;

    }

    public static void init(FMLCommonSetupEvent event) {

    }
}
