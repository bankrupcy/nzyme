package app.nzyme.core.rest.resources.system;

import app.nzyme.core.NzymeNode;
import app.nzyme.plugin.rest.security.PermissionLevel;
import app.nzyme.plugin.rest.security.RESTSecured;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/system/registry")
@RESTSecured(PermissionLevel.SUPERADMINISTRATOR)
@Produces(MediaType.APPLICATION_JSON)
public class RegistryResource {

    @Inject
    private NzymeNode nzyme;

    @DELETE
    @Path("/show/{key}")
    public Response indicators(@PathParam("key") String key) {
        if (key == null || key.trim().isEmpty()) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        nzyme.getDatabaseCoreRegistry().deleteValue(key);

        return Response.ok().build();
    }

}
