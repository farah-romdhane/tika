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
package org.apache.tika.mime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

/**
 * Tests ecrits a la main pour tuer les mutants PIT restants apres
 * les tests originaux et les tests generes par ChatUniTest.
 */
public class MediaTypeManualTest {

    /**
     * Intention : compareTo ordonne selon la representation textuelle.
     * Donnees : text/html et text/plain ne different que par le sous-type ("h" &lt; "p").
     * Oracle : seul le signe est specifie par Comparable, pas la valeur exacte.
     * Mutant vise : L429 retour remplace par 0.
     */
    @Test
    public void compareToOrdersBySubtype() {
        MediaType html = MediaType.parse("text/html");
        MediaType plain = MediaType.parse("text/plain");
        assertTrue(html.compareTo(plain) < 0);
        assertTrue(plain.compareTo(html) > 0);
        assertEquals(0, plain.compareTo(MediaType.parse("text/plain")));
    }

    /**
     * Intention : deux types differents ne sont pas egaux.
     * Donnees : meme type "text", sous-types differents.
     * Oracle : equals compare les chaines canoniques, donc false.
     * Mutant vise : L418 retour remplace par true.
     */
    @Test
    public void equalsIsFalseForDifferentMediaTypes() {
        assertFalse(MediaType.parse("text/plain").equals(MediaType.parse("text/html")));
    }

    /**
     * Intention : equals rejette un objet qui n'est pas un MediaType.
     * Donnees : la chaine "text/plain" (meme contenu textuel) et null.
     * Oracle : contrat de Object.equals, un objet d'un autre type ou null donne false.
     * Mutant vise : L420 retour remplace par true.
     */
    @Test
    public void equalsIsFalseForNonMediaType() {
        assertFalse(MediaType.TEXT_PLAIN.equals("text/plain"));
        assertFalse(MediaType.TEXT_PLAIN.equals(null));
    }

    /**
     * Intention : getBaseType retire les parametres.
     * Donnees : un type avec un parametre charset.
     * Oracle : Javadoc de getBaseType ("text/plain" pour "text/plain; charset=utf-8").
     * Mutants vises : L367 condition niee, L370 retour remplace par null.
     */
    @Test
    public void getBaseTypeRemovesParameters() {
        MediaType base = MediaType.parse("text/plain; charset=UTF-8").getBaseType();
        assertEquals("text/plain", base.toString());
        assertFalse(base.hasParameters());
    }

    /**
     * Intention : parse reutilise l'instance en cache pour un type simple.
     * Donnees : un type simple inconnu (parse deux fois) et la constante TEXT_PLAIN.
     * Oracle : Javadoc de SIMPLE_TYPES (eviter d'avoir trop d'instances en memoire),
     * donc le meme objet est retourne.
     * Mutants vises : L293 retour remplace par false, L260 "slash + 1" remplace par
     * "slash - 1". Les deux ne changent que le chemin (cache ou regex), pas la valeur.
     */
    @Test
    public void parseReturnsCachedInstanceForSimpleTypes() {
        assertSame(MediaType.parse("application/x-ift3913"),
                MediaType.parse("application/x-ift3913"));
        assertSame(MediaType.TEXT_PLAIN, MediaType.parse("text/plain"));
    }

    /**
     * Intention : parse rejette un type ou un sous-type vide.
     * Donnees : "text/" (sous-type vide) et "/plain" (type vide).
     * Oracle : Javadoc de parse, qui retourne null si l'analyse echoue ; RFC 2045
     * exige un type et un sous-type non vides.
     * Mutant vise : L293 retour remplace par true.
     */
    @Test
    public void parseRejectsEmptyTypeOrSubtype() {
        assertNull(MediaType.parse("text/"));
        assertNull(MediaType.parse("/plain"));
    }

    /**
     * Intention : parse accepte le charset place avant le type (TIKA-350).
     * Donnees : "charset=UTF-8; text/plain", format envoye par certains serveurs mal configures.
     * Oracle : Javadoc de parse ("charset=xxx; type/subtype" est gere), donc le
     * resultat est la forme canonique avec le charset en parametre.
     * Mutants vises : L277 condition niee, L278 retour remplace par null.
     */
    @Test
    public void parseHandlesCharsetFirst() {
        MediaType type = MediaType.parse("charset=UTF-8; text/plain");
        assertNotNull(type);
        assertEquals("text/plain; charset=UTF-8", type.toString());
    }

