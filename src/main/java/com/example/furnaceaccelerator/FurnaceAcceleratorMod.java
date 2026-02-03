package com.example.furnaceaccelerator;

import com.example.furnaceaccelerator.handlers.FurnaceTickHandler;
import com.example.furnaceaccelerator.init.ModItems;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = FurnaceAcceleratorMod.MODID, 
     name = FurnaceAcceleratorMod.NAME, 
     version = FurnaceAcceleratorMod.VERSION,
     acceptedMinecraftVersions = "[1.12,1.12.2]")
public class FurnaceAcceleratorMod {
    public static final String MODID = "furnaceaccelerator";
    public static final String NAME = "Furnace Accelerator";
    public static final String VERSION = "1.0.0";
    
    @Mod.Instance(MODID)
    public static FurnaceAcceleratorMod instance;
    
    public static Logger logger;
    
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        logger.info("========================================");
        logger.info(NAME + " v" + VERSION + " initializing...");
        logger.info("Mod ID: " + MODID);
        logger.info("========================================");
    }
    
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        // Регистрируем обработчики событий
        MinecraftForge.EVENT_BUS.register(new FurnaceTickHandler());
        logger.info("Furnace acceleration system initialized");
        logger.info("Use Shift+Right Click with furnace accelerator item");
        
        // Проверяем, что предмет зарегистрирован
        if (ModItems.getFurnaceAccelerator() != null) {
            logger.info("Furnace Accelerator item loaded successfully");
        } else {
            logger.error("Furnace Accelerator item failed to load!");
        }
    }
    
    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        logger.info(NAME + " v" + VERSION + " successfully loaded!");
        logger.info("Ready to accelerate furnaces!");
    }
}