# IFT3913 – Tâche 2

| Nom complet | Identifiant GitHub | Classe |
|---|---|---|
| ROMDHANE Farah | farah-romdhane | `org.apache.tika.io.EndianUtils` |
| KAISSI Ayman | Ayman4401 | `org.apache.tika.mime.MediaType` |

En résumé, on a pris deux classes de `tika-core`, on a généré des tests avec ChatUniTest et un modèle local
(qwen2.5-coder:7b avec Ollama), on les a corrigés, puis on a ajouté nos propres tests pour les mutants qui restaient.

| Score de mutation (PIT) | Tests originaux | + tests de l'IA | + nos tests |
|---|---|---|---|
| `EndianUtils` (206 mutants) | 18 % | 50 % | 99 % (204/206) |
| `MediaType` (85 mutants) | 59 % | 75 % | 99 % (84/85) |

## 1. Choix des classes

On a choisi `tika-core` parce que les classes sont petites, avec peu de dépendances, et que le module compile vite.
Comme la génération avec un modèle local est lente et que PIT relance les tests des centaines de fois, ça comptait.

On a lancé JaCoCo (déjà configuré dans Tika) et PIT avec seulement les tests existants :

| Classe | Tests existants | Lignes couvertes | Méthodes couvertes | Score de mutation |
|---|---|---|---|---|
| `EndianUtils` | `EndianUtilsTest` (4 tests) | 26 % | 4 / 32 | 18 % (14 survivants, 154 non couverts) |
| `MediaType` | `MediaTypeTest` (9 tests) | 68 % | 10 / 28 | 59 % (2 survivants, 33 non couverts) |

Dans `EndianUtils`, seules `readUE7`, `readUIntLE`, `readUIntBE` et `readIntME` sont testées. Dans `MediaType`,
`equals`, `hashCode`, `compareTo`, `getBaseType`, `set`, `union`, `audio` et `video` ne sont jamais appelées.
Les deux classes ont donc du code non couvert et des mutants vivants. Rapports : [`docs/rapports/jacoco-avant/`](docs/rapports/jacoco-avant/),
[`docs/rapports/pit-avant/`](docs/rapports/pit-avant/) et [`docs/pit/mediatype-avant/`](docs/pit/mediatype-avant/).

Petite remarque : une première mesure de `MediaType` faite séparément donnait 54 % au lieu de 59 %. La différence
vient du cache statique `SIMPLE_TYPES` (voir section 6.2), donc on a gardé les trois mesures faites dans les mêmes
conditions.

## 2. ChatUniTest dans Maven

On a ajouté le plugin `chatunitest-maven-plugin` 2.1.1 dans [`tika-core/pom.xml`](tika-core/pom.xml) :

```xml
<configuration>
  <apiKeys>ollama</apiKeys>
  <model>code-llama</model>
  <url>http://localhost:11434/v1/chat/completions</url>
  <testNumber>1</testNumber>
  <maxRounds>3</maxRounds>
  <maxPromptTokens>3000</maxPromptTokens>
  <thread>false</thread>
  <temperature>0.2</temperature>
</configuration>
```

Puis `mvn chatunitest:class -DselectClass=EndianUtils` (et `MediaType`).

Ça n'a pas marché du premier coup. ChatUniTest n'accepte que certains noms de modèles, alors on a copié
`qwen2.5-coder:7b` sous le nom `code-llama` dans Ollama. On avait d'abord essayé `codeqwen:v1.5-chat`, mais aucun
test ne compilait (il inventait des imports) et les requêtes dépassaient le délai de 5 minutes, d'où aussi
`maxPromptTokens=3000`. Enfin, la dépendance `chatunitest-starter` demandée par la doc ramène JUnit 4 et de vieilles
versions de Mockito, ce qui faisait exécuter 0 test avec `mvn test`. Après la génération, on l'a remplacée par
`mockito-core` et `mockito-junit-jupiter`.

La génération a pris 4 h 52 pour `EndianUtils` et 37 min pour `MediaType`, sur des portables sans GPU.

