package com.fragorl.timetracker.ui;

import java.awt.*;

/**
 * A layout manager designed to only be able to layout JPanel children, which it does by asking them to all be the same size (the size of the container it's laying out).
 *
 * @author Alex
 * @version $Id$
 *          <p/>
 *          Created on 18/06/13 2:14 PM
 */
public class OverlappingPanelsLayoutManager implements LayoutManager {

    @Override
    public void addLayoutComponent(String name, Component comp) {}

    @Override
    public void removeLayoutComponent(Component comp) {}

    @Override
    public Dimension preferredLayoutSize(Container parent) {
        return parent.getComponents()[0].getPreferredSize();
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        return parent.getComponents()[0].getMinimumSize();
    }

    @Override
    public void layoutContainer(Container parent) {
        Dimension parentSize =   parent.getSize();
        Rectangle parentBounds = parent.getBounds();
        Point parentLocation =   parent.getLocation();
        for (Component child : parent.getComponents()) {
            child.setSize(parentSize);
            child.setBounds(parentBounds);
            child.setLocation(parentLocation);
        }
    }
}
