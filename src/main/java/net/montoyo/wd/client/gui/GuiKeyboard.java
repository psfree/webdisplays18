/*
 * Copyright (C) 2019 BARBOTIN Nicolas
 */

package net.montoyo.wd.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;
import net.montoyo.wd.WebDisplays;
import net.montoyo.wd.client.gui.controls.Button;
import net.montoyo.wd.client.gui.controls.Control;
import net.montoyo.wd.client.gui.controls.Label;
import net.montoyo.wd.client.gui.loading.FillControl;
import net.montoyo.wd.entity.TileEntityScreen;
import net.montoyo.wd.net.Messages;
import net.montoyo.wd.net.server.SMessageScreenCtrl;
import net.montoyo.wd.utilities.BlockSide;
import net.montoyo.wd.utilities.Log;
import net.montoyo.wd.utilities.TypeData;
import net.montoyo.wd.utilities.Util;
import org.lwjgl.glfw.GLFW;
import org.cef.browser.CefBrowserOsr;
import org.vivecraft.gameplay.VRPlayer;
import org.vivecraft.gameplay.screenhandlers.KeyboardHandler;

import java.io.*;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class GuiKeyboard extends WDScreen {

    private static final String WARNING_FNAME = "wd_keyboard_warning.txt";

    private TileEntityScreen tes;
    private BlockSide side;
    private final ArrayList<TypeData> evStack = new ArrayList<>();
    private BlockPos kbPos;
    private boolean showWarning = true;

    @FillControl
    private Label lblInfo;

    @FillControl
    private Button btnOk;

    public GuiKeyboard() {
        super(Component.nullToEmpty(null));
    }

    public GuiKeyboard(TileEntityScreen tes, BlockSide side, BlockPos kbPos) {
        this();
        this.tes = tes;
        this.side = side;
        this.kbPos = kbPos;
    }

    @Override
    protected void addLoadCustomVariables(Map<String, Double> vars) {
        vars.put("showWarning", showWarning ? 1.0 : 0.0);
    }

    private static final boolean vivecraftPresent;
    
    static {
        boolean vivePres = false;
        if (ModList.get().isLoaded("vivecraft")) vivePres = true;
        // I believe the non-mixin version of vivecraft is not a proper mod, so
        // detect the mod reflectively if the mod is not found
        else {
            try {
                Class<?> clazz = Class.forName("org.vivecraft.gameplay.screenhandlers.KeyboardHandler");
                //noinspection ConstantConditions
                if (clazz == null) vivePres = false;
                else {
                    Method m = clazz.getMethod("setOverlayShowing", boolean.class);
                    //noinspection ConstantConditions
                    vivePres = m != null;
                }
            } catch (Throwable ignored) {
                vivePres = false;
            }
        }
        vivecraftPresent = vivePres;
    }
    
    @Override
    public void init() {
        super.init();

        if (minecraft.getSingleplayerServer() != null && !minecraft.getSingleplayerServer().isPublished())
            showWarning = false; //NO NEED
        else
            showWarning = !hasUserReadWarning();

        loadFrom(new ResourceLocation("webdisplays", "gui/kb_right.json"));

        if (showWarning) {
            int maxLabelW = 0;
            int totalH = 0;

            for (Control ctrl : controls) {
                if (ctrl != lblInfo && ctrl instanceof Label) {
                    if (ctrl.getWidth() > maxLabelW)
                        maxLabelW = ctrl.getWidth();

                    totalH += ctrl.getHeight();
                    ctrl.setPos((width - ctrl.getWidth()) / 2, 0);
                }
            }

            btnOk.setWidth(maxLabelW);
            btnOk.setPos((width - maxLabelW) / 2, 0);
            totalH += btnOk.getHeight();

            int y = (height - totalH) / 2;
            for (Control ctrl : controls) {
                if (ctrl != lblInfo) {
                    ctrl.setPos(ctrl.getX(), y);
                    y += ctrl.getHeight();
                }
            }
        } else {
            if (!minecraft.isWindowActive()) {
                minecraft.setWindowActive(true);
                minecraft.mouseHandler.grabMouse();
            }
        }

        defaultBackground = showWarning;
        syncTicks = 5;
    
        if (vivecraftPresent)
            if (VRPlayer.get() != null)
                KeyboardHandler.setOverlayShowing(true);
    }
    
    @Override
    public void onClose() {
        if (vivecraftPresent)
            if (VRPlayer.get() != null)
                KeyboardHandler.setOverlayShowing(false);
        super.onClose();
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(quitOnEscape && keyCode == GLFW.GLFW_KEY_ESCAPE) {
            Minecraft.getInstance().setScreen(null);
        }
        evStack.add(new TypeData(TypeData.Action.PRESS, keyCode, modifiers));
        if (!evStack.isEmpty() && !syncRequested())
            requestSync();
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    
    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        evStack.add(new TypeData(TypeData.Action.TYPE, codePoint, modifiers));
        if (!evStack.isEmpty() && !syncRequested())
            requestSync();
        return super.charTyped(codePoint, modifiers);
    }
    
    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        evStack.add(new TypeData(TypeData.Action.RELEASE, keyCode, modifiers));
        if (!evStack.isEmpty() && !syncRequested())
            requestSync();
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected void sync() {
        if(!evStack.isEmpty()) {
            Messages.INSTANCE.sendToServer(SMessageScreenCtrl.type(tes, side, WebDisplays.GSON.toJson(evStack), kbPos));
            evStack.clear();
        }
    }

    @GuiSubscribe
    public void onClick(Button.ClickEvent ev) {
        if(showWarning && ev.getSource() == btnOk) {
            writeUserAcknowledge();

            for(Control ctrl: controls) {
                if(ctrl instanceof Label) {
                    Label lbl = (Label) ctrl;
                    lbl.setVisible(!lbl.isVisible());
                }
            }

            btnOk.setDisabled(true);
            btnOk.setVisible(false);
            showWarning = false;
            defaultBackground = false;
            minecraft.setWindowActive(true);
            minecraft.mouseHandler.grabMouse();
        }
    }

    private boolean hasUserReadWarning() {
        try {
            File f = new File(FMLPaths.GAMEDIR.name(), WARNING_FNAME);

            if(f.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(f));
                String str = br.readLine();
                Util.silentClose(br);

                return str != null && str.trim().equalsIgnoreCase("read");
            }
        } catch(Throwable t) {
            Log.warningEx("Can't know if user has already read the warning", t);
        }

        return false;
    }

    private void writeUserAcknowledge() {
        try {
            File f = new File(FMLPaths.GAMEDIR.name(), WARNING_FNAME);

            BufferedWriter bw = new BufferedWriter(new FileWriter(f));
            bw.write("read\n");
            Util.silentClose(bw);
        } catch(Throwable t) {
            Log.warningEx("Can't write that the user read the warning", t);
        }
    }

    @Override
    public boolean isForBlock(BlockPos bp, BlockSide side) {
        return bp.equals(kbPos) || (bp.equals(tes.getBlockPos()) && side == this.side);
    }

}
