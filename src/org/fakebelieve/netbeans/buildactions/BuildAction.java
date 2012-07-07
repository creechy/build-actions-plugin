/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.fakebelieve.netbeans.buildactions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.awt.ActionRegistration;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionID;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;
import org.openide.util.Task;
import org.openide.util.TaskListener;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

@ActionID(category = "Build",
id = "org.fakebelieve.netbeans.buildactions.BuildAction")
@ActionRegistration(iconBase = "org/fakebelieve/netbeans/buildactions/neko.png",
displayName = "#CTL_BuildAction")
@ActionReferences({
    @ActionReference(path = "Toolbars/Build", position = 250)
})
@Messages("CTL_BuildAction=Build Action")
public final class BuildAction implements ActionListener {

    private static final Logger logger = Logger.getLogger(BuildAction.class.getName());
    private final DataObject dataContext;

    public BuildAction(DataObject dataContext) {
        this.dataContext = dataContext;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        String path = dataContext.getPrimaryFile().getPath();
        String file = dataContext.getPrimaryFile().getNameExt();

        Project owner = FileOwnerQuery.getOwner(FileUtil.toFileObject(new File(path)));
        logger.log(Level.INFO, "PRIMARY File Path = {0}", path);
        logger.log(Level.INFO, "PRIMARY File Name = {0}", file);
        logger.log(Level.INFO, "OWNER PROJECT = {0}", ((owner != null) ? owner.getProjectDirectory() : ""));

        String build = NbPreferences.forModule(BuildActionsPanel.class).get("build", null);

        //
        // If a project is selected based on the primary file with focus 
        // use it, otherwise use the main project
        //
        Project project = (owner != null) ? owner : OpenProjects.getDefault().getMainProject();
        if (project != null) {
            try {
                FileObject mainDirectory = project.getProjectDirectory();
                logger.log(Level.INFO, "PROJECT DIRECTORY = {0}", mainDirectory.getPath());

                FileObject projectBuildFile = FileUtil.toFileObject(new File(FileUtil.toFile(mainDirectory), "build.xml"));
                FileObject projectNekoFile = FileUtil.toFileObject(new File(FileUtil.toFile(mainDirectory), "build-neko.xml"));

                final File buildFile;
                final boolean deleteBuildFile;

                //
                // See if a a Neko build file exists in the project and use it if so,
                // Otherwise create a temporary file an use the contents of the build preference
                // for its contents to invoke.
                //
                if (projectNekoFile != null && projectNekoFile.isValid()) {
                    buildFile = FileUtil.toFile(projectNekoFile);
                    deleteBuildFile = false;
                } else if (build != null && build.length() > 0) {
                    buildFile = File.createTempFile("build-", ".xml");
                    buildFile.deleteOnExit();

                    BufferedWriter out = new BufferedWriter(new FileWriter(buildFile));
                    out.write(build);
                    out.close();
                    deleteBuildFile = true;
                } else {
                    buildFile = null;
                    deleteBuildFile = false;
                }

                //
                // If we've located a build file somehow, lets invoke it,
                // otherwise just exit silently.
                //
                if (buildFile != null) {
                    logger.log(Level.INFO, "BUILD FILE = {0}", buildFile);

                    String target = ProjectUtils.getInformation(project).getName();
                    logger.log(Level.INFO, "PROJECT NAME = {0}", target);

                    Properties properties = new Properties();
                    properties.put("project-build-file", projectBuildFile.getPath());
                    properties.put("project-folder", mainDirectory.getPath());
                    if (path != null) {
                        properties.put("primary-path", path.substring(mainDirectory.getPath().length() + 1));
                        properties.put("primary-file", file);
                    }
                    ExecutorTask runTarget = ActionUtils.runTarget(FileUtil.toFileObject(buildFile), new String[]{target}, properties);
                    runTarget.addTaskListener(new TaskListener() {

                        @Override
                        public void taskFinished(Task task) {
                            if (deleteBuildFile) {
                                buildFile.delete();
                            }
                        }
                    });
                }
            } catch (IOException ex) {
                logger.log(Level.SEVERE, "Error", ex);
            } catch (IllegalArgumentException ex) {
                logger.log(Level.SEVERE, "Error", ex);
            }
        } else {
            InputOutput io = IOProvider.getDefault().getIO("Build Actions", false);
            io.select(); //Tree tab is selected
            OutputWriter writer = io.getOut();
            writer.println("Error! No Project/File Selected.");
        }
    }
}
