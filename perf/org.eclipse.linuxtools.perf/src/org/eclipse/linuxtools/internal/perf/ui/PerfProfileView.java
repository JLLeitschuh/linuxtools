/*******************************************************************************
 * (C) Copyright 2010, 2018 IBM Corp. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Thavidu Ranatunga (IBM) - Initial implementation.
 *******************************************************************************/
package org.eclipse.linuxtools.internal.perf.ui;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.linuxtools.internal.perf.PerfPlugin;
import org.eclipse.linuxtools.internal.perf.model.TreeParent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;


public class PerfProfileView extends ViewPart {

    /**
     * The ID of the view as specified by the extension.
     */
    public static final String ID = "org.eclipse.linuxtools.internal.perf.views.ProfileView"; //$NON-NLS-1$

    private TreeViewer viewer;
    private DrillDownAdapter drillDownAdapter;
    private Action doubleClickAction;

    static class NameComparator extends ViewerComparator {
        @Override
        public int compare(Viewer viewer, Object e1, Object e2) {
            return (((TreeParent) e1).getPercent()
                    <= ((TreeParent) e2).getPercent()) ? 1 : -1;
        }
    }

    /**
     * This is a callback that will allow us
     * to create the viewer and initialize it.
     */
    @Override
    public void createPartControl(Composite parent) {
        viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        drillDownAdapter = new DrillDownAdapter(viewer);
        viewer.setContentProvider(new PerfViewContentProvider());

        viewer.setLabelProvider(new PerfViewLabelProvider());
        viewer.setComparator(new NameComparator());

        // Create the help context id for the viewer's control
        PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "org.eclipse.linuxtools.internal.perf.viewer"); //$NON-NLS-1$
        hookContextMenu();
        hookDoubleClickAction();
        contributeToActionBars();
    }

    public void refreshModel() {
        viewer.setInput(PerfPlugin.getDefault().getModelRoot());
        viewer.refresh();
    }

    private void hookContextMenu() {
        MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(manager -> PerfProfileView.this.fillContextMenu(manager));
        Menu menu = menuMgr.createContextMenu(viewer.getControl());
        viewer.getControl().setMenu(menu);
        getSite().registerContextMenu(menuMgr, viewer);
    }

    private void contributeToActionBars() {
        IActionBars bars = getViewSite().getActionBars();
        fillLocalToolBar(bars.getToolBarManager());
    }

    private void fillContextMenu(IMenuManager manager) {
        drillDownAdapter.addNavigationActions(manager);
        // Other plug-ins can contribute there actions here
        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    }

    private void fillLocalToolBar(IToolBarManager manager) {
        drillDownAdapter.addNavigationActions(manager);
    }

    private void hookDoubleClickAction() {
        doubleClickAction = new PerfDoubleClickAction(viewer);
        viewer.addDoubleClickListener(event -> doubleClickAction.run());
    }

    public TreeViewer getTreeViewer () {
        return viewer;
    }

    /**
     * Passing the focus request to the viewer's control.
     */
    @Override
    public void setFocus() {
        viewer.getControl().setFocus();
    }

    @Override
    public void setContentDescription (String name) {
        super.setContentDescription(name);
    }
}