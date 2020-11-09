package me.swirtzly.minecraft.angels.common.world.structures;

import com.google.common.collect.ImmutableMap;
import me.swirtzly.minecraft.angels.WeepingAngels;
import me.swirtzly.minecraft.angels.client.poses.AngelPoses;
import me.swirtzly.minecraft.angels.common.WAObjects;
import me.swirtzly.minecraft.angels.common.tileentities.StatueTile;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.loot.LootTables;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.tileentity.SignTileEntity;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.gen.feature.structure.StructurePiece;
import net.minecraft.world.gen.feature.structure.TemplateStructurePiece;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.fml.client.gui.screen.ModListScreen;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.commons.lang3.ArrayUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Non-Jigsaw method of getting structure pieces based on IglooPieces. Temple and JungleTemple pieces are hardcoded and don't actually use the structure nbt system to load in structures
 */
public class GraveyardStructurePieces {

    private static String[] USERNAMES = new String[]{"WizeWizzard", "Magicmaan", "Icebrin", "Swirtzly", "Cadiboo", "Chell", "UsualTundra1994", "50ap5ud5", "a_dizzle", "dhi", "ConnorDawn", "Spectre0987", "Nictogen"};

    private static final ResourceLocation GRAVEYARD_1 = new ResourceLocation(WeepingAngels.MODID, "graves/graveyard_1");
    private static final ResourceLocation GRAVEYARD_2 = new ResourceLocation(WeepingAngels.MODID, "graves/graveyard_2");

    private static final Map<ResourceLocation, BlockPos> OFFSET = ImmutableMap.of(GRAVEYARD_1, new BlockPos(0, 0, 0), GRAVEYARD_2, new BlockPos(0, 0, 0));

    public static void start(TemplateManager templateManager, BlockPos pos, Rotation rotation, List<StructurePiece> pieceList, Random random) {
        int x = pos.getX();
        int z = pos.getZ();
        BlockPos rotationOffSet = new BlockPos(0, 0, 0).rotate(rotation);
        BlockPos blockpos = rotationOffSet.add(x, pos.getY(), z);
        pieceList.add(new GraveyardStructurePieces.Piece(templateManager, random.nextBoolean() ? GRAVEYARD_1 : GRAVEYARD_2, blockpos, rotation));
    }

    public static class Piece extends TemplateStructurePiece {
        private ResourceLocation resourceLocation;
        private Rotation rotation;

        public Piece(TemplateManager templateManagerIn, ResourceLocation resourceLocationIn, BlockPos pos, Rotation rotationIn) {
            super(WAObjects.Structures.GRAVEYARD_PIECE, 0);
            this.resourceLocation = resourceLocationIn;
            BlockPos blockpos = GraveyardStructurePieces.OFFSET.get(resourceLocation);
            this.templatePosition = pos.add(blockpos.getX(), blockpos.getY(), blockpos.getZ());
            this.rotation = rotationIn;
            this.setupPiece(templateManagerIn);
        }

        public Piece(TemplateManager templateManagerIn, CompoundNBT tagCompound) {
            super(WAObjects.Structures.GRAVEYARD_PIECE, tagCompound);
            this.resourceLocation = new ResourceLocation(tagCompound.getString("Template"));
            this.rotation = Rotation.valueOf(tagCompound.getString("Rot"));
            this.setupPiece(templateManagerIn);
        }

        private void setupPiece(TemplateManager templateManager) {
            Template template = templateManager.getTemplateDefaulted(this.resourceLocation);
            PlacementSettings placementsettings = (new PlacementSettings()).setRotation(this.rotation).setMirror(Mirror.NONE);
            this.setup(template, this.templatePosition, placementsettings);
        }

        /**
         * (abstract) Helper method to read subclass data from NBT
         */
        @Override
        protected void readAdditional(CompoundNBT tagCompound) {
            super.readAdditional(tagCompound);
            tagCompound.putString("Template", this.resourceLocation.toString());
            tagCompound.putString("Rot", this.rotation.name());
        }

        /*
         * If you added any data marker structure blocks to your structure, you can access and modify them here.
         * In this case, our structure has a data maker with the string "chest" put into it. So we check to see
         * if the incoming function is "chest" and if it is, we now have that exact position.
         *
         * So what is done here is we replace the structure block with
         * a chest and we can then set the loottable for it.
         *
         * You can set other data markers to do other behaviors such as spawn a random mob in a certain spot,
         * randomize what rare block spawns under the floor, or what item an Item Frame will have.
         */
        @Override
        protected void handleDataMarker(String function, BlockPos pos, IServerWorld worldIn, Random rand, MutableBoundingBox sbb) {

            if(ServerLifecycleHooks.getCurrentServer().isDedicatedServer()){
                USERNAMES = ArrayUtils.addAll(USERNAMES, ServerLifecycleHooks.getCurrentServer().getPlayerList().getOnlinePlayerNames());;
            }

            if("angel".equals(function)){
                StatueTile statueTile = (StatueTile) worldIn.getTileEntity(pos.down());
                statueTile.setPose(AngelPoses.POSE_HIDING_FACE.getRegistryName());
                statueTile.setAngelType(5);
                statueTile.markDirty();
                worldIn.removeBlock(pos, false);
            }


            if("cobweb".equals(function)){
                Block block = rand.nextBoolean() ? Blocks.COBWEB : Blocks.AIR;
                worldIn.setBlockState(pos, block.getDefaultState(), 2);
            }

            if ("crypt_chest".equals(function) || "chest".equals(function)) {
                LockableLootTileEntity.setLootTable(worldIn, rand, pos.down(), WAObjects.CRYPT_LOOT);
                worldIn.removeBlock(pos, false);
            }

            if ("sign".equals(function)) {
                SignTileEntity signTileEntity = (SignTileEntity) worldIn.getTileEntity(pos.down());
                if (signTileEntity != null) {
                    signTileEntity.setText(0, new TranslationTextComponent("========"));
                    signTileEntity.setText(1, new TranslationTextComponent(USERNAMES[(int) (System.currentTimeMillis() % USERNAMES.length)]));
                    signTileEntity.setText(2, new TranslationTextComponent(createRandomDate().format(DateTimeFormatter.ISO_DATE)));
                    signTileEntity.setText(3, new TranslationTextComponent("========"));
                    worldIn.removeBlock(pos, false);
                }
            }
        }
    }

    public static LocalDate createRandomDate() {
        long startEpochDay = LocalDate.of(1800, 1, 1).toEpochDay();
        long endEpochDay = LocalDate.now().toEpochDay();
        long randomDay = ThreadLocalRandom.current().nextLong(startEpochDay, endEpochDay);
        return LocalDate.ofEpochDay(randomDay);
    }


}
