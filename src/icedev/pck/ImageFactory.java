package icedev.pck;

import java.awt.*;
import java.awt.image.*;

public class ImageFactory
{
    public Image createOpaque(final int n, final int n2) {
        return new BufferedImage(n, n2, 1);
    }
    
    public Image createTranslucent(final int n, final int n2) {
        return new BufferedImage(n, n2, 3);
    }
    
    public Image createTransparent(final int n, final int n2) {
        return new BufferedImage(n, n2, 3);
    }
}
