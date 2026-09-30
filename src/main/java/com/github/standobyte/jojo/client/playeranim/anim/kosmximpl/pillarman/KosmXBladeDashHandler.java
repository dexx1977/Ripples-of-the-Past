package com.github.standobyte.jojo.client.playeranim.anim.kosmximpl.pillarman;

import net.minecraft.world.item.ItemDisplayContext;
import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.playeranim.anim.kosmximpl.hamon.KosmXWindupAttackHandler;
import com.github.standobyte.jojo.client.playeranim.kosmx.anim.KosmXKeyframeAnimPlayer;
import com.github.standobyte.jojo.client.playeranim.kosmx.anim.modifier.KosmXFixedFadeModifier;
import com.github.standobyte.jojo.client.playeranim.kosmx.anim.modifier.KosmXHandsideMirrorModifier;

import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.util.Ease;
import dev.kosmx.playerAnim.core.util.Vec3f;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class KosmXBladeDashHandler extends KosmXWindupAttackHandler {

    public KosmXBladeDashHandler(ResourceLocation id) {
        super(id);
    }

    @Override
    protected ModifierLayer<IAnimation> createAnimLayer(AbstractClientPlayer player) {
        return new ModifierLayer<>(null, new KosmXHandsideMirrorModifier(player));
    }
    
    
    private static final ResourceLocation BLADE_DASH = new ResourceLocation(JojoMod.MOD_ID, "blade_dash");
    
    @Override
    public boolean setWindupAnim(Player player) {
        return setAnimFromName(player, BLADE_DASH, anim -> new ChargedAttackAnimPlayer(anim).windupStopsAt(anim.returnToTick));
    }

    @Override
    public boolean setAttackAnim(Player player) {
        return setToSwingTick(player, -1, BLADE_DASH);
    }
    
    @Override
    public void stopAnim(Player player) {
        fadeOutAnim(player, KosmXFixedFadeModifier.standardFadeIn(10, Ease.OUTCUBIC), null);
    }
    
    
    protected static class ChargedAttackAnimPlayer extends KosmXKeyframeAnimPlayer {
        public boolean isWindup = true;
        private float windupStopsAt;

        public ChargedAttackAnimPlayer(KeyframeAnimation animation) {
            super(animation);
        }

        public ChargedAttackAnimPlayer(KeyframeAnimation emote, int t) {
            super(emote, t);
        }

        public ChargedAttackAnimPlayer(KeyframeAnimation emote, int t, boolean mutable) {
            super(emote, t, mutable);
        }
        
        public ChargedAttackAnimPlayer windupStopsAt(float tick) {
            windupStopsAt = tick;
            return this;
        }
        
        @Override
        public Vec3f get3DTransform(String modelName, ItemDisplayContext type, float tickDelta, Vec3f value0) {
            BodyPartTransform part = bodyParts.get(modelName);
            if (part == null) return value0;
            
            if (isWindup && currentTick + tickDelta >= windupStopsAt) {
                int tick = Mth.floor(windupStopsAt);
                tickDelta = windupStopsAt - tick;
                return part.get3DTransform(type, tick, tickDelta, value0, data, false);
            }
            return super.get3DTransform(modelName, type, tickDelta, value0);
        }
        
        @Override
        public void tick() {
            if (isActive() && isWindup) {
                int maxTick = Mth.floor(windupStopsAt);
                if (currentTick >= maxTick) {
                    currentTick = maxTick;
                    return;
                }
            }
            super.tick();
        }
    }
    
}
