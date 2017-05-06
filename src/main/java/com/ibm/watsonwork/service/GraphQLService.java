package com.ibm.watsonwork.service;

import com.ibm.watsonwork.model.TargetedMessage;
import com.ibm.watsonwork.model.WebhookEvent;

public interface GraphQLService extends Service {

    void createMessage(WebhookEvent webhookEvent,TargetedMessage targetedMessage);

    void createTargetedMessage(WebhookEvent webhookEvent,TargetedMessage targetedMessage);


}
