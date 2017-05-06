package com.ibm.watsonwork.service;

import com.ibm.watsonwork.model.AnnotationPayload;
import com.ibm.watsonwork.model.WebhookEvent;

public interface BreweryDBService extends Service {

    void findBreweries(WebhookEvent webhookEvent, AnnotationPayload payloadMessageFocus);

    void getBrewery(WebhookEvent webhookEvent);

    void shareBrewery(WebhookEvent webhookEvent);

}
