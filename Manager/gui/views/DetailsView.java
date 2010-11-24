package gui.views;

import business.ManagerOptions;
import business.Mod;
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

/**
 * A ModsTable view mode with details, arranged in a table.
 * @author Gcommer
 */
public class DetailsView extends ModsTableView {

    private static final int DEFAULT_ROW_HEIGHT = 16;
    /*
    private static final String[] COLUMN_NAMES = {"",
                                L10n.getString("table.modname"),
                                L10n.getString("table.modauthor"),
                                L10n.getString("table.modversion"),
                                L10n.getString("table.modstatus"),
                                L10n.getString("table.icons")};
     * TODO: The above is obviously better, but using L10n messes with the gui builder...
     */
    private static final String[] COLUMN_NAMES = {"",
                                "Name","Author","Version","Status","Icons"};
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

        component = new JTable(new DetailsViewTableModel());

        refreshOptions();
        
        ((JTable)component).addMouseListener(new PopupListener());
        ((JTable)component).getTableHeader().addMouseListener(new ColumnHeaderMouseAdapter());

        ((JTable)component).getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ((JTable)component).setAutoCreateRowSorter(true);
    }

    /**
     * Saves options to ManagerOptions
     */
    private void saveOptions() {
        ManagerOptions options = ManagerOptions.getInstance();
        options.setShowIconsInTable(columnShown[5]);
        options.setColorCheckboxesInTable(colorCheckboxes);
    }

    /**
     * Gets options from ManagerOptions and changes the table accordingly.
     */
    private void refreshOptions() {
        ManagerOptions options = ManagerOptions.getInstance();
        JTable table = (JTable) getComponent();
        columnShown[5] = options.iconsShownInTable();
        colorCheckboxes = options.getCheckboxesInTableColored();

        if(columnShown[5]){
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
        int index = ((JTable)component).rowAtPoint(p);
        if(index == -1) {
            throw new IndexOutOfBoundsException("DetailsView: Mouse not over a mod.");
        }
        return ManagerOptions.getInstance().getMods().get(index);
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
            public int getRowCount() { return modsList.size(); }

            @Override
            public String getColumnName(int col) { return COLUMN_NAMES[col]; }

            @Override
            public void setValueAt(Object value, int row, int col) {
                if( isCellEditable( row, col ) && !getModsList().isEmpty() ) {
                    Mod mod = getModsList().get(row);
                    if( col == 0 && value instanceof java.lang.Boolean ) {
                        mod.setEnabled( (Boolean)value );

                        // TODO: The line below would be better, but throws an
                        // IndexOutOfBoundsException with sorters enabled. Java bug?
                        //fireTableChanged(new TableModelEvent(this, row));
                        // Instead, we have to just let out a blanket data-changed
                        // event.  I haven't observed any performance issues with this,
                        // however with a large number of mods it may cause problems.
                        fireTableDataChanged();
                    }
                }
            }

            @Override
            public Object getValueAt(int row, int col) {
                Mod mod = modsList.get(row);
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
                            return mod.getSizedIcon();
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

            // TODO: Using L10n is better, but it messes up the gui builder >_>
            JMenuItem color = new JCheckBoxMenuItem( "Color Checkboxes" ); //L10n.getString("table.options.colorcheckboxes") );
            color.setSelected(colorCheckboxes);
            color.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    colorCheckboxes = e.getStateChange() == ItemEvent.SELECTED;

                    saveOptions();
                    refreshOptions();

                    getComponent().repaint();
                }
            });

            JMenuItem icons = new JCheckBoxMenuItem( "Icons" ); //L10n.getString("table.options.showicons") );
            icons.setSelected(columnShown[5]);
            icons.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    columnShown[5] = e.getStateChange() == ItemEvent.SELECTED;

                    saveOptions();
                    refreshOptions();

                    ((DefaultTableModel)((JTable)getComponent()).getModel()).fireTableStructureChanged();
                }
            });

            columnOptions.add(color);
            columnOptions.addSeparator();
            columnOptions.add(icons);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if(e.isPopupTrigger()) {
                columnOptions.show(((JTable)component).getTableHeader(), e.getX(), e.getY());
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
        int index = ManagerOptions.getInstance().getMods().indexOf(mod);
        ((JTable)getComponent()).getSelectionModel().setSelectionInterval(0, index);
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
    // TODO: Move these ColumnWidth methods to ModsTable.java so that if there
    // are ever
    public void setColumnWidth(int i, int w) {
        ((JTable)getComponent()).getColumnModel().getColumn(i).setPreferredWidth(w);
        ((JTable)getComponent()).getColumnModel().getColumn(i).setWidth(w);
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
