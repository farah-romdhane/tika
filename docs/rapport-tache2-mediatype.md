# Tâche 2 : génération de tests et analyse de mutation pour `MediaType`

Classe étudiée : `org.apache.tika.mime.MediaType` (module `tika-core`).
Elle représente un type MIME (`type/sous-type; paramètres`) et fournit son analyse (`parse`), ses accesseurs, l'égalité, la comparaison et la construction à partir d'un type de base.

## 1. Démarche

| Étape | Tests exécutés par PIT | Rapport |
|---|---|---|
| PIT n°1 | tests originaux (`MediaTypeTest`) | `docs/pit/mediatype-avant` |
| Génération | ChatUniTest + modèle local via Ollama | `tika-core/chatunitest-tests/` |
| Correction minimale | tests générés rendus exécutables | `src/test/java/org/apache/tika/mime/MediaType_*_Test.java` |
| PIT n°2 | originaux + générés corrigés | `docs/pit/mediatype-apres-llm` |
| Tests manuels | tests ciblant les mutants restants | `MediaTypeManualTest.java` |
| PIT n°3 | originaux + générés corrigés + manuels | `docs/pit/mediatype-final` |

Chaque groupe de tests est dans ses propres fichiers. On active les groupes un par un dans `<targetTests>` du `pom.xml`, ce qui permet de mesurer séparément l'apport de chaque source de tests (humain d'origine, IA, ajout manuel).

## 2. Génération avec ChatUniTest

### Configuration

- Outil : `chatunitest-maven-plugin` 2.1.1
- Modèle : `qwen2.5-coder:7b` (alias `code-llama`), exécuté localement via Ollama, contexte de 8192 tokens
- Paramètres : `testNumber=1`, `maxRounds=3`, `maxPromptTokens=3000`, `temperature=0.2`
- Matériel : laptop sans GPU, durée totale de **37 min 28 s**

### Où sont les tests générés ?

- Tests bruts, tels que produits par le LLM : `tika-core/chatunitest-tests/org/apache/tika/mime/` (branche `generation-mediatype`)
- Journal de génération : `tika-core/chatunitest-mediatype-log.txt`
- Messages d'erreur de compilation et d'exécution (copiés depuis `/tmp`) : `docs/chatunitest/mediatype-erreurs/`
- Versions corrigées, utilisées par PIT : `tika-core/src/test/java/org/apache/tika/mime/`

### Méthodes traitées

Le journal liste 27 méthodes et constructeurs. ChatUniTest en a **traité 11** et en a **ignoré 16** :

- les 6 constructeurs ;
- les accesseurs simples `getBaseType`, `getType`, `getSubtype`, `getParameters` ;
- les méthodes privées `isSimpleName`, `parseParameters`, `unquote`, `union` ;
- les deux surcharges de `set(...)`.

Pour `equals`, la première version ne compilait pas, et les deux tentatives de réparation ont été abandonnées (`Exceed max prompt tokens`). Aucun test n'a donc été produit pour cette méthode.

### Les tests compilent-ils et s'exécutent-ils sans intervention ?

Ils compilent : seul `application` a eu besoin de 2 rondes de réparation automatique pour compiler. En revanche, **ils ne passent pas tous**. ChatUniTest affiche « compile and execute successfully » même quand des tests échouent, il ne faut donc pas se fier à ce message.

Sur les 48 tests générés, **29 passent et 19 échouent** (exécution avec JUnit Platform Console 6.1.3).

| Fichier | Tests | Échecs | Cause |
|---|---|---|---|
| application | 6 | 2 | exceptions attendues mais jamais levées |
| audio | 1 | 1 | erreur d'environnement Mockito (voir ci-dessous) |
| image | 5 | 2 | exceptions attendues mais jamais levées |
| text | 1 | 1 | oracle inventé (`"text/"` attendu, `null` obtenu) |
| video | 1 | 1 | exceptions attendues mais jamais levées |
| parse | 17 | 11 | « type invalide » qui est en fait valide ; gestion des guillemets mal comprise |
| compareTo | 1 | 1 | `-1` attendu, `8` obtenu |
| hasParameters | 10 | 0 | |
| hashCode | 5 | 0 | |
| toString | 1 | 0 | |

