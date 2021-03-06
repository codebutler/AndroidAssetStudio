/*
Copyright 2011 Google Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A custom Ant task that isolates inline JavaScript tests demarcated with //#BEGIN_TEST and
 * //#END_TEST for all files in the contained FileSets.
 */
public class IsolateTests extends org.apache.tools.ant.Task {
    private List<FileSet> fileSets = new ArrayList<FileSet>();

    public void addFileSet(FileSet fileSet) {
        fileSets.add(fileSet);
    }

    public void execute() {
        Pattern p = Pattern.compile("//#BEGIN_TEST(.*?)//#END_TEST\\n?",
                Pattern.MULTILINE | Pattern.DOTALL);

        for (FileSet fileSet : fileSets) {
            DirectoryScanner ds = fileSet.getDirectoryScanner(getProject());
            File dir = ds.getBasedir();
            String[] filesInSet = ds.getIncludedFiles();

            for (String filename : filesInSet) {
                StringBuilder srcData = new StringBuilder(1000);

                try {
                    File srcFile = new File(dir, filename);
                    File testFile = new File(dir, filename + ".tests.js");

                    String src = BuildTaskHelpers.readFileContents(srcFile);

                    // Create a matcher for the source contents.
                    Matcher m = p.matcher(src);

                    // Write the stripped source file
                    BuildTaskHelpers.writeFileContents(srcFile, m.replaceAll(""));

                    // Write the tests to separate .tests.js files.
                    StringBuilder builder = new StringBuilder();
                    m.reset();
                    while (m.find()) {
                        builder.append(m.group(1));
                    }
                    if (builder.length() > 0) {
                        BuildTaskHelpers.writeFileContents(testFile, builder.toString());
                    }
                } catch (IOException e) {
                    getProject().log(e.toString());
                }
            }
        }
    }
}
