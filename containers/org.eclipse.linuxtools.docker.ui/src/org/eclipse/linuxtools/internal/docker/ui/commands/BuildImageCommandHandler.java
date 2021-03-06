/*******************************************************************************
 * Copyright (c) 2015, 2018 Red Hat Inc. and others.
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
package org.eclipse.linuxtools.internal.docker.ui.commands;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.linuxtools.docker.core.DockerConnectionManager;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.internal.docker.ui.jobs.BuildDockerImageJob;
import org.eclipse.linuxtools.internal.docker.ui.views.DVMessages;
import org.eclipse.linuxtools.internal.docker.ui.wizards.ImageBuild;
import org.eclipse.linuxtools.internal.docker.ui.wizards.WizardMessages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Command handler to build a Docker Image from a Dockerfile.
 */
public class BuildImageCommandHandler extends AbstractHandler {

	private final static String BUILD_IMAGE_JOB_TITLE = "ImageBuild.msg"; //$NON-NLS-1$
	private static final String IMAGE_DIRECTORY_VALIDATE = "ImageDirectoryValidate.msg"; //$NON-NLS-1$
	

	@Override
	public Object execute(final ExecutionEvent event) {
		final ImageBuild wizard = new ImageBuild();
		final WizardDialog wizardDialog = new NonModalWizardDialog(HandlerUtil.getActiveShell(event), wizard);
		wizardDialog.create();
		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		IWorkbenchPage activePage = null;
		IWorkbenchPart activePart = null;
		if (window != null)
			activePage = window.getActivePage();
		if (activePage != null)
			activePart = activePage.getActivePart();
		IDockerConnection connection = CommandUtils
				.getCurrentConnection(activePart);
		// if no current connection, try the first connection in the list of
		// connections
		if (connection == null) {
			connection = DockerConnectionManager.getInstance()
					.getFirstConnection();

		}
		if (connection == null || !connection.isOpen()) {
			// if no active connection, issue error message dialog and return
			Display.getDefault().syncExec(() -> MessageDialog.openError(
					PlatformUI.getWorkbench().getActiveWorkbenchWindow()
							.getShell(),
					WizardMessages.getString("ErrorNoActiveConnection.msg"), //$NON-NLS-1$
					WizardMessages.getString("ErrorNoActiveConnection.desc"))); //$NON-NLS-1$
			return null;
		}
		final boolean buildImage = wizardDialog.open() == Window.OK;
		if (buildImage) {
			performBuildImage(wizard, connection);
		}
		return null;
	}
	
	private void performBuildImage(final ImageBuild wizard,
			final IDockerConnection connection) {
		final Job buildImageJob = new Job(
				DVMessages.getString(BUILD_IMAGE_JOB_TITLE)) {

			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				final String id = wizard.getImageName();
				@SuppressWarnings("unused")
				final int lines = wizard.getNumberOfLines();
				final IPath path = wizard.getDirectory();
				final String dockerfileName = "Dockerfile"; //$NON-NLS-1$

				monitor.beginTask(DVMessages.getString(BUILD_IMAGE_JOB_TITLE),
						2);
				monitor.subTask(
						WizardMessages.getString(IMAGE_DIRECTORY_VALIDATE));
				try {
					Files.walkFileTree(Paths.get(path.toString()),
							new FileVisitor<java.nio.file.Path>() {
								@Override
								public FileVisitResult preVisitDirectory(
										java.nio.file.Path dir,
										BasicFileAttributes attrs) {
									return FileVisitResult.CONTINUE;
								}
								@Override
								public FileVisitResult visitFile(
										java.nio.file.Path file,
										BasicFileAttributes attrs) throws IOException {
									if (!file.toFile().canRead()) {
										throw new IOException();
									}
									return FileVisitResult.CONTINUE;
								}
								@Override
								public FileVisitResult visitFileFailed(
										java.nio.file.Path file, IOException exc)
												throws IOException {
									throw exc;
								}
								@Override
								public FileVisitResult postVisitDirectory(
										java.nio.file.Path dir, IOException exc) {
									return FileVisitResult.CONTINUE;
								}
							});
				} catch (final IOException e) {
					Display.getDefault().syncExec(() -> MessageDialog.openError(
							PlatformUI.getWorkbench().getActiveWorkbenchWindow()
									.getShell(),
							WizardMessages
									.getString("ErrorInvalidDirectory.msg"), //$NON-NLS-1$
							WizardMessages.getFormattedString(
									"ErrorInvalidPermissions.msg", //$NON-NLS-1$
									path.toString())));
					return Status.OK_STATUS;
				}
				monitor.worked(1);

				// build the image and let the progress
				// handler refresh the images when done
				try {
					monitor.subTask(
							DVMessages.getString(BUILD_IMAGE_JOB_TITLE));
					final Job buildImageJob = new BuildDockerImageJob(
							connection, path, dockerfileName, id, null);
					buildImageJob.schedule();
					monitor.worked(1);
				} finally {
					monitor.done();
				}
				return Status.OK_STATUS;
			}

		};

		buildImageJob.schedule();

	}

	/**
	 * We need a non-modal (omitting SWT.APPLICATION_MODAL) wizard dialog to
	 * ensure that any detatched editor windows are brought up can be accessed.
	 */
	private static class NonModalWizardDialog extends WizardDialog {
		public NonModalWizardDialog(Shell parentShell, IWizard newWizard) {
			super(parentShell, newWizard);
			setShellStyle(SWT.CLOSE | SWT.MAX | SWT.TITLE | SWT.BORDER
					| SWT.RESIZE | getDefaultOrientation());
		}
	}

}