### Problèmes d'environnement rencontrés

- **Surefire** détecte automatiquement le fournisseur JUnit 4 (`JUnit4Provider`) et exécute 0 test avec `mvn test`. Les tests ont donc été lancés avec le lanceur JUnit Platform Console. PIT n'est pas touché, car il utilise `pitest-junit5-plugin`.
- **Mockito** : le test `audio` déclare un `@Mock` sur `MediaType`, ce qui fait échouer l'initialisation de Byte Buddy (`Unknown Java version: 21`). Le classpath contient `byte-buddy-1.10.20` et `mockito-junit-jupiter-3.8.0`, amenés par `chatunitest-starter`, des versions antérieures au support de Java 21.

## 3. Correction minimale

PIT refuse de s'exécuter si un test échoue déjà sur le code non muté (*« Mutation testing requires a green suite »*). Les tests générés ont donc été rendus exécutables avec une règle explicite : **on retire uniquement les assertions fausses, sans rien ajouter et sans remplacer un oracle faux par le comportement observé**. Le score du PIT n°2 mesure ainsi ce que les tests de l'IA détectent par eux-mêmes.

Chaque décision a été vérifiée dans le code de `MediaType` à partir du message d'échec réel. Elle est marquée dans les fichiers par un commentaire `[CORRECTION Cn]`.

Trois faits du code expliquent presque tous les échecs :

1. `application(x)`, `audio(x)`, `image(x)`, `text(x)` et `video(x)` appellent seulement `parse("type/" + x)` ;
2. `parse` ne lève jamais d'exception : il retourne un `MediaType` ou `null`. `""` donne `null`, et un sous-type `null` produit la chaîne valide `"text/null"` ;
3. le constructeur ne valide rien : il fait seulement `trim()` et `toLowerCase()`.

| Type de correction | Nombre | Détail |
|---|---|---|
| Assertions retirées (le test est conservé) | 15 | application (7), audio (2), text (3), video (3) |
| Tests supprimés (leur seule assertion était fausse) | 14 | image (2), parse (11), compareTo (1) |
| Nettoyage d'environnement | 1 | `@Mock` inutilisé retiré dans audio |
| Fichier non-test retiré | 1 | `MediaType_Suite.java`, qui aurait exécuté les tests deux fois |
| Fichiers inchangés | 3 | hasParameters, hashCode, toString |

Après correction : **34 tests, tous verts**.

## 4. Comparaison qualitative des oracles

**Oracles hallucinés.** C'est l'erreur dominante. Le LLM suppose une validation stricte qui n'existe pas : 16 assertions `assertThrows` attendent des `IllegalArgumentException` ou des `NullPointerException` que `MediaType` ne lève jamais.

**Bons cas limites, mauvais oracles.** Le LLM a souvent visé les bons cas : sous-type vide, entrée `null`, ordre de `compareTo`, valeurs entre guillemets. Mais il en a deviné le résultat au lieu de le déduire du code. Par exemple, il attend `-1` pour `compareTo` alors que seul le signe est spécifié. Ces cas limites ont dû être supprimés à la correction minimale, puis ont été réécrits à la main avec le bon oracle (section 6).

**Doublons.** Dans `parse`, 8 tests sont strictement identiques (`...Whitespace2` à `...Whitespace8`). Dans `hasParameters`, il y a 10 tests mais seulement 5 cas distincts.

**Absence d'oracle.** `testHashCodeWithEmptyType` construit un objet sans aucune assertion : il passe toujours.

