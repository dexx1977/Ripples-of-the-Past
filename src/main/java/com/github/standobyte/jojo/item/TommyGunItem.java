package com.github.standobyte.jojo.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.sound.ClientTickingSoundsHelper;
import com.github.standobyte.jojo.entity.damaging.projectile.TommyGunBulletEntity;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonSkills;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill.HamonStat;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;
import com.github.standobyte.jojo.util.mod.JojoModUtil;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import com.google.common.collect.Multimap;

import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.NonNullList;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeMod;

public class TommyGunItem extends Item {
    @Override
    public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientItemExtensions> consumer) {
        com.github.standobyte.jojo.client.ClientItemRenderers.initialize(this, consumer);
    }


    private Multimap<Attribute, AttributeModifier> attributeModifiers;
    
    public TommyGunItem(Properties properties) {
        super(properties);
    }
    
    @Override
    public void onUseTick(Level world, LivingEntity entity, ItemStack stack, int remainingTicks) {
        int ammo = getAmmo(stack);
        int tick = getUseDuration(stack) - remainingTicks;
        boolean shotTick = tick % 2 == 0;
        if (remainingTicks <= 1) {
            entity.releaseUsingItem();
            return;
        }
        if (!world.isClientSide()) {
            if (remainingTicks == getUseDuration(stack) - 14 && ammo == MAX_AMMO - 7 && josephVoiceLine(entity)) {
                JojoModUtil.sayVoiceLine(entity, ModSounds.JOSEPH_SCREAM_SHOOTING.get());
            }
            if (ammo > 0) {
//                shotTick = true;
//                int bulletsPerTickForLulz = 20;
                if (shotTick) {
//                    for (int i = 0; i < bulletsPerTickForLulz; i++) {
                        TommyGunBulletEntity bullet = new TommyGunBulletEntity(entity, world);
                        Vec3 pos = entity.getEyePosition(1).subtract(0, bullet.getBbHeight() / 2, 0).add(entity.getLookAngle());
                        bullet.shootFromRotation(entity, 2f, 0);
//                        pos = pos.add(bullet.getDeltaMovement().scale((double) i / bulletsPerTickForLulz));
                        bullet.setPos(pos.x, pos.y, pos.z);
                        world.addFreshEntity(bullet);
                        if (!(entity instanceof Player && ((Player) entity).abilities.instabuild)) {
                            consumeAmmo(stack, 1);
                        }
//                        if (getAmmo(stack) <= 0) break;
//                    }
                }
            }
            else {
                entity.releaseUsingItem();
            }
        }
        if (ammo > 0) {
            if (shotTick) {
                net.minecraft.util.RandomSource random = entity.getRandom();
                if (entity.getType() == EntityType.PLAYER ? world.isClientSide() : !world.isClientSide()) {
                    float recoil = 1F + Math.min((1F - (float) remainingTicks / (float) getUseDuration(stack)) * 6F, 3F);
                    entity.yRot += (random.nextFloat() - 0.5F) * 0.3F * recoil;
                    entity.xRot = Mth.clamp(entity.xRot -random.nextFloat() * 0.75F * recoil, -90, 90);
                }
                if (!world.isClientSide()) {
                    stack.getOrCreateTag().putByte("GunshotTick", (byte) 3);
                }
            }
        }
        else {
            entity.playSound(ModSounds.TOMMY_GUN_NO_AMMO.get(), 1.0F, 1.0F);
        }
        if (world.isClientSide() && remainingTicks == getUseDuration(stack)) {
            ClientTickingSoundsHelper.playTommyGunLoop(entity, ModSounds.TOMMY_GUN_LOOP.get(), 1.0F, stack);
        }
    }
    
    private static final int MAX_AMMO = 50;
    public static int getAmmo(ItemStack gun) {
        return gun.getOrCreateTag().getInt("Ammo");
    }
    
    public static boolean consumeAmmo(ItemStack gun, int amount) {
        int ammo = getAmmo(gun);
        if (ammo < 0) {
            gun.getTag().putInt("Ammo", 0);
            return false;
        }
        if (ammo > 0) {
            gun.getTag().putInt("Ammo", Math.max(ammo - amount, 0));
            return true;
        }
        return false;
    }
    
    private boolean reload(ItemStack stack, LivingEntity entity, Level world) {
        int ammoToLoad = MAX_AMMO - getAmmo(stack);
        if (ammoToLoad > 0) {
            if (entity instanceof Player) {
                Player player = (Player) entity;
                ammoToLoad = consumeAmmo(player, ammoToLoad);
                if (!world.isClientSide()) {
                    player.getCooldowns().addCooldown(this, ammoToLoad * 2);
                }
            }
            if (ammoToLoad > 0) {
                if (!world.isClientSide()) {
                    stack.getTag().putInt("Ammo", getAmmo(stack) + ammoToLoad);
                }
                return true;
            }
        }
        return false;
    }

    private static final int BULLETS_PER_GUNPOWDER = 8;
    private int consumeAmmo(Player player, int ammoToLoad) {
        if (!player.abilities.instabuild) {
            List<ItemStack> ironNuggets = new ArrayList<>();
            int ironNuggetsCount = 0;
            List<ItemStack> gunpowder = new ArrayList<>();
            int gunpowderCount = 0;
            
            for (int i = 0; i < player.inventory.getContainerSize(); ++i) {
                ItemStack inventoryStack = player.inventory.getItem(i);
                if (inventoryStack.getItem() == Items.IRON_NUGGET) {
                    ironNuggets.add(inventoryStack);
                    ironNuggetsCount += inventoryStack.getCount();
                }
                else if (inventoryStack.getItem() == Items.GUNPOWDER) {
                    gunpowder.add(inventoryStack);
                    gunpowderCount += inventoryStack.getCount();
                }
            }
            
            ammoToLoad = MathUtil.min(ironNuggetsCount, gunpowderCount * BULLETS_PER_GUNPOWDER, ammoToLoad);
            ironNuggetsCount = ammoToLoad;
            gunpowderCount = Mth.ceil((float) ammoToLoad / BULLETS_PER_GUNPOWDER);

            for (ItemStack ironNuggetsStack : ironNuggets) {
                int consumed = Math.min(ironNuggetsStack.getCount(), ironNuggetsCount);
                if (!player.level.isClientSide()) {
                    ironNuggetsStack.shrink(consumed);
                }
                ironNuggetsCount -= consumed;
                if (ironNuggetsCount == 0) break;
            }
            for (ItemStack gunpowderStack : gunpowder) {
                int consumed = Math.min(gunpowderStack.getCount(), gunpowderCount);
                if (!player.level.isClientSide()) {
                    gunpowderStack.shrink(consumed);
                }
                gunpowderCount -= consumed;
                if (gunpowderCount == 0) break;
            }
        }
        return ammoToLoad;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getAmmo(stack) < MAX_AMMO;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F * (float) getAmmo(stack) / (float) MAX_AMMO);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return net.minecraft.util.Mth.hsvToRgb(Math.max(0.0F, (float) getAmmo(stack) / (float) MAX_AMMO) / 3.0F, 1.0F, 1.0F);
    }

    /** The gun the mod's tab showed, with a full clip. */
    public void addToCreativeTab(CreativeModeTab.Output output) {
        ItemStack stack = new ItemStack(this);
        stack.getOrCreateTag().putInt("Ammo", MAX_AMMO);
        output.accept(stack);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level world, LivingEntity entity, int remainingTicks) {
        if (remainingTicks <= 1 && josephVoiceLine(entity)) {
            JojoModUtil.sayVoiceLine(entity, ModSounds.JOSEPH_WAR_DECLARATION.get());
        }
    }
    
    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int itemSlot, boolean isSelected) {
        if (!world.isClientSide() && stack.hasTag()) {
            CompoundTag tag = stack.getTag();
            byte textureTicks = tag.getByte("GunshotTick");
            if (textureTicks > 0) {
                tag.putByte("GunshotTick", --textureTicks);
            }
        }
    }
    
    public static int getGunshotTick(ItemStack stack) {
        if (stack.hasTag()) {
            return stack.getTag().getByte("GunshotTick");
        }
        return 0;
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            return reload(stack, player, world) ? InteractionResultHolder.consume(stack) : InteractionResultHolder.fail(stack);
        }
        else {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 100;
    }

    @Override
    public boolean hurtEnemy(ItemStack itemStack, LivingEntity target, LivingEntity user) {
        return INonStandPower.getNonStandPowerOptional(user).map(power -> 
        power.getTypeSpecificData(ModPowers.HAMON.get()).map(hamon -> {
            if (hamon.characterIs(ModHamonSkills.CHARACTER_JOSEPH.get())) {
                if (!user.level.isClientSide()) {
                    if (power.consumeEnergy(200) && DamageUtil.dealHamonDamage(target, 0.15F, user, null)) {
                        target.invulnerableTime = 0;
                        hamon.hamonPointsFromAction(HamonStat.STRENGTH, 200);
                        JojoModUtil.sayVoiceLine(user, ModSounds.JOSEPH_SHOOT.get());
                        return true;
                    }
                    return false;
                }
                return true;
            }
            return false;
        }).orElse(false)).orElse(false);
    }
    
    private boolean josephVoiceLine(LivingEntity entity) {
        return INonStandPower.getNonStandPowerOptional(entity)
                .map(power -> power.getTypeSpecificData(ModPowers.HAMON.get())
                        .map(hamon -> hamon.characterIs(ModHamonSkills.CHARACTER_JOSEPH.get()))
                        .orElse(false))
                .orElse(false);
    }
    
    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return oldStack.getItem() != newStack.getItem();
    }

    @SuppressWarnings("deprecation")
    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        if (slot == EquipmentSlot.MAINHAND) {
            if (attributeModifiers == null) {
                Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
                builder.put(ForgeMod.ENTITY_REACH.get(), new AttributeModifier(UUID.fromString("9b14156e-7ba3-446a-b18b-4c81a7d47a8b"), 
                        "Weapon modifier", 0.5, AttributeModifier.Operation.ADDITION));
                builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, 
                        "Weapon modifier", -2, AttributeModifier.Operation.ADDITION));
                attributeModifiers = builder.build();
            }
            return attributeModifiers;
        }
        return super.getDefaultAttributeModifiers(slot);
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.jojo.tommy_gun.reload_prompt", 
                Component.keybind("key.sneak"), Component.keybind("key.use")).withStyle(ChatFormatting.GRAY));
        
        ClientUtil.addItemReferenceQuote(tooltip, this);
        tooltip.add(ClientUtil.donoItemTooltip("KingKKrill"));
    }
}
