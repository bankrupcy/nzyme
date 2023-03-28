/*
 * This file is part of nzyme.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Server Side Public License, version 1,
 * as published by MongoDB, Inc.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Server Side Public License for more details.
 *
 * You should have received a copy of the Server Side Public License
 * along with this program. If not, see
 * <http://www.mongodb.com/licensing/server-side-public-license>.
 */

package app.nzyme.core.rest.resources.taps;

import app.nzyme.core.NzymeNode;
import app.nzyme.core.rest.authentication.TapSecured;
import app.nzyme.core.rest.resources.taps.reports.StatusReport;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/api/taps/status")
@TapSecured
@Produces(MediaType.APPLICATION_JSON)
public class StatusResource {

    private static final Logger LOG = LogManager.getLogger(StatusResource.class);

    @Inject
    private NzymeNode nzyme;

    @POST
    public Response status(StatusReport report) {
        LOG.debug("Received tap status: {}", report);

        if (report.name().length() > 50) {
            LOG.debug("Tap name [{}] exceeds maximum length of 50 characters.", report.name());
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        
        nzyme.getTapManager().registerTapStatus(report);

        return Response.status(Response.Status.CREATED).build();
    }

}
