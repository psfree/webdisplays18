package net.montoyo.wd.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import net.minecraft.util.Mth;
import net.montoyo.mcef.easy_forge_compat.Configuration;

import java.util.List;

@Config(name = "webdisplays")
public class ModConfig implements ConfigData {
    @ConfigEntry.Category("main")
    public Main main = new Main();

    @ConfigEntry.Category("client")
    public Client client = new Client();

    public static class Main {
        @ConfigEntry.Gui.Tooltip
        public List<String> blacklist = List.of();

        @ConfigEntry.Gui.Tooltip
        public int padHeight = 480;

        @ConfigEntry.Gui.Tooltip
        public boolean hardRecipes = true;

        @ConfigEntry.Gui.Tooltip
        public String homepage = "mod://webdisplays/main.html";

        @ConfigEntry.Gui.Tooltip
        public boolean disableOwnershipThief = false;

        @ConfigEntry.Gui.Tooltip
        public int maxResolutionX = 1920;

        @ConfigEntry.Gui.Tooltip
        public int maxResolutionY = 1080;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(max = Short.MAX_VALUE)
        public int miniservPort = 25566;

        @ConfigEntry.Gui.Tooltip
        public long miniservQuota = 1024; //It's stored as a string anyway

        @ConfigEntry.Gui.Tooltip
        public int maxScreenSizeX = 16;

        @ConfigEntry.Gui.Tooltip
        public int maxScreenSizeY = 16;
    }

    public static class Client {
        @ConfigEntry.Gui.Tooltip
        public double loadDistance = 30.0;

        @ConfigEntry.Gui.Tooltip
        public double unloadDistance = 32.0;

        @ConfigEntry.Gui.CollapsibleObject()
        public AutoVolumeControl autoVolumeControl = new AutoVolumeControl();

        public static class AutoVolumeControl {
            @ConfigEntry.Gui.Tooltip
            public boolean enableAutoVolume = true;

            @ConfigEntry.Gui.Tooltip
            public double ytVolume = 100.0;

            @ConfigEntry.Gui.Tooltip
            public double dist100 = 10.0;

            @ConfigEntry.Gui.Tooltip
            public double dist0 = 30.0;
        }
    }

    @Override
    public void validatePostLoad() throws ValidationException {
        ConfigData.super.validatePostLoad();

        main.miniservPort = Mth.clamp(main.miniservPort, 0, Short.MAX_VALUE);
        client.autoVolumeControl.ytVolume = Mth.clamp(client.autoVolumeControl.ytVolume, 0.0, 100.0);
        client.autoVolumeControl.dist0 = Mth.clamp(client.autoVolumeControl.dist0, 0.0, Double.MAX_VALUE);
        client.autoVolumeControl.ytVolume = Mth.clamp(client.autoVolumeControl.dist100, 0.0, Double.MAX_VALUE);

        if(client.unloadDistance < client.loadDistance + 2.0) {
            client.unloadDistance = client.loadDistance + 2.0;
        }

        if(client.autoVolumeControl.dist0 < client.autoVolumeControl.dist100 + 0.1) {
            client.autoVolumeControl.dist0 = client.autoVolumeControl.dist100 + 0.1;
        }
    }

    //    //Comments & shit
//        blacklist.setComment("An array of domain names you don't want to load.");
//        padHeight.setComment("The minePad Y resolution in pixels. padWidth = padHeight * " + PAD_RATIO);
//        hardRecipe.setComment("If true, breaking the minePad is required to craft upgrades.");
//        homePage.setComment("The URL that will be loaded each time you create a screen");
//        disableOT.setComment("If true, the ownership thief item will be disabled");
//        loadDistance.setComment("All screens outside this range will be unloaded");
//        unloadDistance.setComment("All unloaded screens inside this range will be loaded");
//        maxResX.setComment("Maximum horizontal screen resolution, in pixels");
//        maxResY.setComment("Maximum vertical screen resolution, in pixels");
//        miniservPort.setComment("The port used by miniserv. 0 to disable.");
//        miniservPort.setMaxValue(Short.MAX_VALUE);
//        miniservQuota.setComment("The amount of data that can be uploaded to miniserv, in KiB (so 1024 = 1 MiO)");
//        maxScreenX.setComment("Maximum screen width, in blocks. Resolution will be clamped by maxResolutionX.");
//        maxScreenY.setComment("Maximum screen height, in blocks. Resolution will be clamped by maxResolutionY.");
//        enableAutoVol.setComment("If true, the volume of YouTube videos will change depending on how far you are");
//        ytVolume.setComment("Volume for YouTube videos. This will have no effect if enableSoundDistance is set to false");
//        ytVolume.setMinValue(0.0);
//        ytVolume.setMaxValue(100.0);
//        dist100.setComment("Distance after which the sound starts dropping (in blocks)");
//        dist100.setMinValue(0.0);
//        dist0.setComment("Distance after which you can't hear anything (in blocks)");
//        dist0.setMinValue(0.0);
}
