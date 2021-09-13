/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.dashbuilder.transfer.rest;

import java.nio.file.Files;
import java.nio.file.Paths;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.dashbuilder.transfer.DataTransferExportModel;
import org.dashbuilder.transfer.DataTransferServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
@Path("dashbuilder")
public class DataTransferResource {

    Logger logger = LoggerFactory.getLogger(DataTransferResource.class);

    @Inject
    private DataTransferServices dataTransferServices;

    @GET
    @Path("export")
    @Produces("application/zip")
    public Response export() {
        try {
            var exportFile = dataTransferServices.doExport(DataTransferExportModel.exportAll());
            var path = Paths.get(exportFile);
            return Response.ok(Files.readAllBytes(path)).build();
        } catch (Exception e) {
            var errorMessage = "Error creating export: " + e.getMessage();
            logger.error(errorMessage);
            logger.debug("Not able to create export.", e);
            return Response.serverError()
                           .entity(errorMessage)
                           .build();
        }
    }

}
