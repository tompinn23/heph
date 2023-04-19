package com.tompinn23.euthenia.lib.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import javax.annotation.Nullable;

public class Network {
    private final ResourceLocation location;
    private final SimpleChannel channel;
    private int id;

    public Network(String id) {
        this.location = new ResourceLocation(id, "main");
        this.channel = NetworkRegistry.ChannelBuilder.named(this.location)
                .clientAcceptedVersions("1"::equals)
                .serverAcceptedVersions("1"::equals)
                .networkProtocolVersion(() -> "1")
                .simpleChannel();
    }

    @SuppressWarnings("unchecked")
    public <T> void register(IPacket<T> message) {
        this.channel.registerMessage(this.id++, (Class<T>) message.getClass(), message::encode, message::decode, message::handle);
    }

    @OnlyIn(Dist.CLIENT)
    public <T> void toServer(T message) {
        this.channel.sendToServer(message);
    }

    public <T> void toAll(T message) { this.channel.send(PacketDistributor.ALL.noArg(), message); }

    public <T> void toClient(T message, Player player) {
        if(player instanceof ServerPlayer serverPlayer)
            this.channel.send(PacketDistributor.PLAYER.with(() -> serverPlayer), message);
    }

    public <T> void toClientsAround(T message, @Nullable LevelAccessor world, BlockPos position) {
        if(world instanceof ServerLevel serverLevel)
            toClientsAround(message, serverLevel, position);
    }

    public <T> void toClientsAround(T message, ServerLevel world, BlockPos position) {
        LevelChunk chunk = world.getChunkAt(position);
        this.channel.send(PacketDistributor.TRACKING_CHUNK.with(() -> chunk), message);
    }

    public <T> void toTracking(T msg, Entity entity) {
        this.channel.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), msg);
    }

    public <T> void toTrackingAndSelf(T msg, Entity entity) {
        this.channel.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), msg);
    }

}
