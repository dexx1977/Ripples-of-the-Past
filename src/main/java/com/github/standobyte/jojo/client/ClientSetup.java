package com.github.standobyte.jojo.client;

import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import java.util.function.Consumer;
import java.util.List;
import java.util.ArrayList;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.controls.HudControlSettings;
import com.github.standobyte.jojo.client.particle.AirStreamParticle;
import com.github.standobyte.jojo.client.particle.AtmosphericRiftParticle;
import com.github.standobyte.jojo.client.particle.BloodParticle;
import com.github.standobyte.jojo.client.particle.CDRestorationParticle;
import com.github.standobyte.jojo.client.particle.DivineSandstormParticle;
import com.github.standobyte.jojo.client.particle.HamonAuraParticle;
import com.github.standobyte.jojo.client.particle.HamonSparkParticle;
import com.github.standobyte.jojo.client.particle.LightGlintParticle;
import com.github.standobyte.jojo.client.particle.LightModeFlashParticle;
import com.github.standobyte.jojo.client.particle.MeteoriteVirusParticle;
import com.github.standobyte.jojo.client.particle.OneTickFlameParticle;
import com.github.standobyte.jojo.client.particle.OnomatopoeiaParticle;
import com.github.standobyte.jojo.client.particle.RPSPickPartile;
import com.github.standobyte.jojo.client.particle.custom.CustomParticlesHelper;
import com.github.standobyte.jojo.client.particle.custom.FirstPersonHamonAura;
import com.github.standobyte.jojo.client.playeranim.PlayerAnimationHandler;
import com.github.standobyte.jojo.client.playeranim.anim.ModPlayerAnimations;
import com.github.standobyte.jojo.client.render.armor.ArmorModelRegistry;
import com.github.standobyte.jojo.client.render.armor.model.BladeHatArmorModel;
import com.github.standobyte.jojo.client.render.armor.model.BreathControlMaskModel;
import com.github.standobyte.jojo.client.render.armor.model.GlovesModel;
import com.github.standobyte.jojo.client.render.armor.model.SatiporojaScarfArmorModel;
import com.github.standobyte.jojo.client.render.armor.model.StoneMaskModel;
import com.github.standobyte.jojo.client.render.block.BlockSprites;
import com.github.standobyte.jojo.client.render.entity.layerrenderer.DivineSandstormEffectLayer;
import com.github.standobyte.jojo.client.render.entity.layerrenderer.EnergyRippleLayer;
import com.github.standobyte.jojo.client.render.entity.layerrenderer.FrozenLayer;
import com.github.standobyte.jojo.client.render.entity.layerrenderer.GlovesLayer;
import com.github.standobyte.jojo.client.render.entity.layerrenderer.HamonBurnLayer;
import com.github.standobyte.jojo.client.render.entity.layerrenderer.HamonProtectionLayer;
import com.github.standobyte.jojo.client.render.entity.layerrenderer.InkLipsLayer;
import com.github.standobyte.jojo.client.render.entity.layerrenderer.KnifeLayer;
import com.github.standobyte.jojo.client.render.entity.layerrenderer.LadybugBroochLayer;
import com.github.standobyte.jojo.client.render.entity.layerrenderer.MobStuckArrowLayer;
import com.github.standobyte.jojo.client.render.entity.layerrenderer.PillarmanBladesLayer;
import com.github.standobyte.jojo.client.render.entity.layerrenderer.PillarmanLayer;
import com.github.standobyte.jojo.client.render.entity.layerrenderer.SRSEEyesLayer;
import com.github.standobyte.jojo.client.render.entity.layerrenderer.TornadoOverdriveEffectLayer;
import com.github.standobyte.jojo.client.render.entity.layerrenderer.VampireEyesLayer;
import com.github.standobyte.jojo.client.render.entity.layerrenderer.WindCloakLayer;
import com.github.standobyte.jojo.client.render.entity.layerrenderer.ZombieLayer;
import com.github.standobyte.jojo.client.render.entity.layerrenderer.barrage.BarrageFistAfterimagesLayer;
import com.github.standobyte.jojo.client.render.entity.renderer.AfterimageRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.AngeloRockRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.CocoJumboTurtleRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.ConsciousnessRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.CrimsonBubbleRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.GETransformationRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.HamonBlockChargeRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.HamonProjectileShieldRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.LeavesGliderRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.MRDetectorRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.PillarmanDivineSandstormRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.PillarmanTempleEngravingRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.RoadRollerRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.SendoHamonOverdriveRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.SoulRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.SpriteObjectEntityRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.TurquoiseBlueOverdriveRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.damaging.MRFlameRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.damaging.SCFlameRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.damaging.beam.LightBeamRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.damaging.beam.SpaceRipperStingyEyesRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.damaging.extending.HGBarrierRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.damaging.extending.HGGrapplingStringRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.damaging.extending.HGStringRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.damaging.extending.MRRedBindRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.damaging.extending.PillarmanHornRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.damaging.extending.PillarmanRibRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.damaging.extending.PillarmanVeinRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.damaging.extending.SPStarFingerRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.damaging.extending.SatiporojaScarfBindingRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.damaging.extending.SatiporojaScarfRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.damaging.extending.SnakeMufflerRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.damaging.projectile.BlockShardRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.damaging.projectile.CDBlockBulletRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.damaging.projectile.CDBloodCutterRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.damaging.projectile.HGEmeraldRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.damaging.projectile.HamonBubbleBarrierRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.damaging.projectile.HamonBubbleCutterRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.damaging.projectile.HamonBubbleRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.damaging.projectile.HamonCutterRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.damaging.projectile.MRCrossfireHurricaneRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.damaging.projectile.MRCrossfireHurricaneSpecialRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.damaging.projectile.MRFireballRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.damaging.projectile.MolotovRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.damaging.projectile.SCRapierRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.damaging.projectile.TommyGunBulletRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.damaging.stretching.ZoomPunchRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.itemprojectile.BladeHatRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.itemprojectile.ClackersRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.itemprojectile.KnifeRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.itemprojectile.StandArrowRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.mob.HamonMasterRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.mob.HungryZombieRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.mob.RockPaperScissorsKidRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.mob.StandUserDummyRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.stand.CrazyDiamondRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.stand.GoldExperienceRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.stand.HierophantGreenRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.stand.MagiciansRedRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.stand.SilverChariotRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.stand.StarPlatinumRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.stand.TheWorldRenderer;
import com.github.standobyte.jojo.client.render.item.CustomIconItem;
import com.github.standobyte.jojo.client.render.item.RoadRollerBakedModel;
import com.github.standobyte.jojo.client.render.item.generic.ItemISTERModelWrapper;
import com.github.standobyte.jojo.client.render.item.standdisc.StandDiscISTERModel;
import com.github.standobyte.jojo.client.render.item.standdisc.StandDiscOverrideList;
import com.github.standobyte.jojo.client.render.rendertype.CustomRenderType;
import com.github.standobyte.jojo.client.render.world.shader.ShaderEffectApplier;
import com.github.standobyte.jojo.client.resources.CustomResources;
import com.github.standobyte.jojo.client.sound.loopplayer.LoopPlayerHandler;
import com.github.standobyte.jojo.client.ui.actionshud.ActionsOverlayGui;
import com.github.standobyte.jojo.client.ui.marker.MarkerRenderer;
import com.github.standobyte.jojo.client.ui.screen.hamon.HamonScreen;
import com.github.standobyte.jojo.client.ui.screen.vampirism.VampirismScreen;
import com.github.standobyte.jojo.client.ui.screen.walkman.WalkmanScreen;
import com.github.standobyte.jojo.init.ModBlocks;
import com.github.standobyte.jojo.init.ModContainers;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.init.power.stand.ModStands;
import com.github.standobyte.jojo.item.CassetteRecordedItem;
import com.github.standobyte.jojo.item.StandArrowItem;
import com.github.standobyte.jojo.item.StandDiscItem;
import com.github.standobyte.jojo.item.StoneMaskItem;
import com.github.standobyte.jojo.item.cassette.CassetteCap;
import com.github.standobyte.jojo.util.mc.reflection.ClientReflection;
import com.mco.mcrecog.MCRecog;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.particle.PlayerCloudParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.LavaParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.FireworkEntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.world.item.Items;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import com.github.standobyte.jojo.util.mc.MCUtil;

