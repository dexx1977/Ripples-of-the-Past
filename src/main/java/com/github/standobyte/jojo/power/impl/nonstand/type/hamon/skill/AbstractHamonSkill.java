package com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill;

import com.github.standobyte.jojo.init.power.JojoCustomRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.action.non_stand.HamonAction;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonSkills;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;

import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraftforge.registries.IForgeRegistry;
import com.github.standobyte.jojo.init.power.RegistryEntry;

public abstract class AbstractHamonSkill implements RegistryEntry<AbstractHamonSkill> {
    @Override
    public IForgeRegistry<AbstractHamonSkill> getRegistry() {
        return JojoCustomRegistries.HAMON_SKILLS.getRegistry();
    }

    private final RewardType rewardType;
    private final Object2BooleanMap<Supplier<? extends HamonAction>> rewardActions;
    private final List<Supplier<? extends AbstractHamonSkill>> requiredSkills;

    protected AbstractHamonSkill(AbstractBuilder builder) {
        this.rewardType = builder.rewardType;
        this.rewardActions = builder.rewardActions;
        this.requiredSkills = builder.requiredSkills;
    }
    
    public RewardType getRewardType() {
        return rewardType;
    }
    
    public Stream<HamonAction> getRewardActions() {
        return rewardActions.keySet().stream().map(Supplier::get);
    }
    
    public Stream<HamonAction> getRewardActions(boolean addedToHud) {
        return rewardActions.object2BooleanEntrySet().stream()
                .filter(entry -> entry.getBooleanValue() == addedToHud)
                .map(Map.Entry::getKey)
                .map(Supplier::get);
    }
    
    public boolean addsExtraToHud() {
        return rewardActions.object2BooleanEntrySet().stream()
                .filter(entry -> entry.getBooleanValue())
                .findAny().isPresent();
    }
    
    public boolean isUnlockedByDefault() {
        return false;
    }
    
    public boolean requiresTeacher() {
        return false;
    }
    
    public Stream<AbstractHamonSkill> getRequiredSkills() {
        return requiredSkills.stream().map(Supplier::get);
    }
    
    public abstract SkillType getSkillType();
    
    public boolean isBaseSkill() {
        return getSkillType() == SkillType.BASE;
    }
    
    public void learnNewSkill(HamonData hamon, LivingEntity user) {
        hamon.addHamonSkill(user, this, true, true);
    }
    
    private List<MutableComponent> description;
    
    public MutableComponent getNameTranslated() {
        return Component.translatable("hamonSkill." + getName() + ".name");
    }
    
    public List<MutableComponent> getDescTranslated() {
        if (description == null) {
            description = new ArrayList<>();
            description.add(Component.translatable("hamonSkill." + getName() + ".desc"));
            if (this == ModHamonSkills.OVERDRIVE.get()) {
                description.add(Component.translatable("hamonSkill.overdrive_strong.desc"));
            }
        }
        return description;
    }
    
    private String translationKey = null;
    public String getName() {
        if (translationKey == null) {
            ResourceLocation regName = getRegistryName();
            translationKey = regName.getPath();
            if (!JojoMod.MOD_ID.equals(regName.getNamespace())) {
                translationKey = regName.getNamespace() + "." + translationKey;
            }
        }
        return this.translationKey;
    }
    
    public void onCommonSetup() {
        getRewardActions().forEach(action -> action.initUnlockingSkill(this));
    }
    
    
    
    
    
    public enum RewardType {
        ATTACK("attack"),
        ABILITY("ability"),
        PASSIVE("passive"),
        ITEM("item");
        
        private final Component name;
        
        private RewardType(String key) {
            this.name = Component.translatable("hamon.skill_type." + key).withStyle(ChatFormatting.ITALIC);
        }
        
        public Component getName() {
            return name;
        }
    }
    
    public enum SkillType {
        BASE,
        TECHNIQUE,
        CHARACTER_TECHNIQUE;
    }
    
    
    
    protected static class AbstractBuilder {
        protected final RewardType rewardType;
        protected Object2BooleanMap<Supplier<? extends HamonAction>> rewardActions = new Object2BooleanArrayMap<>();
        protected final List<Supplier<? extends AbstractHamonSkill>> requiredSkills = new ArrayList<>();
        
        public AbstractBuilder(RewardType rewardType) {
            this.rewardType = rewardType;
        }
    }
}
