package gui.views;

import business.Mod;
import java.awt.Component;
import java.awt.Point;
import java.util.ArrayList;
import javax.swing.AbstractListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;

/**
 * A ModsTable display with icons
 * @author Gcommer
 */
public class IconsView extends ModsTableView {

    public IconsView(ArrayList<Mod> _modsList) {
        super(_modsList);

        JList comp = new JList(new ModsListModel());
        comp.setCellRenderer(new IconsListCellRenderer());
        comp.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        comp.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        comp.setVisibleRowCount(-1);

        setComponent(comp);
    }

    @Override
    public Mod getModAt(int x, int y) {
        return getModAt(new Point(x, y));
    }

    @Override
    public Mod getModAt(Point p) {
        JList list = (JList) getComponent();
        // JList doesn't have a fancy "rowAtPoint" like JTable, so work
        // through the visible items ourselves.
        // We could easily make this a binary search if it ever seems worth it.
        int lim = list.getLastVisibleIndex();
        for (int i = list.getFirstVisibleIndex(); i <= lim; ++i) {
            try {
                if (list.getCellBounds(i, i).contains(p)) {
                    return getModsList().get(i);
                }
            } catch (NullPointerException e) {
            }
        }
        throw new IndexOutOfBoundsException("IconsView: Mouse not over a mod.");
    }

    /**
     * Class to renderer cells in the Icons View list.
     *
     * Note, DefaultListCellRenderer actually is_a JLabel
     */
    private class IconsListCellRenderer extends DefaultListCellRenderer {

        public IconsListCellRenderer() {
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected,
                boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected,
                    cellHasFocus);
            Mod mod = ((Mod) value);

            setIcon(mod.getSizedIcon());
            // We display this mod in <HTML> to allow the text to wrap.
            setText("<HTML><CENTER>" + mod.getName() + "</CENTER></HTML>");
            // Grays out the icon
            // TODO: Extend this for more informative icons - should probably
            //   be abstracted to the getIcon()/getSizedIcon() method.
            setEnabled(mod.isEnabled());

            // Allows sub classes of IconsView to add stuff here.
            cellRendererExtension(this, mod);

            /*
            try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(ZIP.getFile(new File(m.getPath()), Mod.ICON_FILENAME)));

            BufferedImage image2;
            if (!m.isEnabled()) {
            ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
            ColorConvertOp op = new ColorConvertOp(cs, null);
            image = op.filter(image, null);
            image2 = ImageIO.read(getClass().getClassLoader().getResource("gui/resources/disabled.png"));
            } else if (model.getAppliedMods().contains(m)) {
            image2 = ImageIO.read(getClass().getClassLoader().getResource("gui/resources/applied.png"));
            } else {
            image2 = ImageIO.read(getClass().getClassLoader().getResource("gui/resources/enabled.png"));
            }
            BufferedImage iamgefinal = new BufferedImage(48, 48, BufferedImage.TYPE_3BYTE_BGR);
            Graphics2D graphics = iamgefinal.createGraphics();
            graphics.drawImage(image, null, 0, 0);
            graphics.drawImage(image2, null, 0, 0);
            data.setIcon(new ImageIcon(iamgefinal));
            } catch (Exception ex) {
            System.err.println(m.getName());
            ex.printStackTrace();
            data.setIcon(m.getIcon());
            }
             */

            return this;
        }
    }

    /**
     * Can be extended to allow cell renderer customizations
     * @param label the label to apply changes to
     * @param mod the mod that this cell represents
     */
    public void cellRendererExtension(JLabel label, Mod mod) {
    }

    private class ModsListModel extends AbstractListModel {

        public Object getElementAt(int i) {
            return getModsList().get(i);
        }

        public int getSize() {
            return getModsList().size();
        }
    }

    public void addListSelectionListener(ListSelectionListener lsl) {
        ((JList) getComponent()).addListSelectionListener(lsl);
    }

    @Override
    public Mod getSelectedMod() {
        return (Mod) ((JList) getComponent()).getSelectedValue();
    }

    @Override
    public void setSelectedMod(Mod mod) {
        ((JList) getComponent()).setSelectedValue(mod, true);
    }

    @Override
    public boolean hasModSelected() {
        return !(((JList) getComponent()).getSelectionModel().isSelectionEmpty());
    }

    public void selectNextMod() {
        int index = ((JList) getComponent()).getSelectedIndex();
        int max = getModsList().size() - 1;
        if (index != -1 && index < max) {
            ((JList) getComponent()).setSelectedIndex(index + 1);
        }
    }

    public void selectPrevMod() {
        int index = ((JList) getComponent()).getSelectedIndex();
        if (index != -1 && index > 0) {
            ((JList) getComponent()).setSelectedIndex(index - 1);
        }
    }
}
