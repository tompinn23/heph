package com.tompinn23.hephaestus.fabric;
import com.tompinn23.hephaestus.Hephaestus;
import net.fabricmc.api.ModInitializer;

public class HephaestusFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Hephaestus.init();
    }
}