**Test qui passe pour une mauvaise raison.** `testImageMethodWithNullParameter` appelle `imageMethod.invoke(instance, null)`, que Java interprète comme « aucun argument ». L'`IllegalArgumentException` attendue vient donc de la réflexion (mauvais nombre d'arguments), pas de `MediaType`.

**Complexité inutile.** La réflexion sert à appeler des méthodes publiques (`image`, `hashCode`), un mock n'est jamais utilisé, et des imports Mockito sont présents dans tous les fichiers.

**Points positifs.** `toString`, `hasParameters`, les accesseurs de `application` et les cas simples de `parse` vérifient de vraies valeurs avec `assertEquals`, `assertTrue` et `assertFalse`.

Les tests écrits à la main dans `MediaTypeTest` vérifient au contraire des valeurs précises, déduites de la spécification. Le PIT n°1 le confirme : 96 % de force sur le code qu'ils exécutent.

## 5. Résultats PIT : n°1, n°2 et n°3

| | Couverture de lignes | Score de mutation | Force des tests | Survivants | Sans couverture |
|---|---|---|---|---|---|
| **PIT n°1** (originaux) | 68 % (107/157) | 59 % (50/85) | 96 % (50/52) | 2 | 33 |
| **PIT n°2** (+ LLM corrigés) | 79 % (124/157) | 75 % (64/85) | 94 % (64/68) | 4 | 17 |
| **PIT n°3** (+ manuels) | 99 % (154/156) | **99 % (84/85)** | 99 % (84/85) | 1 | 0 |

Le total de lignes passe de 157 à 156 après la recompilation complète (`mvn clean`) qui a précédé le PIT n°3. Le nombre de mutants (85) est le même pour les trois analyses.

### PIT n°1 : des tests originaux forts, mais incomplets

La force de 96 % montre que les tests originaux détectent presque tout ce qu'ils exécutent : seuls 2 mutants survivent dans le code couvert. Le score de 59 % vient surtout des **33 mutants jamais atteints** (`NO_COVERAGE`). Le problème de `MediaTypeTest` est donc ce qu'il ne teste pas, pas la faiblesse de ses oracles.

### PIT n°2 : un apport réel mais limité de l'IA

Les tests générés couvrent 17 lignes de plus et tuent **14 mutants de plus** (+16 points). En revanche, la force baisse de 96 % à 94 % : dans le code nouvellement couvert, ils tuent 14 mutants sur 16. Ils exécutent donc du code sans toujours en vérifier le comportement, ce qui est cohérent avec les oracles faibles décrits à la section 4.

Il restait alors 21 mutants : 17 dans du code jamais exécuté et 4 survivants. Ces trous ont trois origines :

- les méthodes ignorées par ChatUniTest : les 7 mutants de `union`, accessible seulement via les constructeurs, et ceux de `set(...)` ;
- les cas limites retirés à la correction minimale (sous-type vide, `compareTo`) ;
- l'absence de tests négatifs pour `equals` et `getBaseType` avec paramètres.

### PIT n°3 : tests manuels

14 tests manuels tuent **20 mutants supplémentaires**, pour un score de 84/85. Une première version de 12 tests donnait 82/85. Deux mutants de `isSimpleName` (ligne 288) survivaient à cause du cache statique (voir 6.2), et deux tests ont été ajoutés pour les tuer.

Le mutant `TIMED_OUT` de la ligne 305 (`parseParameters`, frontière de `while (string.length() > 0)`) provoque une boucle infinie. PIT le compte comme détecté.

## 6. Tests ajoutés manuellement

Fichier : `tika-core/src/test/java/org/apache/tika/mime/MediaTypeManualTest.java`.
Chaque test est documenté dans sa Javadoc (intention, données, oracle, mutants visés).

