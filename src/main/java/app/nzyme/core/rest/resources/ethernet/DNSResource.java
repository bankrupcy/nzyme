package app.nzyme.core.rest.resources.ethernet;

import app.nzyme.core.NzymeNode;
import app.nzyme.plugin.rest.security.PermissionLevel;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import app.nzyme.core.ethernet.dns.db.DNSPairSummary;
import app.nzyme.core.ethernet.dns.db.DNSStatisticsBucket;
import app.nzyme.core.ethernet.dns.db.DNSTrafficSummary;
import app.nzyme.plugin.rest.security.RESTSecured;
import app.nzyme.core.rest.responses.ethernet.dns.DNSPairSummaryResponse;
import app.nzyme.core.rest.responses.ethernet.dns.DNSStatisticsBucketResponse;
import app.nzyme.core.rest.responses.ethernet.dns.DNSStatisticsResponse;
import app.nzyme.core.rest.responses.ethernet.dns.DNSTrafficSummaryResponse;
import org.joda.time.DateTime;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

@Path("/api/ethernet/dns")
@Produces(MediaType.APPLICATION_JSON)
@RESTSecured(PermissionLevel.ANY)
public class DNSResource {

    @Inject
    private NzymeNode nzyme;

    @GET
    @Path("/statistics")
    public Response statistics(@QueryParam("hours") int hours) {
        if (hours <= 0) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        List<DNSStatisticsBucket> statistics = nzyme.getEthernet().dns().getStatistics(hours);

        Map<DateTime, DNSStatisticsBucketResponse> buckets = Maps.newHashMap();
        for (DNSStatisticsBucket b : statistics) {
            buckets.put(b.bucket(), DNSStatisticsBucketResponse.create(
                    b.bucket(),
                    b.requestCount(),
                    b.requestBytes(),
                    b.responseCount(),
                    b.responseBytes(),
                    b.nxdomainCount()
            ));
        }

        DNSTrafficSummary trafficSummary = nzyme.getEthernet().dns().getTrafficSummary(hours);

        List<DNSPairSummaryResponse> pairSummary = Lists.newArrayList();
        for (DNSPairSummary ps : nzyme.getEthernet().dns().getPairSummary(hours, 10)) {
            pairSummary.add(DNSPairSummaryResponse.create(ps.server(), ps.requestCount(), ps.clientCount()));
        }

        return Response.ok(
                DNSStatisticsResponse.create(
                        buckets,
                        DNSTrafficSummaryResponse.create(
                                trafficSummary.totalPackets(),
                                trafficSummary.totalTrafficBytes(),
                                trafficSummary.totalNxdomains()
                        ),
                        pairSummary
                )
        ).build();
    }

}
