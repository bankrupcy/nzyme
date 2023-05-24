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

package app.nzyme.core.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.server.ParamException;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.security.InvalidParameterException;

@Provider
public class NzymeExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOG = LogManager.getLogger(NzymeExceptionMapper.class);

    @Override
    public Response toResponse(Throwable t) {
        if (t instanceof NotFoundException) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (t instanceof ParamException || t instanceof InvalidParameterException) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        LOG.error("Error while handling REST call.", t);
        return Response.serverError().build();
    }

}