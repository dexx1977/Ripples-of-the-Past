package com.github.standobyte.jojo.client.render.entity.model.stand;

import com.github.standobyte.jojo.entity.stand.stands.GoldExperienceEntity;

import com.github.standobyte.jojo.client.render.entity.model.ModelPart;

public class GoldExperienceModel extends HumanoidStandModel<GoldExperienceEntity> {
    private ModelPart theThing;
    private ModelPart rightString;
    private ModelPart leftString;
    private ModelPart loincloth;
    private ModelPart leftPartLoincloth;
    private ModelPart rightPartLoincloth;

    public GoldExperienceModel() {
        super();
    }
    
    @Override
    protected void initOpposites() {
        super.initOpposites();
        oppositeHandside.put(leftPartLoincloth, rightPartLoincloth);
        oppositeHandside.put(leftString, rightString);
    }
}