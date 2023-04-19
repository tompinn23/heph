package com.tompinn23.euthenia.lib.block;

import com.tompinn23.euthenia.lib.container.AbstractContainer;
import com.tompinn23.euthenia.lib.registry.IVariant;
import com.tompinn23.euthenia.lib.registry.IVariantEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.*;

public abstract class AbstractBlock<V extends IVariant, B extends AbstractBlock<V, B>> extends Block implements IVariantEntry<V, B>, IBlock<V, B> {
    public static final VoxelShape SEMI_FULL_SHAPE = box(0.01D, 0.01D, 0.01D, 15.99D, 15.99D, 15.99D);

    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    public static final BooleanProperty IN_STRUCTURE = BooleanProperty.create("in_structure");
    public static final BooleanProperty ACTIVE_MASTER = BooleanProperty.create("active_master");


    protected final Map<Direction, VoxelShape> shapes = new HashMap<>();
    protected final V variant;

    public AbstractBlock(Properties props) {
        this(props, IVariant.EMPTY());
    }

    public AbstractBlock(Properties properties, V variant) {
        super(properties);
        this.variant = variant;
        this.shapes.put(Direction.UP, Shapes.block());
        this.shapes.put(Direction.DOWN, Shapes.block());
        this.shapes.put(Direction.NORTH, Shapes.block());
        this.shapes.put(Direction.SOUTH, Shapes.block());
        this.shapes.put(Direction.EAST, Shapes.block());
        this.shapes.put(Direction.WEST, Shapes.block());
    }

    public static VoxelShape box(double x1, double y1, double z1, double x2, double y2, double z2) {
        return Block.box(Math.min(x1, x2), Math.min(y1, y2), Math.min(z1, z2), Math.max(x1, x2), Math.max(y1, y2), Math.max(z1, z2));
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        if (!this.shapes.isEmpty() && !getFacing().equals(Facing.NONE)) {
            return this.shapes.get(state.getValue(FACING));
        } else {
            return super.getShape(state, world, pos, context);
        }
    }



    public Component getDisplayName(ItemStack stack) {
        return Component.translatable(asItem().getDescriptionId(stack));
    }

