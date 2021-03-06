/*******************************************************************************
 * Copyright (c) 2014, 2018 Red Hat.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.docker.ui.views;

import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.linuxtools.docker.core.IDockerImage;

public class ImageInfoContentProvider implements ITreeContentProvider {

	private static final Object[] EMPTY = new Object[0];

	@Override
	public void dispose() {

	}

	@Override
	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
	}

	@Override
	public Object[] getElements(final Object inputElement) {
		if (inputElement instanceof IDockerImage) {
			final IDockerImage image = (IDockerImage) inputElement;
			return new Object[] {
					new Object[] { "Id", image.id() }, //$NON-NLS-1$
					new Object[] { "ParentId", image.parentId() }, //$NON-NLS-1$
					new Object[] { "Created", //$NON-NLS-1$
							image.createdDate() != null ? image.createdDate()
									: "unknown" }, //$NON-NLS-1$
					new Object[] {
							"RepoTags", LabelProviderUtils.reduce(image.repoTags()) }, //$NON-NLS-1$
					new Object[] { "Size", LabelProviderUtils.toString(image.size()) }, //$NON-NLS-1$
					new Object[] {
							"VirtualSize", LabelProviderUtils.toString(image.virtualSize()) }, //$NON-NLS-1$
					new Object[] {
							"IsIntermediateImage", LabelProviderUtils.toString(image.isIntermediateImage()) }, //$NON-NLS-1$
					new Object[] {
							"IsDangling", LabelProviderUtils.toString(image.isDangling()) }, //$NON-NLS-1$
			};
		}
		return EMPTY;
	}

	@Override
	public Object[] getChildren(final Object parentElement) {
		final Object propertyValue = ((Object[])parentElement)[1];
		final Object value = ((Object[])parentElement)[1];
		if(value instanceof List) {
			@SuppressWarnings("unchecked")
			final List<Object> propertyValues = (List<Object>)propertyValue;
			final Object[] result = new Object[propertyValues.size()];
			for (int i = 0; i < propertyValues.size(); i++) {
				result[i] = new Object[]{"", LabelProviderUtils.toString(propertyValues.get(i))};
			}
			return result;
		} else if(value instanceof Object[]) {
			return (Object[])value;
		}
		return new Object[]{value};
	}

	@Override
	public Object getParent(final Object element) {
		return null;
	}

	@Override
	public boolean hasChildren(final Object element) {
		if(element instanceof Object[]) {
			return !(((Object[])element)[1] instanceof String);
		}
		return false;
	}

}
