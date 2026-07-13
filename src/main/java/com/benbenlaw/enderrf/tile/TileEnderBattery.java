package com.benbenlaw.enderrf.tile;

import org.jetbrains.annotations.Nullable;

import com.benbenlaw.enderrf.storage.EnderEnergyStorage;

import codechicken.enderstorage.manager.EnderStorageManager;
import codechicken.enderstorage.tile.TileFrequencyOwner;
import codechicken.lib.capability.CapabilityCache;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.math.MathHelper;
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
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.EmptyEnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class TileEnderBattery extends TileFrequencyOwner {

    private final CapabilityCache capCache = new CapabilityCache();
    public final PressureState pressure_state = new PressureState();
    private @Nullable IEnergyStorage energyHandler;

    public int clientEnergy = 0;
    private boolean described = false;

    public int rotation;

    public TileEnderBattery(BlockPos pos, BlockState state) {
        super(EnderRFTiles.TILE_ENDER_BATTERY.get(), pos, state);
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        if (level instanceof ServerLevel serverLevel) {
            this.capCache.setLevelPos(serverLevel, this.getBlockPos());
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (level == null)
            return;

        pressure_state.update(level.isClientSide);

        if (!level.isClientSide) {
            if (pressure_state.a_pressure) {
                ejectEnergy();
            }

            if (level.getGameTime() % 20 == 0) {
                this.sendUpdatePacket();
            }
        }
    }

    public EnderEnergyStorage getStorage() {
        return EnderStorageManager
                .instance(this.level.isClientSide)
                .getStorage(this.frequency, EnderEnergyStorage.TYPE);
    }

    public void onPlaced(@Nullable LivingEntity entity) {
        this.rotation = entity != null ? (int) Math.floor((double) (entity.getYRot() * 4.0F / 360.0F) + 2.5) & 3 : 0;
        this.pressure_state.b_rotate = this.pressure_state.a_rotate = this.pressure_state.approachRotate();
        if (this.level != null && !this.level.isClientSide) {
            this.sendUpdatePacket();
        }
    }

    public IEnergyStorage getEnergyHandler(Direction side) {
        if (this.energyHandler == null) {
            this.energyHandler = new IEnergyStorage() {
                @Override
                public int receiveEnergy(int maxReceive, boolean simulate) {
                    return pressure_state.a_pressure ? 0 : getStorage().receiveEnergy(maxReceive, simulate);
                }

                @Override
                public int extractEnergy(int maxExtract, boolean simulate) {
                    return pressure_state.a_pressure ? getStorage().extractEnergy(maxExtract, simulate) : 0;
                }

                @Override
                public int getEnergyStored() {
                    return getStorage().getEnergyStored();
                }

                @Override
                public int getMaxEnergyStored() {
                    return getStorage().getMaxEnergyStored();
                }

                @Override
                public boolean canExtract() {
                    return pressure_state.a_pressure;
                }

                @Override
                public boolean canReceive() {
                    return !pressure_state.a_pressure;
                }
            };
        }
        return this.energyHandler;
    }

    private void ejectEnergy() {
        IEnergyStorage source = this.getStorage();
        for (Direction side : Direction.values()) {
            IEnergyStorage dest = capCache.getCapabilityOr(
                    Capabilities.EnergyStorage.BLOCK,
                    side,
                    EmptyEnergyStorage.INSTANCE);

            int extractedSim = source.extractEnergy(4000, true);
            if (extractedSim > 0) {
                int accepted = dest.receiveEnergy(extractedSim, false);
                if (accepted > 0) {
                    source.extractEnergy(accepted, false);
                }
            }
        }
    }

    @Override
    public boolean activate(Player player, int subHit, InteractionHand hand) {
        if (subHit == 4) {
            pressure_state.a_pressure = !pressure_state.a_pressure;
            pressure_state.invert_redstone = !pressure_state.invert_redstone;

            if (level != null && !level.isClientSide) {
                String mode = pressure_state.a_pressure ? "Output" : "Input";
                player.displayClientMessage(Component.literal("Ender Battery set to: " + mode), true);
                this.sendUpdatePacket();
            }
            return true;
        }
        return false;
    }

    @Override
    public void onFrequencySet() {
        super.onFrequencySet();
        this.energyHandler = null;
        if (this.level != null && !this.level.isClientSide) {
            this.sendUpdatePacket();
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("rot", rotation);
        tag.putBoolean("ir", this.pressure_state.invert_redstone);
        tag.putBoolean("output_mode", this.pressure_state.a_pressure);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.rotation = tag.getInt("rot");
        this.pressure_state.invert_redstone = tag.getBoolean("ir");
        this.pressure_state.a_pressure = tag.getBoolean("output_mode");
    }

    @Override
    public void writeToPacket(MCDataOutput packet) {
        super.writeToPacket(packet);
        packet.writeByte(this.rotation);
        packet.writeInt(this.getStorage().getEnergyStored());
        packet.writeBoolean(this.pressure_state.a_pressure);
        packet.writeBoolean(this.pressure_state.invert_redstone);
    }

    @Override
    public void readFromPacket(MCDataInput packet) {
        super.readFromPacket(packet);
        this.rotation = packet.readByte() & 3;

        int energy = packet.readInt();

        if (this.level != null && this.level.isClientSide) {
            this.clientEnergy = energy;
        }

        this.pressure_state.a_pressure = packet.readBoolean();
        this.pressure_state.invert_redstone = packet.readBoolean();

        if (!described) {
            this.pressure_state.b_rotate = this.pressure_state.a_rotate = this.pressure_state.approachRotate();
        }
        this.described = true;
    }

    public boolean rotate() {
        if (this.level != null && !this.level.isClientSide) {
            this.rotation = (this.rotation + 1) % 4;
            this.sendUpdatePacket();
        }
        return true;
    }

    public int comparatorOutput() {
        EnderEnergyStorage storage = getStorage();
        long energy = storage.getEnergyStored();
        long max = storage.getMaxEnergyStored();
        return (int) (energy * 14 / max + (energy > 0 ? 1 : 0));
    }

    public class PressureState {
        public boolean invert_redstone;
        public boolean a_pressure;
        public boolean b_pressure;
        public double a_rotate;
        public double b_rotate;

        public void update(boolean client) {
            if (client) {
                b_rotate = a_rotate;
                a_rotate = MathHelper.approachExp(a_rotate, approachRotate(), 0.5, 20);
            } else {
                b_pressure = a_pressure;

                boolean hasSignal = TileEnderBattery.this.level.hasNeighborSignal(TileEnderBattery.this.getBlockPos());
                if (hasSignal) {
                    a_pressure = !invert_redstone;
                }

                if (a_pressure != b_pressure) {
                    TileEnderBattery.this.sendUpdatePacket();
                }
            }
        }

        public double approachRotate() {
            return this.a_pressure ? -90.0 : 90.0;
        }
    }
}