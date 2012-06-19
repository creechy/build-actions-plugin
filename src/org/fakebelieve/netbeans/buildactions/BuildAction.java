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
        Project owner = FileOwnerQuery.getOwner(FileUtil.toFileObject(new File(path)));
        logger.log(Level.INFO, "PRIMARY File = " + path);
        logger.log(Level.INFO, "OWNER PROJECT = " + ((owner != null) ? owner.getProjectDirectory() : ""));
        String actions = NbPreferences.forModule(BuildActionsPanel.class).get("actions", "compile-test");
        String build = NbPreferences.forModule(BuildActionsPanel.class).get("build", null);

        Project project = (owner != null) ? owner : OpenProjects.getDefault().getMainProject();
        if (project != null) {
            try {
                FileObject mainDirectory = project.getProjectDirectory();
                logger.log(Level.INFO, "PROJECT DIRECTORY = " + mainDirectory.getPath());

                FileObject projectBuildFile = FileUtil.toFileObject(new File(FileUtil.toFile(mainDirectory), "build.xml"));

                if (build != null && build.length() > 0) {
                    String target = ProjectUtils.getInformation(project).getName();
                    logger.log(Level.INFO, "PROJECT NAME = " + target);

                    final File buildFile = File.createTempFile("build-", ".xml");
                    buildFile.deleteOnExit();
                    logger.log(Level.INFO, "BUILD FILE = " + buildFile);

                    BufferedWriter out = new BufferedWriter(new FileWriter(buildFile));
                    out.write(build);
                    out.close();
                    Properties properties = new Properties();
                    properties.put("project-build-file", projectBuildFile.getPath());
                    ExecutorTask runTarget = ActionUtils.runTarget(FileUtil.toFileObject(buildFile), new String[]{target}, properties);
                    runTarget.addTaskListener(new TaskListener() {

                        @Override
                        public void taskFinished(Task task) {
                            buildFile.delete();
                        }
                    });
                } else {
                    ActionUtils.runTarget(projectBuildFile, actions.split(","), null);
                }
            } catch (IOException ex) {
                logger.log(Level.SEVERE, "Error", ex);
            } catch (IllegalArgumentException ex) {
                logger.log(Level.SEVERE, "Error", ex);
            }
        }
    }
}