    @Override
    public V getVariant() {
        return this.variant;
    }
    @Override
    public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean isMoving) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof IBlockEntity) {
            ((IBlockEntity) tile).onAdded(world, state, oldState, isMoving);
        }
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof IBlockEntity) {
            ((IBlockEntity) tile).onRemoved(world, state, newState, isMoving);
        }
        super.onRemove(state, world, pos, newState, isMoving);
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof IBlockEntity) {
            ((IBlockEntity) tile).onPlaced(world, state, placer, stack);
        }
    }

    @Override
    public void playerDestroy(Level world, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity te, ItemStack stack) {
        if (te instanceof AbstractTile<?,?>) {
            AbstractTile tile = (AbstractTile) te;
            ItemStack stack1 = tile.storeToStack(new ItemStack(this));
            popResource(world, pos, stack1);
            player.awardStat(Stats.BLOCK_MINED.get(this));
            player.causeFoodExhaustion(0.005F);
        } else {
            super.playerDestroy(world, player, pos, state, te, stack);
        }
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor world, BlockPos currentPos, BlockPos facingPos) {
        if (this instanceof SimpleWaterloggedBlock && state.getValue(WATERLOGGED))
            world.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
        if (!state.canSurvive(world, currentPos)) {
            BlockEntity tileEntity = world.getBlockEntity(currentPos);
            if (!world.isClientSide() && tileEntity instanceof AbstractTile) {
                AbstractTile tile = (AbstractTile) tileEntity;
                ItemStack stack = tile.storeToStack(new ItemStack(this));
                popResource((Level) world, currentPos, stack);
                world.destroyBlock(currentPos, false);
            }
        }
        return super.updateShape(state, facing, facingState, world, currentPos, facingPos);
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof AbstractTile) {
            MenuProvider provider = new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return new ItemStack(AbstractBlock.this).getHoverName();
                }

                @Nullable
                @Override
                public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player playerEntity) {
                    return getContainer(i, playerInventory, (AbstractTile) tile, result);
                }
            };
            AbstractContainerMenu container = provider.createMenu(0, player.getInventory(), player);
            if (container != null) {
                if (player instanceof ServerPlayer) {
                    NetworkHooks.openScreen((ServerPlayer) player, provider, buffer -> {
                        buffer.writeBlockPos(pos);
                        additionalGuiData(buffer, state, world, pos, player, hand, result);
                    });
                }
                return InteractionResult.SUCCESS;
            }
        }
        return super.use(state, world, pos, player, hand, result);
    }


    @Nullable
    public <T extends AbstractTile> AbstractContainer getContainer(int id, Inventory inventory, AbstractTile te, BlockHitResult result) {
        return null;
    }

    protected void additionalGuiData(FriendlyByteBuf buffer, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return !state.canOcclude();
    }

    protected void setDefaultState() {
        setStateProps(state -> state);
    }

    protected void setStateProps(BaseState baseState) {
        BlockState state = this.stateDefinition.any();
        if (this instanceof SimpleWaterloggedBlock) {
            state = state.setValue(WATERLOGGED, false);
        }
        if (!getFacing().equals(Facing.NONE)) {
            state = state.setValue(FACING, Direction.NORTH);
        }
        if (hasActiveProp()) {
            state = state.setValue(ACTIVE, false);
        }
        if(isMultiblockPart()) {
            state = state.setValue(IN_STRUCTURE, false);
        }
        registerDefaultState(baseState.get(state));
    }
    protected interface BaseState {
        BlockState get(BlockState state);
    }

    protected boolean isPlacerFacing() {
        return false;
    }

    protected Facing getFacing() {
        return Facing.NONE;
    }

    protected boolean hasActiveProp() {
        return false;
    }

    protected boolean isMultiblockPart() { return false; }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
        return getFluidState(state).isEmpty() || super.propagatesSkylightDown(state, reader, pos);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState();
        if (getFacing().equals(Facing.HORIZONTAL)) {
            if (!isPlacerFacing()) {
                state = facing(context, false);
            } else {
                state = defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
            }
        } else if (getFacing().equals(Facing.ALL)) {
            if (!isPlacerFacing()) {
                state = facing(context, true);
            } else {
                state = defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
            }
        }
        if (state != null && this instanceof SimpleWaterloggedBlock) {
            FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
            state = state.setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
        }
        return state;
    }

    @Nullable
    private BlockState facing(BlockPlaceContext context, boolean b) {
        BlockState blockstate = this.defaultBlockState();
        for (Direction direction : context.getNearestLookingDirections()) {
            if (b || direction.getAxis().isHorizontal()) {
                blockstate = blockstate.setValue(FACING, b ? direction : direction.getOpposite());
                if (blockstate.canSurvive(context.getLevel(), context.getClickedPos())) {
                    return blockstate;
                }
            }
        }
        return null;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        if (getFacing().equals(Facing.ALL) || getFacing().equals(Facing.HORIZONTAL)) {
            return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
        }
        return super.rotate(state, rot);
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        if (getFacing().equals(Facing.ALL) || getFacing().equals(Facing.HORIZONTAL)) {
            return state.rotate(mirror.getRotation(state.getValue(FACING)));
        }
        return super.mirror(state, mirror);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return this instanceof SimpleWaterloggedBlock && state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public boolean triggerEvent(BlockState state, Level world, BlockPos pos, int id, int param) {
        BlockEntity tileEntity = world.getBlockEntity(pos);
        return tileEntity != null && tileEntity.triggerEvent(id, param);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        if (getFacing().equals(Facing.ALL) || getFacing().equals(Facing.HORIZONTAL)) builder.add(FACING);
        if (this instanceof SimpleWaterloggedBlock) builder.add(WATERLOGGED);
        if (hasActiveProp()) builder.add(ACTIVE);
        if (isMultiblockPart()) builder.add(IN_STRUCTURE);
    }



    protected enum Facing {
        HORIZONTAL, ALL, NONE
    }
}

