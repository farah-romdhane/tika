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
package org.apache.tika.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.Test;

/**
 * Tests ecrits a la main (tache 2, IFT3913) pour tuer les mutants qui survivent
 * apres l'ajout des tests generes par ChatUniTest, et pour couvrir les methodes
 * de lecture sur flux que ChatUniTest n'a pas reussi a tester.
 */
public class EndianUtilsManualTest {

    private static InputStream stream(int... bytes) {
        byte[] data = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            data[i] = (byte) bytes[i];
        }
        return new ByteArrayInputStream(data);
    }

    // ---------------------------------------------------------------
    // 1. Mutants survivants
    // ---------------------------------------------------------------

    /**
     * Intention : 4 octets nuls sont une valeur valide, pas une erreur.
     * Donnees : 00 00 00 00, le seul cas ou (ch1|ch2|ch3|ch4) vaut exactement 0.
     * Oracle : 0, et aucune exception (mutant "< 0" remplace par "<= 0", ligne 92).
     */
    @Test
    public void testReadUIntLEAllZeros() throws Exception {
        assertEquals(0L, EndianUtils.readUIntLE(stream(0x00, 0x00, 0x00, 0x00)));
    }

    /**
     * Intention : meme cas que le test precedent pour readUIntBE (ligne 111).
     * Oracle : 0, sans exception.
     */
    @Test
    public void testReadUIntBEAllZeros() throws Exception {
        assertEquals(0L, EndianUtils.readUIntBE(stream(0x00, 0x00, 0x00, 0x00)));
    }

    /**
     * Intention : meme cas pour readIntME (ligne 168).
     * Oracle : 0, sans exception.
     */
    @Test
    public void testReadIntMEAllZeros() throws Exception {
        assertEquals(0, EndianUtils.readIntME(stream(0x00, 0x00, 0x00, 0x00)));
    }

    /**
     * Intention : readUIntBE doit refuser un flux de 3 octets. Le test original
     * censé vérifier ce cas appelle readUIntLE par erreur.
     * Donnees : FF FF FF. Le 4e read() renvoie -1 ; avec le mutant
     * (ch1|ch2|ch3) & ch4, on obtiendrait 0xFF & -1 = 255, donc pas d'exception.
     * Oracle : BufferUnderrunException (contrat documente dans la Javadoc).
     */
    @Test
    public void testReadUIntBEBufferUnderrun() {
        assertThrows(EndianUtils.BufferUnderrunException.class,
                () -> EndianUtils.readUIntBE(stream(0xFF, 0xFF, 0xFF)));
    }

    /**
     * Intention : un octet 0x00 apres un octet de continuation fait partie du nombre.
     * Donnees : 81 00. 0x81 = continuation avec la valeur 1 ; 0x00 = dernier octet, valeur 0.
     * Oracle : (1 << 7) + 0 = 128. Le mutant ">= 0" -> "> 0" (ligne 235) s'arrete
     * sur l'octet 0x00 et renvoie 1.
     */
    @Test
    public void testReadUE7ZeroAfterContinuation() throws Exception {
        assertEquals(128L, EndianUtils.readUE7(stream(0x81, 0x00)));
    }

    /**
     * Intention : le nombre 0 encode sur un seul octet.
     * Donnees : 00.
     * Oracle : 0, sans exception. Le mutant "i < 0" -> "i <= 0" (ligne 246)
     * lance une IOException a tort.
     */
    @Test
    public void testReadUE7SingleZero() throws Exception {
        assertEquals(0L, EndianUtils.readUE7(stream(0x00)));
    }

    // ---------------------------------------------------------------
    // 2. Methodes de lecture sur flux non couvertes
    // ---------------------------------------------------------------

    /**
     * Intention : verifier l'ordre des octets et le resultat non signe sur 16 bits.
     * Donnees : FF 80 (bit de poids fort a 1 dans les deux octets).
     * Oracle : LE = 0x80FF = 33023 ; BE = 0xFF80 = 65408 (calcul a la main).
     */
    @Test
    public void testReadUShortLEAndBE() throws Exception {
        assertEquals(0x80FF, EndianUtils.readUShortLE(stream(0xFF, 0x80)));
        assertEquals(0xFF80, EndianUtils.readUShortBE(stream(0xFF, 0x80)));
    }

    /**
     * Intention : la version signee doit donner un nombre negatif.
     * Donnees : FF 80, les memes octets que le test precedent.
     * Oracle : (short) 0x80FF = -32513 ; (short) 0xFF80 = -128.
     */
    @Test
    public void testReadShortLEAndBENegative() throws Exception {
        assertEquals((short) -32513, EndianUtils.readShortLE(stream(0xFF, 0x80)));
        assertEquals((short) -128, EndianUtils.readShortBE(stream(0xFF, 0x80)));
    }

    /**
     * Intention : verifier l'ordre des 4 octets et le signe pour readIntLE et readIntBE.
     * Donnees : 4 octets differents, dont 0x84 en poids fort, pour que chaque
     * position compte et que le resultat soit negatif.
     * Oracle : 0x84030201 vu comme un int = -2080112127, dans les deux cas
     * (les octets sont donnes dans l'ordre inverse pour BE).
     */
    @Test
    public void testReadIntLEAndBE() throws Exception {
        assertEquals(0x84030201, EndianUtils.readIntLE(stream(0x01, 0x02, 0x03, 0x84)));
        assertEquals(0x84030201, EndianUtils.readIntBE(stream(0x84, 0x03, 0x02, 0x01)));
    }

    /**
     * Intention : verifier l'ordre des 8 octets pour readLongLE et readLongBE.
     * Donnees : 01..07 puis 0x88 en poids fort (resultat negatif).
     * Oracle : 0x8807060504030201L.
     */
    @Test
    public void testReadLongLEAndBE() throws Exception {
        assertEquals(0x8807060504030201L,
                EndianUtils.readLongLE(stream(0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x88)));
        assertEquals(0x8807060504030201L,
                EndianUtils.readLongBE(stream(0x88, 0x07, 0x06, 0x05, 0x04, 0x03, 0x02, 0x01)));
    }

    /**
     * Intention : le 4e octet (bit 31) ne doit pas etre etendu en signe.
     * Le code fait un cast en long expres (commentaire dans EndianUtils).
     * Donnees : seul l'octet de rang 4 vaut 0x80.
     * Oracle : 0x80000000L = 2147483648 (positif). Sans le cast, on obtiendrait
     * 0xFFFFFFFF80000000L.
     */
    @Test
    public void testReadLongLEAndBEBit31() throws Exception {
        assertEquals(0x80000000L,
                EndianUtils.readLongLE(stream(0x00, 0x00, 0x00, 0x80, 0x00, 0x00, 0x00, 0x00)));
        assertEquals(0x80000000L,
                EndianUtils.readLongBE(stream(0x00, 0x00, 0x00, 0x00, 0x80, 0x00, 0x00, 0x00)));
    }

    /**
     * Intention : chaque methode de lecture doit lever BufferUnderrunException
     * quand il manque un octet.
     * Donnees : un octet de moins que necessaire pour chaque methode.
     * Oracle : BufferUnderrunException (Javadoc de chaque methode).
     */
    @Test
    public void testReadMethodsBufferUnderrun() {
        assertThrows(EndianUtils.BufferUnderrunException.class,
                () -> EndianUtils.readUShortLE(stream(0x01)));
        assertThrows(EndianUtils.BufferUnderrunException.class,
                () -> EndianUtils.readUShortBE(stream(0x01)));
        assertThrows(EndianUtils.BufferUnderrunException.class,
                () -> EndianUtils.readIntLE(stream(0x01, 0x02, 0x03)));
        assertThrows(EndianUtils.BufferUnderrunException.class,
                () -> EndianUtils.readIntBE(stream(0x01, 0x02, 0x03)));
        assertThrows(EndianUtils.BufferUnderrunException.class,
                () -> EndianUtils.readLongLE(stream(1, 2, 3, 4, 5, 6, 7)));
        assertThrows(EndianUtils.BufferUnderrunException.class,
                () -> EndianUtils.readLongBE(stream(1, 2, 3, 4, 5, 6, 7)));
    }

    /**
     * Intention : des octets nuls sont des donnees valides pour toutes les methodes de lecture.
     * Donnees : uniquement des 0x00. C'est le seul cas ou le "ou" de tous les octets vaut
     * exactement 0, donc le seul qui distingue "< 0" de "<= 0" dans le test de fin de flux.
     * Oracle : la valeur 0, sans exception, pour chaque methode.
     */
    @Test
    public void testReadMethodsAllZeros() throws Exception {
        assertEquals(0, EndianUtils.readUShortLE(stream(0x00, 0x00)));
        assertEquals(0, EndianUtils.readUShortBE(stream(0x00, 0x00)));
        assertEquals(0, EndianUtils.readIntLE(stream(0x00, 0x00, 0x00, 0x00)));
        assertEquals(0, EndianUtils.readIntBE(stream(0x00, 0x00, 0x00, 0x00)));
        assertEquals(0L, EndianUtils.readLongLE(stream(0, 0, 0, 0, 0, 0, 0, 0)));
        assertEquals(0L, EndianUtils.readLongBE(stream(0, 0, 0, 0, 0, 0, 0, 0)));
    }

    /**
     * Flux qui renvoie exactement les valeurs donnees, y compris -1 au milieu.
     */
    private static InputStream rawStream(int... values) {
        return new InputStream() {
            private int pos = 0;

            @Override
            public int read() {
                return pos < values.length ? values[pos++] : -1;
            }
        };
    }

    /**
     * Construit n valeurs egales a 1, sauf la position "missing" qui vaut -1.
     */
    private static int[] withMissingByte(int n, int missing) {
        int[] values = new int[n];
        for (int i = 0; i < n; i++) {
            values[i] = (i == missing) ? -1 : 0x01;
        }
        return values;
    }

    /**
     * Intention : les methodes testent chaque octet lu (ch1 | ch2 | ...) pour savoir s'il manque.
     * On verifie qu'un octet manquant est detecte quelle que soit sa position.
     * Donnees : un flux qui renvoie -1 a une seule position puis encore des octets (0x01).
     * Avec un ByteArrayInputStream, un -1 est toujours suivi d'autres -1, donc on ne peut
     * pas tester un octet manquant au milieu du flux ; on utilise un InputStream maison.
     * Oracle : BufferUnderrunException pour chaque position (Javadoc : "if the stream cannot
     * provide enough bytes").
     */
    @Test
    public void testReadMethodsDetectMissingByteAtAnyPosition() {
        for (int p = 0; p < 2; p++) {
            int[] v = withMissingByte(2, p);
            assertThrows(EndianUtils.BufferUnderrunException.class, () -> EndianUtils.readUShortLE(rawStream(v)));
            assertThrows(EndianUtils.BufferUnderrunException.class, () -> EndianUtils.readUShortBE(rawStream(v)));
        }
        for (int p = 0; p < 4; p++) {
            int[] v = withMissingByte(4, p);
            assertThrows(EndianUtils.BufferUnderrunException.class, () -> EndianUtils.readUIntLE(rawStream(v)));
            assertThrows(EndianUtils.BufferUnderrunException.class, () -> EndianUtils.readUIntBE(rawStream(v)));
            assertThrows(EndianUtils.BufferUnderrunException.class, () -> EndianUtils.readIntLE(rawStream(v)));
            assertThrows(EndianUtils.BufferUnderrunException.class, () -> EndianUtils.readIntBE(rawStream(v)));
            assertThrows(EndianUtils.BufferUnderrunException.class, () -> EndianUtils.readIntME(rawStream(v)));
        }
        for (int p = 0; p < 8; p++) {
            int[] v = withMissingByte(8, p);
            assertThrows(EndianUtils.BufferUnderrunException.class, () -> EndianUtils.readLongLE(rawStream(v)));
            assertThrows(EndianUtils.BufferUnderrunException.class, () -> EndianUtils.readLongBE(rawStream(v)));
        }
    }

    // ---------------------------------------------------------------
    // 3. Octets negatifs (cas que les tests generes n'ont pas testes)
    // ---------------------------------------------------------------

    /**
     * Intention : un octet negatif doit etre lu comme une valeur entre 0 et 255.
     * Donnees : 0xFF, qui vaut -1 en Java.
     * Oracle : 255 pour ubyteToInt et getUByte ; 4294967295 pour getUIntLE(FF FF FF FF).
     */
    @Test
    public void testUnsignedConversionsWithNegativeBytes() {
        assertEquals(255, EndianUtils.ubyteToInt((byte) 0xFF));
        assertEquals((short) 255, EndianUtils.getUByte(new byte[] {0x00, (byte) 0xFF}, 1));
        assertEquals(4294967295L,
                EndianUtils.getUIntLE(new byte[] {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF}));
    }

    /**
     * Intention : l'appel sur un flux qui leve une IOException doit la propager.
     * Donnees : un InputStream qui leve toujours IOException.
     * Oracle : IOException (clause "throws IOException" et Javadoc).
     */
    @Test
    public void testIOExceptionIsPropagated() {
        InputStream broken = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("flux casse");
            }
        };
        assertThrows(IOException.class, () -> EndianUtils.readIntLE(broken));
    }
}
