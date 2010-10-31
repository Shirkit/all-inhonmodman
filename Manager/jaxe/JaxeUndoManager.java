package jaxe;

import org.apache.log4j.Logger;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

/**
 * UndoManager to recognise a modified document
 * @author Kykal
 */
public class JaxeUndoManager extends UndoManager {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(JaxeUndoManager.class);

    public JaxeUndoManager(final JaxeDocument doc) {
        _doc = doc;
    }

    @Override
    public synchronized boolean addEdit(final UndoableEdit anEdit) {
        _doc.setModif(true);
        return super.addEdit(anEdit);
    }

    @Override
    public synchronized void undo() throws CannotUndoException {
        super.undo();
        _doc.setModif(true);
    }

    @Override
    public synchronized void redo() throws CannotRedoException {
        super.redo();
        _doc.setModif(true);
    }
    
    private final JaxeDocument _doc;
}

