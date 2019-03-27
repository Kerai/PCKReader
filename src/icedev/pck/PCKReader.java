package icedev.pck;

import java.io.*;
import java.awt.*;
import javax.imageio.*;
import java.awt.image.*;
import java.util.*;
import java.util.List;

public class PCKReader
{
    private int[] a;
    private Color[] colors;
    private ImageFactory factory;
    
    public PCKReader() {
        this(new ImageFactory());
    }
    
    public PCKReader(final ImageFactory factory) {
        this.factory = factory;
    }
    
    public void readPalette(final File file) throws IOException {
        final FileInputStream fileInputStream = new FileInputStream(file);
        this.readPalette(fileInputStream, (int)(file.length() / 3L));
        fileInputStream.close();
    }
    
    public void readPalette(final InputStream inputStream, int n) throws IOException {
        final LEDataInStream leDataInStream = new LEDataInStream(inputStream);
        this.a = new int[n];
        this.colors = new Color[this.a.length];
        n = 0;
        while (leDataInStream.available() > 0) {
            final int unsignedByte = leDataInStream.readUnsignedByte();
            final int unsignedByte2 = leDataInStream.readUnsignedByte();
            final int unsignedByte3 = leDataInStream.readUnsignedByte();
            int n2 = 255;
            int n3 = (unsignedByte << 2 | (unsignedByte >> 4 & 0x3)) & 0xFF;
            int n4 = (unsignedByte2 << 2 | (unsignedByte2 >> 4 & 0x3)) & 0xFF;
            int n5 = (unsignedByte3 << 2 | (unsignedByte3 >> 4 & 0x3)) & 0xFF;
            if (n == 0) {
                n4 = (n3 = (n5 = (n2 = 0)));
            }
            this.colors[n] = new Color(n3, n4, n5, n2);
            ++n;
        }
    }
    
    public List<PCKImage> readShadows(final File file) throws IOException {
        final ArrayList<PCKImage> list = new ArrayList<PCKImage>();
        System.out.println("processing " + file.getName());
        final String name = file.getName();
        final LEDataInStream leDataInStream = new LEDataInStream(new FileInputStream(new File(file.getParentFile(), String.valueOf(name.substring(0, name.indexOf(46))) + ".TAB")));
        final LERAF leraf = new LERAF(file, "r");
        System.out.println();
        System.out.println("Finish");
        while (leDataInStream.available() > 0) {
            leraf.seek(leDataInStream.readIntLE());
            leraf.readUnsignedByte();
            leraf.readUnsignedByte();
            leraf.readLEShort();
            final char leShort = leraf.readLEShort();
            final char leShort2 = leraf.readLEShort();
            final Image translucent = this.factory.createTranslucent(leShort, leShort2);
            final int[] array = new int[leShort * leShort2];
            int i = leraf.readUnsignedByte();
            int n = 0;
            while (i != 0xFF) {
                int n2 = i;
                final int unsignedByte;
                if ((unsignedByte = leraf.readUnsignedByte()) == 0) {
                    n += n2 << 2;
                }
                else {
                    if (unsignedByte >= 7) {
                        throw new AssertionError();
                    }
                    while (n2-- > 0) {
                        for (int j = 0; j < 4; ++j) {
                            final int n3 = n % 640;
                            final int n4 = n / 640;
                            if (n3 < leShort && n4 < leShort2) {
                                array[n4 * leShort + n3] = 0x30000000;
                            }
                            ++n;
                        }
                    }
                }
                i = leraf.readUnsignedByte();
            }
            setPixels(translucent, array);
            list.add(new PCKImage(translucent, 0, 0));
        }
        leDataInStream.close();
        leraf.close();
        return list;
    }
    
    public static void setPixels(final Image image, final int[] array) {
        final int width = image.getWidth(null);
        final int height = image.getHeight(null);
        if (image instanceof BufferedImage) {
            ((BufferedImage)image).setRGB(0, 0, width, height, array, 0, width);
            return;
        }
        final Graphics2D graphics2D = (Graphics2D)image.getGraphics();
        final BufferedImage bufferedImage;
        (bufferedImage = new BufferedImage(width, height, 7)).setRGB(0, 0, width, height, array, 0, width);
        graphics2D.drawImage(bufferedImage, 0, 0, null);
        graphics2D.dispose();
    }
    
