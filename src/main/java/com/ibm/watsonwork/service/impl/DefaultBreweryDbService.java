package com.ibm.watsonwork.service.impl;

import java.util.ArrayList;
import java.util.List;

import com.ibm.watsonwork.BreweryDbProperties;
import com.ibm.watsonwork.client.BreweryDBClient;
import com.ibm.watsonwork.model.brewerydb.Brewery;
import com.ibm.watsonwork.model.brewerydb.BreweryDataGeoSearch;
import com.ibm.watsonwork.model.brewerydb.BreweryResponseGeo;
import com.ibm.watsonwork.model.google.LocationData;
import com.ibm.watsonwork.model.graphql.*;
import com.ibm.watsonwork.service.BreweryDBService;
import com.ibm.watsonwork.service.GoogleService;
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
    private GoogleService googleService;

    @Autowired
    private GraphQLService graphQLService;

    @Override
    public void findBreweries(WebhookEvent webhookEvent, AnnotationPayload payloadMessageFocus) {
        String query;
        String city = "";
        String country = "";

        List<Entity> entities = payloadMessageFocus.getExtractedInfo().getEntities();
        for (Entity entity : entities) {
            if (entity.getType().equals("Country")) {
                country = entity.getText();
            }
            else if (entity.getType().equals("City")) {
                city = entity.getText();
            }
        }

        if (!city.equals("") && !country.equals("")) {
            query = city + "," + country;
        } else if (!country.equals("")) {
            query = country;
        } else {
            query = city;
        }

        LOGGER.info("query [{}]", query);

        LocationData locationData = googleService.getLocationData(query);

        Call<BreweryResponseGeo> call = breweryDBClient.geoSearch(breweryDbProperties.getApiKey(), Float.toString(locationData.getLat()), Float.toString(locationData.getLng()),"25");

        call.enqueue(new Callback<BreweryResponseGeo>() {
            @Override
            public void onResponse(Call<BreweryResponseGeo> call, Response<BreweryResponseGeo> response) {
                System.out.println(response.toString());
                BreweryResponseGeo body = response.body();
                LOGGER.info("response [{}]", body);

                AnnotationPayload payload = MessageUtils.mapAnnotationPayload(webhookEvent.getAnnotationPayload());
                List<BreweryDataGeoSearch> breweryData = body.getData();

                if (body.getTotalResults()!=null) {

                    List<Button> breweryButtons = new ArrayList<>();
                    for (BreweryDataGeoSearch brewery : breweryData) {

                        Button breweryButton = new Button();
                        breweryButton.setId(brewery.getBrewery().getId());
                        breweryButton.setStyle("PRIMARY");
                        breweryButton.setTitle(brewery.getBrewery().getName());
                        breweryButtons.add(breweryButton);
                    }

                    Annotation annotation = new Annotation();
                    annotation.setTitle("We found " + body.getTotalResults() +" local breweries.");
                    annotation.setText("");
                    annotation.setButtons(breweryButtons);

                    TargetedMessage targetedMessage = new TargetedMessage();
                    targetedMessage.setConversationId(webhookEvent.getSpaceId());
                    targetedMessage.setTargetDialogId(payload.getTargetDialogId());
                    targetedMessage.setTargetUserId(webhookEvent.getUserId());
                    targetedMessage.setAnnotation(annotation);

                    graphQLService.createTargetedMessage(webhookEvent, targetedMessage);

                } else {

                    Annotation annotation = new Annotation();
                    annotation.setTitle("We have not found any breweries");
                    annotation.setText("");

                    TargetedMessage targetedMessage = new TargetedMessage();
                    targetedMessage.setConversationId(webhookEvent.getSpaceId());
                    targetedMessage.setTargetDialogId(payload.getTargetDialogId());
                    targetedMessage.setTargetUserId(webhookEvent.getUserId());
                    targetedMessage.setAnnotation(annotation);

                    graphQLService.createTargetedMessage(webhookEvent, targetedMessage);
                }
            }

            @Override
            public void onFailure(Call<BreweryResponseGeo> call, Throwable t) {
                LOGGER.error("BreweryDB Request failed.", t);
            }

        });

    }

    @Override
    public void getBrewery(WebhookEvent webhookEvent) {
        AnnotationPayload payload = MessageUtils.mapAnnotationPayload(webhookEvent.getAnnotationPayload());

        Call<Brewery> call = breweryDBClient.getBrewery(payload.getActionId(), breweryDbProperties.getApiKey());

        call.enqueue(new Callback<Brewery>() {
            @Override
            public void onResponse(Call<Brewery> call, Response<Brewery> response) {
                LOGGER.info("BreweryBB Request successful.");
                Brewery brewery = response.body();

                String targetText = "";
                if (brewery.getData().getDescription() != null) {
                    targetText += String.format("*Description*: %s \\n", HtmlUtils.htmlEscape(brewery.getData().getDescription(),"utf8"));
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
        AnnotationPayload payload = MessageUtils.mapAnnotationPayload(webhookEvent.getAnnotationPayload());

        String actionId = payload.getActionId().substring(6);

        Call<Brewery> call = breweryDBClient.getBrewery(actionId, breweryDbProperties.getApiKey());

        call.enqueue(new Callback<Brewery>() {
            @Override
            public void onResponse(Call<Brewery> call, Response<Brewery> response) {
                LOGGER.info("BreweryBB Request successful.");
                Brewery brewery = response.body();

                String targetText = "";
                if (brewery.getData().getName() != null) {
                    targetText += String.format("*Brewery*: %s \\n", HtmlUtils.htmlEscape(brewery.getData().getName(),"utf8"));
                }

                if (brewery.getData().getDescription() != null) {
                    targetText += String.format("*Description*: %s \\n", HtmlUtils.htmlEscape(brewery.getData().getDescription(),"utf8"));
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
