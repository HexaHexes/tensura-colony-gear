package org.bitwisemadness.tensuracolonygear.gear;

import com.minecolonies.api.compatibility.Compatibility;
import java.lang.reflect.Method;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Registers the approved Tensura/vanilla gear ladder into MineColonies'
 * equipment-level system through its public API ({@code Compatibility
 * .registerItemTier}), which backs {@code getItemLevel} and therefore the
 * level window every tool/weapon/bow/shield equipment type checks
 * ({@code 0 <= level <= maxEquipmentLevel}, with {@code maxEquipmentLevel =
 * buildingLevel - 1} below the building's max level and unbounded at max).
 *
 * <p>Armor deliberately registers nothing: MineColonies derives armor levels
 * from each piece's armor points against the vanilla per-slot distributions,
 * and there is no public override. Tensura's sets already grade sensibly that
 * way (silver ~ iron, low_magisteel ~ diamond, everything heavier pins to the
 * top grade).
 *
 * <p>Tensura is never referenced at compile time. Tool tiers are keyed by the
 * {@code TensuraToolTiers} enum constant names, everything else by material
 * fragments in the registry path, and the multitool's tier is read through a
 * reflective {@code getTier()} lookup.
 */
public final class GearLevelRegistry {

    private static final Logger LOG = LogManager.getLogger("tensura_colony_gear");

    public static final String TENSURA_NAMESPACE = "tensura";
    private static final String MULTITOOL_CLASS = "io.github.manasmods.tensura.item.tool.MultitoolItem";

    /**
     * Tensura tool-tier enum constant ({@code TensuraToolTiers}) to colony
     * level. Vanilla is compacted alongside it: netherite = 3 = the first
     * above-netherite Tensura rung, silver/low_magisteel sit with
     * iron/diamond.
     */
    private static final Map<String, Integer> TIER_LEVELS = Map.ofEntries(
            Map.entry("SILVER", 2),
            Map.entry("LOW_MAGISTEEL", 2),
            Map.entry("HIGH_MAGISTEEL", 3),
            Map.entry("MITHRIL", 3),
            Map.entry("ORICHALCUM", 3),
            Map.entry("PURE_MAGISTEEL", 4),
            Map.entry("ADAMANTITE", 4),
            Map.entry("HIHIIROKANE", 5),
            // vanilla tiers, keyed by enum constant name (net.minecraft.world.Tier impls)
            Map.entry("WOOD", 1),
            Map.entry("STONE", 1),
            Map.entry("GOLD", 1),
            Map.entry("IRON", 2),
            Map.entry("DIAMOND", 2),
            Map.entry("NETHERITE", 3));

    /**
     * Fallback for Tensura gear without a readable tier (shields, bows,
     * kunai): most specific fragments first so "low_magisteel" never falls
     * through to a hypothetical bare "magisteel".
     */
    private static final String[][] NAME_LEVELS = {
        {"pure_magisteel", "4"},
        {"high_magisteel", "3"},
        {"low_magisteel", "2"},
        {"hihiirokane", "5"},
        {"adamantite", "4"},
        {"orichalcum", "3"},
        {"mithril", "3"},
        {"silver", "2"},
    };

    private static Method multitoolTierMethod;

    private GearLevelRegistry() {}

    /** FMLCommonSetupEvent enqueueWork entry point; idempotent per game launch. */
    public static void init() {
        resolveMultitoolTierAccessor();
        int registered = 0;
        for (Item item : BuiltInRegistries.ITEM) {
            int level = levelFor(item);
            if (level >= 0) {
                Compatibility.registerItemTier(item, level);
                registered++;
            }
        }
        Compatibility.registerWeaponRecognizer(stack -> isMultitool(stack.getItem()));
        LOG.info(
                "Registered {} gear levels into MineColonies (ladder: wood/gold/stone=1 iron/diamond=2 netherite=3 |"
                        + " silver/low_magisteel=2 high_magisteel/mithril/orichalcum=3 pure_magisteel/adamantite=4"
                        + " hihiirokane=5)",
                registered);
    }

    /** Colony level for the item, or -1 when the item is not part of the ladder. */
    public static int levelFor(Item item) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        if (id == null) {
            return -1;
        }
        if (id.getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE)) {
            // Vanilla stays compact: the tier name map already carries the compaction.
            if (item instanceof TieredItem tiered) {
                return tierLevel(tiered.getTier());
            }
            return -1;
        }
        if (!id.getNamespace().equals(TENSURA_NAMESPACE)) {
            return -1;
        }
        Integer fromTier = tierLevelOrNull(tierOf(item));
        if (fromTier != null) {
            return fromTier;
        }
        if (!isGearShaped(item)) {
            return -1;
        }
        return levelFromName(id.getPath());
    }

    /** Multitools are plain Items with a tier; recognize them as custom weapons. */
    public static boolean isMultitool(Item item) {
        return item.getClass().getName().startsWith(MULTITOOL_CLASS);
    }

    private static void resolveMultitoolTierAccessor() {
        try {
            Class<?> multitool = Class.forName(MULTITOOL_CLASS, false, GearLevelRegistry.class.getClassLoader());
            multitoolTierMethod = multitool.getMethod("getTier");
        } catch (Throwable t) {
            LOG.info("No reflective multitool tier accessor available ({}); multitools fall back to name matching", t);
        }
    }

    /** The item's {@code Tier} via {@link TieredItem} or the reflective multitool accessor. */
    private static Tier tierOf(Item item) {
        if (item instanceof TieredItem tiered) {
            return tiered.getTier();
        }
        if (multitoolTierMethod != null && isMultitool(item)) {
            try {
                return (Tier) multitoolTierMethod.invoke(item);
            } catch (Throwable t) {
                return null;
            }
        }
        return null;
    }

    private static Integer tierLevelOrNull(Tier tier) {
        if (tier == null) {
            return null;
        }
        String name = tier instanceof Enum<?> e ? e.name() : tier.toString();
        return TIER_LEVELS.get(name);
    }

    private static int tierLevel(Tier tier) {
        Integer level = tierLevelOrNull(tier);
        return level == null ? -1 : level;
    }

    private static int levelFromName(String path) {
        for (String[] entry : NAME_LEVELS) {
            if (path.contains(entry[0])) {
                return Integer.parseInt(entry[1]);
            }
        }
        return -1;
    }

    /**
     * Only registry entries that could plausibly be equipment take the
     * name-based fallback, so food/materials/coins sharing a material word
     * are never registered as gear.
     */
    private static boolean isGearShaped(Item item) {
        return item instanceof TieredItem
                || item instanceof Equipable
                || item instanceof BowItem
                || item instanceof CrossbowItem
                || item instanceof ProjectileWeaponItem
                || (isMultitool(item) && multitoolTierMethod != null);
    }
}
