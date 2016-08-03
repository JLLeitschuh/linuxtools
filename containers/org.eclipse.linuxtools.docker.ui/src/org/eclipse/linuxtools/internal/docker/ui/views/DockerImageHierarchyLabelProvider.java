/*******************************************************************************
 * Copyright (c) 2016 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.docker.ui.views;

import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.linuxtools.docker.core.IDockerImageHiearchyNode;
import org.eclipse.linuxtools.internal.docker.ui.SWTImagesFactory;
import org.eclipse.swt.graphics.Image;

/**
 * the {@link ILabelProvider} implementation fo the
 * {@link DockerImageHierarchyView}.
 */
public class DockerImageHierarchyLabelProvider
		implements IStyledLabelProvider, ILabelProvider {

	private Image IMAGE_IMAGE = SWTImagesFactory.DESC_IMAGE.createImage();

	@Override
	public void dispose() {
		IMAGE_IMAGE.dispose();
	}

	@Override
	public Image getImage(Object element) {
		if (element instanceof IDockerImageHiearchyNode) {
			return IMAGE_IMAGE;
		}
		return null;
	}

	@Override
	public String getText(Object element) {
		return getStyledText(element).getString();
	}

	@Override
	public StyledString getStyledText(Object element) {
		if (element instanceof IDockerImageHiearchyNode) {
			return LabelProviderUtils.getStyleString(
					((IDockerImageHiearchyNode) element).getImage());
		}
		return null;
	}

	@Override
	public void addListener(ILabelProviderListener listener) {
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

}
