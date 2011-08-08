package gui.developing;

/*
 *  Copyright (c) 1995-2010, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Thomas Heitz, 02/19/2009
 *
 *  http://gate.ac.uk/gate/src/gate/swing/ErrorDialog.java
 */
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.*;
import java.io.StringWriter;
import java.io.PrintWriter;

public class ErrorDialog extends JOptionPane {

    private ErrorDialog(Object[] message, int errorMessage, int defaultOption,
            Icon icon, Object[] options, Object option) {
        super(message, errorMessage, defaultOption, icon, options, option);
    }

    public static void show(Throwable error, String textMessage, String title,
            Component parentComponent) {
        show(error, textMessage, title, parentComponent, null, null);
    }

    /**
     * Display a user friendly error dialog with the possibility to show the
     * stack trace and configuration and add actions as buttons.
     *
     * @param error exception that occurs, can be null; can contain newlines
     * @param textMessage error message to display
     * @param parentComponent determines the Frame in which the dialog is
     *  displayed; if null, or if the parentComponent has no Frame, a default
     *  Frame is used
     * @param icon the icon to display in the dialog; null otherwise
     * @param optionalActions optional actions that will be add as a button;
     *  null otherwise
     */
    public static void show(Throwable error, String textMessage, String title,
            Component parentComponent, Icon icon,
            Action[] optionalActions) {

        if (textMessage == null) {
            textMessage = "";
        }
        final JDialog dialog;
        String detailedMessage = "";

        // add the error stack trace in a scrollable text area, hidden at start
        if (error != null) {
            detailedMessage += "<h2>Message</h2>";
            detailedMessage += error.getMessage();
            detailedMessage += "<h2>Stack trace</h2>";
            StringWriter sw = new StringWriter();
            error.printStackTrace(new PrintWriter(sw));
            detailedMessage += sw.toString().replaceAll("(at |Caused by:)", "<strong>$1</strong> ").replaceAll("(\\([A-Za-z]+\\.java:[0-9])+\\)", "<strong>$1</strong>");
        }
        detailedMessage = detailedMessage.replace("\n", "<br>\n");
        detailedMessage = detailedMessage.replace("\t", "&nbsp;&nbsp;");
        JEditorPane messageArea = new JEditorPane("text/html", detailedMessage);
        messageArea.setEditable(false);
        messageArea.setMargin(new Insets(10, 10, 10, 10));
        final JScrollPane stackTracePane = new JScrollPane(messageArea);
        stackTracePane.setVisible(false);

        // put the message in an horizontal box
        // with a toggle button to show/hide the stack trace
        Box messageBox = Box.createHorizontalBox();
        messageBox.add(new JLabel(textMessage.startsWith("<html>")
                ? textMessage : "<html><body>"
                + textMessage.replaceAll("\n", "<br>") + "</body></html>"));
        messageBox.add(Box.createHorizontalStrut(5));
        final JToggleButton toggleButton = new JToggleButton();
        toggleButton.setToolTipText(
                "Show the error stack trace and system configuration");
        toggleButton.setMargin(new Insets(2, 4, 2, 2));
        messageBox.add(toggleButton);
        messageBox.add(Box.createHorizontalGlue());

        // add new buttons from the optionalActions parameter
        Object[] options =
                new Object[(optionalActions == null) ? 1 : optionalActions.length + 1];
        if (optionalActions != null) {
            for (int i = 0; i < optionalActions.length; i++) {
                options[i] = optionalActions[i].getValue(Action.NAME);
            }
        }
        // add the cancel button
        options[options.length - 1] = "Let it be";
        Object[] message = {messageBox, stackTracePane};

        // create the dialog
        ErrorDialog pane = new ErrorDialog(message, JOptionPane.ERROR_MESSAGE,
                JOptionPane.DEFAULT_OPTION, icon, options, options[options.length - 1]);
        dialog = pane.createDialog(parentComponent, title);
        dialog.setResizable(true);

        // add a listener for the Detail button
        toggleButton.setAction(new AbstractAction("Detail") {

            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        stackTracePane.setVisible(toggleButton.isSelected());
                        if (toggleButton.isSelected()) {
                            Dimension screenSize =
                                    Toolkit.getDefaultToolkit().getScreenSize();
                            dialog.setBounds(
                                    (screenSize.width - 700) / 2, (screenSize.height - 500) / 2, 700, 500);
                        } else {
                            dialog.pack();
                        }
                    }
                });
            }
        });

        // show the dialog
        dialog.pack();
        dialog.setVisible(true);

        // do the user selected action
        Object choice = pane.getValue();
        if (choice == null
                || choice.equals("Let it be")
                || optionalActions == null) {
            dialog.dispose();
            return;
        }
        for (int i = 0; i < optionalActions.length; i++) {
            if (options[i].equals(choice)) {
                optionalActions[i].actionPerformed(null);
                pane.setValue(JOptionPane.UNINITIALIZED_VALUE);
                break;
            }
        }
    }

    public static void main(String[] args) {
        ErrorDialog.show(new Exception("TESO cacete"), "Mensagem", "Título", null, null, null);
    }
}