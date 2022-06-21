package net.montoyo.wd.config;

public class WDConfig {
    //Config
    public static final double PAD_RATIO = 59.0 / 30.0;
    public String homePage;
    public double padResX;
    public double padResY;
    private int lastPadId = 0;
    public boolean doHardRecipe;
    private boolean hasOC;
    private boolean hasCC;
    private String[] blacklist;
    public boolean disableOwnershipThief;
    public double unloadDistance2;
    public double loadDistance2;
    public int maxResX;
    public int maxResY;
    public int maxScreenX;
    public int maxScreenY;
    public int miniservPort;
    public long miniservQuota;
    public boolean enableSoundDistance;
    public float ytVolume;
    public float avDist100;
    public float avDist0;
        //Load config
        Configuration cfg = new Configuration(ev.getSuggestedConfigurationFile());
        cfg.load();

        //CAT: Main
        Property blacklist = cfg.get("main", "blacklist", new String[0]);
        Property padHeight = cfg.get("main", "padHeight", 480);
        Property hardRecipe = cfg.get("main", "hardRecipes", true);
        Property homePage = cfg.get("main", "homepage", "mod://webdisplays/main.html");
        Property disableOT = cfg.get("main", "disableOwnershipThief", false);
        Property maxResX = cfg.get("main", "maxResolutionX", 1920);
        Property maxResY = cfg.get("main", "maxResolutionY", 1080);
        Property miniservPort = cfg.get("main", "miniservPort", 25566);
        Property miniservQuota = cfg.get("main", "miniservQuota", 1024); //It's stored as a string anyway
        Property maxScreenX = cfg.get("main", "maxScreenSizeX", 16);
        Property maxScreenY = cfg.get("main", "maxScreenSizeY", 16);

        //CAT: Client options
        Property loadDistance = cfg.get("client", "loadDistance", 30.0);
        Property unloadDistance = cfg.get("client", "unloadDistance", 32.0);

        //CAT: Auto volume config (client-side)
        Property enableAutoVol = cfg.get("clientAutoVolume", "enableAutoVolume", true);
        Property ytVolume = cfg.get("clientAutoVolume", "ytVolume", 100.0);
        Property dist100 = cfg.get("clientAutoVolume", "dist100", 10.0);
        Property dist0 = cfg.get("clientAutoVolume", "dist0", 30.0);


        //Comments & shit
        blacklist.setComment("An array of domain names you don't want to load.");
        padHeight.setComment("The minePad Y resolution in pixels. padWidth = padHeight * " + PAD_RATIO);
        hardRecipe.setComment("If true, breaking the minePad is required to craft upgrades.");
        homePage.setComment("The URL that will be loaded each time you create a screen");
        disableOT.setComment("If true, the ownership thief item will be disabled");
        loadDistance.setComment("All screens outside this range will be unloaded");
        unloadDistance.setComment("All unloaded screens inside this range will be loaded");
        maxResX.setComment("Maximum horizontal screen resolution, in pixels");
        maxResY.setComment("Maximum vertical screen resolution, in pixels");
        miniservPort.setComment("The port used by miniserv. 0 to disable.");
        miniservPort.setMaxValue(Short.MAX_VALUE);
        miniservQuota.setComment("The amount of data that can be uploaded to miniserv, in KiB (so 1024 = 1 MiO)");
        maxScreenX.setComment("Maximum screen width, in blocks. Resolution will be clamped by maxResolutionX.");
        maxScreenY.setComment("Maximum screen height, in blocks. Resolution will be clamped by maxResolutionY.");
        enableAutoVol.setComment("If true, the volume of YouTube videos will change depending on how far you are");
        ytVolume.setComment("Volume for YouTube videos. This will have no effect if enableSoundDistance is set to false");
        ytVolume.setMinValue(0.0);
        ytVolume.setMaxValue(100.0);
        dist100.setComment("Distance after which the sound starts dropping (in blocks)");
        dist100.setMinValue(0.0);
        dist0.setComment("Distance after which you can't hear anything (in blocks)");
        dist0.setMinValue(0.0);

        if(unloadDistance.getDouble() < loadDistance.getDouble() + 2.0)
            unloadDistance.set(loadDistance.getDouble() + 2.0);

        if(dist0.getDouble() < dist100.getDouble() + 0.1)
            dist0.set(dist100.getDouble() + 0.1);

        cfg.save();

        this.blacklist = blacklist.getStringList();
        doHardRecipe = hardRecipe.getBoolean();
        this.homePage = homePage.getString();
        disableOwnershipThief = disableOT.getBoolean();
        unloadDistance2 = unloadDistance.getDouble() * unloadDistance.getDouble();
        loadDistance2 = loadDistance.getDouble() * loadDistance.getDouble();
        this.maxResX = maxResX.getInt();
        this.maxResY = maxResY.getInt();
        this.miniservPort = miniservPort.getInt();
        this.miniservQuota = miniservQuota.getLong() * 1024L;
        this.maxScreenX = maxScreenX.getInt();
        this.maxScreenY = maxScreenY.getInt();
        enableSoundDistance = enableAutoVol.getBoolean();
        this.ytVolume = (float) ytVolume.getDouble();
        avDist100 = (float) dist100.getDouble();
        avDist0 = (float) dist0.getDouble();
}
