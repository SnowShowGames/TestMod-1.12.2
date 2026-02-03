package com.example.furnaceaccelerator.init;

import com.example.furnaceaccelerator.FurnaceAcceleratorMod;
import com.example.furnaceaccelerator.items.ItemFurnaceAccelerator;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber
public class ModItems {
    
    public static Item FURNACE_ACCELERATOR;
    
    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();
        
        // Создаем предмет с правильным registry name
        FURNACE_ACCELERATOR = new ItemFurnaceAccelerator()
            .setRegistryName(new ResourceLocation(FurnaceAcceleratorMod.MODID, "furnace_accelerator"));
        
        // Регистрируем предмет
        registry.register(FURNACE_ACCELERATOR);
        
        FurnaceAcceleratorMod.logger.info("✅ Furnace Accelerator item registered!");
    }
    
    public static Item getFurnaceAccelerator() {
        return FURNACE_ACCELERATOR;
    }
}