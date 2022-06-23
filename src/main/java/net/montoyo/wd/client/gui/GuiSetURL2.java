/*
 * Copyright (C) 2019 BARBOTIN Nicolas
 */

package net.montoyo.wd.client.gui;

import net.minecraft.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.montoyo.wd.WebDisplays;
import net.montoyo.wd.client.ClientProxy;
import net.montoyo.wd.client.gui.controls.Button;
import net.montoyo.wd.client.gui.controls.TextField;
import net.montoyo.wd.client.gui.loading.FillControl;
import net.montoyo.wd.entity.TileEntityScreen;
import net.montoyo.wd.net.Messages;
import net.montoyo.wd.net.server.SMessagePadCtrl;
import net.montoyo.wd.net.server.SMessageScreenCtrl;
import net.montoyo.wd.utilities.BlockSide;
import net.montoyo.wd.utilities.Util;
import net.montoyo.wd.utilities.Vector3i;

import java.util.Map;

public class GuiSetURL2 extends WDScreen {

    //Screen data
    private TileEntityScreen tileEntity;
    private BlockSide screenSide;
    private Vector3i remoteLocation;

    //Pad data
    private final boolean isPad;

    //Common
    private final String screenURL;

    @FillControl
    private TextField tfURL;

    @FillControl
    private Button btnShutDown;

    @FillControl
    private Button btnCancel;

    @FillControl
    private Button btnOk;

    public GuiSetURL2(TileEntityScreen tes, BlockSide side, String url, Vector3i rl) {
        super();
        tileEntity = tes;
        screenSide = side;
        remoteLocation = rl;
        isPad = false;
        screenURL = url;
    }

    public GuiSetURL2(String url) {
        isPad = true;
        screenURL = url;
    }

    @Override
    public void init() {
        super.init();
        loadFrom(new ResourceLocation("webdisplays", "gui/seturl.json"));
        tfURL.setText(screenURL);
    }

    @Override
    protected void addLoadCustomVariables(Map<String, Double> vars) {
        vars.put("isPad", isPad ? 1.0 : 0.0);
    }

    @GuiSubscribe
    public void onButtonClicked(Button.ClickEvent ev) {
        if(ev.getSource() == btnCancel)
            minecraft.setScreen(null);
        else if(ev.getSource() == btnOk)
            validate(tfURL.getText());
        else if(ev.getSource() == btnShutDown) {
            if(isPad)
                Messages.INSTANCE.sendToServer(new SMessagePadCtrl(""));

            minecraft.setScreen(null);
        }
    }

    @GuiSubscribe
    public void onEnterPressed(TextField.EnterPressedEvent ev) {
        validate(ev.getText());
    }

    private void validate(String url) {
        if(!url.isEmpty()) {
            url = Util.addProtocol(url);
            url = ((ClientProxy) WebDisplays.PROXY).getMCEF().punycode(url);

            if(isPad) {
                Messages.INSTANCE.sendToServer(new SMessagePadCtrl(url));
                ItemStack held = minecraft.player.getItemInHand(InteractionHand.MAIN_HAND);

                if(held.getItem() == WebDisplays.INSTANCE.itemMinePad && held.getTag() != null && held.getTag().contains("PadID")) {
                    ClientProxy.PadData pd = ((ClientProxy) WebDisplays.PROXY).getPadByID(held.getTag().getInt("PadID"));

                    if(pd != null && pd.view != null)
                        pd.view.loadURL(WebDisplays.applyBlacklist(url));
                }
            } else
                Messages.INSTANCE.sendToServer(SMessageScreenCtrl.setURL(tileEntity, screenSide, url, remoteLocation));
        }

        mc.displayGuiScreen(null);
    }

    @Override
    public boolean isForBlock(BlockPos bp, BlockSide side) {
        return (remoteLocation != null && remoteLocation.equalsBlockPos(bp)) || (bp.equals(tileEntity.getPos()) && side == screenSide);
    }

}