## 3. Les tests générés

### 3.1 Où sont-ils ?

| | `EndianUtils` | `MediaType` |
|---|---|---|
| Tests bruts produits par ChatUniTest | [`chatunitest-tests/.../io/`](tika-core/chatunitest-tests/org/apache/tika/io/) | [`chatunitest-tests/.../mime/`](tika-core/chatunitest-tests/org/apache/tika/mime/) |
| Tests corrigés (exécutés par Maven et PIT) | [`src/test/java/.../io/EndianUtils_*_Test.java`](tika-core/src/test/java/org/apache/tika/io/) | [`src/test/java/.../mime/MediaType_*_Test.java`](tika-core/src/test/java/org/apache/tika/mime/) |
| Journal de génération | [`chatunitest-endianutils-log.txt`](tika-core/chatunitest-endianutils-log.txt) | [`chatunitest-mediatype-log.txt`](tika-core/chatunitest-mediatype-log.txt) |
| Erreurs de compilation des tests rejetés | [`docs/chatunitest/endianutils-erreurs/`](docs/chatunitest/endianutils-erreurs/) | [`docs/chatunitest/mediatype-erreurs/`](docs/chatunitest/mediatype-erreurs/) |

### 3.2 Compilent-ils et s'exécutent-ils sans intervention ?

| | `EndianUtils` | `MediaType` |
|---|---|---|
| Méthodes traitées | 31 | 11 (constructeurs, accesseurs et méthodes privées ignorés) |
| Fichiers gardés / tests générés | 19 / 53 | 9 / 48 |
| Tests qui échouent sans intervention | 16 (28 %) | 19 (40 %) |

Donc non, ils ne s'exécutent pas sans intervention, même si ChatUniTest affiche « compile and execute successfully »
pour des tests qui échouent ensuite dans Maven. Voici toutes les corrections qu'on a faites :

| Correction | `EndianUtils` | `MediaType` | Effet sur l'exécution |
|---|---|---|---|
| Dépendance `chatunitest-starter` remplacée par Mockito (`pom.xml`) | 1 (commune aux deux classes) | | sans elle, 0 test exécuté |
| Oracles faux | 19 corrigés (10 fichiers) | 15 assertions retirées, 14 tests supprimés | change ce que les tests vérifient |
| Mock inutile retiré | compté dans les 19 (`getShortBE_16`, mock sur une méthode statique) | 1 (`@Mock` sur `MediaType`) | le test plantait |
| Imports avec étoile remplacés (Checkstyle) | 19 fichiers | 9 fichiers | aucun : seules les lignes `import` changent |

Les imports avec étoile (`Assertions.*`, `org.mockito.*`…) sont refusés par le Checkstyle de Tika, ce qui faisait
échouer le build avant même les tests. On a vérifié avec `git diff` que seules les lignes `import` ont changé, donc
les scores PIT ne sont pas affectés. Les tests passent maintenant avec Checkstyle activé.

### 3.3 Corrections pour `EndianUtils`

On a remplacé chaque oracle faux par la bonne valeur, calculée à la main à partir du code et de l'ordre des octets.
On partait du message de Maven (`expected <…> but was <…>`) et on vérifiait qui avait raison : c'était toujours
l'IA qui se trompait. Les 19 corrections sont marquées `// CORRECTION MANUELLE` dans le code.

| Erreur de l'IA | Exemple | Nombre |
|---|---|---|
| inverse big-endian et little-endian | `getIntBE` de `01 02 03 04` : attendu `0x04030201`, correct `0x01020304` | 5 |
| se trompe sur le nombre d'octets lus, sur l'offset, ou invente une valeur | `getIntLE` comparé à une valeur sur 8 octets | 6 |
| ne comprend pas l'encodage de `readUE7` | attend `0x0102030405060708` alors que `0x01` termine la lecture (résultat 1) | 4 |
| message ou type d'exception inventé | `NullPointerException` pour un tableau vide | 3 |
| oublie que la valeur est non signée | `getUIntBE(FF FF FF FF)` : attendu -1, correct 4294967295 | 1 |

