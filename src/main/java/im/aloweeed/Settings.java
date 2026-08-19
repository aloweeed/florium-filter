package im.aloweeed;

import net.elytrium.limboapi.api.chunk.Dimension;
import net.elytrium.limboapi.thirdparty.commons.config.YamlConfig;
import net.elytrium.limboapi.thirdparty.commons.kyori.serialization.Serializers;

import java.util.ArrayList;
import java.util.List;

public class Settings extends YamlConfig {
    @Ignore
    public static final Settings it = new Settings();

    @Comment({
            "Made by t.me/alow3d_dev",
            "",
            "Available serializers:",
            "LEGACY_AMPERSAND - \"&c&lExample &c&9Text\".",
            "LEGACY_SECTION - \"§c§lExample §c§9Text\".",
            "MINIMESSAGE - \"<bold><red>Example</red> <blue>Text</blue></bold>\". (https://webui.adventure.kyori.net/)",
            "GSON - \"[{\"text\":\"Example\",\"bold\":true,\"color\":\"red\"},{\"text\":\" \",\"bold\":true},{\"text\":\"Text\",\"bold\":true,\"color\":\"blue\"}]\". (https://minecraft.tools/en/json_text.php/)",
            "GSON_COLOR_DOWNSAMPLING - Same as GSON, but uses downsampling."
    })
    public Serializers SERIALIZER = Serializers.MINIMESSAGE;
    public String PREFIX = "<gradient:#8EF56A:#37D67A:#00C2A8>FloriumFilter";

    @Create
    public MAIN MAIN;

    public static class MAIN {
        @Comment({
                "Dimension types:",
                "OVERWORLD, NETHER, THE_END"
        })
        public Dimension DIMENSION_TYPE = Dimension.OVERWORLD;
    }

    @Create
    public STRINGS STRINGS;

    @Comment("Leave empty to disable")
    public static class STRINGS {
        public String TITLE = "{PRFX}";
        public String SUBTITLE = "<gray>пройдите паркур";
        public String RELOADED = "{PRFX} <gray>- <green>Конфиг перезагружен";
        public String SENT_FILTER = "{PRFX} <gray>- <green>Игрок <white>%s</white> отправлен в бот-фильтр";
        public String NOT_FOUND = "{PRFX} <gray>- <red>Игрок не найден";
    }
    @Create
    public VALIDATE VALIDATE;

    @Comment("Validating player's actions like block skip (its impossible due to 2 distance between blocks so its cheating or hardcoded bot :p )")
    public static class VALIDATE {
        public boolean ENABLED = true;
        public String SKIP_BLOCK_KICK = "{PRFX}{NL}<red>похоже вы бот :(";
    }
    @Create
    public GENERATION GENERATION;

    @Comment("Parkour generation settings")
    public static class GENERATION {
        @Comment({
                "Distance between blocks by \"width\"",
                "Highly recommended to not change it (if value will be so small players can skip blocks and validate check will be so angry >:x)"
        })
        public String RANDOMIZE_X = "-2:2";
        @Comment({
                "Distance between blocks by \"length\"",
                "Highly recommended to not change it (if value will be so small players can skip blocks and validate check will be so angry >:x)",
                "NOTE: value must be positive"
        })
        public String RANDOMIZE_Z = "3:4";
        @Comment({
                "Distance between blocks by Y",
                "Highly recommended to not change it (if value will be so small players can skip blocks and validate check will be so angry >:x)"
        })
        public String RANDOMIZE_Y = "-1:1";

        @Comment({
                "====== BLOCK LISTS ======",
                "",
                "FloriumFilter uses limbo api so you need to define block like that - minecraft:oak_log[axis=y]",
                "if you don't know block name you can use F3+H in game + debug stick",
                "",
                "Base parkour blocks"
        })
        public List<String> PARKOUR_BLOCKS = new ArrayList<>(List.of(
                "minecraft:stone",
                "minecraft:cobblestone",
                "minecraft:mossy_cobblestone",
                "minecraft:stone_bricks",
                "minecraft:mossy_stone_bricks",
                "minecraft:bricks",
                "minecraft:andesite",
                "minecraft:diorite",
                "minecraft:granite",
                "minecraft:deepslate",
                "minecraft:cobbled_deepslate",
                "minecraft:deepslate_bricks",
                "minecraft:deepslate_tiles"
        ));
        @Comment({
                "Final parkour block if you want randomize just put values like in parkour_blocks"
        })
        public List<String> FINAL_PARKOUR_BLOCKS = new ArrayList<>(List.of(
                "minecraft:gold_block"
        ));
    }
}
