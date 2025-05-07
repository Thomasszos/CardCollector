
# Card Collector

A comprehensive JavaFX application for Pokémon card collectors to manage, track, and analyze their card collections.

![Card Collector Logo](https://placeholder-for-logo.png)

## Description

Card Collector is a feature-rich desktop application designed for Pokémon card enthusiasts. It allows users to search for cards, manage their collections, track market prices, and maintain a watchlist for cards they're interested in acquiring. With an intuitive user interface and real-time price data integration, Card Collector helps collectors organize and maximize the value of their collections.

## Features

### Collection Management
- Add and remove cards from your personal collection
- View detailed card information including type, set, and market price
- Sort and filter your collection by various criteria
- Calculate total collection value based on current market prices
- View collection statistics and distribution by card type

### Watchlist Functionality
- Track cards you're interested in without adding them to your collection
- Monitor price changes over time
- Easily transfer cards from watchlist to collection
- Filter and sort your watchlist for easy management

### Card Search
- Search for cards by name, type, set, or ID
- View high-quality card images
- See detailed card information including moves and descriptions
- Auto-complete suggestions for faster searching

### Price Tracking
- Real-time price data from TCG API
- Historical price charts for each card
- Total value calculation for collections and watchlists
- Price trend analysis

### User Experience
- Clean, intuitive interface with modern design
- Responsive layout that adapts to different screen sizes
- Audio feedback for interactions
- Smooth animations and transitions

## Technologies Used

- **Java** - Core programming language
- **JavaFX** - UI framework
- **Azure Cosmos DB** - Cloud database for storing user data
- **TCG API** - For fetching card data and prices
- **PokéAPI** - For additional Pokémon information
- **Jackson/Gson** - JSON parsing libraries

## Installation

### Prerequisites
- Java JDK 17 or higher
- Maven for dependency management
- Azure Cosmos DB account (for database functionality)

### Setup
1. Clone the repository:
   ```
   git clone https://github.com/yourusername/CardCollector.git
   ```

2. Navigate to the project directory:
   ```
   cd CardCollector
   ```

3. Configure your Azure Cosmos DB credentials:
   - Create a file named `config.properties` in the `src/main/resources` directory
   - Add your Cosmos DB connection details:
     ```
     cosmos.endpoint=your_cosmos_db_endpoint
     cosmos.key=your_cosmos_db_key
     cosmos.database=your_database_name
     cosmos.container.users=users
     cosmos.container.cardprices=cardprices
     ```

4. Build the project:
   ```
   mvn clean package
   ```

5. Run the application:
   ```
   java -jar target/CardCollector.jar
   ```

## Usage

### Getting Started
1. Launch the application
2. Create a new account or log in with existing credentials
3. Navigate through the tabs to access different features

### Searching for Cards
1. Go to the Search tab
2. Enter search criteria (name, type, set, or ID)
3. Browse through the results
4. Click on a card to view detailed information

### Managing Your Collection
1. From the card detail view, click "Add to Collection"
2. View your collection in the Collection tab
3. Use filters and sorting options to organize your cards
4. Click on the Stats button to view collection analytics

### Using the Watchlist
1. Add cards to your watchlist from the card detail view
2. Monitor prices in the Watchlist tab
3. Transfer cards to your collection when ready to acquire them

## Screenshots

![Login Screen](https://placeholder-for-screenshot1.png)
*Login screen with animated background*

![Card Search](https://placeholder-for-screenshot2.png)
*Card search interface with results*

![Collection View](https://placeholder-for-screenshot3.png)
*Collection management with filtering options*

![Card Details](https://placeholder-for-screenshot4.png)
*Detailed card view with price history*

## Future Enhancements

- Mobile companion app for on-the-go collection management
- Trading functionality between users
- Collection sharing and social features
- Advanced analytics and investment recommendations
- Barcode/image scanning for physical cards
- Export/import functionality for collection backup

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Pokémon TCG API for card data
- PokéAPI for Pokémon information
- JavaFX community for UI components and inspiration
- All contributors who have helped improve this project
