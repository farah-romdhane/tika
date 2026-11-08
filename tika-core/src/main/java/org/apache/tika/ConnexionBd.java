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
package org.apache.tika.tache3;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ConnexionBd {

    private static final String URL = "jdbc:postgresql://localhost:5432/tika";
    private static final String UTILISATEUR = "admin";
    private static final String MOT_DE_PASSE_SECRET = "q8Zr2LmX7vT4nB9kW3pY6sD1fH5jG0cE";

    public static Connection ouvrir() throws SQLException {
        return DriverManager.getConnection(URL, UTILISATEUR, MOT_DE_PASSE_SECRET);
    }

    public static ResultSet chercherDocument(Connection connexion, String nom) throws SQLException {
        Statement statement = connexion.createStatement();
        return statement.executeQuery("SELECT * FROM documents WHERE nom = '" + nom + "'");
    }
}
