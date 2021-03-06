package org.zstack.storage.fusionstor.primary;

import org.springframework.http.HttpMethod;
import org.zstack.header.query.APIQueryMessage;
import org.zstack.header.query.AutoQuery;
import org.zstack.header.rest.RestRequest;
import org.zstack.header.storage.primary.APIQueryPrimaryStorageReply;

import java.util.List;

import static java.util.Arrays.asList;

/**
 * Created by frank on 8/6/2015.
 */
@AutoQuery(replyClass = APIQueryPrimaryStorageReply.class, inventoryClass = FusionstorPrimaryStorageInventory.class)
@RestRequest(
        path = "/primary-storage/fusionstor",
        optionalPaths = {"/primary-storage/fusionstor/{uuid}"},
        method = HttpMethod.GET,
        responseClass = APIQueryPrimaryStorageReply.class
)
public class APIQueryFusionstorPrimaryStorageMsg extends APIQueryMessage {

    public static List<String> __example__() {
        return asList();
    }

}
