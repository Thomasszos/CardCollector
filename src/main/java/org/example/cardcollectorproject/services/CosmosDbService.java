package org.example.cardcollectorproject.services;

import com.azure.cosmos.*;
import com.azure.cosmos.models.*;
import com.azure.cosmos.util.CosmosPagedIterable;
import org.example.cardcollectorproject.config.DatabaseConfig;
import org.example.cardcollectorproject.models.User;
import org.example.cardcollectorproject.models.CardPrice;

import java.util.ArrayList;
import java.util.List;

public class CosmosDbService {
    private final CosmosClient cosmosClient;
    private final CosmosContainer usersContainer;
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
                // Also set the userId to the same value as id to maintain consistency
                user.setUserId(uniqueId);
            }

            // Insert the user into the container
            usersContainer.createItem(user, new PartitionKey(user.getUserId()), new CosmosItemRequestOptions());
            System.out.println("User created: " + user.getUsername());
        } catch (CosmosException e) {
            System.err.println("Error creating user: " + e.getMessage());
        }
    }

//    /**
//     * Retrieves a user by their userId. The userId is the partition key in this implementation.
//     *
//     * @param userId The ID of the user (partition key).
//     * @return The User object if found, or null if not found.
//     */
//    public User getUser(String userId) {
//        try {
//            // Use the partition key `/userId` to retrieve the document
//            return usersContainer.readItem(userId, new PartitionKey(userId), User.class).getItem();
//        } catch (CosmosException e) {
//            if (e.getStatusCode() == 404) {
//                System.err.println("User not found with userId: " + userId);
//            } else {
//                System.err.println("Error fetching user: " + e.getMessage());
//            }
//        }
//        return null; // Return null if the user is not found
//    }
    public User getUserByUsername(String username) {
        String query = "SELECT * FROM c WHERE c.username = @username";
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();

        // Create a parameter list
        List<SqlParameter> parameters = new ArrayList<>();
        parameters.add(new SqlParameter("@username", username));

        // Construct the SQL query spec
        SqlQuerySpec querySpec = new SqlQuerySpec(query, parameters);

        // Perform SQL query
        CosmosPagedIterable<User> queryResult = usersContainer.queryItems(querySpec, options, User.class);

        // Iterate through the results and return the first match
        for (User user : queryResult) {
            return user; // Return the first matching user
        }

        return null; // Return null if no user with the given username is found
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