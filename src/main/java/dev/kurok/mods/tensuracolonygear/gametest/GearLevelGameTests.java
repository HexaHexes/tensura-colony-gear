package dev.kurok.mods.tensuracolonygear.gametest;

import com.minecolonies.api.compatibility.Compatibility;
import dev.kurok.mods.tensuracolonygear.gear.GearLevelRegistry;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Verifies the registered gear ladder end to end through MineColonies' own
 * lookup ({@code Compatibility.getItemLevel}) - the exact value the equipment
 * windows check in-game. Tensura items are discovered from the registry by
 * tier enum name / class name / material fragment, so the tests do not depend
 * on individual item ids and stay valid across Tensura updates.
 */
@GameTestHolder("tensura_colony_gear")
@PrefixGameTestTemplate(false)
public class GearLevelGameTests {

    /** The approved ladder, mirrored here independently of the registry code. */
    private static final Map<String, Integer> EXPECTED_TIER_LEVELS = Map.ofEntries(
            Map.entry("SILVER", 2),
            Map.entry("LOW_MAGISTEEL", 2),
            Map.entry("HIGH_MAGISTEEL", 3),
            Map.entry("MITHRIL", 3),
            Map.entry("ORICHALCUM", 3),
            Map.entry("PURE_MAGISTEEL", 4),
            Map.entry("ADAMANTITE", 4),
            Map.entry("HIHIIROKANE", 5));

    private static final Map<String, Integer> EXPECTED_NAME_LEVELS = Map.ofEntries(
            Map.entry("pure_magisteel", 4),
            Map.entry("high_magisteel", 3),
            Map.entry("low_magisteel", 2),
            Map.entry("hihiirokane", 5),
            Map.entry("adamantite", 4),
            Map.entry("orichalcum", 3),
            Map.entry("mithril", 3),
            Map.entry("silver", 2));

    private static Item item(String id) {
        return BuiltInRegistries.ITEM
                .getOptional(ResourceLocation.parse(id))
                .orElseThrow(() -> new GameTestAssertException("missing item " + id));
    }

    private static void assertLevel(String id, int expected) {
        int actual = Compatibility.getItemLevel(new ItemStack(item(id)));
        if (actual != expected) {
            throw new GameTestAssertException(id + " has colony level " + actual + " expected " + expected);
        }
    }

    /** Vanilla stays compacted: two tiers cover wood..diamond, netherite is the first TR rung. */
    @GameTest(template = "empty")
    public static void vanillaToolsCompact(GameTestHelper helper) {
        assertLevel("minecraft:wooden_pickaxe", 1);
        assertLevel("minecraft:stone_sword", 1);
        assertLevel("minecraft:golden_axe", 1);
        assertLevel("minecraft:iron_hoe", 2);
        assertLevel("minecraft:diamond_sword", 2);
        assertLevel("minecraft:netherite_pickaxe", 3);
        helper.succeed();
    }

    /** Every Tensura tool tier lands on its approved rung (discovered via tier enum name). */
    @GameTest(template = "empty")
    public static void tensuraToolTiersMapToLadder(GameTestHelper helper) {
        Map<String, Integer> found = new LinkedHashMap<>();
        for (Item item : BuiltInRegistries.ITEM) {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
            if (id == null
                    || !id.getNamespace().equals(GearLevelRegistry.TENSURA_NAMESPACE)
                    || !(item instanceof TieredItem tiered)) {
                continue;
            }
            String tierName = tiered.getTier() instanceof Enum<?> e ? e.name() : tiered.getTier().toString();
            Integer expected = EXPECTED_TIER_LEVELS.get(tierName);
            if (expected == null) {
                continue;
            }
            int actual = Compatibility.getItemLevel(new ItemStack(item));
            if (actual != expected) {
                throw new GameTestAssertException(
                        id + " (tier " + tierName + ") has colony level " + actual + " expected " + expected);
            }
            found.putIfAbsent(tierName, actual);
        }
        for (Map.Entry<String, Integer> expected : EXPECTED_TIER_LEVELS.entrySet()) {
            if (!found.containsKey(expected.getKey())) {
                throw new GameTestAssertException("no Tensura TieredItem found for tier " + expected.getKey());
            }
        }
        helper.succeed();
    }

