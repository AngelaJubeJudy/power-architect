/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of Power*Architect.
 *
 * Power*Architect is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*Architect is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */
package ca.sqlpower.architect.swingui.action;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.sqlobject.SQLCatalog;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLObject;
import ca.sqlpower.sqlobject.SQLObjectUtils;
import ca.sqlpower.sqlobject.SQLSchema;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.util.SQLPowerUtils;


public class ProfileAction extends AbstractArchitectAction {
    private static final Logger logger = Logger.getLogger(ProfileAction.class);


    public ProfileAction(ArchitectFrame frame) {
        super(frame, Messages.getString("ProfileAction.name"), Messages.getString("ProfileAction.desctiption"), "Table_profiled"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    private void profileItemsFromDBTree() {
        TreePath targetDBPath = getSession().getDBTree().getPathForRow(0);
        
        if (getSession().getDBTree().getSelectionPaths() != null) {
            for (TreePath path: getSession().getDBTree().getSelectionPaths()){
                if (targetDBPath.isDescendant(path)) {
    
                    int answer = JOptionPane.showConfirmDialog(getSession().getArchitectFrame(),
                            Messages.getString("ProfileAction.cannotProfileProjectDb"), //$NON-NLS-1$
                            Messages.getString("ProfileAction.continueProfilingOption"),JOptionPane.OK_CANCEL_OPTION); //$NON-NLS-1$
                    if (answer == JOptionPane.CANCEL_OPTION){
                        return;
                    } else {
                        break;
                    }
    
                }
            }
        } else {
            //this is a very rare case where you load a project then immediately push 
            //profile, it should not tell you to select a table. All other cases will
            //have something selected
            getSession().getDBTree().setSelectionPath(targetDBPath);
        }

        //logger.debug("getSession().getDBTree().getSelectionPaths() # = " + getSession().getDBTree().getSelectionPaths().length);
        try {
            Set<SQLObject> sqlObject = new HashSet<SQLObject>();
            for ( TreePath tp : getSession().getDBTree().getSelectionPaths() ) {
                logger.debug("Top of first loop, treepath=" + tp); //$NON-NLS-1$
                // skip the target db
                if (targetDBPath.isDescendant(tp)) continue;
                if ( tp.getLastPathComponent() instanceof SQLDatabase ) {
                    sqlObject.add((SQLDatabase)tp.getLastPathComponent());
                }
                else if ( tp.getLastPathComponent() instanceof SQLCatalog ) {
                    SQLCatalog cat = (SQLCatalog)tp.getLastPathComponent();
                    sqlObject.add(cat);
                    SQLDatabase db = SQLPowerUtils.getAncestor(cat,SQLDatabase.class);
                    if ( db != null && sqlObject.contains(db))
                        sqlObject.remove(db);
                } else if ( tp.getLastPathComponent() instanceof SQLSchema ) {
                    SQLSchema sch = (SQLSchema)tp.getLastPathComponent();
                    sqlObject.add(sch);

                    SQLCatalog cat = SQLPowerUtils.getAncestor(sch,SQLCatalog.class);
                    if ( cat != null && sqlObject.contains(cat))
                        sqlObject.remove(cat);
                    SQLDatabase db = SQLPowerUtils.getAncestor(sch,SQLDatabase.class);
                    if ( db != null && sqlObject.contains(db))
                        sqlObject.remove(db);
                }  else if ( tp.getLastPathComponent() instanceof SQLTable ) {
                    SQLTable tab = (SQLTable)tp.getLastPathComponent();
                    sqlObject.add(tab);

                    SQLSchema sch = SQLPowerUtils.getAncestor(tab,SQLSchema.class);
                    if ( sch != null && sqlObject.contains(sch))
                        sqlObject.remove(sch);
                    SQLCatalog cat = SQLPowerUtils.getAncestor(sch,SQLCatalog.class);
                    if ( cat != null && sqlObject.contains(cat))
                        sqlObject.remove(cat);
                    SQLDatabase db = SQLPowerUtils.getAncestor(sch,SQLDatabase.class);
                    if ( db != null && sqlObject.contains(db))
                        sqlObject.remove(db);

                } else if ( tp.getLastPathComponent() instanceof SQLColumn ) {
                    SQLTable tab = ((SQLColumn)tp.getLastPathComponent()).getParent();
                    sqlObject.add((SQLColumn)tp.getLastPathComponent());
                    SQLSchema sch = SQLPowerUtils.getAncestor(tab,SQLSchema.class);
                    if ( sch != null && sqlObject.contains(sch))
                        sqlObject.remove(sch);
                    SQLCatalog cat = SQLPowerUtils.getAncestor(sch,SQLCatalog.class);
                    if ( cat != null && sqlObject.contains(cat))
                        sqlObject.remove(cat);
                    SQLDatabase db = SQLPowerUtils.getAncestor(sch,SQLDatabase.class);
                    if ( db != null && sqlObject.contains(db))
                        sqlObject.remove(db);

                }
            }
            final Set<SQLTable> tables = new HashSet<SQLTable>();
            for ( SQLObject o : sqlObject ) {
                if ( o instanceof SQLColumn){
                    tables.add(((SQLColumn)o).getParent());
                } else {
                    tables.addAll(SQLObjectUtils.findDescendentsByClass(o, SQLTable.class, new ArrayList<SQLTable>()));
                }
            }

            logger.debug("Calling profileManager.asynchCreateProfiles(tables)"); //$NON-NLS-1$
            getSession().getProfileManager().asynchCreateProfiles(tables);
            JDialog profileDialog = getSession().getProfileDialog();
            profileDialog.pack();
            profileDialog.setVisible(true);

        } catch (Exception ex) {
            logger.error("Error in Profile Action ", ex); //$NON-NLS-1$
            ASUtils.showExceptionDialog(getSession(), Messages.getString("ProfileAction.profileError"), ex); //$NON-NLS-1$
        }
    }

    public void actionPerformed(ActionEvent e) {
        profileItemsFromDBTree();
    }

}