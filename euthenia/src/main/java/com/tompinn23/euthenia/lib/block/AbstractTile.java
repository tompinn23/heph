package com.tompinn23.euthenia.lib.block;

import com.tompinn23.euthenia.lib.logistics.fluid.ITankHolder;
import com.tompinn23.euthenia.lib.logistics.inventory.IInventoryHolder;
import com.tompinn23.euthenia.lib.logistics.inventory.Inventory;
import com.tompinn23.euthenia.lib.logistics.Redstone;
import com.tompinn23.euthenia.lib.logistics.fluid.Tank;
import com.tompinn23.euthenia.lib.registry.IVariant;
import com.tompinn23.euthenia.lib.util.NBT;
import com.tompinn23.euthenia.lib.util.Stack;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class AbstractTile<V extends IVariant, B extends AbstractBlock<V, B>> extends BlockEntity implements IBlockEntity {


    protected final Inventory inv = Inventory.createBlank();
    protected final Tank tank = new Tank(0);

    protected Redstone redstone = Redstone.IGNORE;

    protected V variant;
    protected boolean isContainerOpen;

    public AbstractTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        this(type, pos, state, IVariant.EMPTY());

    }

    public AbstractTile(BlockEntityType<?> type, BlockPos pos, BlockState state, V variant) {
        super(type, pos, state);
        this.variant = variant;
        if (this instanceof IInventoryHolder) {
            this.inv.setTile((IInventoryHolder) this);
        }
    }

    @SuppressWarnings("deprecation")
    public void setChangedFast() {
        if(level != null) {
            if(level.hasChunkAt(worldPosition)) {
                level.getChunkAt(worldPosition).setUnsaved(true);
            }
        }
    }

    public B getBlock() { return (B) getBlockState().getBlock(); }

    public V getVariant() { return this.variant; }


    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        readSync(tag);
        if(!tag.contains("#c"))
            loadServerOnly(tag);
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        writeSync(tag);
        saveServerOnly(tag);
    }

    @Override
    public CompoundTag getUpdateTag() {
        var tag = saveWithoutMetadata();
        tag.putBoolean("#c", true);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    protected void loadServerOnly(CompoundTag tag) {
    }

    protected CompoundTag saveServerOnly(CompoundTag compound) {
        return compound;
    }

    protected void readSync(CompoundTag tag) {
        if(!this.variant.isEmpty() && tag.contains("variant", Tag.TAG_INT)) {
            this.variant = (V) this.variant.read(tag, "variant");
        }
        if(this instanceof IInventoryHolder && !keepInventory()) {
            this.inv.load(tag);
        }
        if (this instanceof ITankHolder tankHolder) {
            if (!tankHolder.keepFluid()) {
                this.tank.load(tag, "fluid0", true);
            }
        }
        this.redstone = Redstone.values()[tag.getInt("redstone_mode")];
        readStorable(tag);
    }

    protected CompoundTag writeSync(CompoundTag tag) {
        if(!this.variant.isEmpty()) {
            tag.putInt("variant", this.variant.ordinal());
        }
        if (this instanceof IInventoryHolder && !keepInventory()) {
            tag.merge(this.inv.save());
        }
        if (this instanceof ITankHolder tankHolder) {
            if (!tankHolder.keepFluid()) {
                this.tank.save(tag, "fluid0", true);
            }
        }
        tag.putInt("redstone_mode", this.redstone.ordinal());
        return writeStorable(tag);
    }

    protected void readStorable(CompoundTag tag) {
        if(this instanceof IInventoryHolder && keepInventory()) {
            this.inv.load(tag);
        }
        if(this instanceof ITankHolder tankHolder) {
            if(tankHolder.keepFluid()) {
                this.tank.load(tag, "fluid0", true);
            }
        }
    }

    protected CompoundTag writeStorable(CompoundTag tag) {
        if(this instanceof IInventoryHolder && keepInventory()) {
            tag.merge(this.inv.save());
        }
        if(this instanceof ITankHolder tankHolder) {
            if(tankHolder.keepFluid()) {
                this.tank.save(tag, "fluid0", true);
            }
        }
        return tag;
    }

    @Override
    public void onPlaced(Level world, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        CompoundTag tag = Stack.getTagOrEmpty(stack);
        if (!tag.isEmpty()) {
            readStorable(tag.getCompound(NBT.TAG_STORABLE_STACK));
        }
    }

    @Override
    public void onRemoved(Level world, BlockState state, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            if (this instanceof IInventoryHolder) {
                if (!keepInventory() || !keepStorable()) {
                    getInventory().drop(world, this.worldPosition);
                }
            }
        }
    }

    public ItemStack storeToStack(ItemStack stack) {
        CompoundTag nbt = writeStorable(new CompoundTag());
        CompoundTag nbt1 = Stack.getTagOrEmpty(stack);
        if (!nbt.isEmpty() && keepStorable()) {
            nbt1.put(NBT.TAG_STORABLE_STACK, nbt);
            stack.setTag(nbt1);
        }
        return stack;
    }

    public static <T extends AbstractTile> T fromStack(ItemStack stack, T tile) {
        CompoundTag nbt = stack.getTagElement(NBT.TAG_STORABLE_STACK);
        if (nbt != null) {
            tile.readStorable(nbt);
        }
        return tile;
    }

    public boolean keepStorable() {
        return true;
    }

    protected boolean keepInventory() {
        return false;
    }

    public Tank getTank() {
        return this.tank;
    }

    public Redstone getRedstoneMode() {
        return this.redstone;
    }

    public void setRedstoneMode(Redstone mode) {
        this.redstone = mode;
    }

    public boolean checkRedstone() {
        boolean power = this.level != null && this.level.getBestNeighborSignal(this.worldPosition) > 0;
        return Redstone.IGNORE.equals(getRedstoneMode()) || power && Redstone.ON.equals(getRedstoneMode()) || !power && Redstone.OFF.equals(getRedstoneMode());
    }

    public void sync() {
        if (this.level instanceof ServerLevel) {
            final BlockState state = getBlockState();
            this.level.sendBlockUpdated(this.worldPosition, state, state, 3);
            this.level.blockEntityChanged(this.worldPosition);
        }
    }

    public boolean isRemote() {
        return this.level != null && this.level.isClientSide;
    }

    public void setContainerOpen(boolean value) {
        final boolean b = this.isContainerOpen;
        this.isContainerOpen = value;
        if (b != value) {
            sync();
        }
    }

    public Inventory getInventory() {
        return this.inv;
    }


}