/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.uberfire.ext.editor.commons.backend.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.uberfire.backend.vfs.Path;
import org.uberfire.backend.vfs.PathFactory;
import org.uberfire.backend.vfs.impl.LockInfo;
import org.uberfire.ext.editor.commons.backend.service.restriction.LockRestrictor;
import org.uberfire.ext.editor.commons.service.ValidationService;
import org.uberfire.ext.editor.commons.service.restrictor.RenameRestrictor;
import org.uberfire.io.IOService;
import org.uberfire.rpc.SessionInfo;

@Ignore("fix after figure out IO")
@RunWith(MockitoJUnitRunner.class)
public class RenameServiceImplTest {

    @Mock
    private IOService ioService;

    @Mock
    private SessionInfo sessionInfo;

    @Mock
    private ValidationService validationService;

    @Spy
    @InjectMocks
    private RenameServiceImpl renameService;

    @Spy
    @InjectMocks
    private LockRestrictor lockRestrictor;

    @Before
    public void setup() throws Exception {

        doReturn(getPath()).when(renameService).renamePath(any(Path.class),
                                                           any(String.class),
                                                           any(String.class));
        doNothing().when(renameService).renamePathIfExists(any(Path.class),
                                                           any(String.class),
                                                           any(String.class));
        doNothing().when(renameService).startBatch(ArgumentMatchers.<Collection<Path>>any());
        doNothing().when(renameService).endBatch();

        List<RenameRestrictor> renameRestrictors = new ArrayList<RenameRestrictor>();
        renameRestrictors.add(lockRestrictor);
        when(renameService.getRenameRestrictors()).thenReturn(renameRestrictors);
    }

    @Test
    public void renameLockedPathTest() {
        final Path path = getPath();

        try {
            whenPathIsRenamed(path);
        } catch (RuntimeException e) {
            thenPathWasNotRenamed(path,
                                  e);
        }

        thenPathWasNotRenamed(path);
    }

    @Test
    public void renameUnlockedPathTest() {
        final Path path = getPath();

        whenPathIsRenamed(path);
        thenPathWasRenamed(path);
    }

    @Test
    public void renameLockedPathIfExistsTest() {
        final List<Path> paths = new ArrayList<Path>();
        paths.add(getPath("file0.txt"));
        paths.add(getPath("file1.txt"));
        paths.add(getPath("file2.txt"));

        try {
            whenPathsAreRenamedIfExists(paths);
        } catch (RuntimeException e) {
            thenPathWasNotRenamedIfExists(paths.get(1),
                                          e);
        }

        thenPathWasRenamedIfExists(paths.get(0));
        thenPathWasNotRenamedIfExists(paths.get(1));

        // This will not be renamed because the process stops when some exception is raised.
        thenPathWasNotRenamedIfExists(paths.get(2));
    }

    @Test
    public void renameUnlockedPathIfExistsTest() {
        final List<Path> paths = new ArrayList<Path>();
        paths.add(getPath("file0.txt"));
        paths.add(getPath("file1.txt"));
        paths.add(getPath("file2.txt"));

        whenPathsAreRenamedIfExists(paths);

        thenPathWasRenamedIfExists(paths.get(0));
        thenPathWasRenamedIfExists(paths.get(1));
        thenPathWasRenamedIfExists(paths.get(2));
    }

    @Test
    public void pathHasNoRenameRestrictionTest() {
        final Path path = getPath();

        boolean hasRestriction = whenPathIsCheckedForRenameRestrictions(path);
        thenPathHasNoRenameRestrictions(hasRestriction);
    }

    @Test
    public void pathHasRenameRestrictionTest() {
        final Path path = getPath();

        boolean hasRestriction = whenPathIsCheckedForRenameRestrictions(path);
        thenPathHasRenameRestrictions(hasRestriction);
    }

    private void whenPathIsRenamed(final Path path) {
        renameService.rename(path,
                             "newname",
                             "comment");
    }

    private void whenPathsAreRenamedIfExists(final Collection<Path> paths) {
        renameService.renameIfExists(paths,
                                     "newname",
                                     "comment");
    }

    private boolean whenPathIsCheckedForRenameRestrictions(final Path path) {
        return renameService.hasRestriction(path);
    }

    private void thenPathWasRenamed(final Path path) {
        verify(renameService).renamePath(eq(path),
                                         any(String.class),
                                         any(String.class));
    }

    private void thenPathWasNotRenamed(final Path path) {
        verify(renameService,
               never()).renamePath(eq(path),
                                   any(String.class),
                                   any(String.class));
    }

    private void thenPathWasNotRenamed(final Path path,
                                       final RuntimeException e) {
        assertEquals(path.toURI() + " cannot be deleted, moved or renamed. It is locked by: lockedBy",
                     e.getMessage());
    }

    private void thenPathWasRenamedIfExists(final Path path) {
        verify(renameService).renamePathIfExists(eq(path),
                                                 any(String.class),
                                                 any(String.class));
    }

    private void thenPathWasNotRenamedIfExists(final Path path) {
        verify(renameService,
               never()).renamePathIfExists(eq(path),
                                           any(String.class),
                                           any(String.class));
    }

    private void thenPathWasNotRenamedIfExists(final Path path,
                                               final RuntimeException e) {
        assertEquals(path.toURI() + " cannot be deleted, moved or renamed. It is locked by: lockedBy",
                     e.getMessage());
    }

    private void thenPathHasNoRenameRestrictions(final boolean hasRestriction) {
        assertFalse(hasRestriction);
    }

    private void thenPathHasRenameRestrictions(final boolean hasRestriction) {
        assertTrue(hasRestriction);
    }

    private Path getPath() {
        return getPath("file.txt");
    }

    private Path getPath(String fileName) {
        return PathFactory.newPath(fileName,
                                   "file://tmp/" + fileName);
    }

}