    /**
     * Intention : set(String...) ignore les chaines qui ne sont pas des types valides.
     * Donnees : deux types valides entourant une chaine sans "/".
     * Oracle : Javadoc de set ("parsed types"), parse retourne null pour
     * "pas-un-type", qui n'est donc pas ajoute.
     * Mutants vises : L230 condition niee, L234 retour remplace par un ensemble vide.
     */
    @Test
    public void setOfStringsSkipsInvalidTypes() {
        Set<MediaType> types = MediaType.set("text/plain", "pas-un-type", "image/png");
        assertEquals(2, types.size());
        assertTrue(types.contains(MediaType.TEXT_PLAIN));
        assertTrue(types.contains(MediaType.image("png")));
    }

    /**
     * Intention : set(MediaType...) ignore les valeurs null.
     * Donnees : deux constantes entourant un null.
     * Oracle : le code filtre explicitement les null, l'ensemble contient les deux types.
     * Mutants vises : L211 condition niee, L215 retour remplace par un ensemble vide.
     */
    @Test
    public void setOfMediaTypesSkipsNull() {
        Set<MediaType> types = MediaType.set(MediaType.TEXT_PLAIN, null, MediaType.TEXT_HTML);
        assertEquals(2, types.size());
        assertTrue(types.contains(MediaType.TEXT_PLAIN));
        assertTrue(types.contains(MediaType.TEXT_HTML));
    }

    /**
     * Intention : ajouter un parametre a un type de base sans parametre.
     * Donnees : TEXT_PLAIN (aucun parametre) + charset=UTF-8.
     * Oracle : Javadoc du constructeur ("adding a parameter to a base type").
     * Mutant vise : L350 retour remplace par une Map vide.
     */
    @Test
    public void addParameterToTypeWithoutParameters() {
        MediaType type = new MediaType(MediaType.TEXT_PLAIN, "charset", "UTF-8");
        assertEquals("text/plain; charset=UTF-8", type.toString());
    }

    /**
     * Intention : n'ajouter aucun parametre conserve ceux du type de base.
     * Donnees : type avec charset + Map vide.
     * Oracle : l'union avec un ensemble vide ne change rien.
     * Mutants vises : L349 condition niee, L352 retour remplace par une Map vide.
     */
    @Test
    public void addNoParameterKeepsBaseParameters() {
        MediaType type = new MediaType(MediaType.parse("text/plain; charset=UTF-8"),
                Collections.emptyMap());
        assertEquals("text/plain; charset=UTF-8", type.toString());
    }

    /**
     * Intention : les parametres du type de base et les nouveaux sont fusionnes.
     * Donnees : type avec charset + format=flowed (cles distinctes, aucune ne masque l'autre).
     * Oracle : les deux parametres sont presents, tries par nom (getParameters est une Map triee).
     * Mutants vises : L351 condition niee, L355 et L356 appels putAll retires,
     * L357 retour remplace par une Map vide.
     */
    @Test
    public void addParameterMergesWithBaseParameters() {
        MediaType type = new MediaType(MediaType.parse("text/plain; charset=UTF-8"),
                "format", "flowed");
        assertEquals("text/plain; charset=UTF-8; format=flowed", type.toString());
    }

    /**
     * Intention : chaque caractere autorise d'un nom simple passe par le cache.
     * Donnees : un nom contenant les bornes et symboles acceptes par isSimpleName
     * ('0', '9', 'a', 'z', '-', '+', '.', '_'), suivi d'un UUID pour garantir une
     * chaine jamais vue (le cache SIMPLE_TYPES est statique et partage entre tests).
     * Oracle : Javadoc de SIMPLE_TYPES, le meme objet est retourne.
     * Mutants vises : L288-289, bornes et conditions niees de isSimpleName.
     */
    @Test
    public void parseCachesNamesWithEveryAllowedCharacter() {
        String name = "application/x-0az9+._" + UUID.randomUUID();
        assertSame(MediaType.parse(name), MediaType.parse(name));
    }

    /**
     * Intention : un type avec parametre n'est pas pris pour un nom simple.
     * Donnees : un parametre dont la valeur est unique (UUID), pour la meme raison de cache.
     * Oracle : le parametre doit etre extrait ; un nom simple n'aurait aucun parametre.
     * Mutants vises : L288, conditions niees qui font accepter ';', ' ' ou '='.
     */
    @Test
    public void parseExtractsParameterInsteadOfTreatingAsSimpleName() {
        String value = UUID.randomUUID().toString();
        MediaType type = MediaType.parse("text/plain; format=" + value);
        assertEquals(value, type.getParameters().get("format"));
    }
}
