## Card game 'Love Letter'
This project aims to create a simulator for the card game Love Letter. "Love Letter" is a popular card game designed for 2 to 4 players. It's a quick and easy-to-learn game that typically takes around 15 minutes to play. The game is set in the context of courtly intrigue and romance, where players aim to deliver their love letter to the Princess while navigating the complexities of the royal court. The objective of the game is to be the last player standing or to have the highest-ranked card in hand when the draw pile runs out.

### Components:
The game consists of a deck of 16 cards, each featuring a different character from the royal court. Some of the characters include the Princess, the King, the Baron, the Priest, and the Guard.

### Gameplay:
At the beginning of the game, each player is dealt one card from the deck, and the remaining cards are placed face-down in a draw pile. On their turn, players draw one card from the draw pile and then discard one of the two cards in their hand, applying the effect of the discarded card if applicable. The goal is to use the special abilities of the cards strategically to eliminate other players or to protect oneself from elimination. For example, the Guard allows players to guess another player's hand, potentially eliminating them from the round if guessed correctly. The Baron forces players to compare hands, with the lower-ranked card being eliminated. The Princess is the highest-ranked card and is eliminated if discarded.

The round ends when there is only one player left standing or when the draw pile runs out. Players earn a token of affection (represented by a red cube or token) for winning a round, and the first player to earn a certain number of tokens wins the game.

## Installation

### Prerequisites

Before installing the application, ensure that you have the following prerequisites installed on your system:

    Java Development Kit (JDK) 22 or later

### Installation Steps

To install and run the application, follow these steps:

1. Download the JAR File 
   - Go to the Releases section of this GitHub repository. 
   - Download the latest release package.

2. Run the Server Instance 
   - Open a terminal or command prompt.  
   - Navigate to the directory where you downloaded the JAR file. 
   - Run the following command to start the server instance:

        java -jar vorprojekt-ivayla-ivanova-1.0-SNAPSHOT.jar

   - Wait for the server to start. You should see a message indicating that the server is running.

3. Run the Client Instances 
   - Open another terminal or command prompt. 
   - Navigate to the directory where you downloaded the JAR file.  
   - Run the following command to start the first client instance:


        java -jar vorprojekt-ivayla-ivanova-1.0-SNAPSHOT.jar

   - Repeat this step as many times as you wish to start certain amount of client instances in total.  Each client instance will connect to the server automatically.

4. Play with Other Clients 
   - Once you have at least two client instances running, you can play with each other. 
   - Follow the on-screen instructions to interact with the application and play the game.

### Additional Notes
Make sure to keep the server instance running while you're using the application. Clients need to connect to the server to communicate with each other.

## Roadmap

This section outlines the future plans and roadmap for the project.

### Refactoring and Network Playability
- Refactor parts of the project codebase to improve maintainability and readability.
- Enable network playability to allow users to play the game over a network connection.

### Architecture Changes
- Redesign the server-client architecture.

### GUI Implementation
- Introduce a graphical user interface (GUI).

### Extended Player Support
- Expand the game rules and mechanics to support gameplay for 5 to 8 players.

### Platform-Specific Distribution
- Convert the application from a JAR file format to OS-specific formats (e.g., .exe for Windows, .app for macOS, .deb/.rpm for Linux) for easier installation and distribution.

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
