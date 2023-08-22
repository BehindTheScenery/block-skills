package net.impleri.blockskills.integrations.kubejs.events;

import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.impleri.blockskills.BlockHelper;
import net.impleri.blockskills.BlockSkills;
import net.impleri.blockskills.restrictions.Restriction;
import net.impleri.playerskills.restrictions.AbstractRestrictionBuilder;
import net.impleri.playerskills.utils.SkillResourceLocation;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class RestrictionJS extends Restriction {
    private static final ResourceKey<Registry<Restriction>> key = ResourceKey.createRegistryKey(SkillResourceLocation.of("block_restriction_builders_registry"));

    public static final RegistryInfo registry = RegistryInfo.of(key).type(Restriction.class);

    public RestrictionJS(Block block, Builder builder) {
        super(
                block,
                builder.condition,
                builder.breakable,
                builder.harvestable,
                builder.usable,
                builder.includeDimensions,
                builder.excludeDimensions,
                builder.includeBiomes,
                builder.excludeBiomes,
                builder.replacement
        );
    }

    public static class Builder extends AbstractRestrictionBuilder<Restriction> {
        public boolean breakable = true;
        public boolean harvestable = true;
        public boolean usable = true;
        public Block replacement;

        @HideFromJS
        public Builder(ResourceLocation id, MinecraftServer server) {
            super(id, server);
        }

        public Builder replaceWithBlock(String replacement) {
            var name = SkillResourceLocation.of(replacement);

            var block = BlockHelper.getBlock(name);
            if (BlockHelper.isEmptyBlock(block)) {
                BlockSkills.LOGGER.warn("Could not find any block named %s", name);
                return this;
            }

            this.replacement = block;

            return this;
        }

        public Builder replaceWithAir() {
            this.replacement = Blocks.AIR;

            return this;
        }

        public Builder breakable() {
            this.breakable = true;

            return this;
        }

        public Builder unbreakable() {
            this.breakable = false;

            return this;
        }

        public Builder harvestable() {
            this.harvestable = true;

            return this;
        }

        public Builder unharvestable() {
            this.harvestable = false;

            return this;
        }

        public Builder usable() {
            this.usable = true;

            return this;
        }

        public Builder unusable() {
            this.usable = false;

            return this;
        }

        public Builder nothing() {
            breakable = true;
            harvestable = true;
            usable = true;

            return this;
        }

        public Builder everything() {
            breakable = false;
            harvestable = false;
            usable = false;

            return this;
        }

        @HideFromJS
        @Override
        public RegistryInfo getRegistryType() {
            return registry;
        }

        @HideFromJS
        @Override
        public Restriction createObject() {
            return null;
        }

        @HideFromJS
        public Restriction createObject(Block block) {
            return new RestrictionJS(block, this);
        }
    }
}
