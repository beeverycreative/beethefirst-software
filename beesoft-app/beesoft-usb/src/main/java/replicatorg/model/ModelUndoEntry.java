package replicatorg.model;

import org.scijava.java3d.Transform3D;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import replicatorg.app.ui.modeling.EditingModel;

/**
* Copyright (c) 2013 BEEVC - Electronic Systems
* This file is part of BEESOFT software: you can redistribute it and/or modify 
* it under the terms of the GNU General Public License as published by the 
* Free Software Foundation, either version 3 of the License, or (at your option)
* any later version. BEESOFT is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
* or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License 
* for more details. You should have received a copy of the GNU General
* Public License along with BEESOFT. If not, see <http://www.gnu.org/licenses/>.
*/
public class ModelUndoEntry implements UndoableEdit {

    private Transform3D before;
    private Transform3D after;
    private Transform3D transform = new Transform3D();
    private EditingModel editListener = null;
    protected UndoManager undo = new UndoManager();
    private boolean modified = false;
    private String description;
    private boolean newOp;


    public ModelUndoEntry(Transform3D before, Transform3D after, String description, boolean newOp) {
        this.before = new Transform3D(before);
        this.after = new Transform3D(after);
        this.description = description;
        this.newOp = newOp;
    }

    public boolean addEdit(UndoableEdit edit) {
            if (!this.newOp && description == this.description) {
                after = this.after;
                return true;
            }

        return false;
    }
    
    public void setEditListener(EditingModel eModel)
    {
        editListener = eModel;
    }

    public void doEdit(Transform3D edit) {
        transform.set(edit);
        setModified(undo.canUndo());
        editListener.modelTransformChanged(edit);
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        if (this.modified == modified) {
            return;
        }
        this.modified = modified;
    }

    public boolean canRedo() {
        return true;
    }

    public boolean canUndo() {
        return true;
    }

    public void die() {
    }

    public String getPresentationName() {
        return description;
    }

    public String getRedoPresentationName() {
        return "Redo " + getPresentationName();
    }

    public String getUndoPresentationName() {
        return "Undo " + getPresentationName();
    }

    public boolean isSignificant() {
        return true;
    }

    public void redo() throws CannotRedoException {
        doEdit(after);
    }

    public boolean replaceEdit(UndoableEdit edit) {
        return false;
    }

    public void undo() throws CannotUndoException {
        doEdit(before);
    }
}