package im.aloweeed;

import com.velocitypowered.api.scheduler.ScheduledTask;
import net.elytrium.limboapi.api.Limbo;
import net.elytrium.limboapi.api.LimboFactory;
import net.elytrium.limboapi.api.LimboSessionHandler;
import net.elytrium.limboapi.api.chunk.Dimension;
import net.elytrium.limboapi.api.chunk.VirtualBlock;
import net.elytrium.limboapi.api.chunk.VirtualChunk;
import net.elytrium.limboapi.api.player.LimboPlayer;
import net.elytrium.limboapi.api.protocol.PreparedPacket;
import net.elytrium.limboapi.api.protocol.packets.PacketFactory;
import net.elytrium.limboapi.server.world.SimpleBlock;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.title.TitlePart;

import java.util.ArrayList;
import java.util.Random;

public class FilterSessionHandler implements LimboSessionHandler {
    private long fallTime = -1;

    private final LimboFactory factory;
    private final PacketFactory packets;
    private LimboPlayer player;
    private final FloriumFilter plugin;

    private double x, y, z;
    private double prevY;
    private float yaw, pitch;
    private boolean onGround;

    private int duplicate = 0;

    private ScheduledTask task;

    private ArrayList<Vec> blocks = new ArrayList<Vec>();

    PreparedPacket teleportPacket, chunkPacket, botMessage;

    private record Vec(int x, int y, int z) {

    }

    public FilterSessionHandler(LimboFactory factory, FloriumFilter plugin) {
        this.factory = factory;
        this.packets = factory.getPacketFactory();
        this.plugin = plugin;
    }

    private void generateParkour(VirtualChunk chunk, int startX, int startY, int startZ, int length) {
        Random rand = new Random();
        int x = startX;
        int y = startY;
        int z = startZ;

        VirtualBlock baseBlock = factory.createSimpleBlock("minecraft:stone");
        VirtualBlock finalBlock = factory.createSimpleBlock("minecraft:gold_block");

        for (int i = 0; i < length; i++) {
            if (z < 0 || z > 15 || x < 0 || x > 15 || y < 0) break;
            boolean isLastBlock = (i == length - 1);
            chunk.setBlock(x, y, z,
                    new SimpleBlock(true, false, true,
                            isLastBlock ? finalBlock.getModernID() : baseBlock.getModernID()
                    )
            );
            blocks.add(new Vec(x, y, z));

            x += rand.nextInt(-1, 2);
            y += rand.nextInt(-1, 2);
            z += 3;
        }
    }

    @Override
    public void onSpawn(Limbo server, LimboPlayer player) {
        this.player = player;

        VirtualChunk chunk = factory.createVirtualChunk(0, 0);
//        chunk.setBlock(8, 0, 8, new SimpleBlock(true, false, true, (short)1));
        generateParkour(chunk, 8, 66, 0, 15);

        teleportPacket = factory.createPreparedPacket()
                .prepare(this.packets.createPositionRotationPacket(8.5, 67, 0, 0, 0, false, 0, false))
                .build();
        chunkPacket = factory.createPreparedPacket()
                .prepare(this.packets.createChunkDataPacket(chunk.getFullChunkSnapshot(), Dimension.OVERWORLD))
                .build();
//        PreparedPacket packet = factory.createPreparedPacket()
//                .prepare(teleportPacket).prepare(chunkPacket)
//                .build();
        player.getProxyPlayer().sendTitlePart(TitlePart.TITLE, FloriumFilter.it.serializer.deserialize(Settings.it.STRINGS.TITLE));
        player.getProxyPlayer().sendTitlePart(TitlePart.SUBTITLE, FloriumFilter.it.serializer.deserialize(Settings.it.STRINGS.SUBTITLE));

        player.writePacket(chunkPacket);
        player.writePacket(teleportPacket);

        this.plugin.logger.info(player.getProxyPlayer().getUsername() + " spawned in filter.");

//        this.task = this.plugin.server.getScheduler().buildTask(this.plugin, this::tick).repeat(Duration.ofMillis(50)).schedule();
    }
    //
    private void tick() {
        if (this.fallTime == -1 && this.y != 30) {
            this.fallTime = System.currentTimeMillis();
        }
    }

    private void checkBlock(double x, double y, double z) {
        if (blocks.isEmpty()) {
            completed();
            return;
        }
//        if (blocks.size() > 3) {
//            Vec l = blocks.getLast();
//        }

        Vec v = blocks.getFirst();
        if (Math.sqrt(Math.pow(v.x - x, 2) + Math.pow(v.y + 1 - y, 2) + Math.pow(v.z - z, 2)) < 1.5) {
            blocks.removeFirst();
            player.getProxyPlayer().playSound(Sound.sound(Key.key("minecraft:entity.experience_orb.pickup"), Sound.Source.PLAYER, 1f, 1f));
            return;
        }

        if (Settings.it.VALIDATE.ENABLED) {
            Vec last = blocks.getLast();
            if (Math.sqrt(Math.pow(last.x - x, 2) + Math.pow(last.y + 1 - y, 2) + Math.pow(last.z - z, 2)) < 1.5) {
                player.getProxyPlayer().disconnect(FloriumFilter.it.serializer.deserialize(Settings.it.VALIDATE.SKIP_BLOCK_KICK));
            }
        }
    }

    @Override
    public void onMove(double posX, double posY, double posZ) {
        this.prevY = this.y;

        this.x = posX;
        this.y = posY;
        this.z = posZ;

        checkBlock(posX, posY, posZ);

        if (this.y < 50) {
            player.writePacketAndFlush(teleportPacket);
        }
    }

    @Override
    public void onMove(double posX, double posY, double posZ, float yaw, float pitch) {
        this.prevY = this.y;

        this.x = posX;
        this.y = posY;
        this.z = posZ;
        this.yaw = yaw;
        this.pitch = pitch;

        checkBlock(posX, posY, posZ);

        if (this.y < 50) {
            player.writePacketAndFlush(teleportPacket);
        }
    }

    @Override
    public void onGround(boolean onGround) {
        this.onGround = onGround;
    }

    @Override
    public void onRotate(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    private void completed() {
        this.player.disconnect();
        this.plugin.checked.add(this.player.getProxyPlayer().getUniqueId());
    }
}
