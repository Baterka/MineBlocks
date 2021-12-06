package pl.socketbyte.opengui;

import pl.socketbyte.opengui.event.ElementResponse;

public class ItemPack {

    private int slot;
    private GUIItemBuilder itemBuilder;
    private ElementResponse elementResponse;

    public ItemPack(int slot, GUIItemBuilder itemBuilder) {
        this.itemBuilder = itemBuilder;
        this.slot = slot;
    }

    public ItemPack(int slot, GUIItemBuilder itemBuilder, ElementResponse elementResponse) {
        this.itemBuilder = itemBuilder;
        this.slot = slot;
        this.elementResponse = elementResponse;
    }

    public ItemPack(GUIItemBuilder itemBuilder) {
        this.itemBuilder = itemBuilder;
    }

    public ItemPack(GUIItemBuilder itemBuilder, ElementResponse elementResponse) {
        this.itemBuilder = itemBuilder;
        this.elementResponse = elementResponse;
    }

    public int getSlot() {
        return slot;
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }

    public GUIItemBuilder getItemBuilder() {
        return itemBuilder;
    }

    public void setItemBuilder(GUIItemBuilder itemBuilder) {
        this.itemBuilder = itemBuilder;
    }

    public ElementResponse getElementResponse() {
        return elementResponse;
    }

    public void setElementResponse(ElementResponse elementResponse) {
        this.elementResponse = elementResponse;
    }
}
