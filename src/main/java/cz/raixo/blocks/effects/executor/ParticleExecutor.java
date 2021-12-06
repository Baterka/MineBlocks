package cz.raixo.blocks.effects.executor;

import cz.raixo.blocks.MineBlocksPlugin;
import cz.raixo.blocks.effects.Effect;
import cz.raixo.blocks.models.MineBlock;
import cz.raixo.blocks.util.WeakConcurrentHashMap;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ParticleExecutor {

    private final MineBlocksPlugin plugin;
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final WeakConcurrentHashMap<MineBlock, List<Player>> nearByPlayers = new WeakConcurrentHashMap<>(2000);
    private final Map<Effect, ExecutionToken> tokens = new HashMap<>();
    private boolean disabled = false;

    public ParticleExecutor(MineBlocksPlugin plugin) {
        this.plugin = plugin;
        for (MineBlock block : plugin.getBlocks()) {
            for (Effect effect : block.getEffects()) {
                runEffect(effect, block);
            }
        }
    }

    public void runEffect(Effect effect, MineBlock mineBlock) {
        if (this.isDisabled()) return;
        if (!plugin.isRegistered(mineBlock)) return;
        if (!mineBlock.getEffects().contains(effect)) return;
        this.executor.execute(() -> {
            Block block = mineBlock.getLocation().getBlock();
            List<Player> players = getNearByPlayers(mineBlock);
            ExecutionToken executionToken = new ExecutionToken.Simple(this) {

                @Override
                public void executeNow() {
                    if (ParticleExecutor.this.tokens.get(effect) != this) return;
                    ParticleExecutor.this.tokens.remove(effect);
                    ParticleExecutor.this.runEffect(effect, mineBlock);
                }

                @Override
                public void executeAfter(long millis) {
                    ParticleExecutor.this.scheduler.schedule(() -> ParticleExecutor.this.executor.execute(this::executeNow), millis, TimeUnit.MILLISECONDS);
                }

                @Override
                public boolean shouldStop() {
                    return super.shouldStop() || !mineBlock.getEffects().contains(effect) || !MineBlocksPlugin.getInstance().isRegistered(mineBlock);
                }

            };
            tokens.put(effect, executionToken);
            effect.make(block, mineBlock, players, executionToken);
        });
    }

    public void disable() {
        if (executor.isShutdown()) return;
        this.disabled = true;
        executor.shutdown();
        this.nearByPlayers.stop();
    }

    protected boolean isDisabled() {
        return disabled;
    }

    public List<Player> getNearByPlayers(MineBlock mineBlock) {
        if (nearByPlayers.containsKey(mineBlock)) return nearByPlayers.get(mineBlock);
        return nearByChunks(mineBlock.getLocation().getChunk()).stream().flatMap(chunk -> Arrays.stream(chunk.getEntities())).filter(entity -> entity instanceof Player).map(entity -> (Player) entity).collect(Collectors.toCollection(LinkedList::new));
    }

    public List<Chunk> nearByChunks(Chunk forChunk) {
        World world = forChunk.getWorld();
        int x = forChunk.getX();
        int z = forChunk.getZ();
        return new LinkedList<>(Arrays.asList(
                forChunk,
                world.getChunkAt(x, z + 1),
                world.getChunkAt(x, z - 1),
                world.getChunkAt(x + 1, z),
                world.getChunkAt(x - 1, z),
                world.getChunkAt(x - 1, z - 1),
                world.getChunkAt(x - 1, z + 1),
                world.getChunkAt(x + 1, z - 1),
                world.getChunkAt(x + 1, z + 1)
        ));
    }

}
