package com.benbenlaw.enderrf.block.entity;

import static mekanism.common.capabilities.Capabilities.CHEMICAL;

import net.neoforged.fml.ModList;
import org.jetbrains.annotations.Nullable;

import com.benbenlaw.enderrf.util.EnderChemicalStorage;

import codechicken.enderstorage.manager.EnderStorageManager;
import codechicken.enderstorage.tile.TileEnderTank;
import codechicken.enderstorage.tile.TileFrequencyOwner;
import codechicken.lib.capability.CapabilityCache;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.math.MathHelper;
import mekanism.api.Action;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.IChemicalTank;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class TileEnderFlask extends TileFrequencyOwner {

    public int rotation;
    public ChemicalStack chemical_state = ChemicalStack.EMPTY;
    public final PressureState pressure_state = new PressureState();
    private final CapabilityCache capCache = new CapabilityCache();

    private @Nullable IChemicalHandler chemicalHandler;

    private boolean described;

    public TileEnderFlask(BlockPos pos, BlockState state) {
        super(EnderRFBlockEntities.TILE_ENDER_FLASK.get(), pos, state);
    }

    @Override
    public void tick() {

        if (!ModList.get().isLoaded("mekanism")) return;

        super.tick();
        if (this.level == null)
            return;

        this.pressure_state.update(this.level.isClientSide);

        if (this.level.isClientSide)
            return;

        if (this.pressure_state.a_pressure)
            this.ejectChemical();

        if (this.level.getGameTime() % 20 == 0)
            this.sendUpdatePacket();
    }

    @Override
    public void setLevel(Level p_155231_) {
        super.setLevel(p_155231_);
        if (p_155231_ instanceof ServerLevel serverLevel) {
            this.capCache.setLevelPos(serverLevel, getBlockPos());
        }
    }

    private void ejectChemical() {
        IChemicalHandler source = this.getStorage();
        for (Direction side : Direction.values()) {
            IChemicalHandler dest = capCache.getCapability(CHEMICAL.block(), side);
            if (dest == null)
                continue;

            ChemicalStack drain = source.extractChemical(400, Action.SIMULATE);
            if (drain.isEmpty())
                continue;

            long remainder = dest.insertChemical(drain, Action.EXECUTE).getAmount();
            long accepted = drain.getAmount() - remainder;
            if (accepted > 0)
                source.extractChemical(accepted, Action.EXECUTE);
        }
    }

    @Override
    public void onFrequencySet() {
        this.chemicalHandler = null;
        if (this.level != null && !this.level.isClientSide) {
            this.sendUpdatePacket();
        }
    }

    @Override
    public EnderChemicalStorage getStorage() {
        return EnderStorageManager.instance(this.level.isClientSide).getStorage(this.frequency,
                EnderChemicalStorage.TYPE);
    }

    @Override
    public void onPlaced(@Nullable LivingEntity entity) {
        rotation = entity != null ? (int) Math.floor(entity.getYRot() * 4 / 360 + 2.5D) & 3 : 0;
        this.pressure_state.b_rotate = this.pressure_state.a_rotate = this.pressure_state.approachRotate();
        if (this.level != null && !level.isClientSide)
            this.sendUpdatePacket();
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("rot", rotation);
        tag.putBoolean("ir", pressure_state.invert_redstone);
        tag.putBoolean("output_mode", this.pressure_state.a_pressure);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.rotation = tag.getInt("rot");
        this.pressure_state.invert_redstone = tag.getBoolean("ir");
        this.pressure_state.a_pressure = tag.getBoolean("output_mode");
    }

    @Override
    public void writeToPacket(MCDataOutput packet) {
        super.writeToPacket(packet);
        packet.writeByte(this.rotation);
        packet.writeWithRegistryCodec(ChemicalStack.OPTIONAL_STREAM_CODEC, this.getStorage().getStack());
        packet.writeBoolean(this.pressure_state.a_pressure);
        packet.writeBoolean(this.pressure_state.invert_redstone);
    }

    @Override
    public void readFromPacket(MCDataInput packet) {
        super.readFromPacket(packet);
        this.rotation = packet.readByte() & 3;

        ChemicalStack stack = packet.readWithRegistryCodec(ChemicalStack.OPTIONAL_STREAM_CODEC);

        if (this.level != null && this.level.isClientSide)
            this.chemical_state = stack;

        this.pressure_state.a_pressure = packet.readBoolean();
        this.pressure_state.invert_redstone = packet.readBoolean();

        if (!described) {
            this.pressure_state.b_rotate = this.pressure_state.a_rotate = this.pressure_state.approachRotate();
        }
        this.described = true;
    }

    @Override
    public boolean activate(Player player, int subHit, InteractionHand hand) {
        if (subHit != 4)
            return false;

        pressure_state.a_pressure = !pressure_state.a_pressure;
        pressure_state.invert_redstone = !pressure_state.invert_redstone;

        if (level != null && !level.isClientSide) {
            String mode = pressure_state.a_pressure ? "Output" : "Input";
            player.displayClientMessage(Component.literal("Ender Flask set to: " + mode), true);
            this.sendUpdatePacket();
        }

        return true;
    }

    @Override
    public boolean rotate() {
        if (this.level != null && !this.level.isClientSide) {
            this.rotation = (this.rotation + 1) % 4;
            this.sendUpdatePacket();
        }
        return true;
    }

    @Override
    public int comparatorOutput() {
        IChemicalTank flask = this.getStorage();
        long max = flask.getCapacity();

        ChemicalStack chemical = flask.getStack();
        long stored = chemical.getAmount();

        return (int) (stored * 14 / max + (stored > 0 ? 1 : 0));
    }

    public IChemicalHandler getChemicalHandler(Direction side) {
        if (this.chemicalHandler == null)
            this.chemicalHandler = new IChemicalHandler() {
                @Override
                public int getChemicalTanks() {
                    return getStorage().getChemicalTanks();
                }

                @Override
                public ChemicalStack getChemicalInTank(int tank) {
                    return getStorage().getChemicalInTank(tank);
                }

                @Override
                public void setChemicalInTank(int tank, ChemicalStack stack) {
                    getStorage().setChemicalInTank(tank, stack);
                }

                @Override
                public long getChemicalTankCapacity(int tank) {
                    return getStorage().getChemicalTankCapacity(tank);
                }

                @Override
                public boolean isValid(int tank, ChemicalStack stack) {
                    return getStorage().isValid(stack);
                }

                @Override
                public ChemicalStack insertChemical(int tank, ChemicalStack stack, Action action) {
                    return pressure_state.a_pressure ? ChemicalStack.EMPTY : getStorage().insertChemical(tank, stack, action);
                }

                @Override
                public ChemicalStack extractChemical(int tank, long amount, Action action) {
                    return pressure_state.a_pressure ? getStorage().extractChemical(tank, amount, action) : ChemicalStack.EMPTY;
                }
            };

        return this.chemicalHandler;
    }

    public class PressureState {

        public boolean invert_redstone;
        public boolean a_pressure;
        public boolean b_pressure;

        public double a_rotate;
        public double b_rotate;

        public void update(boolean client) {
            if (client) {
                this.b_rotate = this.a_rotate;
                this.a_rotate = MathHelper.approachExp(this.a_rotate, this.approachRotate(), 0.5, 20);
            } else {
                b_pressure = a_pressure;

                boolean hasSignal = TileEnderFlask.this.level.hasNeighborSignal(TileEnderFlask.this.getBlockPos());
                if (hasSignal)
                    this.a_pressure = !this.invert_redstone;

                if (a_pressure != b_pressure)
                    TileEnderFlask.this.sendUpdatePacket();
            }
        }

        public double approachRotate() {
            return this.a_pressure ? -90 : 90;
        }
    }
}
