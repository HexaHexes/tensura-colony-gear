package dev.kurok.mods.tensuracolonygear;

import dev.kurok.mods.tensuracolonygear.gear.GearLevelRegistry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

/**
 * Tensura Colony Gear - folds Tensura: Reincarnated gear into MineColonies'
 * citizen weapon/tool/armor ladder through MineColonies' public registration
 * API only (no mixins, no access transformers).
 */
@Mod(TensuraColonyGear.MOD_ID)
public class TensuraColonyGear {

    public static final String MOD_ID = "tensura_colony_gear";

    public TensuraColonyGear(IEventBus modBus) {
        modBus.addListener((FMLCommonSetupEvent event) -> event.enqueueWork(GearLevelRegistry::init));
    }
}
