package com.fragorl.timetracker.util;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * @author Alex
 * @version $Id$
 *          <p/>
 *          Created on 27/05/13 3:13 PM
 */
public class ImageUtils {
    private static final String RESOURCES_FOLDER_NAME = "resources";

    private ImageUtils() {}

    public static ImageIcon getSquareIcon(String iconNameAndExtension, int dimension) throws IOException {
        File fileLocation = new File(RESOURCES_FOLDER_NAME + File.separator + iconNameAndExtension);
        ImageIcon imageIcon = new ImageIcon(fileLocation.getCanonicalPath()); // throws IOException if not found
        Image img = imageIcon.getImage();
        BufferedImage bufferedImage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics g = bufferedImage.createGraphics();
        g.drawImage(img, 0, 0, dimension, dimension, null);
        return new ImageIcon(bufferedImage);
    }
}