    public List<PCKImage> readPCK(final File file) throws IOException {
        final ArrayList<PCKImage> list = new ArrayList<PCKImage>();
        final String name = file.getName();
        final LEDataInStream leDataInStream = new LEDataInStream(new FileInputStream(new File(file.getParentFile(), String.valueOf(name.substring(0, name.indexOf(46))) + ".TAB")));
        final LERAF leraf = new LERAF(file, "r");
        while (leDataInStream.available() > 0) {
            leraf.seek(leDataInStream.readIntLE() << 2);
            final int n = leraf.readByte() & 0xFF;
            leraf.skipBytes(3);
            final char leShort = leraf.readLEShort();
            final char leShort2 = leraf.readLEShort();
            final char leShort3 = leraf.readLEShort();
            final char leShort4 = leraf.readLEShort();
            final char c = (char)(leShort2 - leShort);
            final char c2 = (char)(leShort4 - leShort3);
            final Image transparent = this.factory.createTransparent(c, c2);
            final int[] array = new int[c * c2];
            if (n == 1) {
                int leInt;
                while ((leInt = leraf.readLEInt()) != -1) {
                    final int n2 = leInt / 640;
                    final int unsignedByte = leraf.readUnsignedByte();
                    final int unsignedByte2 = leraf.readUnsignedByte();
                    leraf.readUnsignedByte();
                    leraf.readUnsignedByte();
                    for (int i = 0; i < unsignedByte2; ++i) {
                        final int unsignedByte3 = leraf.readUnsignedByte();
                        final int n3 = unsignedByte + i - leShort;
                        final char c3 = (char)(n2 - leShort3);
                        if (n3 < c && c3 < c2) {
                            array[c3 * c + n3] = this.colors[unsignedByte3].getRGB();
                        }
                    }
                }
                setPixels(transparent, array);
            }
            else if (n == 2) {
                System.err.println("NOT IMPLEMENTED COMPRESSION TYPE 2");
            }
            else {
                System.err.println("Unknown compression: " + n);
            }
            list.add(new PCKImage(transparent, leShort, leShort3));
        }
        leDataInStream.close();
        leraf.close();
        return list;
    }
    
    public void convertShadowsTo(final File file, final File file2) throws IOException {
        if (file2.exists()) {
            System.out.println("Omitting " + file);
            return;
        }
        final List shadows = this.readShadows(file);
        file2.mkdirs();
        int n = 0;
        System.out.println("Saving images");
        final Iterator<PCKImage> iterator = shadows.iterator();
        while (iterator.hasNext()) {
            ImageIO.write((RenderedImage)iterator.next().img, "PNG", new File(file2, String.valueOf(n) + ".png"));
            if (n > 0 && n % 80 == 0) {
                System.out.println();
            }
            System.out.print(".");
            ++n;
        }
        System.out.println();
        System.out.println("Finish");
    }
    
    public void convertTo(final File file, final File file2) throws IOException  {
        if (file2.exists()) {
            System.out.println("Omitting " + file);
            return;
        }
        System.out.println("Parsing file " + file);
        final List<PCKImage> pck = this.readPCK(file);
        file2.mkdirs();
        int n = 0;
        System.out.println("Saving images");
        final Iterator<PCKImage> iterator = pck.iterator();
        while (iterator.hasNext()) {
            ImageIO.write((RenderedImage)iterator.next().img, "PNG", new File(file2, String.valueOf(n) + ".png"));
            if (n > 0 && n % 80 == 0) {
                System.out.println();
            }
            System.out.print(".");
            ++n;
        }
        System.out.println();
        System.out.println("Finish");
    }
    
    public static void main(final String[] array) throws IOException {
        final PCKReader pckReader = new PCKReader();
        pckReader.readPalette(new File("xcomdata\\PAL_01.DAT"));
        pckReader.convertTo(new File("xcomdata\\CITY.PCK"), new File("sprites/city"));
        pckReader.convertTo(new File("xcomdata\\SAUCER.PCK"), new File("sprites/saucer"));
        pckReader.convertTo(new File("xcomdata\\VEHICLE.PCK"), new File("sprites/vehicle"));
        pckReader.convertTo(new File("xcomdata\\CITYOVR.PCK"), new File("sprites/cityovr"));
        pckReader.convertTo(new File("xcomdata\\PHOTO.PCK"), new File("sprites/photo"));
        pckReader.convertTo(new File("xcomdata\\PTANG.PCK"), new File("sprites/ptang"));
        pckReader.convertTo(new File("G:\\XCOM3\\TACDATA\\GAMEOBJ.PCK"), new File("sprites/gameobj"));
        pckReader.convertShadowsTo(new File("xcomdata\\SHADOW.PCK"), new File("sprites/shadow"));
    }
}
