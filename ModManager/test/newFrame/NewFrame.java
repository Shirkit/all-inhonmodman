/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package newFrame;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Shirkit
 */
public class NewFrame extends JFrame {

    /**
     * Components
     */
    private JPanel tablePanel;
    private JTable tableMods;
    /**
     * Components
     */

    public NewFrame() {
        setLayout(new BorderLayout());
        setTitle("All-In Hon ModManager");
        setMinimumSize(new Dimension(600, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        tablePanel = new JPanel();
        tableMods = new JTable();
        tableMods.setModel(new DefaultTableModel());
        tableMods.setMinimumSize(new Dimension(1200, 1200));
        tablePanel.add(tableMods);
        tablePanel.setMinimumSize(new Dimension(1600, 1600));
        add(tablePanel);

        pack();
    }

    public static void main(String[] args) {
        NewFrame frame = new NewFrame();
        frame.setVisible(true);
    }

}
