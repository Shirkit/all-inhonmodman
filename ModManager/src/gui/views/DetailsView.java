package gui.views;

import business.ManagerOptions;
import business.Mod;
import gui.ManagerCtrl;
import gui.ManagerGUI;
import gui.l10n.L10n;
import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import org.apache.log4j.Logger;

/**
 * A ModsTable view mode with details, arranged in a table.
 * @author Gcommer
 */
public class DetailsView extends ModsTableView {
    private static final Logger logger = Logger.getLogger(DetailsView.class.getPackage().getName());

    public static final int DEFAULT_ROW_HEIGHT = 25;
    private static final String[] COLUMN_NAMES = {"",
                                L10n.getString("table.modname"),
                                L10n.getString("table.modauthor"),
                                L10n.getString("table.modversion"),
                                L10n.getString("table.modstatus"),
                                L10n.getString("table.icons")};
    /*
    private static final String[] COLUMN_NAMES = {"","Name","Author","Version","Status","Icons"};
     * Using L10n messes up the GUI builder unfortunately, so if you have to
     * change the GUI, temporarily use the non-L10n definition of COLUMN_NAMES.
     */
    private static final Class[] COLUMN_TYPES = new Class [] {
        java.lang.Boolean.class, java.lang.String.class, java.lang.String.class,
        java.lang.String.class, java.lang.String.class, javax.swing.ImageIcon.class
    };
    private static final boolean[] CAN_EDIT_COLUMN = new boolean [] {
        true, false, false, false, false, false
    };
    private boolean[] columnShown = new boolean [] {
        true, true, true, true, true, true
    };
    private boolean colorCheckboxes;

    public DetailsView(ArrayList<Mod> _modsList) {
        super(_modsList);

        columnShown[5] = options.iconsShownInTable();
        colorCheckboxes = options.getCheckboxesInTableColored();

        JTable comp = new JTable(new DetailsViewTableModel());
        
        comp.getTableHeader().addMouseListener(new ColumnHeaderMouseAdapter());
        comp.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        comp.setAutoCreateRowSorter(true);

        setComponent(comp);
        applyOptions();
    }

    /**
     * Saves options to ManagerOptions
     */
    private void saveOptions() {
        options.setShowIconsInTable(columnShown[5]);
        options.setColorCheckboxesInTable(colorCheckboxes);
    }

    /**
     * Gets options from ManagerOptions and changes the table accordingly.
     */
    @Override
    public void applyOptions() {
        JTable table = (JTable) getComponent();

        if(columnShown[5] && !options.usingSmallIcons()) {
            table.setRowHeight(Mod.ICON_HEIGHT);
        } else {
            table.setRowHeight(DEFAULT_ROW_HEIGHT);
        }

        if(colorCheckboxes) {
            table.setDefaultRenderer(Boolean.class, new ColorCodedBooleanTabelCellRenderer());
        } else {
            table.setDefaultRenderer(Boolean.class, (new JTable()).getDefaultRenderer(Boolean.class));
        }
    }

    @Override
    public Mod getModAt(int x, int y) {
        return getModAt(new Point(x,y));
    }

    @Override
    public Mod getModAt(Point p) {
        JTable comp = ((JTable)getComponent());
        int index = comp.convertRowIndexToModel(comp.rowAtPoint(p));
        if(index == -1) {
            throw new IndexOutOfBoundsException("DetailsView: Mouse not over a mod.");
        }
        return getModsList().get(index);
    }

