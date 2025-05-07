package org.example.cardcollectorproject.services;

import com.azure.cosmos.*;
import com.azure.cosmos.models.*;
import com.azure.cosmos.util.CosmosPagedIterable;
import org.example.cardcollectorproject.config.DatabaseConfig;
import org.example.cardcollectorproject.models.PokemonCard;
import org.example.cardcollectorproject.models.User;
import org.example.cardcollectorproject.models.UserCollection;
import org.example.cardcollectorproject.models.CardPrice;


import java.util.ArrayList;
import java.util.List;

public class CosmosDbService {
    private final CosmosClient cosmosClient;
    private final CosmosContainer usersContainer;
    private final CosmosContainer collectionsContainer;
    private final CosmosContainer cardPriceContainer;

    public CosmosDbService() {
        DatabaseConfig config = DatabaseConfig.getInstance();

        this.cosmosClient = new CosmosClientBuilder()
                .endpoint(config.getCosmosEndpoint())
                .key(config.getCosmosKey())
                .buildClient();

        CosmosDatabase database = cosmosClient.getDatabase(config.getCosmosDatabaseName());
        this.usersContainer = database.getContainer(config.getCosmosContainerName());
        this.cardPriceContainer = database.getContainer(config.getCardPriceContainerName());
        // Get or create collections container
        String collectionsContainerName = "collections";
        this.collectionsContainer = getOrCreateCollectionsContainer(database, collectionsContainerName);
    }  

    /**
     * Gets an existing collections container or creates a new one if it doesn't exist
     */
    private CosmosContainer getOrCreateCollectionsContainer(CosmosDatabase database, String containerName) {
        try {
            // Try to get the container
            return database.getContainer(containerName);
        } catch (CosmosException e) {
            if (e.getStatusCode() == 404) {
                // Container doesn't exist, create it
                CosmosContainerProperties containerProperties =
                        new CosmosContainerProperties(containerName, "/userId");
                database.createContainer(containerProperties);
                return database.getContainer(containerName);
            } else {
                // Rethrow any other exception
                throw e;
            }
        }
    }

    /**
     * Creates a user in the Cosmos DB container.
     *
     * @param user The user object to be created.
     */
    public void createUser(User user) {
        try {
            // Ensure the `id` field is set (required by Cosmos DB)
            if (user.getId() == null || user.getId().isEmpty()) {
                String uniqueId = java.util.UUID.randomUUID().toString();
                user.setId(uniqueId);
                user.setUserId(uniqueId);
            }

            // Insert the user into the container
            usersContainer.createItem(user, new PartitionKey(user.getUserId()), new CosmosItemRequestOptions());
            System.out.println("User created: " + user.getUsername());

            // Create empty collection and watchlist for the user
            createUserCollection(user.getUserId(), "collection");
            createUserCollection(user.getUserId(), "watchlist");

        } catch (CosmosException e) {
            System.err.println("Error creating user: " + e.getMessage());
        }
    }

    public User getUserByUsername(String username) {
        String query = "SELECT * FROM c WHERE c.username = @username";
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();

        List<SqlParameter> parameters = new ArrayList<>();
        parameters.add(new SqlParameter("@username", username));

        SqlQuerySpec querySpec = new SqlQuerySpec(query, parameters);

        CosmosPagedIterable<User> queryResult = usersContainer.queryItems(querySpec, options, User.class);

        for (User user : queryResult) {
            return user;
        }

        return null;
    }

    /**
     * Creates an empty collection for a user
     */
    private void createUserCollection(String userId, String collectionType) {
        try {
            UserCollection userCollection = new UserCollection(userId, collectionType);
            collectionsContainer.createItem(userCollection,
                    new PartitionKey(userCollection.getUserId()), new CosmosItemRequestOptions());
        } catch (CosmosException e) {
            // Ignore conflict errors (collection already exists)
            if (e.getStatusCode() != 409) {
                System.err.println("Error creating user collection: " + e.getMessage());
            }
        }
    }

    /**
     * Gets a user's collection by type (collection or watchlist)
     */
    public UserCollection getUserCollection(String userId, String collectionType) {
        try {
            String id = userId + "_" + collectionType;
            return collectionsContainer.readItem(id, new PartitionKey(userId), UserCollection.class)
                    .getItem();
        } catch (CosmosException e) {
            if (e.getStatusCode() == 404) {
                // Collection doesn't exist, create it
                UserCollection newCollection = new UserCollection(userId, collectionType);
                collectionsContainer.createItem(newCollection,
                        new PartitionKey(userId), new CosmosItemRequestOptions());
                return newCollection;
            } else {
                System.err.println("Error retrieving user collection: " + e.getMessage());
                return new UserCollection(userId, collectionType);
            }
        }
    }

    /**
     * Adds a card to a user's collection
     */
    public void addCardToUserCollection(String userId, String collectionType, PokemonCard card) {
        try {
            UserCollection userCollection = getUserCollection(userId, collectionType);
            userCollection.addCard(card);

            collectionsContainer.replaceItem(userCollection, userCollection.getId(),
                    new PartitionKey(userId), new CosmosItemRequestOptions());

        } catch (CosmosException e) {
            System.err.println("Error adding card to collection: " + e.getMessage());
        }
    }

    /**
     * Removes a card from a user's collection
     */
    public void removeCardFromUserCollection(String userId, String collectionType, String cardNumber) {
        try {
            UserCollection userCollection = getUserCollection(userId, collectionType);
            userCollection.removeCard(cardNumber);

            collectionsContainer.replaceItem(userCollection, userCollection.getId(),
                    new PartitionKey(userId), new CosmosItemRequestOptions());

        } catch (CosmosException e) {
            System.err.println("Error removing card from collection: " + e.getMessage());
        }
    }

    public void saveCardPrice(CardPrice cardPrice) {
        try {
            if (cardPrice.getId() == null) {
                cardPrice.setId(java.util.UUID.randomUUID().toString());
            }
            cardPriceContainer.createItem(cardPrice, new PartitionKey(cardPrice.getCardprice()), new CosmosItemRequestOptions());
        } catch (CosmosException e) {
            System.err.println("Error saving card price: " + e.getMessage());
        }
    }

    public List<CardPrice> getCardPriceHistory(String cardNumber) {
        String query = "SELECT * FROM c WHERE c.cardprice = @cardprice ORDER BY c.timestamp";
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        List<SqlParameter> parameters = new ArrayList<>();
        parameters.add(new SqlParameter("@cardprice", cardNumber));
        SqlQuerySpec querySpec = new SqlQuerySpec(query, parameters);
        List<CardPrice> result = new ArrayList<>();
        CosmosPagedIterable<CardPrice> queryResult = cardPriceContainer.queryItems(querySpec, options, CardPrice.class);
        for (CardPrice cp : queryResult) {
            result.add(cp);
        }
        return result;
    }
}