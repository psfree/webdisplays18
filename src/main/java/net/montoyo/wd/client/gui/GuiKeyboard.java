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

import java.io.*;
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
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        key(keyCode, scanCode, true, modifiers);
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        key(keyCode, scanCode, false, modifiers);
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public void key(int keyCode, int scanCode, boolean pressed, int mod) {
        if (pressed) {
            if(quitOnEscape && keyCode == GLFW.GLFW_KEY_ESCAPE) {
                Minecraft.getInstance().setScreen(null);
            }

            int chr = CefBrowserOsr.remapKeycode(keyCode, (char) keyCode, mod);
            evStack.add(new TypeData(TypeData.Action.PRESS, chr, mod));
            evStack.add(new TypeData(TypeData.Action.RELEASE, chr, mod));

            if (keyCode != 0)
                evStack.add(new TypeData(TypeData.Action.TYPE, chr, mod));

            if (!evStack.isEmpty() && !syncRequested())
                requestSync();
        }
    }

    public int getChar(int keyCode, int scanCode) {
        String keystr = GLFW.glfwGetKeyName(keyCode, scanCode);
        if(keystr == null){
            keystr = "\0";
        }
        if(keyCode == GLFW.GLFW_KEY_ENTER){
            return 13;
        }
        if (keyCode == GLFW.GLFW_KEY_SPACE) {
            return 32;
        }
        if(keystr.length() == 0){
            return -1;
        }
        if(hasShiftDown()) {
            keystr = keystr.toUpperCase(Locale.ROOT);
            return CefBrowserOsr.remapKeycode(keyCode, keystr.charAt(keystr.length() - 1), 0);
        } else {
            return CefBrowserOsr.remapKeycode(keyCode, keystr.charAt(keystr.length() - 1), 0);
        }
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
