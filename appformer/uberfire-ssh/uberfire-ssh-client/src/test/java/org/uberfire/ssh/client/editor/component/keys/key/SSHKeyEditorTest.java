/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.uberfire.ssh.client.editor.component.keys.key;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.uberfire.mvp.Command;
import org.uberfire.ssh.service.shared.editor.PortableSSHPublicKey;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class SSHKeyEditorTest {

    @Mock
    private SSHKeyEditorView view;

    @Mock
    private PortableSSHPublicKey key;

    @Mock
    private Command onDelete;

    private SSHKeyEditor editor;

    @Before
    public void init() {
        editor = new SSHKeyEditor(view);
    }

    @Test
    public void testFunctionality() {
        verify(view).init(editor);

        editor.getElement();
        verify(view).getElement();

        editor.render(key, onDelete);

        verify(view).clear();
        verify(view).render(key);

        editor.notifyDelete();
        verify(onDelete).execute();

        editor.clear();
        verify(view, times(2)).clear();
    }

    @Test
    public void testInitFailures() {
        Assertions.assertThatThrownBy(() -> editor.render(null, onDelete))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Parameter named 'key' should be not null!");

        Assertions.assertThatThrownBy(() -> editor.render(key, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Parameter named 'onDelete' should be not null!");
    }
}
