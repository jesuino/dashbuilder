/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.uberfire.backend.server.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.uberfire.java.nio.file.FileSystem;
import org.uberfire.java.nio.file.Path;
import org.uberfire.mocks.FileSystemTestingUtils;

public class JGitFileSystemLazyCacheTest {

    private static FileSystemTestingUtils fsUtils = new FileSystemTestingUtils();

    static {
        System.out.println("Working Dir: " + new File("").getAbsoluteFile().getAbsolutePath());
    }

    @Before
    public void setup() throws IOException {
        fsUtils.setup(false);
    }

    @After
    public void cleanupFileSystem() {
        fsUtils.cleanup();
    }

    @Test
    public void regenerateFSCache() throws IOException {
        String defaultRepo = "git://amend-repo-test";

        Path firstWriteFS1 = fsUtils.getIoService().get(URI.create(defaultRepo + "/init1.file"));

        FileSystem fileSystem1Instance1 = firstWriteFS1.getFileSystem();

        String dora1 = "dora1";
        String dora2 = "dora2";
        fsUtils.getIoService().write(firstWriteFS1,
                                     dora1);

        fsUtils.setupJGitRepository(defaultRepo + "2",
                                    false);
        Path writeFS2 = fsUtils.getIoService().get(URI.create(defaultRepo + "2" + "/init1.file"));
        fsUtils.getIoService().write(writeFS2,
                                     dora1);

        fsUtils.setupJGitRepository(defaultRepo + "3",
                                    false);
        Path writeFS3 = fsUtils.getIoService().get(URI.create(defaultRepo + "3" + "/init1.file"));
        fsUtils.getIoService().write(writeFS3,
                                     dora1);

        //memoized cache size == 2 , so

        fsUtils.setupJGitRepository(defaultRepo,
                                    false);
        Path secondWriteFS1 = fsUtils.getIoService().get(URI.create(defaultRepo + "/init2.file"));
        fsUtils.getIoService().write(secondWriteFS1,
                                     dora2);

        FileSystem fileSystem1Instance2 = secondWriteFS1.getFileSystem();

        //not equals because we have to regenerate, but still represent the same FS
        assertTrue(System.identityHashCode(fileSystem1Instance1) != System.identityHashCode(fileSystem1Instance2));
        assertTrue(fileSystem1Instance1.hashCode() == fileSystem1Instance2.hashCode());
        assertEquals(fileSystem1Instance1,
                     fileSystem1Instance2);

        //let's remove fs1 again from cache
        fsUtils.setupJGitRepository(defaultRepo + "5",
                                    false);
        fsUtils.setupJGitRepository(defaultRepo + "6",
                                    false);

        //read to see if all the writes are fine on fs1

        String actual1 = fsUtils.getIoService().readAllString(fsUtils.getIoService().get(URI.create(defaultRepo + "/init1.file")));
        String actual2 = fsUtils.getIoService().readAllString(fsUtils.getIoService().get(URI.create(defaultRepo + "/init2.file")));

        assertEquals(dora1,
                     actual1);
        assertEquals(dora2,
                     actual2);
    }

}
