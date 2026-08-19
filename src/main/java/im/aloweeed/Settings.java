package im.aloweeed;

import net.elytrium.limboapi.api.chunk.Dimension;
import net.elytrium.limboapi.thirdparty.commons.config.YamlConfig;
import net.elytrium.limboapi.thirdparty.commons.kyori.serialization.Serializers;

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
    }
    @Create
    public VALIDATE VALIDATE;

    @Comment("Validating player's actions like block skip (its impossible due to 2 distance between blocks so its cheating or hardcoded bot :p )")
    public static class VALIDATE {
        public boolean ENABLED = true;
        public String SKIP_BLOCK_KICK = "{PRFX}{NL}<red>похоже вы бот :(";
    }
}
