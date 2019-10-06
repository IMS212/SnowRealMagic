package snownee.snow.block;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SixWayBlock;
import net.minecraft.block.WallBlock;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.kiwi.block.ModBlock;
import snownee.kiwi.util.Util;
import snownee.snow.MainModule;
import snownee.snow.SnowCommonConfig;

public class SnowWallBlock extends WallBlock implements ISnowVariant
{
    public static final BooleanProperty DOWN = SixWayBlock.DOWN;

    public SnowWallBlock(Properties properties)
    {
        super(properties);
    }

    @Override
    public boolean hasTileEntity(BlockState state)
    {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world)
    {
        return new SnowTextureTile();
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player)
    {
        return ModBlock.pickBlock(state, target, world, pos, player);
    }

    @Override
    public BlockRenderLayer getRenderLayer()
    {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn)
    {
        String key = Util.getTextureItem(stack, "0");
        if (!key.isEmpty())
        {
            tooltip.add(new TranslationTextComponent(key).applyTextStyle(TextFormatting.GRAY));
        }
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(UP, NORTH, EAST, WEST, SOUTH, WATERLOGGED, DOWN);
    }

    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos)
    {
        if (facing == Direction.DOWN)
        {
            ModSnowTileBlock.updateSnowyDirt(worldIn, facingPos, facingState);
            return stateIn.with(DOWN, MainModule.BLOCK.isValidPosition(stateIn, worldIn, currentPos, true));
        }
        return super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack)
    {
        //Ensure that after we place the block already containing snow that it updates the block under it
        BlockPos down = pos.down();
        ModSnowTileBlock.updateSnowyDirt(world, down, world.getBlockState(down));
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context)
    {
        World iblockreader = context.getWorld();
        BlockPos blockpos = context.getPos();
        BlockState stateIn = iblockreader.getBlockState(blockpos);
        return super.getStateForPlacement(context).with(DOWN, MainModule.BLOCK.isValidPosition(stateIn, iblockreader, blockpos));
    }

    @Override
    public boolean canContainFluid(IBlockReader worldIn, BlockPos pos, BlockState state, Fluid fluidIn)
    {
        return false;
    }

    @Override
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items)
    {
        MainModule.fillTextureItems(ItemTags.WALLS, this, items);
    }

    @Override
    public void randomTick(BlockState state, World worldIn, BlockPos pos, Random random)
    {
        if (!SnowCommonConfig.snowNeverMelt && worldIn.getLightFor(LightType.BLOCK, pos) > 11)
        {
            worldIn.setBlockState(pos, getRaw(state, worldIn, pos));
        }
    }
}
