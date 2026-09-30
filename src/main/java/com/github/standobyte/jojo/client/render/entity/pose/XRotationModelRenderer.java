package com.github.standobyte.jojo.client.render.entity.pose;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.render.entity.model.stand.StandEntityModel;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.Model;
import com.github.standobyte.jojo.client.render.entity.model.ModelPart;

@Deprecated
// This type needs to exist to not break previously made Stand addons
// TODO: delete in v0.3
public class XRotationModelRenderer extends ModelPart {
    /** @deprecated use {@link StandEntityModel#setSecondXRot(ModelRenderer, float)} */
    @Deprecated
    public float xRotSecond = 0;
    
    public XRotationModelRenderer(Model model) {
        super(model);
    }

    public XRotationModelRenderer(Model model, int xTexOffs, int yTexOffs) {
        super(model, xTexOffs, yTexOffs);
    }

    public XRotationModelRenderer(int xTexSize, int yTexSize, int xTexOffs, int yTexOffs) {
        super(xTexSize, yTexSize, xTexOffs, yTexOffs);
    }
    
    @Override
    public void translateAndRotate(PoseStack matrixStack) {
        if (xRotSecond != 0.0F) {
            ClientUtil.rotateAngles(this, xRotSecond * MathUtil.RAD_TO_DEG);
            xRotSecond = 0;
        }
        super.translateAndRotate(matrixStack);
    }
}