### 3.4 Corrections pour `MediaType`

Ici on a été plus strict : on a seulement retiré les assertions fausses, sans les remplacer. Les corrections sont
marquées `[CORRECTION Cn]` dans le code. Presque toutes les erreurs viennent du fait que le modèle croit que
`MediaType` valide ses entrées : il attend des exceptions que `parse` ne lance jamais (elle retourne `null`). Il
attendait aussi `-1` pour `compareTo` alors que seul le signe compte.

### 3.5 Deux façons de corriger

On n'a donc pas corrigé les deux classes de la même façon. La méthode utilisée pour `MediaType` mesure mieux ce
que l'IA détecte toute seule. Pour `EndianUtils`, le score « après l'IA » (50 %) est sûrement un peu optimiste,
puisque certains oracles viennent de nous.

## 4. Oracles de l'IA vs tests écrits à la main

Les tests d'origine sont peu nombreux mais justes et ciblés. Dans `EndianUtilsTest`, les données contiennent des
octets comme `0xFF` ou `0xF0`, ce qui vérifie que les valeurs sont lues comme non signées. Pour `MediaType`, PIT
montre que les tests d'origine tuent 96 % des mutants du code qu'ils exécutent.

Les tests de l'IA couvrent beaucoup plus de méthodes, et certains sont bons : `FF FF FF FE` qui doit donner
4294967294 et pas -2, plusieurs cas limites sur les offsets (négatif, trop grand, tableau vide, `null`), ou les
tests de `toString` et `hasParameters` de `MediaType`. Mais on a aussi trouvé :

- des oracles devinés au lieu d'être déduits du code (sections précédentes) ;
- des tests qui ne peuvent pas échouer : un `try`/`catch` sans `fail()` dans `getUShortBE_18`, et un test de
  `hashCode` sans aucune assertion ;
- des données qui ne testent pas l'essentiel : presque toujours `0x01` à `0x08`, jamais un octet négatif.
  `ubyteToInt` est testé seulement avec `0x01`, alors que tout l'intérêt est `0xFF → 255` ;
- des doublons (8 tests identiques dans `parse`) et de la réflexion pour appeler des méthodes publiques ;
- un test qui passe pour une mauvaise raison : `invoke(instance, null)` lance une exception à cause de la
  réflexion, pas à cause de `MediaType`.

Les tests d'origine ne sont pas parfaits non plus : dans `testReadUIntBE`, le cas du flux trop court appelle
`readUIntLE` au lieu de `readUIntBE`, sûrement un copier-coller.

En gros, l'IA choisit souvent de bons cas à tester, mais elle devine le résultat attendu.

## 5. Analyse de mutation et mutants détectés

PIT 1.19.1 avec le plugin JUnit 5, mutateurs par défaut. On a lancé PIT trois fois en ajoutant les groupes de tests
un par un dans `<targetTests>`. Rapports : [`docs/rapports/`](docs/rapports/) pour `EndianUtils`,
[`docs/pit/`](docs/pit/) pour `MediaType`, et le rapport final après la fusion de nos deux parties dans
[`docs/rapports/pit-final-fusion/`](docs/rapports/pit-final-fusion/) (288/291).

| | Originaux | + IA | + nos tests |
|---|---|---|---|
| `EndianUtils` : tués / survivants / non couverts | 38 / 14 / 154 | 103 / 14 / 89 | 204 / 2 / 0 |
| `MediaType` : tués / survivants / non couverts | 50 / 2 / 33 | 64 / 4 / 17 | 84 / 1 / 0 |

Les tests de l'IA ne tuent donc pas tous les mutants.

### 5.1 Mutants détectés par les tests générés : `EndianUtils`

