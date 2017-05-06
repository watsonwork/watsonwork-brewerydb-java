package com.ibm.watsonwork.service;

import com.ibm.watsonwork.model.graphql.AnnotationPayload;
import com.ibm.watsonwork.model.graphql.WebhookEvent;

public interface BreweryDBService extends Service {

    void findBreweries(WebhookEvent webhookEvent, AnnotationPayload payloadMessageFocus);

    void getBrewery(WebhookEvent webhookEvent);

    void shareBrewery(WebhookEvent webhookEvent);

}
