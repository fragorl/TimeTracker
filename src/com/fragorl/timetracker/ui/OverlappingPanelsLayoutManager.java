package com.fragorl.timetracker.ui;

import java.awt.*;
import java.util.Vector;

/**
 * A layout manager designed to only be able to layout JPanel children, which it does by asking them to all be the same size (the size of the container it's laying out).
 * Code has basically been hacked from Swing's CardLayout.
 *
 * @author Alex
 * @version $Id$
 *          <p/>
 *          Created on 18/06/13 2:14 PM
 */
public class OverlappingPanelsLayoutManager implements LayoutManager {

    /*
     * This creates a Vector to store associated
     * pairs of components and their names.
     * @see java.util.Vector
     */
    Vector vector = new Vector();

    /*
     * A pair of Component and String that represents its name.
     */
    class Card {
        public String name;
        public Component comp;
        public Card(String cardName, Component cardComponent) {
            name = cardName;
            comp = cardComponent;
        }
    }


    /*
    * A cards horizontal Layout gap (inset). It specifies
    * the space between the left and right edges of a
    * container and the current component.
    * This should be a non negative Integer.
    * @see getHgap()
    * @see setHgap()
    */
    int hgap;

    /*
    * A cards vertical Layout gap (inset). It specifies
    * the space between the top and bottom edges of a
    * container and the current component.
    * This should be a non negative Integer.
    * @see getVgap()
    * @see setVgap()
    */
    int vgap;

    /**
     * Creates a new card layout with gaps of size zero.
     */
    public OverlappingPanelsLayoutManager() {
        this(0, 0);
    }

    /**
     * Creates a new card layout with the specified horizontal and
     * vertical gaps. The horizontal gaps are placed at the left and
     * right edges. The vertical gaps are placed at the top and bottom
     * edges.
     * @param     hgap   the horizontal gap.
     * @param     vgap   the vertical gap.
     */
    public OverlappingPanelsLayoutManager(int hgap, int vgap) {
        this.hgap = hgap;
        this.vgap = vgap;
    }

    /**
     * Gets the horizontal gap between components.
     * @return    the horizontal gap between components.
     * @see       java.awt.CardLayout#setHgap(int)
     * @see       java.awt.CardLayout#getVgap()
     * @since     JDK1.1
     */
    public int getHgap() {
        return hgap;
    }

    /**
     * Sets the horizontal gap between components.
     * @param hgap the horizontal gap between components.
     * @see       java.awt.CardLayout#getHgap()
     * @see       java.awt.CardLayout#setVgap(int)
     * @since     JDK1.1
     */
    public void setHgap(int hgap) {
        this.hgap = hgap;
    }

    /**
     * Gets the vertical gap between components.
     * @return the vertical gap between components.
     * @see       java.awt.CardLayout#setVgap(int)
     * @see       java.awt.CardLayout#getHgap()
     */
    public int getVgap() {
        return vgap;
    }

    /**
     * Sets the vertical gap between components.
     * @param     vgap the vertical gap between components.
     * @see       java.awt.CardLayout#getVgap()
     * @see       java.awt.CardLayout#setHgap(int)
     * @since     JDK1.1
     */
    public void setVgap(int vgap) {
        this.vgap = vgap;
    }

    /**
     * Adds the specified component to this card layout's internal
     * table of names. The object specified by <code>constraints</code>
     * must be a string. The card layout stores this string as a key-value
     * pair that can be used for random access to a particular card.
     * By calling the <code>show</code> method, an application can
     * display the component with the specified name.
     * @param     comp          the component to be added.
     * @param     constraints   a tag that identifies a particular
     *                                        card in the layout.
     * @see       java.awt.CardLayout#show(java.awt.Container, java.lang.String)
     * @exception  IllegalArgumentException  if the constraint is not a string.
     */
    public void addLayoutComponent(Component comp, Object constraints) {
        synchronized (comp.getTreeLock()) {
            if (constraints == null){
                constraints = "";
            }
            if (constraints instanceof String) {
                addLayoutComponent((String)constraints, comp);
            } else {
                throw new IllegalArgumentException("cannot add to layout: constraint must be a string");
            }
        }
    }

    /**
     * @deprecated   replaced by
     *      <code>addLayoutComponent(Component, Object)</code>.
     */
    @Deprecated
    public void addLayoutComponent(String name, Component comp) {
        synchronized (comp.getTreeLock()) {
            if (!vector.isEmpty()) {
                comp.setVisible(false);
            }
            for (int i=0; i < vector.size(); i++) {
                if (((Card)vector.get(i)).name.equals(name)) {
                    ((Card)vector.get(i)).comp = comp;
                    return;
                }
            }
            vector.add(new Card(name, comp));
        }
    }

