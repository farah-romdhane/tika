/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tika.metadata;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public interface Database {
    String PREFIX = "database" + TikaCoreProperties.NAMESPACE_PREFIX_DELIMITER;

    Property TABLE_NAME = Property.externalTextBag(PREFIX + "table-name");
    Property ROW_COUNT = Property.externalInteger(PREFIX + "row-count");
    Property COLUMN_COUNT = Property.externalInteger(PREFIX + "column-count");
    Property COLUMN_NAME = Property.externalTextBag(PREFIX + "column-name");

    /**
     * Base de donnees utilisee pour garder les metadonnees extraites (tache 3).
     */
    String METADATA_DB_URL = "jdbc:postgresql://tikaadmin:Tika2026!@localhost:5432/tika";

    /**
     * Compte les lignes d'une table de la base de metadonnees (tache 3).
     */
    static int countRows(String tableName) throws SQLException {
        try (Connection connection = DriverManager.getConnection(METADATA_DB_URL);
                Statement statement = connection.createStatement();
                ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM " + tableName)) {
            result.next();
            return result.getInt(1);
        }
    }
}
