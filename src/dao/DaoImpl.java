package dao;

import java.sql.*;
import java.util.ArrayList;

import model.Card;
import model.Player;

public class DaoImpl {
	private Connection conexion;

	public static final String SCHEMA_NAME = "unoDatabase";
	public static final String CONNECTION = "jdbc:mysql://localhost:3306/" + SCHEMA_NAME
			+ "?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
	public static final String USER_CONNECTION = "root";
	public static final String PASS_CONNECTION = "";
	public static final String GET_ALL_PERSONAS = "select * from persona";
	public static final String GET_PERSONA_BY_ID = "select * from persona where id = ?";
	public static final String UPDATE_PERSONA = "update persona set nombre = ?, apellido = ? where id = ?";
	public static final String INSERT_PERSONA = "insert into persona (nombre, apellido) values (?,?)";

	public void conectar() throws SQLException {
		String url = CONNECTION;
		String user = USER_CONNECTION;
		String pass = PASS_CONNECTION;
		conexion = DriverManager.getConnection(url, user, pass);
	}

	public void desconectar() throws SQLException {
		if (conexion != null) {
			conexion.close();
		}
	}

	// Consultas SQL
	public static final String DELETE_CARD = "DELETE FROM card WHERE id_Card = ?";
	public static final String SAVE_GAME = "INSERT INTO game (id_Card) VALUES (?)";
	public static final String ADD_VICTORIES = "UPDATE player SET victories = victories + 1 WHERE id_Player = ?";
	public static final String ADD_GAMES = "UPDATE player SET games = games + 1 WHERE id_Player = ?";
	public static final String CLEAR_DECK = "DELETE FROM card WHERE id_Player = ?";
	public static final String GET_PLAYER = "SELECT * FROM player WHERE user = ? AND password = ?";
	public static final String GET_CARDS = "SELECT * FROM card WHERE id_Player = ?";
	public static final String GET_LAST_CARD = "SELECT * FROM game INNER JOIN card ON game.id_Card = card.id_Card ORDER BY game.id_Game DESC LIMIT 1";
	public static final String GET_LAST_ID_CARD = "SELECT MAX(id_Card) AS lastId FROM card WHERE id_Player = ?";
	public static final String SAVE_CARD = "INSERT INTO card (id_Player, number, color) VALUES (?, ?, ?)";

	public void deleteCard(Card card) throws SQLException {
		try (PreparedStatement statement = conexion.prepareStatement(DELETE_CARD)) {
			statement.setInt(1, card.getId());
			statement.executeUpdate();
		}
	}

	public void saveGame(Card card) throws SQLException {
		try (PreparedStatement statement = conexion.prepareStatement(SAVE_GAME, Statement.RETURN_GENERATED_KEYS)) {
			statement.setInt(1, card.getId());
			statement.executeUpdate();
		}
	}

	public void addVictories(int id) throws SQLException {
		try (PreparedStatement statement = conexion.prepareStatement(ADD_VICTORIES)) {
			statement.setInt(1, id);
			statement.executeUpdate();
		}
	}

	public void addGames(int id) throws SQLException {
		try (PreparedStatement statement = conexion.prepareStatement(ADD_GAMES)) {
			statement.setInt(1, id);
			statement.executeUpdate();
		}
	}

	public void clearDeck(int id) throws SQLException {
		try (PreparedStatement statement = conexion.prepareStatement(CLEAR_DECK)) {
			statement.setInt(1, id);
			statement.executeUpdate();
		}
	}

	public Player getPlayer(String user, String pass) throws SQLException {
		try (PreparedStatement statement = conexion.prepareStatement(GET_PLAYER)) {
			statement.setString(1, user);
			statement.setString(2, pass);
			try (ResultSet resultSet = statement.executeQuery()) {
				if (resultSet.next()) {
					// Mapear el resultado a un objeto Player
					Player player = new Player();
					player.setId(resultSet.getInt("id_Player"));
					player.setUser(resultSet.getString("user"));
					player.setPassword(resultSet.getString("password"));
					player.setName(resultSet.getString("name"));
					player.setGames(resultSet.getInt("games"));
					player.setVictories(resultSet.getInt("victories"));
					return player;
				}
			}
		}
		return null;
	}

	public ArrayList<Card> getCards(int id) throws SQLException {
		ArrayList<Card> cards = new ArrayList<>();
		try (PreparedStatement statement = conexion.prepareStatement(GET_CARDS)) {
			statement.setInt(1, id);
			try (ResultSet resultSet = statement.executeQuery()) {
				while (resultSet.next()) {
					// Mapear el resultado a objetos Card y agregar a la lista
					int cardId = resultSet.getInt("id_Card");
					int playerId = resultSet.getInt("id_Player"); // Asegúrate de que este campo exista en tu base de
																	// datos
					String number = resultSet.getString("number");
					String color = resultSet.getString("color");

					Card card = new Card(cardId, number, color, playerId);
					cards.add(card);
				}
			}
		}
		return cards;
	}

	public Card getLastCard() throws SQLException {
		try (PreparedStatement statement = conexion.prepareStatement(GET_LAST_CARD)) {
			try (ResultSet resultSet = statement.executeQuery()) {
				if (resultSet.next()) {
					// Mapear el resultado a un objeto Card
					int cardId = resultSet.getInt("id_Card");
					int playerId = resultSet.getInt("playerId"); // Asegúrate de que este campo exista en tu base de
																	// datos
					String number = resultSet.getString("number");
					String color = resultSet.getString("color");

					Card card = new Card(cardId, number, color, playerId);
					return card;
				}
			}
		}
		return null;
	}

	public int getLastIdCard(int id) throws SQLException {
		try (PreparedStatement statement = conexion.prepareStatement(GET_LAST_ID_CARD)) {
			statement.setInt(1, id);
			try (ResultSet resultSet = statement.executeQuery()) {
				if (resultSet.next()) {
					return resultSet.getInt("lastId");
				}
			}
		}
		return 0;
	}

	public void saveCard(Card c) throws SQLException {
	    try (PreparedStatement statement = conexion.prepareStatement(SAVE_CARD, Statement.RETURN_GENERATED_KEYS)) {
	        // Don't set the ID here if it's auto-increment
	        statement.setInt(1, c.getPlayerId());
	        statement.setString(2, c.getNumber());
	        statement.setString(3, c.getColor());
	        statement.executeUpdate();

	        // Retrieve the generated primary key (id_Card) if needed
	        try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
	            if (generatedKeys.next()) {
	                int generatedId = generatedKeys.getInt(1);
	                c.setId(generatedId); // Set the generated ID in the Card object
	            } else {
	                throw new SQLException("Failed to retrieve auto-generated key.");
	            }
	        }
	    }
	}
}