@EventBusSubscriber(modid = JojoMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    public static final ClampedItemPropertyFunction STAND_ITEM_INVISIBLE = (itemStack, clientWorld, livingEntity, seed) -> {
        return !ClientUtil.canSeeStands() ? 1 : 0;
    };
    
    public static ConsciousnessRenderer xxd;
    
    /**
     * Renderer and key-mapping registration. 1.20.1 removed
     * RenderingRegistry/ClientRegistry, so the registrations declared during client
     * setup are recorded here and handed to Forge when it asks for them. The order
     * the mod declares them in is preserved.
     */
    private static final List<Consumer<EntityRenderersEvent.RegisterRenderers>> RENDERER_REGISTRATIONS = new ArrayList<>();
    private static boolean renderersDeclared = false;

    /**
     * The 1.16.5 mod registered its renderers during client setup; 1.20.1 hands them
     * out from EntityRenderersEvent.RegisterRenderers, which fires before that, so
     * the declarations are made when the event is received.
     */
    private static void declareEntityRenderers() {
        if (renderersDeclared) {
            return;
        }
        renderersDeclared = true;
        registerRenderer(ModEntityTypes.BLADE_HAT.get(), BladeHatRenderer::new);
        registerRenderer(ModEntityTypes.SPACE_RIPPER_STINGY_EYES.get(), SpaceRipperStingyEyesRenderer::new);
        registerRenderer(ModEntityTypes.TURQUOISE_BLUE_OVERDRIVE.get(), TurquoiseBlueOverdriveRenderer::new);
        registerRenderer(ModEntityTypes.SENDO_HAMON_OVERDRIVE.get(), SendoHamonOverdriveRenderer::new);
        registerRenderer(ModEntityTypes.ZOOM_PUNCH.get(), ZoomPunchRenderer::new);
        registerRenderer(ModEntityTypes.AFTERIMAGE.get(), AfterimageRenderer::new);
        registerRenderer(ModEntityTypes.HAMON_PROJECTILE_SHIELD.get(), HamonProjectileShieldRenderer::new);
        registerRenderer(ModEntityTypes.LEAVES_GLIDER.get(), LeavesGliderRenderer::new);
        registerRenderer(ModEntityTypes.HAMON_BLOCK_CHARGE.get(), HamonBlockChargeRenderer::new);
        registerRenderer(ModEntityTypes.AJA_STONE_BEAM.get(), LightBeamRenderer::new);
        registerRenderer(ModEntityTypes.HAMON_CUTTER.get(), HamonCutterRenderer::new);
        registerRenderer(ModEntityTypes.CLACKERS.get(), ClackersRenderer::new);
        registerRenderer(ModEntityTypes.HAMON_BUBBLE.get(), HamonBubbleRenderer::new);
        registerRenderer(ModEntityTypes.HAMON_BUBBLE_BARRIER.get(), HamonBubbleBarrierRenderer::new);
        registerRenderer(ModEntityTypes.HAMON_BUBBLE_CUTTER.get(), HamonBubbleCutterRenderer::new);
        registerRenderer(ModEntityTypes.CRIMSON_BUBBLE.get(), CrimsonBubbleRenderer::new);
        registerRenderer(ModEntityTypes.SATIPOROJA_SCARF.get(), SatiporojaScarfRenderer::new);
        registerRenderer(ModEntityTypes.SATIPOROJA_SCARF_BINDING.get(), SatiporojaScarfBindingRenderer::new);
        registerRenderer(ModEntityTypes.SNAKE_MUFFLER.get(), SnakeMufflerRenderer::new);
        registerRenderer(ModEntityTypes.TOMMY_GUN_BULLET.get(), TommyGunBulletRenderer::new);
        registerRenderer(ModEntityTypes.KNIFE.get(), KnifeRenderer::new);
        registerRenderer(ModEntityTypes.MOLOTOV.get(), manager -> new MolotovRenderer<>(manager, Minecraft.getInstance().getItemRenderer(), 1.0F, true));
        registerRenderer(ModEntityTypes.STAND_ARROW.get(), StandArrowRenderer::new);
        registerRenderer(ModEntityTypes.SOUL.get(), SoulRenderer::new);
        registerRenderer(ModEntityTypes.BLOCK_SHARD.get(), BlockShardRenderer::new);
        registerRenderer(ModEntityTypes.PILLARMAN_TEMPLE_ENGRAVING.get(), PillarmanTempleEngravingRenderer::new);
        registerRenderer(ModEntityTypes.SP_STAR_FINGER.get(), SPStarFingerRenderer::new);
        registerRenderer(ModEntityTypes.HG_STRING.get(), HGStringRenderer::new);
        registerRenderer(ModEntityTypes.HG_EMERALD.get(), HGEmeraldRenderer::new);
        registerRenderer(ModEntityTypes.HG_GRAPPLING_STRING.get(), HGGrapplingStringRenderer::new);
        registerRenderer(ModEntityTypes.HG_BARRIER.get(), HGBarrierRenderer::new);
        registerRenderer(ModEntityTypes.SC_RAPIER.get(), SCRapierRenderer::new);
        registerRenderer(ModEntityTypes.SC_FLAME.get(), SCFlameRenderer::new);
        registerRenderer(ModEntityTypes.ROAD_ROLLER.get(), RoadRollerRenderer::new);
        registerRenderer(ModEntityTypes.MR_FLAME.get(), MRFlameRenderer::new);
        registerRenderer(ModEntityTypes.MR_FIREBALL.get(), MRFireballRenderer::new);
        registerRenderer(ModEntityTypes.MR_CROSSFIRE_HURRICANE.get(), MRCrossfireHurricaneRenderer::new);
        registerRenderer(ModEntityTypes.MR_CROSSFIRE_HURRICANE_SPECIAL.get(), MRCrossfireHurricaneSpecialRenderer::new);
        registerRenderer(ModEntityTypes.MR_RED_BIND.get(), MRRedBindRenderer::new);
        registerRenderer(ModEntityTypes.MR_DETECTOR.get(), MRDetectorRenderer::new);
        registerRenderer(ModEntityTypes.CD_BLOOD_CUTTER.get(), CDBloodCutterRenderer::new);
        registerRenderer(ModEntityTypes.CD_BLOCK_BULLET.get(), CDBlockBulletRenderer::new);
        registerRenderer(ModEntityTypes.EYE_OF_ENDER_INSIDE.get(), manager -> new ThrownItemRenderer<>(manager, 1.0F, true));
        // the mod's firework is a vanilla firework, so the vanilla renderer (typed to
        // the parent class) can render it
        registerRenderer((net.minecraft.world.entity.EntityType) ModEntityTypes.FIREWORK_INSIDE.get(), manager -> new FireworkEntityRenderer(manager));
        registerRenderer(ModEntityTypes.ANGELO_ROCK.get(), AngeloRockRenderer::new);
        registerRenderer(ModEntityTypes.GE_LIFEFORM_TRANSFORMATION.get(), GETransformationRenderer::new);
        registerRenderer(ModEntityTypes.HUNGRY_ZOMBIE.get(), HungryZombieRenderer::new);
        registerRenderer(ModEntityTypes.HAMON_MASTER.get(), HamonMasterRenderer::new);
        registerRenderer(ModEntityTypes.COCO_JUMBO_TURTLE.get(), CocoJumboTurtleRenderer::new);
        registerRenderer(ModEntityTypes.ROCK_PAPER_SCISSORS_KID.get(), RockPaperScissorsKidRenderer::new);
        registerRenderer(ModEntityTypes.STAND_USER_DUMMY.get(), StandUserDummyRenderer::new);
        registerRenderer(ModEntityTypes.PILLARMAN_HORN.get(), PillarmanHornRenderer::new);
        registerRenderer(ModEntityTypes.PILLARMAN_DIVINE_SANDSTORM.get(), PillarmanDivineSandstormRenderer::new);
        registerRenderer(ModEntityTypes.PILLARMAN_VEINS.get(), PillarmanVeinRenderer::new);
        registerRenderer(ModEntityTypes.PILLARMAN_RIBS.get(), PillarmanRibRenderer::new);
        registerRenderer(ModEntityTypes.OBJECT.get(), SpriteObjectEntityRenderer::new);

        // Stand entity renderers must be declared here as well: EntityRenderersEvent.
        // RegisterRenderers fires BEFORE the client setup event in 1.20.1, so the
        // registrations made in onFMLClientSetup below would never be handed to Forge,
        // leaving the Stand entities without a renderer (NullPointerException when the
        // client renders a summoned Stand).
        registerRenderer(ModStands.STAR_PLATINUM.getEntityType(), ClientUtil.logException(StarPlatinumRenderer::new));
        registerRenderer(ModStands.THE_WORLD.getEntityType(), ClientUtil.logException(TheWorldRenderer::new));
        registerRenderer(ModStands.HIEROPHANT_GREEN.getEntityType(), ClientUtil.logException(HierophantGreenRenderer::new));
        registerRenderer(ModStands.SILVER_CHARIOT.getEntityType(), ClientUtil.logException(SilverChariotRenderer::new));
        registerRenderer(ModStands.MAGICIANS_RED.getEntityType(), ClientUtil.logException(MagiciansRedRenderer::new));
        registerRenderer(ModStands.CRAZY_DIAMOND.getEntityType(), ClientUtil.logException(CrazyDiamondRenderer::new));
        registerRenderer(ModStands.GOLD_EXPERIENCE.getEntityType(), ClientUtil.logException(GoldExperienceRenderer::new));
    }


    private static final List<KeyMapping> KEY_MAPPINGS = new ArrayList<>();

    public static <T extends Entity> void registerRenderer(EntityType<T> entityType, EntityRendererProvider<T> rendererProvider) {
        RENDERER_REGISTRATIONS.add(event -> event.registerEntityRenderer(entityType, rendererProvider));
    }

    public static KeyMapping registerKeyMapping(KeyMapping keyMapping) {
        KEY_MAPPINGS.add(keyMapping);
        return keyMapping;
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        declareEntityRenderers();
        RENDERER_REGISTRATIONS.forEach(registration -> registration.accept(event));
    }

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        // 1.20.1: RegisterKeyMappingsEvent fires BEFORE FMLClientSetupEvent, so InputHandler
        // (which creates all KeyMappings in its constructor) must be initialized here,
        // otherwise none of the mod's keybinds are registered and cannot be rebound in the Controls screen.
        InputHandler.init(Minecraft.getInstance());
        KEY_MAPPINGS.forEach(event::register);
    }

    @SubscribeEvent
    public static void onFMLClientSetup(FMLClientSetupEvent event) {
        Minecraft mc = Minecraft.getInstance();
        
        
        EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();
        xxd = new ConsciousnessRenderer(new EntityRendererProvider.Context(dispatcher, mc.getItemRenderer(), 
                mc.getBlockRenderer(), mc.gameRenderer.itemInHandRenderer, mc.getResourceManager(), 
                mc.getEntityModels(), mc.font));
        
        PlayerAnimationHandler.initAnimator();
        
        ArmorModelRegistry.registerArmorModel(StoneMaskModel::new, ModItems.STONE_MASK.get());
        ArmorModelRegistry.registerArmorModel(StoneMaskModel::new, ModItems.AJA_STONE_MASK.get());
        ArmorModelRegistry.registerArmorModel(BladeHatArmorModel::new, ModItems.BLADE_HAT.get());
        ArmorModelRegistry.registerArmorModel(BreathControlMaskModel::new, ModItems.BREATH_CONTROL_MASK.get());
        ArmorModelRegistry.registerArmorModel(SatiporojaScarfArmorModel::new, ModItems.SATIPOROJA_SCARF.get());
        
        ClientModSettings.init(mc, new File(mc.gameDirectory, "config/jojo_rotp/client_settings.json"));
        HudControlSettings.init(new File(mc.gameDirectory, "config/jojo_rotp/controls/"));
        
        HamonScreen.clientInit();
        VampirismScreen.clientInit();
        
        event.enqueueWork(() -> {
            CustomIconItem.registerModelOverride();
            ItemProperties.register(ModItems.KNIFE.get(), new ResourceLocation(JojoMod.MOD_ID, "count"), (itemStack, clientWorld, livingEntity, seed) -> {
                return livingEntity != null ? itemStack.getCount() : 1;
            });
            ItemProperties.register(ModItems.STONE_MASK.get(), new ResourceLocation(JojoMod.MOD_ID, "stone_mask_activated"), (itemStack, clientWorld, livingEntity, seed) -> {
                return itemStack.getTag().getByte(StoneMaskItem.NBT_ACTIVATION_KEY) > 0 ? 1 : 0;
            });
            ItemProperties.register(ModItems.TOMMY_GUN.get(), new ResourceLocation(JojoMod.MOD_ID, "swing"), (itemStack, clientWorld, livingEntity, seed) -> {
                return livingEntity != null && livingEntity.swinging && livingEntity.getItemInHand(livingEntity.swingingArm) == itemStack ? 1 : 0;
            });
            ItemProperties.register(Items.BOW, new ResourceLocation(JojoMod.MOD_ID, "stand_arrow"), (itemStack, clientWorld, livingEntity, seed) -> {
                return livingEntity != null && livingEntity.isUsingItem() && livingEntity.getUseItem() == itemStack
                        && livingEntity.getProjectile(itemStack).getItem() instanceof StandArrowItem ? 1 : 0;
            });
            ItemProperties.register(Items.CROSSBOW, new ResourceLocation(JojoMod.MOD_ID, "stand_arrow"), (itemStack, clientWorld, livingEntity, seed) -> {
                return livingEntity != null && CrossbowItem.isCharged(itemStack) && (
                        CrossbowItem.containsChargedProjectile(itemStack, ModItems.STAND_ARROW.get()) || 
                        CrossbowItem.containsChargedProjectile(itemStack, ModItems.STAND_ARROW_BEETLE.get())) ? 1 : 0;
            });
            ItemProperties.register(ModItems.STAND_DISC.get(), new ResourceLocation(JojoMod.MOD_ID, "stand_id"), (itemStack, clientWorld, livingEntity, seed) -> {
                return StandDiscItem.validStandDisc(itemStack, true) ? JojoCustomRegistries.STANDS.getNumericId(StandDiscItem.getStandFromStack(itemStack).getType().getRegistryName()) : -1;
            });
            ItemProperties.register(ModItems.CASSETTE_RECORDED.get(), new ResourceLocation(JojoMod.MOD_ID, "cassette_distortion"), (itemStack, clientWorld, livingEntity, seed) -> {
                return CassetteRecordedItem.getCassetteData(itemStack)
                        .map(cap -> Mth.clamp(cap.getGeneration(), 0, CassetteCap.MAX_GENERATION))
                        .orElse(0).floatValue();
            });
//            ItemModelsProperties.register(ModItems.EMPEROR.get(), new ResourceLocation(JojoMod.MOD_ID, "stand_invisible"), STAND_ITEM_INVISIBLE);
            ItemProperties.register(ModItems.POLAROID.get(), new ResourceLocation(JojoMod.MOD_ID, "is_held"), (itemStack, clientWorld, livingEntity, seed) -> {
                return livingEntity != null && (livingEntity.getItemInHand(InteractionHand.MAIN_HAND) == itemStack || livingEntity.getItemInHand(InteractionHand.OFF_HAND) == itemStack) ? 1 : 0;
            });

            ItemBlockRenderTypes.setRenderLayer(ModBlocks.STONE_MASK.get(), RenderType.cutoutMipped());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.AJA_STONE_MASK.get(), RenderType.cutoutMipped());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.SLUMBERING_PILLARMAN.get(), RenderType.cutoutMipped());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.MAGICIANS_RED_FIRE.get(), RenderType.cutout());
            ModBlocks.WOODEN_COFFIN_OAK.values().forEach(coffinBlock -> ItemBlockRenderTypes.setRenderLayer(coffinBlock.get(), RenderType.cutout()));
            
            MenuScreens.register(ModContainers.WALKMAN.get(), WalkmanScreen::new);

            ClientEventHandler.init(mc);
            ActionsOverlayGui.init(mc);
            ControllerStand.init(mc);
            ControllerSoul.init(mc);
            ControllerConsciousness.init(mc);
            InputHandler.init(mc);
            InputHandler.getInstance().setActionsOverlay(ActionsOverlayGui.getInstance());
            LoopPlayerHandler.init();
            ClientTimeStopHandler.init(mc);
            ShaderEffectApplier.init(mc);
            FirstPersonHamonAura.init();
            TemporaryDimensionEffects.init();
            
            
            MarkerRenderer.registerMarkers(mc);
            
            statsStatsOverrideExamples();

            MCRecog.init();
        });
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void loadCustomArmorModels(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ArmorModelRegistry.loadArmorModels();
            
            ModPlayerAnimations.init();
        });
    }
    
