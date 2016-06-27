/*******************************************************************************
 * Copyright (c) 2016 Albert Shun-Dat Chan
 *
 * Licensed under MIT License
 * See LICENSE file at the root of the project or
 * https://github.com/Javalbert/sql-builder-orm/blob/master/LICENSE
 *******************************************************************************/
package chan.shundat.albert.utils.jdbc;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.ReaderInputStream;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import chan.shundat.albert.utils.json.GsonUtils;

public final class JdbcUtils {
	public static void closeQuietly(Connection connection) {
		if (connection == null) {
			return;
		}
		
		try {
			connection.close();
		} catch (SQLException e) {}
	}
	
	public static void closeQuietly(PreparedStatement stmt) {
		if (stmt == null) {
			return;
		}
		
		try {
			stmt.close();
		} catch (SQLException e) {}
	}
	
	public static void closeQuietly(ResultSet rs) {
		if (rs == null) {
			return;
		}
		
		try {
			rs.close();
		} catch (SQLException e) {}
	}

	public static String fromClobToString(Clob clob) {
		return fromClobToString(clob, StandardCharsets.UTF_8);
	}
	
	public static String fromClobToString(Clob clob, Charset charset) {
		// CREDIT: http://stackoverflow.com/a/2169799
		try {
			InputStream in = new ReaderInputStream(clob.getCharacterStream(), charset);
			StringWriter w = new StringWriter();
			IOUtils.copy(in, w, charset);
			return w.toString();
		} catch (SQLException | IOException e) {
			return null;
		}
	}
	
	public static JsonObject toJson(PreparedStatement stmt) throws SQLException {
		JsonObject jsonObj = null;
		
		ResultSet rs = null;
		try {
			rs = stmt.executeQuery();
			
			if (rs.next()) {
				jsonObj = toJson(rs, stmt.getMetaData());
				
				if (rs.next()) {
					throw new RuntimeException("non-unique result set");
				}
			}
		} catch (SQLException e) {
			throw e;
		} finally {
			closeQuietly(rs);
		}
		
		return jsonObj;
	}

	public static List<JsonObject> toJsonList(ResultSet rs) throws SQLException {
		List<JsonObject> list = new ArrayList<>();
		ResultSetMetaData rsmd = rs.getMetaData();
		
		while (rs.next()) {
			JsonObject jsonObj = toJson(rs, rsmd);
			list.add(jsonObj);
		}
		return list;
	}
	
	public static List<JsonObject> toJsonList(PreparedStatement stmt) throws SQLException {
		ResultSet rs = null;
		try {
			rs = stmt.executeQuery();
			return toJsonList(rs);
		} catch (SQLException e) {
			throw e;
		} finally {
			closeQuietly(rs);
		}
	}
	
	private static JsonObject toJson(ResultSet rs, ResultSetMetaData rsmd) throws SQLException {
		JsonObject row = new JsonObject();
		
		Gson gson = GsonUtils.newGson();
		for (int column = 1; column <= rsmd.getColumnCount(); column++) {
			String name = rsmd.getColumnLabel(column);
			Object obj = rs.getObject(column);
			
			if (obj instanceof Clob) {
				obj = fromClobToString((Clob)obj);
			}
			
			JsonElement value = gson.toJsonTree(obj);
			row.add(name, value);
		}
		
		return row;
	}
	
	private JdbcUtils() {}
}