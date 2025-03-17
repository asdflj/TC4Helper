package com.github.wohaopa.tc4helper.mixins;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.github.wohaopa.tc4helper.autoplay.AutoPlayButton;
import com.github.wohaopa.tc4helper.autoplay.GuiResearchTableHelperInterface;

import thaumcraft.api.aspects.Aspect;
import thaumcraft.client.gui.GuiResearchTable;
import thaumcraft.common.lib.network.PacketHandler;
import thaumcraft.common.lib.network.playerdata.PacketAspectCombinationToServer;
import thaumcraft.common.lib.network.playerdata.PacketAspectPlaceToServer;
import thaumcraft.common.lib.research.ResearchNoteData;
import thaumcraft.common.lib.utils.HexUtils;
import thaumcraft.common.tiles.TileResearchTable;

@Mixin(value = GuiResearchTable.class, remap = false)
public abstract class GuiResearchTableMixin extends GuiContainer implements GuiResearchTableHelperInterface {

    private GuiResearchTableMixin(Container p_i1072_1_) {
        super(p_i1072_1_);
    }

    @Shadow
    EntityPlayer player;
    @Shadow
    public ResearchNoteData note;
    @Shadow
    private TileResearchTable tileEntity;
    private AutoPlayButton btn;

    @Override
    public void initGui() {
        super.initGui();
        this.buttonList.add(this.btn = new AutoPlayButton(101, width / 2 - 25, this.guiTop + 12, 50, 20));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button instanceof AutoPlayButton autoPlayButton) {
            autoPlayButton.onAction(player, note, this);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (this.btn != null) {
            ItemStack note = this.tileEntity.getStackInSlot(1);
            ItemStack scribing = this.tileEntity.getStackInSlot(0);
            this.btn.visible = note != null && scribing != null;
            this.btn.enabled = calculateBtnState(note, scribing);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private boolean calculateBtnState(ItemStack note, ItemStack scribing) {
        if (note == null || scribing == null) {
            return false;
        }
        if (scribing.getMaxDamage() == scribing.getItemDamage()) {
            return false;
        }
        NBTTagCompound compound = note.getTagCompound();

        if (compound == null) {
            note.setTagCompound(compound = new NBTTagCompound());
        }

        return !compound.getBoolean("complete");
    }

    public void place(HexUtils.Hex hex, Aspect aspect) {
        PacketHandler.INSTANCE.sendToServer(
            new PacketAspectPlaceToServer(
                this.player,
                (byte) hex.q,
                (byte) hex.r,
                this.tileEntity.xCoord,
                this.tileEntity.yCoord,
                this.tileEntity.zCoord,
                aspect));
    }

    public void combine(Aspect aspect1, Aspect aspect2) {
        PacketHandler.INSTANCE.sendToServer(
            new PacketAspectCombinationToServer(
                this.player,
                this.tileEntity.xCoord,
                this.tileEntity.yCoord,
                this.tileEntity.zCoord,
                aspect1,
                aspect2,
                this.tileEntity.bonusAspects.getAmount(aspect1) > 0,
                this.tileEntity.bonusAspects.getAmount(aspect2) > 0,
                true));
    }

}
