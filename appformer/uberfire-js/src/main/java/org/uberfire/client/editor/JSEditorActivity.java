/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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
package org.uberfire.client.editor;

import javax.enterprise.inject.Alternative;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import org.uberfire.backend.vfs.ObservablePath;
import org.uberfire.client.annotations.WorkbenchEditor;
import org.uberfire.client.mvp.AbstractWorkbenchEditorActivity;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.mvp.Command;
import org.uberfire.mvp.ParameterizedCommand;
import org.uberfire.mvp.PlaceRequest;

@Alternative
public class JSEditorActivity extends AbstractWorkbenchEditorActivity {

    private JSNativeEditor nativeEditor;

    public JSEditorActivity(final JSNativeEditor nativeEditor,
                            final PlaceManager placeManager) {
        super(placeManager);
        this.nativeEditor = nativeEditor;
    }

    @Override
    public void onStartup(final ObservablePath path,
                          final PlaceRequest place) {
        super.onStartup(path,
                place);
        setupObservablePathCallBacks();
        nativeEditor.onStartup(path.toURI());
    }

    @Override
    public void onOpen() {
        super.onOpen();
        nativeEditor.onOpen(path.toURI());
    }

    @Override
    public void onClose() {
        super.onClose();
        nativeEditor.onClose();
    }

    @Override
    public void onShutdown() {
        super.onShutdown();
        nativeEditor.onShutdown();
    }

    @Override
    public String getIdentifier() {
        return nativeEditor.getId();
    }

    private void setupObservablePathCallBacks() {
        path.onRename(() -> nativeEditor.onRename());
        path.onDelete(() -> nativeEditor.onDelete());
        path.onUpdate(() -> nativeEditor.onUpdate());
        path.onCopy(() -> nativeEditor.onCopy());
    }

    @Override
    public String getTitle() {
        return nativeEditor.getTitle();
    }

    @Override
    public IsWidget getWidget() {
        return new HTML(nativeEditor.getElement().getInnerHTML());
    }

    public JSNativeEditor getNativeEditor() {
        return nativeEditor;
    }

    public void setNativeEditor(JSNativeEditor nativeEditor) {
        this.nativeEditor = nativeEditor;
    }

    @Override
    protected WorkbenchEditor.LockingStrategy getLockingStrategy() {
        return WorkbenchEditor.LockingStrategy.EDITOR_PROVIDED;
    }
}
