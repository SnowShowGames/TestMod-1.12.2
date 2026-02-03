package com.example.furnaceaccelerator.handlers;

import com.example.furnaceaccelerator.FurnaceAcceleratorMod;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;

public class FurnaceTickHandler {
    
    // Список для отслеживания ускоренных печей
    private final List<FurnaceLocation> acceleratedFurnaces = new ArrayList<>();
    
    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        // Только на сервере и только на определенной фазе тика
        if (event.phase != TickEvent.Phase.END || event.world.isRemote) {
            return;
        }
        
        World world = event.world;
        long worldTime = world.getTotalWorldTime();
        
        // Каждые 10 тиков проверяем ускоренные печи
        if (worldTime % 10 == 0) {
            processAcceleratedFurnaces(world);
        }
    }
    
    private void processAcceleratedFurnaces(World world) {
        // Создаем копию списка для безопасной итерации
        List<FurnaceLocation> furnacesToProcess = new ArrayList<>(acceleratedFurnaces);
        
        for (FurnaceLocation location : furnacesToProcess) {
            try {
                // Проверяем, существует ли ещё мир и координаты
                if (world.provider.getDimension() != location.dimension) {
                    continue;
                }
                
                // Получаем TileEntity по координатам
                if (!world.isBlockLoaded(location.pos)) {
                    continue; // Чанк не загружен
                }
                
                TileEntityFurnace furnace = (TileEntityFurnace) world.getTileEntity(location.pos);
                
                if (furnace == null) {
                    // Печь больше не существует - удаляем из списка
                    acceleratedFurnaces.remove(location);
                    FurnaceAcceleratorMod.logger.info("Removed furnace from tracking (no longer exists): " + location);
                    continue;
                }
                
                // Проверяем, всё ли ещё ускорена печь
                if (!furnace.getTileData().hasKey("facc_accelerated")) {
                    // Флаг ускорения пропал - удаляем
                    acceleratedFurnaces.remove(location);
                    FurnaceAcceleratorMod.logger.debug("Furnace acceleration flag removed: " + location);
                    continue;
                }
                
                // Ускоряем печь (работает только когда печь активна)
                accelerateFurnace(furnace);
                
            } catch (Exception e) {
                FurnaceAcceleratorMod.logger.error("Error processing furnace at " + location + ": " + e.getMessage());
                // Удаляем проблемную печь из списка
                acceleratedFurnaces.remove(location);
            }
        }
    }
    
    /**
     * Ускоряет работу печи
     */
    private void accelerateFurnace(TileEntityFurnace furnace) {
        if (furnace.isBurning()) {
            // Ускорение горения топлива
            int burnTime = furnace.getField(0); // Текущее время горения
            int burnTimeTotal = furnace.getField(1); // Общее время горения
            
            if (burnTime > 0) {
                // Уменьшаем время горения в 2 раза быстрее
                int newBurnTime = burnTime - 2;
                if (newBurnTime < 0) newBurnTime = 0;
                furnace.setField(0, newBurnTime);
            }
            
            // Ускорение плавки
            int cookTime = furnace.getField(2); // Текущее время плавки
            int cookTimeTotal = furnace.getField(3); // Общее время плавки
            
            if (cookTime > 0 && cookTimeTotal > 0) {
                // Увеличиваем прогресс плавки в 2 раза быстрее
                int newCookTime = cookTime + 2;
                if (newCookTime >= cookTimeTotal) {
                    newCookTime = cookTimeTotal; // Готово
                }
                furnace.setField(2, newCookTime);
            }
        }
    }
    
    /**
     * Добавляет печь в список ускоренных
     */
    public void addAcceleratedFurnace(World world, net.minecraft.util.math.BlockPos pos) {
        FurnaceLocation location = new FurnaceLocation(
            pos, 
            world.provider.getDimension(),
            world.getTotalWorldTime()
        );
        
        // Проверяем, нет ли уже этой печи в списке
        if (!acceleratedFurnaces.contains(location)) {
            acceleratedFurnaces.add(location);
            FurnaceAcceleratorMod.logger.info("Added furnace to acceleration tracking: " + location);
        }
    }
    
    /**
     * Удаляет печь из списка ускоренных
     */
    public void removeAcceleratedFurnace(net.minecraft.util.math.BlockPos pos, int dimension) {
        FurnaceLocation toRemove = null;
        for (FurnaceLocation location : acceleratedFurnaces) {
            if (location.pos.equals(pos) && location.dimension == dimension) {
                toRemove = location;
                break;
            }
        }
        
        if (toRemove != null) {
            acceleratedFurnaces.remove(toRemove);
            FurnaceAcceleratorMod.logger.info("Removed furnace from acceleration tracking: " + toRemove);
        }
    }
    
    /**
     * Вспомогательный класс для хранения информации о печи
     */
    private static class FurnaceLocation {
        public final net.minecraft.util.math.BlockPos pos;
        public final int dimension;
        public final long addedTime;
        
        public FurnaceLocation(net.minecraft.util.math.BlockPos pos, int dimension, long addedTime) {
            this.pos = pos;
            this.dimension = dimension;
            this.addedTime = addedTime;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof FurnaceLocation)) return false;
            
            FurnaceLocation other = (FurnaceLocation) obj;
            return this.pos.equals(other.pos) && this.dimension == other.dimension;
        }
        
        @Override
        public int hashCode() {
            return pos.hashCode() * 31 + dimension;
        }
        
        @Override
        public String toString() {
            return String.format("[Dim=%d, X=%d, Y=%d, Z=%d, Added=%d]", 
                dimension, pos.getX(), pos.getY(), pos.getZ(), addedTime);
        }
    }
}