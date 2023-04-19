package com.tompinn23.euthenia.lib.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public interface IPacket<T> {
    void encode(T msg, FriendlyByteBuf buf);
    T decode(FriendlyByteBuf buf);
    void handle(T msg, Supplier<NetworkEvent.Context> ctx);
}