    /**
     * Name-based fallback covers gear without a readable tier (shields, bows),
     * and every Tensura item we place on the ladder keeps its level at runtime
     * (i.e. MineColonies never overwrites our registration with its own
     * defaults - its bow/tool discovery runs later than our common setup).
     */
    @GameTest(template = "empty")
    public static void nameFallbackAndBoundedLevels(GameTestHelper helper) {
        int fallbackMatches = 0;
        int ours = 0;
        for (Item item : BuiltInRegistries.ITEM) {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
            if (id == null || !id.getNamespace().equals(GearLevelRegistry.TENSURA_NAMESPACE)) {
                continue;
            }
            int expected = GearLevelRegistry.levelFor(item);
            if (expected < 0) {
                // Not our ladder (e.g. short_bow keeps MineColonies' own bow default 0) - not ours to police.
                continue;
            }
            if (expected < 1 || expected > 5) {
                throw new GameTestAssertException(id + " placed outside the 1..5 ladder (" + expected + ")");
            }
            int actual = Compatibility.getItemLevel(new ItemStack(item));
            if (actual != expected) {
                throw new GameTestAssertException(
                        id + " has runtime colony level " + actual + " but was registered at " + expected);
            }
            ours++;
            String cls = item.getClass().getName();
            boolean fallbackShaped = cls.endsWith("SimpleShieldItem")
                    || cls.endsWith("SimpleBowItem")
                    || cls.endsWith("SimpleCrossbowItem")
                    || cls.endsWith("KunaiItem");
            if (fallbackShaped && !(item instanceof TieredItem)) {
                for (Map.Entry<String, Integer> entry : EXPECTED_NAME_LEVELS.entrySet()) {
                    if (id.getPath().contains(entry.getKey())) {
                        fallbackMatches++;
                        break;
                    }
                }
            }
        }
        if (ours == 0) {
            throw new GameTestAssertException("no Tensura gear registered at all");
        }
        if (fallbackMatches == 0) {
            throw new GameTestAssertException("no Tensura shield/bow/kunai matched the name-based fallback");
        }
        helper.succeed();
    }

    /**
     * Multitools get their tier's level and are flagged as custom weapons for
     * MineColonies. Tensura 2.0.1.3 ships the MultitoolItem class but
     * registers no items of it, so when none exist the test instead pins that
     * the recognizer has no false positives.
     */
    @GameTest(template = "empty")
    public static void multitoolLeveledAndRecognized(GameTestHelper helper) {
        Item multitool = null;
        for (Item item : BuiltInRegistries.ITEM) {
            if (GearLevelRegistry.isMultitool(item)) {
                multitool = item;
                break;
            }
        }
        if (multitool == null) {
            if (Compatibility.isCustomWeapon(new ItemStack(item("tensura:short_bow")))) {
                throw new GameTestAssertException("weapon recognizer false-positives on a plain Tensura bow");
            }
            helper.succeed();
            return;
        }
        ItemStack stack = new ItemStack(multitool);
        int level = Compatibility.getItemLevel(stack);
        Integer expected = EXPECTED_TIER_LEVELS.get(tierNameOfMultitool(multitool));
        if (expected == null || level != expected) {
            throw new GameTestAssertException("multitool level " + level + " does not match its tier ("
                    + tierNameOfMultitool(multitool) + " -> " + expected + ")");
        }
        if (!Compatibility.isCustomWeapon(stack)) {
            throw new GameTestAssertException("multitool not recognized via registerWeaponRecognizer");
        }
        helper.succeed();
    }

    private static String tierNameOfMultitool(Item multitool) {
        try {
            Object tier = multitool.getClass().getMethod("getTier").invoke(multitool);
            return tier instanceof Enum<?> e ? e.name() : String.valueOf(tier);
        } catch (Throwable t) {
            throw new GameTestAssertException("multitool has no readable getTier() - " + t);
        }
    }
}
