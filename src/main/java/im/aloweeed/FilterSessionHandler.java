package im.aloweeed;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.scheduler.ScheduledTask;
import com.velocitypowered.proxy.protocol.packet.chat.ComponentHolder;
import com.velocitypowered.proxy.protocol.packet.title.GenericTitlePacket;
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

    // config cache $
    public static int minX, maxX, minY, maxY, minZ, maxZ;

    public static ArrayList<VirtualBlock> base_blocks = new ArrayList<>();
    public static ArrayList<VirtualBlock> final_blocks = new ArrayList<>();

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

    PreparedPacket teleportPacket, chunkPacket, sound;

    private record Vec(int x, int y, int z) {

    }

    public FilterSessionHandler(LimboFactory factory, FloriumFilter plugin) {
        this.factory = factory;
        this.packets = factory.getPacketFactory();
        this.plugin = plugin;
    }

    private void generateParkour(VirtualChunk chunk) {
        Random rand = new Random();
        int x = 8;
        int y = 66;
        int z = 0;

        int lastX = 0, lastY = 0, lastZ = 0;

//        VirtualBlock baseBlock = factory.createSimpleBlock("minecraft:stone");
//        VirtualBlock finalBlock = factory.createSimpleBlock("minecraft:gold_block");

        for (int i = 0; i < 15; i++) {
            if (z < 0 || z > 14 || x < 0 || x > 15 || y < 0) break;
            lastX = x;
            lastY = y;
            lastZ = z;
            chunk.setBlock(x, y, z,
                    new SimpleBlock(true, false, true,
                            base_blocks.get(rand.nextInt(base_blocks.size())).getModernID()
                    )
            );
            blocks.add(new Vec(x, y, z));

            x += rand.nextInt(minX, maxX + 1);
            y += rand.nextInt(minY, maxY + 1);
            z += rand.nextInt(minZ, maxZ + 1);
        }
        chunk.setBlock(lastX, lastY, lastZ,
                new SimpleBlock(true, false, true,
                        final_blocks.get(rand.nextInt(final_blocks.size())).getModernID()
                )
        );
    }

    @Override
    public void onSpawn(Limbo server, LimboPlayer player) {
        this.player = player;

        VirtualChunk chunk = factory.createVirtualChunk(0, 0);
//        chunk.setBlock(8, 0, 8, new SimpleBlock(true, false, true, (short)1));
        generateParkour(chunk);

        PreparedPacket preparedPacket = factory.createPreparedPacket();

        teleportPacket = preparedPacket
                .prepare(this.packets.createPositionRotationPacket(8.5, 67, 0, 0, 0, false, 0, false))
                .build();
        chunkPacket = preparedPacket
                .prepare(this.packets.createChunkDataPacket(chunk.getFullChunkSnapshot(), Dimension.OVERWORLD))
                .build();
//        PreparedPacket packet = factory.createPreparedPacket()
//                .prepare(teleportPacket).prepare(chunkPacket)
//                .build();
//        player.getProxyPlayer().sendTitlePart(TitlePart.TITLE, FloriumFilter.it.serializer.deserialize(Settings.it.STRINGS.TITLE));
//        player.getProxyPlayer().sendTitlePart(TitlePart.SUBTITLE, FloriumFilter.it.serializer.deserialize(Settings.it.STRINGS.SUBTITLE));

        player.writePacket(
                preparedPacket.prepare(version -> {
                    GenericTitlePacket packet =
                            GenericTitlePacket.constructTitlePacket(
                                    GenericTitlePacket.ActionType.SET_TITLE,
                                    version
                            );

                    packet.setComponent(
                            new ComponentHolder(version, FloriumFilter.it.serializer.deserialize(Settings.it.STRINGS.TITLE))
                    );

                    return packet;
                }, ProtocolVersion.MINECRAFT_1_8)
        );
        player.writePacket(
                preparedPacket.prepare(version -> {
                    GenericTitlePacket packet =
                            GenericTitlePacket.constructTitlePacket(
                                    GenericTitlePacket.ActionType.SET_SUBTITLE,
                                    version
                            );

                    packet.setComponent(
                            new ComponentHolder(version, FloriumFilter.it.serializer.deserialize(Settings.it.STRINGS.SUBTITLE))
                    );

                    return packet;
                }, ProtocolVersion.MINECRAFT_1_8)
        );
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
//            player.writePacketAndFlush(factory.createPreparedPacket()
//                    .prepare(new ClientboundCustomSoundPacket(
//                            Key.key("minecraft:entity.experience_orb.pickup"),
//                            Sound.Source.PLAYER, x, y, z, 1f, 1f))
//                    .build());
            blocks.removeFirst();
//            player.getProxyPlayer().playSound(Sound.sound(Key.key("minecraft:entity.experience_orb.pickup"), Sound.Source.PLAYER, 1f, 1f));
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
