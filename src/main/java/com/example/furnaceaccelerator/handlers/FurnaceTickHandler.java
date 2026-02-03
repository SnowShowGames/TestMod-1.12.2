package com.example.furnaceaccelerator.handlers;

import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.lang.reflect.Field;

public class FurnaceTickHandler {
    
    private static Field cookTimeField;
    private static Field currentItemBurnTimeField;
    
    static {
        try {
            // Получаем приватные поля через рефлексию
            cookTimeField = TileEntityFurnace.class.getDeclaredField("field_174906_j"); // cookTime
            currentItemBurnTimeField = TileEntityFurnace.class.getDeclaredField("field_174904_k"); // burnTime
            
            cookTimeField.setAccessible(true);
            currentItemBurnTimeField.setAccessible(true);
            
        } catch (Exception e) {
            System.err.println("Failed to get furnace fields: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.world.isRemote) {
            // Проверяем все загруженные TileEntity
            event.world.loadedTileEntityList.forEach(tile -> {
                if (tile instanceof TileEntityFurnace) {
                    TileEntityFurnace furnace = (TileEntityFurnace) tile;
                    
                    // Проверяем, ускорена ли эта печь
                    if (furnace.getTileData().hasKey("facc_accelerated")) {
                        try {
                            // Ускоряем горение топлива в 2 раза
                            int burnTime = currentItemBurnTimeField.getInt(furnace);
                            if (burnTime > 0) {
                                // Уменьшаем на 2 вместо 1 (ускорение x2)
                                currentItemBurnTimeField.setInt(furnace, Math.max(0, burnTime - 2));
                            }
                            
                            // Ускоряем приготовление в 2 раза
                            int cookTime = cookTimeField.getInt(furnace);
                            if (cookTime > 0) {
                                // Уменьшаем на 2 вместо 1 (ускорение x2)
                                cookTimeField.setInt(furnace, Math.max(0, cookTime - 2));
                            }
                            
                        } catch (Exception e) {
                            System.err.println("Error accelerating furnace: " + e.getMessage());
                        }
                    }
                }
            });
        }
    }
    
    // Обработчик разрушения блока - снимаем ускорение
    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!event.getWorld().isRemote) {
            if (event.getWorld().getTileEntity(event.getPos()) instanceof TileEntityFurnace) {
                TileEntityFurnace furnace = (TileEntityFurnace) event.getWorld().getTileEntity(event.getPos());
                if (furnace.getTileData().hasKey("facc_accelerated")) {
                    furnace.getTileData().removeTag("facc_accelerated");
                    furnace.getTileData().removeTag("facc_x");
                    furnace.getTileData().removeTag("facc_y");
                    furnace.getTileData().removeTag("facc_z");
                    
                    // Можно отправить сообщение игроку
                    if (event.getPlayer() != null) {
                        event.getPlayer().sendMessage(new net.minecraft.util.text.TextComponentString(
                            net.minecraft.util.text.TextFormatting.GOLD + 
                            "Accelerated furnace destroyed!"
                        ));
                    }
                }
            }
        }
    }
    
    // Обработчик помещения блока - сбрасываем ускорение
    @SubscribeEvent
    public void onBlockPlace(BlockEvent.PlaceEvent event) {
        if (!event.getWorld().isRemote) {
            if (event.getWorld().getTileEntity(event.getPos()) instanceof TileEntityFurnace) {
                TileEntityFurnace furnace = (TileEntityFurnace) event.getWorld().getTileEntity(event.getPos());
                // Новая печь - сбрасываем ускорение
                furnace.getTileData().removeTag("facc_accelerated");
                furnace.getTileData().removeTag("facc_x");
                furnace.getTileData().removeTag("facc_y");
                furnace.getTileData().removeTag("facc_z");
            }
        }
    }
}