| Test | Intention | Données et motivation | Oracle | Mutants tués |
|---|---|---|---|---|
| `compareToOrdersBySubtype` | `compareTo` ordonne selon la forme textuelle | `text/html` et `text/plain` ne diffèrent que par le sous-type (`h` < `p`) | contrat de `Comparable` : signe seulement (`< 0`, `> 0`, `== 0`) | L429 |
| `equalsIsFalseForDifferentMediaTypes` | deux types différents ne sont pas égaux | même type, sous-types différents | `false` | L418 |
| `equalsIsFalseForNonMediaType` | `equals` rejette un autre type et `null` | la chaîne `"text/plain"` (même texte) et `null` | contrat de `Object.equals` : `false` | L420 |
| `getBaseTypeRemovesParameters` | `getBaseType` retire les paramètres | `text/plain; charset=UTF-8` | Javadoc de `getBaseType` : `text/plain` | L367, L370 |
| `parseReturnsCachedInstanceForSimpleTypes` | `parse` réutilise l'instance en cache | un type simple analysé deux fois, et la constante `TEXT_PLAIN` | Javadoc de `SIMPLE_TYPES` : même objet (`assertSame`) | L260, L293 |
| `parseRejectsEmptyTypeOrSubtype` | un type ou un sous-type vide est rejeté | `"text/"` et `"/plain"` | Javadoc de `parse` et RFC 2045 : `null` | L293 |
| `parseHandlesCharsetFirst` | format « charset en premier » (TIKA-350) | `"charset=UTF-8; text/plain"`, envoyé par certains serveurs | Javadoc de `parse` : `text/plain; charset=UTF-8` | L277, L278 |
| `setOfStringsSkipsInvalidTypes` | `set(String...)` ignore les chaînes invalides | deux types valides et `"pas-un-type"` (sans `/`) | ensemble de taille 2 contenant les deux types valides | L230, L234 |
| `setOfMediaTypesSkipsNull` | `set(MediaType...)` ignore `null` | deux constantes et `null` | ensemble de taille 2 | L211, L215 |
| `addParameterToTypeWithoutParameters` | ajout d'un paramètre à un type sans paramètre | `TEXT_PLAIN` + `charset=UTF-8` | Javadoc du constructeur | L350 |
| `addNoParameterKeepsBaseParameters` | une `Map` vide conserve les paramètres de base | type avec `charset` + `Map` vide | l'union avec un ensemble vide ne change rien | L349, L352 |
| `addParameterMergesWithBaseParameters` | fusion des deux ensembles de paramètres | `charset` + `format=flowed` (clés distinctes) | les deux présents, triés par nom | L351, L355, L356, L357 |
| `parseCachesNamesWithEveryAllowedCharacter` | chaque caractère autorisé passe par le cache | `application/x-0az9+._` + UUID : les bornes et symboles de `isSimpleName`, avec un nom jamais vu | même objet (`assertSame`) | L288, L289 |
| `parseExtractsParameterInsteadOfTreatingAsSimpleName` | un type avec paramètre n'est pas un nom simple | `text/plain; format=` + UUID | la valeur du paramètre est extraite | L288 |

### 6.1 Mutants « quasi équivalents » liés au cache

Plusieurs mutants (`isSimpleName` qui retourne `false`, `slash + 1` remplacé par `slash - 1`, les bornes de la ligne 288) ne changent **aucune valeur retournée**. `parse` passe alors par la regex au lieu du raccourci et produit un objet égal. La seule différence observable est que l'objet n'est plus mis en cache dans `SIMPLE_TYPES`. Le seul oracle capable de les tuer est donc `assertSame`.

Ce choix teste un détail d'implémentation, mais il est justifié : le cache est un comportement documenté de la classe, dont le but est d'éviter d'avoir trop d'instances de `MediaType` en mémoire.

### 6.2 Sensibilité à l'ordre d'exécution