Les tests de l'IA tuent 65 mutants de plus, presque tous dans les méthodes `get*` (`getIntBE` et
`getIntLE` 15 chacune, `getLongLE` 8, `getUShortBE` et `getUShortLE` 7). Ces méthodes n'étaient jamais exécutées
avant. Comme elles reconstruisent un nombre à partir d'octets et que les tests comparent la valeur exacte, presque
toutes les mutations changent le résultat : un `<<` remplacé par `>>`, un `+` par `-` ou un `& 0xFF` par
`| 0xFF` (38 mutants), un retour remplacé par 0 (17), un `i++` remplacé par `i--` qui fait lire le mauvais octet (7).

Il restait 89 mutants non couverts dans les méthodes `read*` sur flux, que l'IA n'a pas réussi à tester, et 14
survivants. Parmi eux, 2 sont équivalents : le dernier `i++` de `getIntLE` et `getIntBE` devient `i--`, mais `i`
ne sert plus après. Les autres demandaient des données que personne n'avait utilisées : `00 00 00 00`, un octet
`0x00` dans `readUE7`, ou un flux trop court pour `readUIntBE`.

### 5.2 Mutants détectés par les tests générés : `MediaType`

Les tests de l'IA tuent 14 mutants de plus (+16 points), tous dans des méthodes que les tests
d'origine n'appelaient pas :

- les 5 mutants « retourne `null` » de `application`, `audio`, `image`, `text` et `video` : les tests vérifient le
  type obtenu (par exemple `application/json`), donc un `null` fait échouer l'assertion ;
- `getType`, `getSubtype` et `getBaseType` (4 mutants : retour remplacé par `""` ou `null`, `+` remplacé par `-`
  dans le calcul du sous-type) : les tests de `application` comparent la valeur exacte de chaque partie ;
- `equals` (2 mutants) : `testGetBaseType` compare deux `MediaType` égaux avec `assertEquals`, ce qui échoue si
  `equals` retourne `false` ou si sa condition est inversée ;
- `hasParameters` (2 mutants) : les tests vérifient `true` avec des paramètres et `false` sans paramètres ;
- `hashCode` (1 mutant) : le test compare le `hashCode` à la valeur attendue, qui n'est pas 0.

Il restait 17 mutants non couverts, surtout dans les méthodes que ChatUniTest a ignorées (`union`, `set`), et
4 survivants : `equals` qui retourne toujours `true` (aucun test ne compare deux types différents), la condition
de `getBaseType` (aucun test avec des paramètres), et deux mutants liés au cache de `parse` / `isSimpleName`.
Ce sont justement des cas limites que l'IA avait essayés, mais avec un oracle faux, et qu'on avait dû retirer.

## 6. Nos tests

### 6.1 `EndianUtils` : [`EndianUtilsManualTest.java`](tika-core/src/test/java/org/apache/tika/io/EndianUtilsManualTest.java) (16 tests)

