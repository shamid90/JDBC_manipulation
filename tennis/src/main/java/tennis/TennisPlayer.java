package tennis;

import java.sql.*;
import java.util.*;


public class TennisPlayer {
	public static void main(String[] args) {
		try (Connection c = DriverManager.getConnection("<url>", "<user>", "<password>")) {
			c.setAutoCommit(false);
			System.out.println("Opened database successfully");
			
			//Adding two players correcting spelling mistakes
			final DBPlayer p1 = (new DBPlayer("Boris", "Beker")).persist(c).last_name("Becker").persist(c);
			final DBPlayer p2 = (new DBPlayer("Martine", "Navratilova")).first_name("Martina").persist(c);
			
			//to see the changes on the server, commit
			c.commit();
			
			//print p1 and p2
			System.out.println(p1);
			System.out.println(p2+"\n");
			
			//print the whole table: tennis_player
			System.out.println("All players:");
			List<DBPlayer> all_players = DBPlayer.all(c);
			for(int i=0; i<all_players.size();i++) {
				System.out.println(all_players.get(i));
			}
			System.out.println();
			//search in the table	
			System.out.println("Search results:");
			//search based on id
			final Optional<DBPlayer> aID1 = DBPlayer.from_id(c,4);
			final Optional<DBPlayer> aID2 = DBPlayer.from_id(c,300);
			System.out.println(aID1);
			System.out.println(aID2+"\n");
			
			//search based on first_name and/or last_name or none(returns all)
			List<DBPlayer> search_player = null;
			final Optional<String> empty_arg = Optional.empty();
			System.out.println("Players with first_name 'Roger'");
			search_player = DBPlayer.search(c,Optional.of("Roger"),empty_arg);
			for(int i=0; i<search_player.size(); i++) {
				System.out.println(search_player.get(i));
			}
			System.out.println("Players with last_name 'Graff'");
			search_player = DBPlayer.search(c,null,Optional.of("Graf"));
			for(int i=0; i<search_player.size(); i++) {
				System.out.println(search_player.get(i));
			}
			
			System.out.println("Players with first_name 'baz' and last_name 'foo'");
			search_player = DBPlayer.search(c,Optional.of("baz"),Optional.of("foo"));
			for(int i=0; i<search_player.size(); i++) {
				System.out.println(search_player.get(i));
			}	
			
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getClass().getName()+": "+e.getMessage());
			System.exit(0);
		}
	}
}


