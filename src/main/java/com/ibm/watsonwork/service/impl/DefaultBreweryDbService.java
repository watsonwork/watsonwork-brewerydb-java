package com.ibm.watsonwork.service.impl;

import java.util.ArrayList;
import java.util.List;

import com.ibm.watsonwork.BreweryDbProperties;
import com.ibm.watsonwork.client.BreweryDBClient;
import com.ibm.watsonwork.model.Annotation;
import com.ibm.watsonwork.model.AnnotationPayload;
import com.ibm.watsonwork.model.Brewery;
import com.ibm.watsonwork.model.BreweryData;
import com.ibm.watsonwork.model.BreweryResponse;
import com.ibm.watsonwork.model.Button;
import com.ibm.watsonwork.model.Entity;
import com.ibm.watsonwork.model.TargetedMessage;
import com.ibm.watsonwork.model.WebhookEvent;
import com.ibm.watsonwork.service.BreweryDBService;
import com.ibm.watsonwork.service.GraphQLService;
import com.ibm.watsonwork.utils.MessageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Service
public class DefaultBreweryDbService implements BreweryDBService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultBreweryDbService.class);

    @Autowired
    private BreweryDbProperties breweryDbProperties;

    @Autowired
    private BreweryDBClient breweryDBClient;

    @Autowired
    private GraphQLService graphQLService;

    @Override
    public void findBreweries(WebhookEvent webhookEvent, AnnotationPayload payloadMessageFocus) {
        String query = "";

        List<Entity> entities = payloadMessageFocus.getExtractedInfo().getEntities();
        for (Entity entity : entities) {
            if (entity.getType().equals("Country")) {
                query = entity.getText();
            }
            else if (entity.getType().equals("City")) {
                query = entity.getText();
            }
        }

        LOGGER.info("query [{}]", query);

        Call<BreweryResponse> call = breweryDBClient.search(breweryDbProperties.getApiKey(), query);

        call.enqueue(new Callback<BreweryResponse>() {
            @Override
            public void onResponse(Call<BreweryResponse> call, Response<BreweryResponse> response) {
                BreweryResponse body = response.body();
                LOGGER.info("response [{}]", body);

                AnnotationPayload payload = MessageUtils.mapAnnotationPayload(webhookEvent);
                List<BreweryData> breweryData = body.getData();

                List<Button> breweryButtons = new ArrayList<>();
                for (BreweryData brewery : breweryData) {
                    if (brewery.getType().equals("brewery")) {
                        Button breweryButton = new Button();
                        breweryButton.setId(brewery.getId());
                        breweryButton.setStyle("PRIMARY");
                        breweryButton.setTitle(brewery.getName());
                        breweryButtons.add(breweryButton);
                    }
                }

                Annotation annotation = new Annotation();
                annotation.setTitle("We found these breweries ...");
                annotation.setText("");
                annotation.setButtons(breweryButtons);

                TargetedMessage targetedMessage = new TargetedMessage();
                targetedMessage.setConversationId(webhookEvent.getSpaceId());
                targetedMessage.setTargetDialogId(payload.getTargetDialogId());
                targetedMessage.setTargetUserId(webhookEvent.getUserId());
                targetedMessage.setAnnotation(annotation);

                graphQLService.createTargetedMessage(webhookEvent, targetedMessage);
            }

            @Override
            public void onFailure(Call<BreweryResponse> call, Throwable t) {
                LOGGER.error("BreweryDB Request failed.", t);
            }

        });

    }

    @Override
    public void getBrewery(WebhookEvent webhookEvent) {
        AnnotationPayload payload = MessageUtils.mapAnnotationPayload(webhookEvent);

        Call<Brewery> call = breweryDBClient.getBrewery(payload.getActionId(), breweryDbProperties.getApiKey());

        call.enqueue(new Callback<Brewery>() {
            @Override
            public void onResponse(Call<Brewery> call, Response<Brewery> response) {
                LOGGER.info("BreweryBB Request successful.");
                Brewery brewery = response.body();

                String targetText = "";
                if (brewery.getData().getDescription() != null) {
                    targetText += String.format("*Description*: %s \\n", brewery.getData().getDescription().replace('"', '\"'));
                }

                if (brewery.getData().getWebsite() != null) {
                    targetText += String.format("*Website*: %s \\n", brewery.getData().getWebsite());
                }

                if (brewery.getData().getEstablished() != null) {
                    targetText += String.format("*Established*: %s \\n", brewery.getData().getEstablished());
                }

                List<Button> breweryButtons = new ArrayList<>();
                Button shareButton = new Button();
                shareButton.setId("Share-" + brewery.getData().getId());
                shareButton.setStyle("PRIMARY");
                shareButton.setTitle("Share");
                breweryButtons.add(shareButton);

                Button cancelButton = new Button();
                cancelButton.setId("Cancel");
                cancelButton.setStyle("SECONDARY");
                cancelButton.setTitle("Cancel");
                breweryButtons.add(cancelButton);

                Annotation annotation = new Annotation();
                annotation.setTitle(brewery.getData().getName());
                annotation.setText(targetText);
                annotation.setButtons(breweryButtons);

                TargetedMessage targetedMessage = new TargetedMessage();
                targetedMessage.setConversationId(webhookEvent.getSpaceId());
                targetedMessage.setTargetDialogId(payload.getTargetDialogId());
                targetedMessage.setTargetUserId(webhookEvent.getUserId());
                targetedMessage.setAnnotation(annotation);

                graphQLService.createTargetedMessage(webhookEvent, targetedMessage);

            }

            @Override
            public void onFailure(Call<Brewery> call, Throwable t) {
                LOGGER.error("BreweryDB Request failed.", t);
            }

        });
    }

    @Override
    public void shareBrewery(WebhookEvent webhookEvent) {
        AnnotationPayload payload = MessageUtils.mapAnnotationPayload(webhookEvent);

        String actionId = payload.getActionId().substring(6);

        Call<Brewery> call = breweryDBClient.getBrewery(actionId, breweryDbProperties.getApiKey());

        call.enqueue(new Callback<Brewery>() {
            @Override
            public void onResponse(Call<Brewery> call, Response<Brewery> response) {
                LOGGER.info("BreweryBB Request successful.");
                Brewery brewery = response.body();

                String targetText = "";
                if (brewery.getData().getName() != null) {
                    targetText += String.format("*Brewery*: %s \\n", brewery.getData().getName().replace('"', '\"'));
                }

                if (brewery.getData().getDescription() != null) {
                    targetText += String.format("*Description*: %s \\n", brewery.getData().getDescription().replace('"', '\"'));
                }

                if (brewery.getData().getWebsite() != null) {
                    targetText += String.format("*Website*: %s \\n", brewery.getData().getWebsite());
                }

                if (brewery.getData().getEstablished() != null) {
                    targetText += String.format("*Established*: %s \\n", brewery.getData().getEstablished());
                }

                List<Button> breweryButtons = new ArrayList<>();
                Button shareButton = new Button();
                shareButton.setId("Share-" + brewery.getData().getId());
                shareButton.setStyle("PRIMARY");
                shareButton.setTitle("Share");
                breweryButtons.add(shareButton);

                Button cancelButton = new Button();
                cancelButton.setId("Cancel");
                cancelButton.setStyle("SECONDARY");
                cancelButton.setTitle("Cancel");
                breweryButtons.add(cancelButton);

                Annotation annotation = new Annotation();
                annotation.setTitle("");
                annotation.setText(targetText);
                annotation.setButtons(breweryButtons);

                TargetedMessage targetedMessage = new TargetedMessage();
                targetedMessage.setConversationId(webhookEvent.getSpaceId());
                targetedMessage.setAnnotation(annotation);

                graphQLService.createMessage(webhookEvent, targetedMessage);

                Annotation annotation1 = new Annotation();
                annotation1.setTitle("");
                annotation1.setText("Brewery '" + brewery.getData().getName() + "' shared in the space.");
                annotation1.setButtons(null);

                TargetedMessage targetedMessage1 = new TargetedMessage();
                targetedMessage1.setConversationId(webhookEvent.getSpaceId());
                targetedMessage1.setTargetDialogId(payload.getTargetDialogId());
                targetedMessage1.setTargetUserId(webhookEvent.getUserId());
                targetedMessage1.setAnnotation(annotation1);

                graphQLService.createTargetedMessage(webhookEvent, targetedMessage1);
            }

            @Override
            public void onFailure(Call<Brewery> call, Throwable t) {
                LOGGER.error("BreweryDB Request failed.", t);
            }
        });
    }


}
