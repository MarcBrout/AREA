package eu.epitech;

import eu.epitech.action.AAction;
import eu.epitech.reaction.AReaction;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Area {
	private String name = null;
    private AAction action = null;
    private AReaction reaction = null;
    private User userRef = null;

    public Area(User user) {
    	this.userRef = user;
	}

	public Area(User user, String name) {
    	this.name = name;
    	this.userRef = user;
	}

    public Area(AAction action, AReaction reaction, User user) {
    	this.name = action.getName() + "_to_" + reaction.getName();
    	this.action = action;
    	this.reaction = reaction;
    	this.userRef = user;
	}

	/*
	*** Adds the area and all it's components to the database.
	*** It will change the area's name to avoid duplicates, be careful !
	 */
	public void addToDatabase(DatabaseManager dbm) {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int userId;
		String newName = null;

		try {
			if (this.name == null || this.action == null || this.reaction == null)
				return;
			userId = this.userRef.getDbId(dbm);
			if (userId != -1) { // user exists in the database
				for (int i = 0; newName == null; i++) { // while an area with the same name and fk_area_user is found, increments the area's name
					newName = this.name + i;
					pstmt = dbm.getConnection().prepareStatement("SELECT id FROM area WHERE fk_area_user = ? AND name LIKE ?");
					pstmt.setInt(1, userId);
					pstmt.setString(2, newName);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						newName = null;
					}
				}
				this.name = newName; // changes the area's name to avoid duplicates
				pstmt = dbm.getConnection().prepareStatement("INSERT INTO area(fk_area_user, name) VALUES (?, ?)");
				pstmt.setInt(1, userId);
				pstmt.setString(2, this.name);
				pstmt.executeUpdate();
				this.action.addToDatabase(dbm, this);
				this.reaction.addToDatabase(dbm, this);
			}
		} catch (SQLException e) {
			System.out.println("SQLException: " + e.getMessage());
			System.out.println("SQLState: " + e.getSQLState());
			System.out.println("VendorError: " + e.getErrorCode());
		} finally { // Close statements and results before returning.
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException ignored) { }
			}
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException ignored) { }
			}
		}
	}

	/*
	*** Removes an area from the database, will also remove it's action and reaction.
	 */
	public void removeFromDatabase(DatabaseManager dbm) {
		PreparedStatement pstmt = null;
		int areaId;

		try {
			if (this.action != null && this.reaction != null) {
				this.action.removeFromDatabase(dbm, this);
				this.reaction.removeFromDatabase(dbm, this);
			}
			if ((areaId = this.getDbId(dbm)) == -1)
				return;
			pstmt = dbm.getConnection().prepareStatement("DELETE FROM area WHERE id = ?");
			pstmt.setInt(1, areaId);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			System.out.println("SQLException: " + e.getMessage());
			System.out.println("SQLState: " + e.getSQLState());
			System.out.println("VendorError: " + e.getErrorCode());
		} finally { // Close statements before returning.
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException ignored) { }
			}
		}
	}

	public int getDbId(DatabaseManager dbm, int userId) {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int areaId = -1;

		try {
			if (userId == -1) // user not found
				return (-1);
			pstmt = dbm.getConnection().prepareStatement("SELECT id FROM area WHERE name LIKE ?");
			pstmt.setString(1, this.name);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				areaId = rs.getInt("id");
			}
		} catch (SQLException e) {
			System.out.println("SQLException: " + e.getMessage());
			System.out.println("SQLState: " + e.getSQLState());
			System.out.println("VendorError: " + e.getErrorCode());
		} finally { // Close statements and results before returning.
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException ignored) { }
			}
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException ignored) { }
			}
		}
		return (areaId);
	}

	public int getDbId(DatabaseManager dbm) {
		return (this.getDbId(dbm, this.userRef.getDbId(dbm)));
	}

	public String getName() {
		return name;
	}

	public void setName() {
		if (this.name == null) {
			this.name = action.getName() + "_to_" + reaction.getName();
		}
	}

	public void setName(String name) {
		this.name = name;
	}

	public AAction getAction() {
		return action;
	}

	public void setAction(AAction action) {
		this.action = action;
	}

	public AReaction getReaction() {
		return reaction;
	}

	public void setReaction(AReaction reaction) {
		this.reaction = reaction;
	}
}
