package com.github.standobyte.jojo.client.render.entity.model.stand;

import com.github.standobyte.jojo.client.render.FlameModelRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.stand.MagiciansRedRenderer;

import net.minecraft.client.renderer.Sheets;
import com.github.standobyte.jojo.client.render.entity.model.ModelPart;

public class MagiciansRedFlameLayerModel extends MagiciansRedModel {

    public MagiciansRedFlameLayerModel() {
        super(tex -> Sheets.translucentCullBlockSheet(), 64, 64);
        isLayerModel = true;
        
        clearAllCubes();
        leftForeArm.addChild(new FlameModelRenderer(this).setFireSprites(MagiciansRedRenderer.FIRE_0_SPRITE, MagiciansRedRenderer.FIRE_1_SPRITE)
                .addFlame(3.0F, 4.0F, 3.0F));
        rightForeArm.addChild(new FlameModelRenderer(this).setFireSprites(MagiciansRedRenderer.FIRE_0_SPRITE, MagiciansRedRenderer.FIRE_1_SPRITE)
                .addFlame(3.0F, 4.0F, 3.0F));
        ModelPart.addChild(leftLeg, new FlameModelRenderer(this).setFireSprites(MagiciansRedRenderer.FIRE_0_SPRITE, MagiciansRedRenderer.FIRE_1_SPRITE)
                .addFlame(5.0F, 4.0F, 4.0F));
        leftLowerLeg.addChild(new FlameModelRenderer(this).setFireSprites(MagiciansRedRenderer.FIRE_0_SPRITE, MagiciansRedRenderer.FIRE_1_SPRITE)
                .addFlame(4.0F, 4.0F, 4.0F));
        ModelPart.addChild(rightLeg, new FlameModelRenderer(this).setFireSprites(MagiciansRedRenderer.FIRE_0_SPRITE, MagiciansRedRenderer.FIRE_1_SPRITE)
                .addFlame(5.0F, 4.0F, 4.0F));
        rightLowerLeg.addChild(new FlameModelRenderer(this).setFireSprites(MagiciansRedRenderer.FIRE_0_SPRITE, MagiciansRedRenderer.FIRE_1_SPRITE)
                .addFlame(4.0F, 4.0F, 4.0F));
    }
    
}