| Test | Intention | Données | Oracle | Mutants visés |
|---|---|---|---|---|
| `testReadUIntLEAllZeros`, `…UIntBE…`, `…IntME…` | 4 octets nuls ne sont pas une erreur | `00 00 00 00`, seul cas où le « ou » des octets vaut 0 (tue `< 0` → `<= 0`) | 0, sans exception | `< 0` → `<= 0` (L92, L111, L168) |
| `testReadUIntBEBufferUnderrun` | `readUIntBE` refuse un flux trop court | `FF FF FF` (avec `0xFF`, le mutant `\|` → `&` donne 255) | `BufferUnderrunException` (Javadoc) | 3e `\|` → `&` (L111) |
| `testReadUE7ZeroAfterContinuation` | un `0x00` après une continuation est lu | `81 00` | (1 << 7) + 0 = 128 | `>= 0` → `> 0` (L235) |
| `testReadUE7SingleZero` | 0 sur un seul octet | `00` | 0, sans `IOException` | `< 0` → `<= 0` (L246) |
| `testReadUShortLEAndBE` | ordre des octets, valeur non signée | `FF 80` | LE 0x80FF = 33023, BE 0xFF80 = 65408 | calcul de `readUShortLE/BE` (non couverts avant) |
| `testReadShortLEAndBENegative` | version signée | `FF 80` | -32513 et -128 (complément à deux) | `readShortLE/BE` (non couverts avant) |
| `testReadIntLEAndBE` | ordre de 4 octets et signe | `01 02 03 84` (inversé pour BE) | 0x84030201 = -2080112127 | calcul de `readIntLE/BE` (non couverts avant) |
| `testReadLongLEAndBE` | ordre de 8 octets | `01` à `07` puis `88` | 0x8807060504030201 | calcul de `readLongLE/BE` (non couverts avant) |
| `testReadLongLEAndBEBit31` | le bit 31 n'est pas étendu en signe (le code caste exprès en `long`) | seul l'octet du bit 31 vaut `80` | 2147483648 | décalages de l'octet du bit 31 dans `readLong*` |
| `testReadMethodsBufferUnderrun` | toutes les méthodes refusent un flux trop court | un octet de moins que nécessaire | `BufferUnderrunException` | dernier `\|` → `&` et négation de la condition de fin de flux |
| `testReadMethodsAllZeros` | octets nuls pour les 6 autres méthodes `read*` | que des `00` | 0 | `< 0` → `<= 0` dans les 6 méthodes |
| `testReadMethodsDetectMissingByteAtAnyPosition` | un octet manquant est détecté à n'importe quelle position | flux maison qui renvoie -1 au milieu puis encore des octets | `BufferUnderrunException` | les autres `\|` → `&` des conditions de fin de flux (22) |
| `testUnsignedConversionsWithNegativeBytes` | octets négatifs | `0xFF` | 255 (et 4294967295 pour 4 octets) | `getUByte`, `getUIntLE(byte[])` (non couverts avant) |
| `testIOExceptionIsPropagated` | une erreur du flux remonte à l'appelant | flux qui lance toujours `IOException` | `IOException` | aucun en particulier (vérifie la propagation de l'exception) |

Pour `testReadMethodsDetectMissingByteAtAnyPosition`, on pensait au départ que les mutants `|` → `&` dans `(ch1 | ch2 | ch3 | ch4) < 0`
étaient équivalents, parce qu'avec un `ByteArrayInputStream`, après -1 on a toujours -1. Avec un flux écrit à la
main, on a pu les tuer. Au premier passage avec 14 tests on était à 85 % ; ce test et `testReadMethodsAllZeros` nous ont amenés à 99 %. Les 2 mutants restants sont les mutants équivalents `i++` → `i--`.

### 6.2 `MediaType` : [`MediaTypeManualTest.java`](tika-core/src/test/java/org/apache/tika/mime/MediaTypeManualTest.java) (14 tests)

| Test | Intention | Données | Oracle | Mutants visés |
|---|---|---|---|---|
| `compareToOrdersBySubtype` | ordre de `compareTo` | `text/html` vs `text/plain` (seul le sous-type change) | signe seulement (contrat de `Comparable`) | L429 |
| `equalsIsFalseForDifferentMediaTypes` | types différents | même type, sous-types différents | `false` | L418 |
| `equalsIsFalseForNonMediaType` | autre classe et `null` | `"text/plain"` (String) et `null` | `false` (contrat de `equals`) | L420 |
| `getBaseTypeRemovesParameters` | retire les paramètres | `text/plain; charset=UTF-8` | `text/plain` (Javadoc) | L367, L370 |
| `parseReturnsCachedInstanceForSimpleTypes` | cache des types simples | même type analysé deux fois | même objet (`assertSame`) | L260, L293 |
| `parseRejectsEmptyTypeOrSubtype` | type ou sous-type vide | `"text/"`, `"/plain"` | `null` (RFC 2045) | L293 |
| `parseHandlesCharsetFirst` | format « charset d'abord » (TIKA-350) | `"charset=UTF-8; text/plain"` | `text/plain; charset=UTF-8` | L277, L278 |
| `setOfStringsSkipsInvalidTypes` | `set(String...)` ignore l'invalide | 2 types valides + `"pas-un-type"` (pas de `/`) | ensemble de taille 2 | L230, L234 |
| `setOfMediaTypesSkipsNull` | `set(MediaType...)` ignore `null` | 2 constantes + `null` | ensemble de taille 2 | L211, L215 |
| `addParameterToTypeWithoutParameters` | ajout d'un paramètre | `TEXT_PLAIN` + `charset` | Javadoc du constructeur | L350 |
| `addNoParameterKeepsBaseParameters` | `Map` vide | type avec `charset` + `Map` vide | paramètres inchangés | L349, L352 |
| `addParameterMergesWithBaseParameters` | fusion des paramètres | `charset` + `format=flowed` | les deux, triés | L351, L355-357 |
| `parseCachesNamesWithEveryAllowedCharacter` | tous les caractères permis | `application/x-0az9+._` + UUID (les bornes et symboles de `isSimpleName`, avec un nom jamais vu) | même objet | L288, L289 |
| `parseExtractsParameterInsteadOfTreatingAsSimpleName` | un type avec paramètre n'est pas un nom simple | `text/plain; format=` + UUID | paramètre extrait | L288 |

