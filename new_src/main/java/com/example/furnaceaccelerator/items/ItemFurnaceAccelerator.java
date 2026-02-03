package com.example.furnaceaccelerator.items;

import com.example.furnaceaccelerator.FurnaceAcceleratorMod;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class ItemFurnaceAccelerator extends Item {
    
    public ItemFurnaceAccelerator() {
        this.setTranslationKey(FurnaceAcceleratorMod.MODID + ".furnace_accelerator");
        this.setCreativeTab(CreativeTabs.TOOLS);
        this.setMaxStackSize(1);
        this.setMaxDamage(100);
        this.setNoRepair(); // Нельзя починить
        
        FurnaceAcceleratorMod.logger.info("Furnace Accelerator item created");
    }
    
    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, 
                                      EnumHand hand, EnumFacing facing, 
                                      float hitX, float hitY, float hitZ) {
        
        // Проверяем, зажат ли Shift
        if (!player.isSneaking()) {
            // Если Shift не зажат - ничего не делаем
            if (!world.isRemote) {
                player.sendMessage(new TextComponentString(
                    TextFormatting.YELLOW + "Hold " + TextFormatting.GOLD + "SHIFT" + 
                    TextFormatting.YELLOW + " + Right Click to accelerate furnace"
                ));
            }
            return EnumActionResult.PASS;
        }
        
        // Только на серверной стороне
        if (!world.isRemote) {
            // Проверяем, что блок - это печь
            if (world.getTileEntity(pos) instanceof TileEntityFurnace) {
                TileEntityFurnace furnace = (TileEntityFurnace) world.getTileEntity(pos);
                
                // Проверяем, не ускорена ли уже печь
                if (!furnace.getTileData().hasKey("facc_accelerated")) {
                    // Устанавливаем флаг ускорения
                    furnace.getTileData().setBoolean("facc_accelerated", true);
                    
                    // Сохраняем оригинальные координаты для идентификации
                    furnace.getTileData().setInteger("facc_x", pos.getX());
                    furnace.getTileData().setInteger("facc_y", pos.getY());
                    furnace.getTileData().setInteger("facc_z", pos.getZ());
                    
                    // Наносим урон предмету
                    ItemStack stack = player.getHeldItem(hand);
                    stack.damageItem(1, player);
                    
                    // Отправляем сообщение игроку
                    player.sendMessage(new TextComponentString(
                        TextFormatting.GREEN + "Success! " + TextFormatting.WHITE + 
                        "Furnace accelerated x2 at [" + 
                        pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "]"
                    ));
                    
                    FurnaceAcceleratorMod.logger.info(
                        "Furnace accelerated at " + pos + " by " + player.getName()
                    );
                    
                    return EnumActionResult.SUCCESS;
                    
                } else {
                    // Печь уже ускорена
                    player.sendMessage(new TextComponentString(
                        TextFormatting.RED + "This furnace is already accelerated!"
                    ));
                    return EnumActionResult.FAIL;
                }
                
            } else {
                // Блок не печь
                player.sendMessage(new TextComponentString(
                    TextFormatting.RED + "This is not a furnace!"
                ));
                return EnumActionResult.FAIL;
            }
        }
        
        return EnumActionResult.PASS;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, 
                               List<String> tooltip, ITooltipFlag flag) {
        // Базовое описание
        tooltip.add(TextFormatting.GREEN + "Accelerates furnaces x2");
        tooltip.add(TextFormatting.GOLD + "Shift + Right Click" + TextFormatting.GRAY + " on furnace");
        
        // Прочность
        tooltip.add(TextFormatting.BLUE + "Durability: " + 
                   TextFormatting.WHITE + (stack.getMaxDamage() - stack.getItemDamage()) + 
                   TextFormatting.GRAY + "/" + 
                   TextFormatting.WHITE + stack.getMaxDamage());
        
        // Подробная информация при зажатом Shift
        if (net.minecraft.client.gui.GuiScreen.isShiftKeyDown()) {
            tooltip.add("");
            tooltip.add(TextFormatting.YELLOW + "Details:");
            tooltip.add(TextFormatting.GRAY + "• Accelerates cooking speed");
            tooltip.add(TextFormatting.GRAY + "• Accelerates fuel burn rate");
            tooltip.add(TextFormatting.GRAY + "• Works until furnace breaks");
            tooltip.add(TextFormatting.GRAY + "• Consumes durability on use");
        } else {
            tooltip.add(TextFormatting.DARK_GRAY + "Hold " + TextFormatting.YELLOW + 
                       "SHIFT" + TextFormatting.DARK_GRAY + " for details");
        }
    }
    
    @Override
    public boolean hasEffect(ItemStack stack) {
        // Блестящий эффект, если предмет не сломан
        return stack.getItemDamage() < stack.getMaxDamage();
    }
    
    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        // Показывать полосу прочности
        return true;
    }
    
    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        // Отображение прочности (обратное значение)
        return 1.0 - (double)stack.getItemDamage() / (double)stack.getMaxDamage();
    }
    
    @Override
    public int getRGBDurabilityForDisplay(ItemStack stack) {
        // Цвет полосы прочности (зеленый->желтый->красный)
        float durability = 1.0f - (float)stack.getItemDamage() / (float)stack.getMaxDamage();
        
        if (durability > 0.7f) return 0x00FF00; // Зеленый
        if (durability > 0.3f) return 0xFFFF00; // Желтый
        return 0xFF0000; // Красный
    }
}