    protected class ColorCodedBooleanTabelCellRenderer extends
                                                     DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                            JTable table, Object value, boolean isSelected,
                            boolean hasFocus, int row, int col )
        {
            Component renderer = (new JTable()).getDefaultRenderer(Boolean.class)
                                      .getTableCellRendererComponent(
                                          table,  value, isSelected,
                                          hasFocus, row, col );

            if((Boolean)value) {
                renderer.setBackground(Color.GREEN);
            }
            else {
                renderer.setBackground(Color.RED);
            }

            return renderer;
        }
    }

    private class DetailsViewTableModel extends DefaultTableModel {
            @Override
            public boolean isCellEditable(int row, int col) {
                return CAN_EDIT_COLUMN[col];
            }

            @Override
            public int getColumnCount() { return COLUMN_NAMES.length - (columnShown[5]? 0:1); }

            @Override
            public int getRowCount() { return getModsList().size(); }

            @Override
            public String getColumnName(int col) { return COLUMN_NAMES[col]; }

            @Override
            public void setValueAt(Object value, int row, int col) {
                if( isCellEditable( row, col ) && !getModsList().isEmpty() ) {
                    Mod mod = getModsList().get(row);
                    if( col == 0 && value instanceof java.lang.Boolean ) {
                        ManagerCtrl.getInstance().enableMod(mod);

                        // TODO: The line below would be better, but throws an
                        // IndexOutOfBoundsException with sorters enabled. Java bug?
                        //fireTableChanged(new TableModelEvent(this, row));
                        // Instead, we have to just let out a blanket data-changed
                        // event.  I haven't observed any performance issues with this,
                        // however with a large number of mods it may cause problems.
                        // From Shirkit: The line above causes after enabling the mod to clear the mod selection and disabling it causes no strange behavior.
                        //fireTableDataChanged();
                        setSelectedMod(mod);
                    }
                }
            }

            @Override
            public Object getValueAt(int row, int col) {
                if (row < getModsList().size()) {
                Mod mod = getModsList().get(row);
                switch(col) {
                    case 0:
                        return (Boolean)mod.isEnabled();
                    case 1:
                        return mod.getName();
                    case 2:
                        return mod.getAuthor();
                    case 3:
                        return mod.getVersion();
                    case 4:
                        if (mod.isEnabled()) {
                            if (ManagerOptions.getInstance().getAppliedMods().contains(mod)) {
                                return (String) L10n.getString("table.modstatus.applied");
                            } else {
                                return (String) L10n.getString("table.modstatus.enabled");
                            }
                        } else {
                            return (String) L10n.getString("table.modstatus.disabled");
                        }
                    case 5:
                        if(columnShown[5]) {
                            if(options.usingSmallIcons()) {
                                return mod.getSmallIcon();
                            } else {
                                return mod.getSizedIcon();
                            }
                        }
                }
                }
                return null;
            }

            @Override
            public Class<?> getColumnClass(int col) { return COLUMN_TYPES[col];}
    }

    /**
     * A MouseAdapter for the table's column that displays a popup to select
     * which columns are shown. So far, only the icons column can be changed.
     */
    private class ColumnHeaderMouseAdapter extends MouseAdapter {
        JPopupMenu columnOptions;

        public ColumnHeaderMouseAdapter() {
            super();
            
            columnOptions = new JPopupMenu();

            JMenuItem color = new JCheckBoxMenuItem( L10n.getString("table.options.colorcheckboxes") );
            color.setSelected(colorCheckboxes);
            color.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    colorCheckboxes = e.getStateChange() == ItemEvent.SELECTED;

                    saveOptions();
                    applyOptions();

                    getComponent().repaint();
                }
            });

            JMenuItem icons = new JCheckBoxMenuItem( L10n.getString("table.options.showicons") );
            icons.setSelected(columnShown[5]);
            icons.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    columnShown[5] = e.getStateChange() == ItemEvent.SELECTED;

                    saveOptions();
                    applyOptions();

                    ((DefaultTableModel)((JTable)getComponent()).getModel()).fireTableStructureChanged();
                }
            });

            columnOptions.add(color);
            columnOptions.addSeparator();
            columnOptions.add(icons);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if(e.isPopupTrigger()) { // Show the popup menu.
                columnOptions.show(((JTable)getComponent()).getTableHeader(), e.getX(), e.getY());
            }
        }
    }
    
    /**
     * @return the showIcons
     */
    public boolean showIcons() {
        return showIcons;
    }

    /**
     * @param showIcons the showIcons to set
     */
    public void showIcons(boolean showIcons) {
        this.showIcons = showIcons;
    }

    public void addListSelectionListener(ListSelectionListener lsl) {
        ((JTable)getComponent()).getSelectionModel().addListSelectionListener(lsl);
    }

    public Mod getSelectedMod() {
        JTable table = ((JTable)getComponent());
        if(table.getSelectedRow() < 0) {
            throw new IndexOutOfBoundsException("Invalid or no mod selected.");
        }
        return getModsList().get(table.convertRowIndexToModel(table.getSelectedRow()));
    }

    /**
     * @param mod Mod to select
     */
    @Override
    public void setSelectedMod(Mod mod) {
        //int index = getModsList().indexOf(mod);
        //((JTable)getComponent()).getSelectionModel().setSelectionInterval(0, index);
        ManagerGUI.getInstance().displayModDetail(mod);
    }

    /**
     * @return if a mod is selected
     */
    @Override
    public boolean hasModSelected() {
        return ((JTable)getComponent()).getSelectedRow() != -1;
    }

    /**
     * @param i index of column to set the width for
     * @param w desired width of column i
     */
    public void setColumnWidth(int i, int w) {
        if(i > COLUMN_NAMES.length - (columnShown[5]? 0:1))
            return; // TODO: We should probably thrown an exception, but OH WELL
        ((JTable)getComponent()).getColumnModel().getColumn(i).setPreferredWidth(w);
        ((JTable)getComponent()).getColumnModel().getColumn(i).setWidth(w);
    }

    /**
     * @return a string representing the current order of the columns.
     */
    public String serializeColumnOrder() {
        JTable comp = (JTable) getComponent();
        String ret = "";
        int lim = COLUMN_NAMES.length - (columnShown[5]? 0:1);
        for(int n=0; n < lim; ++n) {
            ret += comp.getTableHeader().getColumnModel().getColumn(n).getModelIndex() + "-";
        }
        return ret.substring(0, ret.length()-1);
    }
    
    public void deserializeColumnOrder(String data) {
        TableColumnModel model = ((JTable) getComponent()).getColumnModel();
        String[] indexes = data.split("-");
        int lim = COLUMN_NAMES.length - (columnShown[5]? 0:1);
        for(int n=0; n < indexes.length && n < lim; ++n) {
            int i = Integer.parseInt(indexes[n]);
            if(i < lim && i < COLUMN_NAMES.length) {
                model.getColumn(n).setModelIndex(i);
                model.getColumn(n).setHeaderValue(COLUMN_NAMES[i]);
            }
        }
    }

    /**
     * @param i index of column to set the width for
     * @return width of column i
     */
    public int getColumnWidth(int i) {
        return ((JTable)getComponent()).getColumnModel().getColumn(i).getWidth();
    }
    
    public void selectNextMod() {
        JTable table = (JTable) getComponent();
        int index = table.getSelectedRow();
        if(index != -1 && index < getModsList().size()-1) {
            index = table.convertColumnIndexToModel(index) + 1;
            table.getSelectionModel().setSelectionInterval(index, index);
        }
    }

    public void selectPrevMod() {
        JTable table = (JTable) getComponent();
        int index = table.getSelectedRow();
        if(index != -1 && index > 0) {
            index = table.convertColumnIndexToModel(index) - 1;
            table.getSelectionModel().setSelectionInterval(index, index);
        }
    }

    private boolean showIcons;
}
