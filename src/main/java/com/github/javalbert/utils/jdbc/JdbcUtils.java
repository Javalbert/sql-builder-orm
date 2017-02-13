/*******************************************************************************
 * Copyright 2016 Albert Shun-Dat Chan
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.github.javalbert.utils.jdbc;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.ReaderInputStream;

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
	
	private JdbcUtils() {}
}