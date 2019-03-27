package icedev.pck;

import java.awt.*;

public class PCKImage
{
    public Image img;
    public int offX;
    public int offY;
    
    public PCKImage(final Image img, final int offX, final int offY) {
        this.img = img;
        this.offX = offX;
        this.offY = offY;
    }
}
