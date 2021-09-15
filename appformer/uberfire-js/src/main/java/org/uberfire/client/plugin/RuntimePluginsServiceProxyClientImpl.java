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

package org.uberfire.client.plugin;

import java.util.Collection;
import java.util.Collections;

import javax.enterprise.context.Dependent;

import org.uberfire.mvp.ParameterizedCommand;

/**
 * Searches for runtime plugins and frameworks by parsing the directory listing at <tt>${appBase}/plugins/</tt> or
 * <tt>${appBase}/plugins/</tt> respectively. Any {@code <a>} tag with an href whose URI ends in <tt>.js</tt> will
 * qualify as a runtime plugin or framework, and its contents will be fetched with a separate request. Normally, this
 * will be compatible with any web server's built-in directory listing feature. If it isn't, just manually add an
 * <tt>index.html</tt> file that has links to the .js files you care about.
 */
@Dependent
public class RuntimePluginsServiceProxyClientImpl implements RuntimePluginsServiceProxy {

    @Override
    public void getTemplateContent(final String contentUrl,
                                   final ParameterizedCommand<String> command) {
        command.execute("");
    }

    @Override
    public void listFrameworksContent(ParameterizedCommand<Collection<String>> command) {
        command.execute(Collections.emptyList());
    }

    @Override
    public void listPluginsContent(final ParameterizedCommand<Collection<String>> command) {
        command.execute(Collections.emptyList());
    }

}