    /**
     * Removes the specified component from the layout.
     * If the card was visible on top, the next card underneath it is shown.
     * @param   comp   the component to be removed.
     * @see     java.awt.Container#remove(java.awt.Component)
     * @see     java.awt.Container#removeAll()
     */
    public void removeLayoutComponent(Component comp) {
        synchronized (comp.getTreeLock()) {
            for (int i = 0; i < vector.size(); i++) {
                if (((Card)vector.get(i)).comp == comp) {
                    vector.remove(i);

                    break;
                }
            }
        }
    }

    /**
     * Determines the preferred size of the container argument using
     * this card layout.
     * @param   parent the parent container in which to do the layout
     * @return  the preferred dimensions to lay out the subcomponents
     *                of the specified container
     * @see     java.awt.Container#getPreferredSize
     * @see     java.awt.CardLayout#minimumLayoutSize
     */
    public Dimension preferredLayoutSize(Container parent) {
        synchronized (parent.getTreeLock()) {
            Insets insets = parent.getInsets();
            int ncomponents = parent.getComponentCount();
            int w = 0;
            int h = 0;

            for (int i = 0 ; i < ncomponents ; i++) {
                Component comp = parent.getComponent(i);
                Dimension d = comp.getPreferredSize();
                if (d.width > w) {
                    w = d.width;
                }
                if (d.height > h) {
                    h = d.height;
                }
            }
            return new Dimension(insets.left + insets.right + w + hgap*2,
                    insets.top + insets.bottom + h + vgap*2);
        }
    }

    /**
     * Calculates the minimum size for the specified panel.
     * @param     parent the parent container in which to do the layout
     * @return    the minimum dimensions required to lay out the
     *                subcomponents of the specified container
     * @see       java.awt.Container#doLayout
     * @see       java.awt.CardLayout#preferredLayoutSize
     */
    public Dimension minimumLayoutSize(Container parent) {
        synchronized (parent.getTreeLock()) {
            Insets insets = parent.getInsets();
            int ncomponents = parent.getComponentCount();
            int w = 0;
            int h = 0;

            for (int i = 0 ; i < ncomponents ; i++) {
                Component comp = parent.getComponent(i);
                Dimension d = comp.getMinimumSize();
                if (d.width > w) {
                    w = d.width;
                }
                if (d.height > h) {
                    h = d.height;
                }
            }
            return new Dimension(insets.left + insets.right + w + hgap*2,
                    insets.top + insets.bottom + h + vgap*2);
        }
    }

    /**
     * Returns the maximum dimensions for this layout given the components
     * in the specified target container.
     * @param target the component which needs to be laid out
     * @see Container
     * @see #minimumLayoutSize
     * @see #preferredLayoutSize
     */
    public Dimension maximumLayoutSize(Container target) {
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    /**
     * Returns the alignment along the x axis.  This specifies how
     * the component would like to be aligned relative to other
     * components.  The value should be a number between 0 and 1
     * where 0 represents alignment along the origin, 1 is aligned
     * the furthest away from the origin, 0.5 is centered, etc.
     */
    public float getLayoutAlignmentX(Container parent) {
        return 0.5f;
    }

    /**
     * Returns the alignment along the y axis.  This specifies how
     * the component would like to be aligned relative to other
     * components.  The value should be a number between 0 and 1
     * where 0 represents alignment along the origin, 1 is aligned
     * the furthest away from the origin, 0.5 is centered, etc.
     */
    public float getLayoutAlignmentY(Container parent) {
        return 0.5f;
    }

    /**
     * Invalidates the layout, indicating that if the layout manager
     * has cached information it should be discarded.
     */
    public void invalidateLayout(Container target) {
    }

    /**
     * Lays out the specified container using this card layout.
     * <p>
     * Each component in the <code>parent</code> container is reshaped
     * to be the size of the container, minus space for surrounding
     * insets, horizontal gaps, and vertical gaps.
     *
     * @param     parent the parent container in which to do the layout
     * @see       java.awt.Container#doLayout
     */
    public void layoutContainer(Container parent) {
        synchronized (parent.getTreeLock()) {
            Insets insets = parent.getInsets();
            int ncomponents = parent.getComponentCount();
            Component comp;
            boolean currentFound = false;

            for (int i = 0 ; i < ncomponents ; i++) {
                comp = parent.getComponent(i);
                comp.setBounds(hgap + insets.left, vgap + insets.top,
                        parent.getWidth() - (hgap*2 + insets.left + insets.right),
                        parent.getHeight() - (vgap*2 + insets.top + insets.bottom));
                if (comp.isVisible()) {
                    currentFound = true;
                }
            }

            if (!currentFound && ncomponents > 0) {
                parent.getComponent(0).setVisible(true);
            }
        }
    }

    /**
     * Make sure that the Container really has a CardLayout installed.
     * Otherwise havoc can ensue!
     */
    void checkLayout(Container parent) {
        if (parent.getLayout() != this) {
            throw new IllegalArgumentException("wrong parent for this Layout");
        }
    }

    /**
     * Returns a string representation of the state of this card layout.
     * @return    a string representation of this card layout.
     */
    public String toString() {
        return getClass().getName() + "[hgap=" + hgap + ",vgap=" + vgap + "]";
    }
}