Certains mutants de `isSimpleName` ne changent pas la valeur retournée, seulement le fait que l'objet est mis en
cache. Le seul moyen de les tuer est `assertSame`. Et comme `SIMPLE_TYPES` est statique et que PIT réutilise la
JVM, une chaîne déjà mise en cache par un autre test faussait le résultat. On utilise donc un UUID pour avoir un
nom jamais vu. C'est aussi ce qui explique l'écart entre 54 % et 59 % mentionné à la section 1.

Résultat : 84/85. Il y a un mutant `TIMED_OUT` (boucle infinie dans `parseParameters`), que PIT compte comme
détecté. Le dernier mutant est équivalent : à la ligne 257, PIT remplace `return null` par `return null`. À cause
du bloc `synchronized`, il ne voit pas que la valeur est déjà `null`.

## 7. GitHub Action

Le workflow [`.github/workflows/tache2-tests.yml`](.github/workflows/tache2-tests.yml) compile `tika-core`, lance les 130 tests (`EndianUtils*Test` et
`MediaType*Test`) avec Checkstyle activé, puis PIT, et publie les rapports. Il passe sur `main`.

| | Originaux | IA (corrigés) | Nos tests | Total |
|---|---|---|---|---|
| `EndianUtils` | 4 | 53 | 16 | 73 |
| `MediaType` | 9 | 34 | 14 | 57 |

On a retiré de notre fork les workflows d'origine de Tika (builds complets sur plusieurs JDK, Docker), qui
prenaient plus d'une heure et n'avaient rien à voir avec la tâche.

## 8. Conclusion et limites

Les tests générés font monter la couverture et le score de mutation, mais leurs oracles ne sont pas fiables :
28 % et 40 % des tests échouaient sur du code correct. ChatUniTest a aussi des angles morts (constructeurs,
méthodes privées, lecture sur flux). Les tests qu'on a écrits à la main sont moins nombreux mais beaucoup plus
efficaces : avec eux, on arrive à 99 % sur les deux classes, et les 3 mutants restants sont équivalents.

Quelques limites : la génération n'est pas reproductible (une autre exécution donnerait d'autres tests, c'est
pour ça qu'on a gardé les versions brutes et les journaux), on n'a pas corrigé les deux classes de la même façon,
on a utilisé un petit modèle faute de GPU, et certaines vraies erreurs (comme oublier `& 0xFF`) ne sont pas
générées par les mutateurs par défaut de PIT.

## 9. Utilisation de l'IA

Les tests `*_Test.java` ont été générés par ChatUniTest avec `qwen2.5-coder:7b`, comme demandé dans l'énoncé.
On a aussi utilisé Claude (Anthropic) comme assistant pendant le travail : pour comprendre les outils et les
erreurs, configurer Maven, PIT et GitHub Actions, analyser les mutants et proposer des tests .
On a vérifié les résultats en lançant nous-mêmes les tests et PIT, et tous les chiffres viennent de nos exécutions.
On a aussi fait la réduction et la documentation par nous mêmes. 