class DBPlayer {
	@Override
	public String toString() {
		return "DBPlayer [player_id=" + player_id + ", first_name=" + first_name + ", last_name=" + last_name
				+ ", is_persisted=" + is_persisted + "]";
	}
	final Optional<Integer> player_id;
	final String first_name;
	final String last_name;
	final boolean is_persisted;
	//constructor 1 (player_id: empty)
	DBPlayer (String first_name, String last_name) {
		this.player_id = Optional.empty();
		this.first_name = first_name;
		this.last_name = last_name;
		this.is_persisted = false;
	}
	//constructor 2
	private DBPlayer (Integer player_id, String first_name, String last_name, boolean is_persisted) {
		this.player_id = Optional.of(player_id);
		this.first_name = first_name;
		this.last_name = last_name;
		this.is_persisted = is_persisted;
	}
	//changing the first_name
	DBPlayer first_name(String first_name) {
		if (this.player_id.isPresent())
			return new DBPlayer(this.player_id.get(), first_name, this.last_name, false);
		else
			return new DBPlayer(first_name, this.last_name);		
	}
	//changing the last_name
	DBPlayer last_name(String last_name) {
		if (this.player_id.isPresent())
			return new DBPlayer(this.player_id.get(), this.first_name, last_name, false);
		else
			return new DBPlayer(this.first_name, last_name);
	}
	//UPDATE or INSERT new row
	DBPlayer persist(Connection c) throws SQLException {
		// UPDATEs user with ID
		if (this.player_id.isPresent()) {
			String query = "UPDATE tennis_player SET (first_name, last_name) = (?,?) WHERE player_id= ?;";
			try (PreparedStatement stm = c.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)){
				stm.setString(1, this.first_name);
				stm.setString(2, this.last_name);
				stm.setInt(3, this.player_id.get());
				stm.execute();
				ResultSet res = stm.getGeneratedKeys();
				//if(res != null && res.next()){
				if(res != null){
					return new DBPlayer(this.player_id.get(), this.first_name, this.last_name, true);
	     		}
				else
					return new DBPlayer(this.first_name,this.last_name);
			}
		}
		else {
			String query = "INSERT INTO tennis_player (first_name, last_name) VALUES (?,?);";
			try (PreparedStatement stm = c.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)){
				stm.setString(1, this.first_name);
				stm.setString(2, this.last_name);
				stm.execute();
				ResultSet res = stm.getGeneratedKeys();

				if(res != null && res.next()){
					return new DBPlayer(res.getInt(1), this.first_name, this.last_name, true);
	     		}
				else {
					return new DBPlayer(this.first_name,this.last_name);
				}
			}
		}
		//persisted tennis_player
	}
	static Optional<DBPlayer> from_id(Connection c, Integer id) throws SQLException {
		// returns Players with that ID
		try (PreparedStatement stm = c.prepareStatement("SELECT * FROM tennis_player WHERE player_id=?;")){
			stm.setInt(1, id);
			stm.execute();
			ResultSet rs = stm.getResultSet();
			if(rs != null && rs.next()) {
				return Optional.of(new DBPlayer(rs.getInt(1),rs.getString(2),rs.getString(3),true));
			}
			else
				return Optional.empty();
		}
	}
	static List<DBPlayer> all(Connection c) throws SQLException {
		// returns All Players
		List<DBPlayer> li = new ArrayList<>();
		String query = "SELECT * FROM tennis_player ORDER BY 1;";
		Statement stm = null;
		ResultSet rs = null;
		try {
			stm = c.createStatement();
			rs = stm.executeQuery(query);
			while (rs.next()) {
				li.add(new DBPlayer(rs.getInt(1),rs.getString(2),rs.getString(3),true));
			}
		} catch(SQLException e){
			System.out.println(e.getMessage());
		} finally {
			try {
				if(rs != null)
					rs.close();
			}catch(SQLException e) {
				System.out.println(e.getMessage());
			}
		}
		return li;
	}
	static List<DBPlayer> search(Connection c, Optional<String> first_name, Optional<String> last_name) throws SQLException {
		// returns All Players that have BOTH that first_name and that last_name. If either is empty, then just
		//search(c, empty, empty) =:= all(c)
		List<DBPlayer> li = new ArrayList<>();
		if(first_name != null && last_name != null && first_name.isPresent() && last_name.isPresent()) {
			String query="SELECT * FROM tennis_player WHERE first_name=? AND last_name=? ORDER BY 1;";
			try (PreparedStatement stm = c.prepareStatement(query)){
				stm.setString(1, first_name.get());
				stm.setString(2, last_name.get());
				stm.execute();
				ResultSet rs = stm.getResultSet();
				while(rs != null && rs.next()) {
					li.add(new DBPlayer(rs.getInt(1),rs.getString(2),rs.getString(3),true));
				}
			return li;
			}
		}else if(first_name != null && first_name.isPresent()) {
			String query="SELECT * FROM tennis_player WHERE first_name=? ORDER BY 1;";
			try (PreparedStatement stm = c.prepareStatement(query)){
				stm.setString(1, first_name.get());
				stm.execute();
				ResultSet rs = stm.getResultSet();
				while(rs != null && rs.next()) {
					li.add(new DBPlayer(rs.getInt(1),rs.getString(2),rs.getString(3),true));
				}
			return li;
			}
		}else if(last_name != null && last_name.isPresent()) {
			String query="SELECT * FROM tennis_player WHERE last_name=? ORDER BY 1;";
			try (PreparedStatement stm = c.prepareStatement(query)){
				stm.setString(1, last_name.get());
				stm.execute();
				ResultSet rs = stm.getResultSet();
				while(rs != null && rs.next()) {
					li.add(new DBPlayer(rs.getInt(1),rs.getString(2),rs.getString(3),true));
				}
			return li;
			}
		}else
			return all(c);
	}
}
