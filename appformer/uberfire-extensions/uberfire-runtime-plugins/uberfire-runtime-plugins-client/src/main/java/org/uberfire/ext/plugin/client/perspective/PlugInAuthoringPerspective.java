/*
 * Copyright 2015 JBoss, by Red Hat, Inc
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

package org.uberfire.ext.plugin.client.perspective;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.uberfire.client.annotations.Perspective;
import org.uberfire.client.annotations.WorkbenchPerspective;
import org.uberfire.client.workbench.docks.UberfireDock;
import org.uberfire.client.workbench.docks.UberfireDockPosition;
import org.uberfire.client.workbench.docks.UberfireDocks;
import org.uberfire.client.workbench.panels.impl.MultiListWorkbenchPanelPresenter;
import org.uberfire.lifecycle.OnOpen;
import org.uberfire.mvp.impl.DefaultPlaceRequest;
import org.uberfire.workbench.model.PerspectiveDefinition;
import org.uberfire.workbench.model.impl.PerspectiveDefinitionImpl;

@ApplicationScoped
@WorkbenchPerspective(identifier = PlugInAuthoringPerspective.IDENTIFIER)
public class PlugInAuthoringPerspective {

    public static final String IDENTIFIER = "PlugInAuthoringPerspective";

    @Inject
    UberfireDocks uberfireDocks;
    private UberfireDock dock;

    @PostConstruct
    public void setupDocks() {
        dock = new UberfireDock(UberfireDockPosition.WEST,
                                "ADJUST",
                                new DefaultPlaceRequest("Plugins Explorer"),
                                "PlugInAuthoringPerspective").withSize(400)
                .withLabel("Plugin Explorer");
        uberfireDocks.add(dock);
    }

    @OnOpen
    public void onOpen() {
        uberfireDocks.open(dock);
    }

    @Perspective
    public PerspectiveDefinition getPerspective() {
        return buildPerspective();
    }

    private PerspectiveDefinition buildPerspective() {
        final PerspectiveDefinitionImpl perspective = new PerspectiveDefinitionImpl(
                MultiListWorkbenchPanelPresenter.class.getName());
        perspective.setName("Plugin Authoring");
        return perspective;
    }
}
