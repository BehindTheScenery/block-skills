package net.impleri.blockskills;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.InteractionEvent;
import dev.architectury.event.events.common.PlayerEvent;
import net.impleri.blockskills.api.Restrictions;
import net.impleri.playerskills.server.events.SkillChangedEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;

public class BlockEvents {
    public void registerEventHandlers() {
        InteractionEvent.LEFT_CLICK_BLOCK.register(this::beforeMineBlock);
        InteractionEvent.RIGHT_CLICK_BLOCK.register(this::beforeUseItemBlock);
        PlayerEvent.PLAYER_JOIN.register(this::onJoin);
        PlayerEvent.PLAYER_QUIT.register(this::onQuit);
        SkillChangedEvent.EVENT.register(this::onSkillChanged);
    }

    private EventResult beforeMineBlock(Player player, InteractionHand hand, BlockPos pos, Direction face) {
        BlockSkills.LOGGER.info("Call to arch:leftClickBlock");
        var block = BlockHelper.getBlockState(pos, player.getLevel());
        var blockName = BlockHelper.getBlockName(block);

        if (!BlockHelper.isBreakable(player, block)) {
            BlockSkills.LOGGER.info("{} cannot break {}", player.getName().getString(), blockName);
            return EventResult.interruptFalse();
        }

        BlockSkills.LOGGER.info("{} is about to mine block {}", player.getName().getString(), blockName);

        return EventResult.pass();
    }

    private EventResult beforeUseItemBlock(Player player, InteractionHand hand, BlockPos pos, Direction face) {
        BlockSkills.LOGGER.info("Call to arch:rightClickBlock");
        var block = BlockHelper.getBlockState(pos, player.getLevel());
        var blockName = BlockHelper.getBlockName(block);

        if (!BlockHelper.isUsable(player, block)) {
            BlockSkills.LOGGER.info("{} cannot interact with block {}", player.getName().getString(), blockName);
            return EventResult.interruptFalse();
        }

        BlockSkills.LOGGER.info("{} is about to interact with block {}", player.getName().getString(), blockName);
        return EventResult.pass();
    }

    private final HashMap<Player, Long> playerMap = new HashMap<>();

    private void onJoin(ServerPlayer player) {
        playerMap.put(player, BlockHelper.getReplacementsCountFor(player));
    }

    private void onQuit(ServerPlayer player) {
        playerMap.remove(player);
    }

    private void onSkillChanged(SkillChangedEvent<?> event) {
        var eventPlayerId = event.getPlayer().getUUID();
        var playerOption = playerMap.keySet().stream().filter(player1 -> eventPlayerId.equals(player1.getUUID())).findFirst();

        // We *should* always have a matching ServerPlayer in playerMap, but this handles the odd case where we don't
        var originalCount = (playerOption.isEmpty()) ? 0 : playerMap.get(playerOption.get());
        var newCount = BlockHelper.getReplacementsCountFor(event.getPlayer());

        // We're assuming that the number of replaced blocks should change if a skill change actually changes replacements
        // If we run into an issue where a skills change should trigger a refresh but the count difference doesn't change,
        // we'll have to rework this

        if (originalCount == newCount) {
            return;
        }

        var player = playerOption.orElse(event.getPlayer());
        playerMap.put(player, newCount);
        Restrictions.INSTANCE.clearPlayerCache(player);

        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.getLevel().getChunkSource().chunkMap.updatePlayerStatus(serverPlayer, true);
        }
    }
}
