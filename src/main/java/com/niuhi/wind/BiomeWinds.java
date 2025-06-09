package com.niuhi.wind;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;

public class BiomeWinds {
    public record WindTypeWeights(float softChance, float normalChance, float heavyChance) {
        public WindTypeWeights {
            if (Math.abs(softChance + normalChance + heavyChance - 1.0f) > 0.001f) {
                throw new IllegalArgumentException("Wind type probabilities must sum to 1.0");
            }
        }
    }

    public static WindTypeWeights getWindTypeWeights(ClientWorld world, BlockPos pos) {
        RegistryEntry<Biome> biomeEntry = world.getBiome(pos);

        // Check against specific biome keys since Category was removed
        if (biomeEntry.matchesKey(BiomeKeys.PLAINS) ||
                biomeEntry.matchesKey(BiomeKeys.SUNFLOWER_PLAINS)) {
            return new WindTypeWeights(0.4f, 0.4f, 0.2f); // Windy open areas
        }

        if (biomeEntry.matchesKey(BiomeKeys.DESERT) ||
                biomeEntry.matchesKey(BiomeKeys.BADLANDS) ||
                biomeEntry.matchesKey(BiomeKeys.ERODED_BADLANDS) ||
                biomeEntry.matchesKey(BiomeKeys.WOODED_BADLANDS)) {
            return new WindTypeWeights(0.6f, 0.3f, 0.1f); // Calmer, arid regions
        }

        if (biomeEntry.matchesKey(BiomeKeys.FOREST) ||
                biomeEntry.matchesKey(BiomeKeys.FLOWER_FOREST) ||
                biomeEntry.matchesKey(BiomeKeys.BIRCH_FOREST) ||
                biomeEntry.matchesKey(BiomeKeys.OLD_GROWTH_BIRCH_FOREST) ||
                biomeEntry.matchesKey(BiomeKeys.DARK_FOREST)) {
            return new WindTypeWeights(0.3f, 0.5f, 0.2f); // Sheltered but breezy
        }

        if (biomeEntry.matchesKey(BiomeKeys.SAVANNA) ||
                biomeEntry.matchesKey(BiomeKeys.SAVANNA_PLATEAU) ||
                biomeEntry.matchesKey(BiomeKeys.WINDSWEPT_SAVANNA)) {
            return new WindTypeWeights(0.5f, 0.3f, 0.2f); // Open, moderate winds
        }

        if (biomeEntry.matchesKey(BiomeKeys.TAIGA) ||
                biomeEntry.matchesKey(BiomeKeys.OLD_GROWTH_PINE_TAIGA) ||
                biomeEntry.matchesKey(BiomeKeys.OLD_GROWTH_SPRUCE_TAIGA) ||
                biomeEntry.matchesKey(BiomeKeys.SNOWY_TAIGA)) {
            return new WindTypeWeights(0.3f, 0.4f, 0.3f); // Colder, stronger winds
        }

        if (biomeEntry.matchesKey(BiomeKeys.WINDSWEPT_HILLS) ||
                biomeEntry.matchesKey(BiomeKeys.WINDSWEPT_GRAVELLY_HILLS) ||
                biomeEntry.matchesKey(BiomeKeys.WINDSWEPT_FOREST) ||
                biomeEntry.matchesKey(BiomeKeys.STONY_PEAKS) ||
                biomeEntry.matchesKey(BiomeKeys.JAGGED_PEAKS) ||
                biomeEntry.matchesKey(BiomeKeys.FROZEN_PEAKS)) {
            return new WindTypeWeights(0.2f, 0.4f, 0.4f); // High winds at altitude
        }

        if (biomeEntry.matchesKey(BiomeKeys.JUNGLE) ||
                biomeEntry.matchesKey(BiomeKeys.SPARSE_JUNGLE) ||
                biomeEntry.matchesKey(BiomeKeys.BAMBOO_JUNGLE)) {
            return new WindTypeWeights(0.5f, 0.4f, 0.1f); // Dense, calmer winds
        }

        if (biomeEntry.matchesKey(BiomeKeys.SWAMP) ||
                biomeEntry.matchesKey(BiomeKeys.MANGROVE_SWAMP)) {
            return new WindTypeWeights(0.5f, 0.4f, 0.1f); // Humid, lighter winds
        }

        if (biomeEntry.matchesKey(BiomeKeys.RIVER) ||
                biomeEntry.matchesKey(BiomeKeys.FROZEN_RIVER)) {
            return new WindTypeWeights(0.4f, 0.4f, 0.2f); // Similar to plains
        }

        if (biomeEntry.matchesKey(BiomeKeys.BEACH) ||
                biomeEntry.matchesKey(BiomeKeys.SNOWY_BEACH) ||
                biomeEntry.matchesKey(BiomeKeys.STONY_SHORE)) {
            return new WindTypeWeights(0.3f, 0.4f, 0.3f); // Coastal breezes
        }

        if (biomeEntry.matchesKey(BiomeKeys.OCEAN) ||
                biomeEntry.matchesKey(BiomeKeys.DEEP_OCEAN) ||
                biomeEntry.matchesKey(BiomeKeys.LUKEWARM_OCEAN) ||
                biomeEntry.matchesKey(BiomeKeys.WARM_OCEAN) ||
                biomeEntry.matchesKey(BiomeKeys.COLD_OCEAN) ||
                biomeEntry.matchesKey(BiomeKeys.DEEP_LUKEWARM_OCEAN) ||
                biomeEntry.matchesKey(BiomeKeys.DEEP_COLD_OCEAN) ||
                biomeEntry.matchesKey(BiomeKeys.DEEP_FROZEN_OCEAN) ||
                biomeEntry.matchesKey(BiomeKeys.FROZEN_OCEAN)) {
            return new WindTypeWeights(0.2f, 0.4f, 0.4f); // Strong sea winds
        }

        // Snowy/Cold biomes
        if (biomeEntry.matchesKey(BiomeKeys.SNOWY_PLAINS) ||
                biomeEntry.matchesKey(BiomeKeys.ICE_SPIKES) ||
                biomeEntry.matchesKey(BiomeKeys.SNOWY_SLOPES) ||
                biomeEntry.matchesKey(BiomeKeys.GROVE) ||
                biomeEntry.matchesKey(BiomeKeys.FROZEN_PEAKS)) {
            return new WindTypeWeights(0.2f, 0.3f, 0.5f); // Cold, harsh winds
        }

        // Cave biomes (underground - calmer)
        if (biomeEntry.matchesKey(BiomeKeys.DRIPSTONE_CAVES) ||
                biomeEntry.matchesKey(BiomeKeys.LUSH_CAVES) ||
                biomeEntry.matchesKey(BiomeKeys.DEEP_DARK)) {
            return new WindTypeWeights(0.7f, 0.3f, 0.0f); // Very calm underground
        }

        // Special/Rare biomes
        if (biomeEntry.matchesKey(BiomeKeys.MUSHROOM_FIELDS)) {
            return new WindTypeWeights(0.5f, 0.4f, 0.1f); // Calm island winds
        }

        if (biomeEntry.matchesKey(BiomeKeys.CHERRY_GROVE)) {
            return new WindTypeWeights(0.4f, 0.5f, 0.1f); // Gentle spring breezes
        }

        if (biomeEntry.matchesKey(BiomeKeys.PALE_GARDEN)) {
            return new WindTypeWeights(0.3f, 0.4f, 0.3f); // Eerie, unsettling winds
        }

        if (biomeEntry.matchesKey(BiomeKeys.MEADOW)) {
            return new WindTypeWeights(0.5f, 0.4f, 0.1f); // Gentle mountain meadow
        }


        // Fallback for custom biomes or any missed vanilla biomes
        return new WindTypeWeights(0.4f, 0.4f, 0.2f);
    }
}