`SIMPLE_TYPES` est **statique**, et PIT exécute plusieurs mutants dans la même JVM. Une chaîne déjà mise en cache par un test précédent rend `assertSame` vrai même sur le code muté. Avec des chaînes fixes, le résultat dépend donc de l'ordre d'exécution. C'est ce qui explique que deux mutants de la ligne 288 aient survécu au premier PIT n°3.

La correction consiste à construire un nom unique à chaque exécution (`UUID.randomUUID()`). Le résultat attendu ne dépend pas de la valeur générée, donc le test reste déterministe.

## 7. Pourquoi pas 85/85 : le mutant équivalent de la ligne 257

```java
synchronized (SIMPLE_TYPES) {
    MediaType type = SIMPLE_TYPES.get(string);
    if (type == null) {
        int slash = string.indexOf('/');
        if (slash == -1) {
            return null;            // ligne 257
        }
        ...
```

Le dernier mutant est un `NullReturnValsMutator` sur la ligne 257 : PIT remplace la valeur retournée par `null`. Mais cette ligne retourne **déjà** `null`. Le code muté se comporte exactement comme l'original : c'est un **mutant équivalent**, qu'aucun test ne peut tuer.

Ce n'est pas un problème de couverture : la ligne 257 est bien exécutée, par exemple par `parse("")` dans les tests générés et par `"pas-un-type"` dans `setOfStringsSkipsInvalidTypes`. D'ailleurs, la force de 84/85 indique que les 85 mutants sont tous couverts. Le mutant survit parce qu'il ne change rien, pas parce qu'il n'est jamais atteint.

PIT génère ce mutant à cause du bloc `synchronized`. Pour retourner depuis l'intérieur du bloc, le compilateur range la valeur dans une variable locale, libère le verrou (`monitorexit`), puis retourne cette variable. Au niveau du bytecode, PIT voit seulement « retourner une variable locale » et ne peut pas savoir qu'elle contient la constante `null`.

On pourrait le faire disparaître en déplaçant le test `slash == -1` hors du bloc `synchronized`, ou en l'excluant de l'analyse. Mais cela reviendrait à modifier le code étudié ou à masquer le résultat. Le score de **84/85 (99 %)** est donc le maximum atteignable sur cette classe telle qu'elle est écrite.

## 8. Synthèse

| | Tests ajoutés | Mutants tués en plus | Coût |
|---|---|---|---|
| LLM (après correction) | 34 (sur 48 générés) | +14 | 37 min de génération, 30 corrections manuelles |
| Tests manuels | 14 | +20 | analyse ciblée de chaque mutant |

1. **Les tests originaux sont de bonne qualité mais incomplets** : 96 % de force, mais un tiers du code n'est jamais exécuté.
2. **L'IA augmente la couverture, mais avec des oracles peu fiables.** 40 % des tests générés (19/48) échouaient sur un code correct, presque toujours à cause d'un oracle halluciné. Après correction, ils apportent +16 points de score de mutation, avec une force inférieure à celle des tests humains.
3. **ChatUniTest laisse des angles morts structurels** : il ignore les constructeurs, les accesseurs, les méthodes privées et `set(...)`. Une bonne partie des mutants restants se trouvait justement là, en particulier les 7 mutants de `union`.
4. **Le LLM sait où chercher, mais pas quoi vérifier.** Il a choisi de bons cas limites (sous-type vide, `compareTo`, guillemets), mais en a inventé le résultat. Un humain qui part de ces mêmes cas et dérive l'oracle du code ou de la spécification obtient des tests utiles.
5. **Les tests manuels ciblés sont plus efficaces** : 14 tests tuent 20 mutants, contre 34 tests pour 14 mutants côté LLM. Ils portent le score de 59 % à 99 %, et le seul mutant restant est équivalent.
6. **Le score de mutation a ses propres pièges** : des mutants équivalents (ligne 257), des mutants seulement observables via un détail d'implémentation (le cache), et des résultats qui dépendent de l'ordre d'exécution quand la classe a un état statique.
