/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui.views;

import business.Mod;
import java.awt.Point;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JComponent;
import javax.swing.event.ListSelectionListener;

/**
 * @author George
 */
public abstract class ModsTableView {

    public ModsTableView(ArrayList<Mod> _modsList) {
        setModsList(_modsList);
        if (getComponent() != null) {
            getComponent().addMouseListener(new TableViewMouseListener());
        }
    }

    /**
     * @return the modsList
     */
    private int previousHash = 0;
    public ArrayList<Mod> getModsList() {
        if (modsList.toString().hashCode() != previousHash) {
            previousHash = modsList.toString().hashCode();
            sortList();
        }
        return modsList;
    }

    /**
     * @param modsList the modsList to set
     */
    public void setModsList(ArrayList<Mod> modsList) {
        this.modsList = modsList;
        if (modsList.toString().hashCode() != previousHash) {
            previousHash = modsList.toString().hashCode();
            sortList();
        }
    }

    private void sortList() {
        if (modsList != null && !modsList.isEmpty()) {
             Mod[] list = new Mod[modsList.size()];
            for (int i = 0; i < list.length; i++) {
                list[i] = modsList.get(i);
            }
            quickSort(list, 0, list.length - 1);
            ArrayList<Mod> newList = new ArrayList<Mod>();
            newList.addAll(Arrays.asList(list));
            this.modsList = newList;
        }
    }

    /**
     * @param kl The key listener to add to this view's component
     */
    public void addKeyListener(KeyListener kl) {
        getComponent().addKeyListener(kl);
    }

    /**
     * @return the component
     */
    public JComponent getComponent() {
        return component;
    }

    /**
     * @param component the component to set
     */
    protected void setComponent(JComponent component) {
        this.component = component;
        this.component.addMouseListener(new TableViewMouseListener());
    }

    public abstract Mod getModAt(int x, int y);

    public abstract Mod getModAt(Point p);

    public abstract Mod getSelectedMod();

    public abstract void setSelectedMod(Mod mod);

    public abstract boolean hasModSelected();

    public abstract void addListSelectionListener(ListSelectionListener lsl);

    public abstract void selectNextMod();

    public abstract void selectPrevMod();
    private ArrayList<Mod> modsList;
    private JComponent component;

    void quickSort(Mod arr[], int left, int right) {
        int index = partition(arr, left, right);
        if (left < index - 1) {
            quickSort(arr, left, index - 1);
        }
        if (index < right) {
            quickSort(arr, index, right);
        }
    }

    int partition(Mod arr[], int left, int right) {
        int i = left, j = right;
        Mod tmp;
        Mod pivot = arr[(left + right) / 2];

        while (i <= j) {
            while (arr[i].getName().compareToIgnoreCase(pivot.getName()) < 0) {
                i++;
            }
            while (arr[j].getName().compareToIgnoreCase(pivot.getName()) > 0) {
                j--;
            }
            if (i <= j) {
                tmp = arr[i];
                arr[i] = arr[j];
                arr[j] = tmp;
                i++;
                j--;
            }
        }

        return i;
    }
}
