##Build Actions NetBeans Plugin##

This is a simple plugin which provides a button to execute ANT-based actions on Projects and Project Files.

Its a little like NetBeans "bookmarking" feature, but in a more generalized way where a single button can do different things based on the Project or Project File that is highlighted.

You can use it to do things like

* Run specific ANT targets contained in the project `build.xml` file.
* Copy selected files in selected projects to other locations.
* Execute custom actions for specific files or projects.

###Configuration is Simple###

Once you have installed the plugin into NetBeans, a new "Build Actions" tab will appear in the Miscellaneous section of the Options page.

In the text area within this tab, you configure a single `build.xml` file to perform the actions you want for a given situation. Typically you will have different target for each action you want to perform.

This `build.xml` will be passed the following properties

* `project-build-file` - This is the path of the `build.xml` file for the project that is currently selected.
* `project-folder` - This is the base folder for the selected project.
* `primary-path` - This is the full path to the primary file that is selected, if there is one selected.
* `primary-file` - This is the file name only of the selected file, if there is one selected.

Finally, executed, the target that will be called will be the name of the project, as defined in the `name` attribute of the `project` tag in `build.xml` for the project.

With this, you can do a variety of things, in combination with all the regular ANT facilities (like the `condition` tags.)

####Example####

One problem I have noticed with Netbeans, is that there is no easy to compile ALL of the unit test code. This can be annoying when you have supporting classes that need recompiled but you only want to run a single unit test.

With this plugin, you can do this pretty simply with the following code.


    <?xml version="1.0" encoding="UTF-8"?>
    <project default="default" name="BuildActions">

      <target name="My_Netbeans_Project">
        <ant antfile="${project-build-file}" inheritall="false" target="compile-test"/>
      </target>

    </project>
