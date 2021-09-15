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

package org.uberfire.server;

import static org.uberfire.server.UploadUriProvider.getTargetLocation;
import static org.uberfire.server.UploadUriProvider.isUpdate;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.dashbuilder.project.storage.ProjectStorageServices;
import java.nio.file.Paths;

public class FileUploadServlet extends BaseUploadServlet {

    private static final String RESPONSE_OK = "OK";
    private static final String RESPONSE_FAIL = "FAIL";
    private static final String RESPONSE_CONFLICT = "CONFLICT";

    @Inject
    ProjectStorageServices projectStorageServices;

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException, IOException {

        try {

            var targetLocation = getTargetLocation(request);
            final var isUpdate = isUpdate(request);
            finalizeResponse(response,
                    getFileItem(request),
                    targetLocation,
                    isUpdate);
        } catch (FileUploadException e) {
            logError(e);
            writeResponse(response,
                    RESPONSE_FAIL);
        } catch (URISyntaxException e) {
            logError(e);
            writeResponse(response,
                    RESPONSE_FAIL);
        }
    }

    private void finalizeResponse(HttpServletResponse response,
                                  FileItem fileItem,
                                  URI uri,
                                  boolean isUpdate) throws IOException {

        var path = Paths.get(uri);
        var tempPath = projectStorageServices.getTempPath(path.getFileName().toString());
        if (tempPath.toFile().exists() && !isUpdate) {
            writeResponse(response,
                    RESPONSE_CONFLICT);
            response.sendError(HttpServletResponse.SC_CONFLICT);
            return;
        }
        writeFile(tempPath, fileItem);
        writeResponse(response, RESPONSE_OK);
    }
    
    void setProjectStorageServices(ProjectStorageServices projectStorageServices) {
        this.projectStorageServices = projectStorageServices;
    }
}