//    @SubscribeEvent(priority = EventPriority.LOWEST)
//    public static void finalizeCustomPlayerRenderers(FMLClientSetupEvent event) {
//        Map<String, PlayerRenderer> skinMap = event.getMinecraftSupplier().get().getEntityRenderDispatcher().getSkinMap();
//    }

    @SubscribeEvent
    public static void registerLayers(net.minecraftforge.client.event.EntityRenderersEvent.AddLayers event) {
        PlayerRenderer defaultSkin = event.getSkin("default");
        if (defaultSkin != null) {
            addLayers(defaultSkin, false);
        }
        PlayerRenderer slimSkin = event.getSkin("slim");
        if (slimSkin != null) {
            addLayers(slimSkin, true);
        }
        // the mod added its living layers to every entity renderer; 1.20.1 offers
        // them per entity type, so the registered types are walked
        for (net.minecraft.world.entity.EntityType<?> entityType : net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES.getValues()) {
            EntityRenderer<?> renderer = event.getEntityRenderer((net.minecraft.world.entity.EntityType) entityType);
            if (renderer != null) {
                addLayersToEntities(renderer);
            }
        }
    }
    
    private static void addLayers(PlayerRenderer renderer, boolean slim) {
        renderer.addLayer(new KnifeLayer<>(renderer));
        renderer.addLayer(new TornadoOverdriveEffectLayer<>(renderer));
        renderer.addLayer(new DivineSandstormEffectLayer<>(renderer));
        renderer.addLayer(new BarrageFistAfterimagesLayer(renderer));
        renderer.addLayer(new EnergyRippleLayer<>(renderer));
        renderer.addLayer(new LadybugBroochLayer<>(renderer));
        renderer.addLayer(new InkLipsLayer<>(renderer));
        addLivingLayers(renderer);
        addBipedLayers(renderer, slim);
        renderer.addLayer(new GlovesLayer<>(renderer, new GlovesModel<>(0.3F, slim), slim));
        renderer.addLayer(new WindCloakLayer<>(renderer));
        renderer.addLayer(new VampireEyesLayer<>(renderer));
        renderer.addLayer(new SRSEEyesLayer<>(renderer));
        renderer.addLayer(new HamonProtectionLayer<>(renderer));
    }
    
    private static <T extends LivingEntity, M extends HumanoidModel<T>> void addLayersToEntities(EntityRenderer<?> renderer) {
        if (renderer instanceof LivingEntityRenderer<?, ?>) {
            LivingEntityRenderer<T, M> livingRenderer = (LivingEntityRenderer<T, M>) renderer;
            addLivingLayers(livingRenderer);
            if (((LivingEntityRenderer<?, ?>) renderer).getModel() instanceof HumanoidModel<?>) {
                addBipedLayers(livingRenderer, false);
            }
            else {
                livingRenderer.addLayer(new FrozenLayer<T, M>(livingRenderer, FrozenLayer.NON_BIPED_PATH));
            }
            livingRenderer.addLayer(new MobStuckArrowLayer<>(livingRenderer));
        }
    }
    
    private static <T extends LivingEntity, M extends EntityModel<T>> void addLivingLayers(LivingEntityRenderer<T, M> renderer) {
        renderer.addLayer(new HamonBurnLayer<>(renderer));
    }
    
    private static <T extends LivingEntity, M extends HumanoidModel<T>> void addBipedLayers(LivingEntityRenderer<T, M> renderer, boolean slim) {
        renderer.addLayer(new ZombieLayer<>(renderer));
        renderer.addLayer(new PillarmanLayer<>(renderer));
        renderer.addLayer(new FrozenLayer<>(renderer, FrozenLayer.BIPED_PATH));
        renderer.addLayer(new PillarmanBladesLayer<>(renderer, slim));
    }

    @SubscribeEvent
    public static void registerItemColoring(RegisterColorHandlersEvent.Item event) {
        
        event.register((stack, layer) -> {
            switch (layer) {
            case 1:
                return ClientUtil.discColor(StandDiscItem.getColor(stack));
            case 2:
                return StandDiscItem.getColor(stack);
            default:
                return -1;
            }
        }, ModItems.STAND_DISC.get());
        
        event.register((stack, layer) -> {
            if (layer != 1) return -1;

            Optional<DyeColor> dye = CassetteRecordedItem.getCassetteData(stack).map(cap -> cap.getDye());
            return dye.isPresent() ? dye.get().getTextColor() : 0xeff0e0;
        }, ModItems.CASSETTE_RECORDED.get());
    }
    
    
    @SubscribeEvent
    public static void registerTooltipComponents(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(com.github.standobyte.jojo.client.ui.text.JojoSpriteTooltipComponent.class, 
                com.github.standobyte.jojo.client.ui.text.JojoSpriteClientTooltipComponent::new);
    }
    
    @SubscribeEvent
    public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        // 1.20.1 bakes every model it finds, so the old "unreferenced textures" list
        // (a reflection into ModelBakery) has no equivalent and is not needed.
        event.register(new ModelResourceLocation(new ResourceLocation(JojoMod.MOD_ID, "tommy_gun_flipped"), "inventory"));
        StandDiscOverrideList.onModelRegistry(event);
    }
    
    // 1.16.5 added extra materials to ModelBakery's unreferenced texture set, which
    // 1.20.1's model loading does not have; the method had no callers left.
    
    
    @SubscribeEvent
    public static void onModelBake(ModelEvent.ModifyBakingResult event) {
        // 1.20.1 only allows replacing models here, the completed map is read only
        Map<ResourceLocation, BakedModel> registry = event.getModels();
        registerCustomBakedModel(MCUtil.id(ModItems.ROAD_ROLLER.get()), registry,                model -> new RoadRollerBakedModel(model));
        registerCustomBakedModel(MCUtil.id(ModItems.STAND_DISC.get()), registry,                 model -> new StandDiscISTERModel(model));
        registerCustomBakedModel(MCUtil.id(ModItems.POLAROID.get()), registry,                   model -> new ItemISTERModelWrapper(model).setCaptureEntity());
        registerCustomBakedModel(new ResourceLocation(JojoMod.MOD_ID, "tommy_gun_flipped"), registry,   model -> new ItemISTERModelWrapper(model));
        registerCustomBakedModel(MCUtil.id(ModItems.TOMMY_GUN.get()), registry,                  model -> new ItemISTERModelWrapper(model).refreshOverrides(registry));
        registerCustomBakedModel(MCUtil.id(ModItems.CLACKERS.get()), registry,                   model -> new ItemISTERModelWrapper(model).setCaptureEntity());
        CustomIconItem.onModelBake(registry);
    }
    
    public static void registerCustomBakedModel(ResourceLocation resLoc, 
            Map<ResourceLocation, BakedModel> modelRegistry, UnaryOperator<BakedModel> newModel) {
        ModelResourceLocation modelResLoc = new ModelResourceLocation(resLoc, "inventory");
        BakedModel existingModel = modelRegistry.get(modelResLoc);
        if (existingModel == null) {
            JojoMod.getLogger().error("Did not find original {} model in registry", modelResLoc);
        }
        else if (existingModel.isCustomRenderer()) {
            JojoMod.getLogger().error("Tried to replace {} model twice", modelResLoc);
        }
        else {
            modelRegistry.put(modelResLoc, newModel.apply(existingModel));
        }
    }
    
    
    @SubscribeEvent
    public static void onMcConstructor(RegisterParticleProvidersEvent event) {
        Minecraft mc = Minecraft.getInstance();
        event.registerSpriteSet(ModParticles.BLOOD.get(),                BloodParticle.Factory::new);
        event.registerSpriteSet(ModParticles.HAMON_SPARK.get(),          HamonSparkParticle.HamonParticleFactory::new);
        event.registerSpriteSet(ModParticles.HAMON_SPARK_BLUE.get(),     HamonSparkParticle.HamonParticleFactory::new);
        event.registerSpriteSet(ModParticles.HAMON_SPARK_YELLOW.get(),   HamonSparkParticle.HamonParticleFactory::new);
        event.registerSpriteSet(ModParticles.HAMON_SPARK_RED.get(),      HamonSparkParticle.HamonParticleFactory::new);
        event.registerSpriteSet(ModParticles.HAMON_SPARK_SILVER.get(),   HamonSparkParticle.HamonParticleFactory::new);
        event.registerSpriteSet(ModParticles.HAMON_AURA.get(),           HamonAuraParticle.Factory::new);
        event.registerSpriteSet(ModParticles.HAMON_AURA_BLUE.get(),      HamonAuraParticle.Factory::new);
        event.registerSpriteSet(ModParticles.HAMON_AURA_YELLOW.get(),    HamonAuraParticle.Factory::new);
        event.registerSpriteSet(ModParticles.HAMON_AURA_RED.get(),       HamonAuraParticle.Factory::new);
        event.registerSpriteSet(ModParticles.HAMON_AURA_SILVER.get(),    HamonAuraParticle.Factory::new);
        event.registerSpriteSet(ModParticles.HAMON_AURA_GREEN.get(),     HamonAuraParticle.Factory::new);
        event.registerSpriteSet(ModParticles.HAMON_AURA_RAINBOW.get(),   HamonAuraParticle.Factory::new);
        event.registerSpriteSet(ModParticles.BOILING_BLOOD_POP.get(),    LavaParticle.Provider::new);
        event.registerSpriteSet(ModParticles.METEORITE_VIRUS.get(),      MeteoriteVirusParticle.Factory::new);
        event.registerSpriteSet(ModParticles.MENACING.get(),             OnomatopoeiaParticle.GoFactory::new);
        event.registerSpriteSet(ModParticles.RESOLVE.get(),              OnomatopoeiaParticle.DoFactory::new);
        event.registerSpriteSet(ModParticles.SOUL_CLOUD.get(),           SoulCloudParticleFactory::new);
        event.registerSpriteSet(ModParticles.AIR_STREAM.get(),           AirStreamParticle.Factory::new);
        event.registerSpriteSet(ModParticles.FLAME_ONE_TICK.get(),       OneTickFlameParticle.Factory::new);
        event.registerSpriteSet(ModParticles.CD_RESTORATION.get(),       CDRestorationParticle.Factory::new);
        event.registerSpriteSet(ModParticles.RPS_ROCK.get(),             RPSPickPartile.Factory::new);
        event.registerSpriteSet(ModParticles.RPS_PAPER.get(),            RPSPickPartile.Factory::new);
        event.registerSpriteSet(ModParticles.RPS_SCISSORS.get(),         RPSPickPartile.Factory::new);
        event.registerSpriteSet(ModParticles.SANDSTORM.get(),         DivineSandstormParticle.Factory::new);
        event.registerSpriteSet(ModParticles.RIFT.get(),         AtmosphericRiftParticle.Factory::new);
        event.registerSpriteSet(ModParticles.LIGHT_SPARK.get(),       LightGlintParticle.Factory::new);
        event.registerSpriteSet(ModParticles.LIGHT_MODE_FLASH.get(),     LightModeFlashParticle.Factory::new);

        CustomParticlesHelper.saveSprites(mc);
        CustomResources.initCustomResourceManagers(mc);
        CustomRenderType.addExtraFixedBuffers(mc);
    }

    private static class SoulCloudParticleFactory extends PlayerCloudParticle.Provider {

        public SoulCloudParticleFactory(SpriteSet sprite) {
            super(sprite);
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
           Particle particle = super.createParticle(type, world, x, y, z, xSpeed, ySpeed, zSpeed);
           particle.setColor(1.0F, 1.0F, 0.25F);
           return particle;
        }
    }
    
    
    private static void statsStatsOverrideExamples() {
//        StandStatsRenderer.overrideCosmeticStats(
//                ModStands.GOLD_EXPERIENCE_REQUIEM.getStandType().getRegistryName(), 
//                new StandStatsRenderer.ICosmeticStandStats() {
//                    @Override public double statConvertedValue(StandStat stat, IStandPower standData, StandStats stats, float statLeveling) {
//                        return 0;
//                    }
//                });
//        
//        StandStatsRenderer.overrideCosmeticStats(
//                ModStands.MADE_IN_HEAVEN.getStandType().getRegistryName(), 
//                new StandStatsRenderer.ICosmeticStandStats() {
//                    @Override public String statRankLetter(StandStat stat, IStandPower standData, double statConvertedValue) {
//                        if (stat == StandStat.SPEED) {
//                            return "∞";
//                        }
//                        return StandStatsRenderer.ICosmeticStandStats.super.statRankLetter(stat, standData, statConvertedValue);
//                    }
//                });
//        
//        StandStatsRenderer.overrideCosmeticStats(
//                ModStands.NOTORIOUS_BIG.getStandType().getRegistryName(), 
//                new StandStatsRenderer.ICosmeticStandStats() {
//                    @Override public String statRankLetter(StandStat stat, IStandPower standData, double statConvertedValue) {
//                        switch (stat) {
//                        case SPEED:
//                        case RANGE:
//                        case DURABILITY:
//                            return "∞";
//                        default:
//                            return StandStatsRenderer.ICosmeticStandStats.super.statRankLetter(stat, standData, statConvertedValue);
//                        }
//                    }
//                });
//        
//        StandStatsRenderer.overrideCosmeticStats(
//                ModStands.BABY_FACE.getStandType().getRegistryName(), 
//                new StandStatsRenderer.ICosmeticStandStats() {
//                    @Override public double statConvertedValue(StandStat stat, IStandPower standData, StandStats stats, float statLeveling) {
//                        if (dependsOnEducation(stat)) {
//                            return 0;
//                        }
//                        return StandStatsRenderer.ICosmeticStandStats.super.statConvertedValue(stat, standData, stats, statLeveling);
//                    }
//                    
//                    @Override public String statRankLetter(StandStat stat, IStandPower standData, double statConvertedValue) {
//                        if (dependsOnEducation(stat)) {
//                            return StandStatsRenderer.REFERENCE_MARK;
//                        }
//                        return StandStatsRenderer.ICosmeticStandStats.super.statRankLetter(stat, standData, statConvertedValue);
//                    }
//                    
//                    @Override public List<ITextComponent> statTooltip(StandStat stat, IStandPower standData) {
//                        List<ITextComponent> tooltip = StandStatsRenderer.ICosmeticStandStats.super.statTooltip(stat, standData);
//                        if (dependsOnEducation(stat)) {
//                            tooltip.add(new TranslationTextComponent("babyface_stat_note")); // "babyface_stat_note": "(Depends on education)"
//                        }
//                        return tooltip;
//                    }
//                    
//                    private boolean dependsOnEducation(StandStat stat) {
//                        return true;
//                    }
//                });
    }
}