package gui;

import business.ManagerOptions;
import business.Mod;
import gui.views.DetailedIconsView;
import gui.views.DetailsView;
import gui.views.IconsView;
import gui.views.ModsTableView;
import gui.views.TableViewMouseListener;
import gui.views.TilesView;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * A JPanel that displays a list of mods and allows them to be displayed in
 * multiple ways.
 *
 * To add a new view, all you have to do is:
 *  1. extend ModsTableView (or an already existing view)
 *  2. add an entry to the ViewType enum for it
 *  3. add it to the views HashMap in the ModsTable constructor
 *  4. add it to the view selection menu in the GUI
 *
 * @author Gcommer
 *
 * TODO:
 *  - the DetailsView needs preferences for whether or not the are shown
 *  - the DetailsView needs the order of the mods to be displayed
 *  - whether or not the check box column is colored
 *  - (I'm sure there are many more, but I haven't listed them yet.)
 */
public final class ModsTable extends JPanel {
    private final static int TABLE_HEIGHT = 619, TABLE_WIDTH = 619;

    public enum ViewType {
        DETAILS,
        ICONS,
        DETAILED_ICONS,
        TILES
    };

    public ModsTable() {
        this(ManagerOptions.getInstance().getMods(), ViewType.DETAILS);
    }

    public ModsTable(ArrayList<Mod> _mods, ViewType _viewMode) {
        super();

        setModsList(_mods);

        views = new HashMap(4);
        views.put( ViewType.DETAILS, new DetailsView( getModsList() ) );
        views.put( ViewType.TILES, new TilesView( getModsList() ) );
        views.put( ViewType.ICONS, new IconsView( getModsList() ) );
        views.put( ViewType.DETAILED_ICONS, new DetailedIconsView( getModsList() ) );

        setLayout(cardLayout);
        ModsTableView view;
        for( Object key : views.keySet() ) {
            view = (ModsTableView)views.get(key);
            view.addListSelectionListener( new ModSelectionListener( view ) );
            
            add( new JScrollPane(view.getComponent()),
                 key.toString() );
        }

        setViewMode(_viewMode);
        setPreferredSize(new Dimension(TABLE_WIDTH, TABLE_HEIGHT));
    }

    /**
     * Enables or disables all of the views.
     * @param enabled true to enable, false to disable
     */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        for( ModsTableView view : views.values() ) {
            view.getComponent().setEnabled(enabled);
        }
    }

    /**
     * @return whether the current view is enabled
     */
    @Override
    public boolean isEnabled() {
        // could also be:
        // return super.isEnabled();
        return getCurrentView().getComponent().isEnabled();
    }
    
    /**
     * @return the modsList
     */
    public ArrayList<Mod> getModsList() {
        return modsList;
    }

    /**
     * @param modsList the modsList to set
     */
    public void setModsList(ArrayList<Mod> modsList) {
        this.modsList = modsList;
    }

    /**
     * @return The view that is currently being displayed.
     */
    public ViewType getViewMode() {
        return viewMode;
    }

    /**
     * Sets the view mode
     * @param viewType The view to use to display mods, such as Icons or Details
     */
    public void setViewMode(ViewType viewType) {
        viewMode = viewType;
        cardLayout.show( this, viewType.toString());
        // This fixes an issue where mods wouldn't be displayed because the
        // interface wasn't repainted after being initialized with the modslist
        // size at 0.
        getCurrentView().getComponent().repaint();
    }

    /**
     * @param viewType a ViewType to get the ModsTableView for
     * @return the ModsTableView that corresponds with the provided ViewType
     */
    public ModsTableView getView(ViewType viewType) {
        return (ModsTableView)(views.get(viewType));
    }

    /**
     * @return the currently selected mod
     */
    public Mod getSelectedMod() {
        return selectedMod;
    }

    /**
     * Returns the currently visible view mode.
     * @return the current ModsTableView that is being shown.
     */
    public ModsTableView getCurrentView() {
        return getView(getViewMode());
    }

    /**
     * @param p the point at which to look for a mod.
     * @return the mod at Point p
     */
    public Mod getModAtPoint(Point p) {
        return getCurrentView().getModAt(p);
    }

    /**
     * @param selectedMod the selectedMod to set
     */
    public void setSelectedMod(Mod selectedMod) {
        this.selectedMod = selectedMod;
        for( Object key : views.values() ) {
            ((ModsTableView)key).setSelectedMod(selectedMod);
        }
    }

    public class ModSelectionListener implements ListSelectionListener {
        ModsTableView view;

        public ModSelectionListener(ModsTableView _view) {
            view = _view;
        }

        public void valueChanged(ListSelectionEvent e) {
            if( !e.getValueIsAdjusting() ) {

                try {
                    setSelectedMod( view.getSelectedMod() );
                ManagerGUI.getInstance().displayModDetail(getSelectedMod());
                } catch (Exception ex) {
                    // Due to the JTable in DetailsView, we ocassionally get an
                    // exception because the indexes don't work properly when
                    // the table is sorting; but it still works correctly overall.
                }
            }
        }
    }

    public boolean isModSelected() {
        return getCurrentView().hasModSelected();
    }

    public void redraw() {
        getCurrentView().getComponent().repaint();
    }

    private ArrayList<Mod> modsList;
    private Map<ViewType, ModsTableView> views;
    private Mod selectedMod;
    private ViewType viewMode = ViewType.DETAILS;
    private CardLayout cardLayout = new CardLayout();
}
