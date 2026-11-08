# IFT3913 – Tâche 3

| Nom complet | Identifiant GitHub |
|---|---|
| ROMDHANE Farah | farah-romdhane |
| KAISSI Ayman | Ayman4401 |

Tout le travail de la tâche 3 est sur la branche **`tache3`** de ce fork. La branche `main` contient toujours la
tâche 2 (son rapport est dans le `README.md` de `main`). Le README d'origine de Tika est dans
[`README-tika.md`](README-tika.md).

En résumé :

| Élément | Où |
|---|---|
| Workflow modifié (tests + Semgrep + rickroll) | [`.github/workflows/tache2-tests.yml`](.github/workflows/tache2-tests.yml) |
| Action réutilisable qui rickroll | [`.github/actions/rickroll/action.yml`](.github/actions/rickroll/action.yml) |
| Rapports Semgrep avant / après | [`docs/semgrep/`](docs/semgrep/) |
| Captures d'écran | [`docs/semgrep/captures/`](docs/semgrep/captures/) |
| Code modifié (7 vulnérabilités) | 5 fichiers de `tika-core`, voir la section 4 |

| | Avant | Après |
|---|---|---|
| Alertes Semgrep dans `tika-core` | 3 | 11 |
| Nouvelles alertes (font échouer le build) | 0 | 8 |
| Résultat du workflow | Vert | Rouge (voulu) |

## 1. Semgrep sur Tika avant nos modifications

On a lancé Semgrep 1.183.0 (image Docker `semgrep/semgrep`) sur `tika-core/src/main/java`, avec les règles
`p/java` (sécurité Java) et `p/secrets` (mots de passe, clés et jetons écrits dans le code). Le rapport est dans
[`semgrep-avant.txt`](docs/semgrep/semgrep-avant.txt) et [`semgrep-avant.json`](docs/semgrep/semgrep-avant.json).

Semgrep trouve 3 alertes. On a regardé le code à chaque endroit :

| Fichier (ligne) | Règle | Notre avis |
|---|---|---|
| `mime/MimeTypesReader.java` (223) | `saxparserfactory-disallow-doctype-decl-missing` (XXE) | Faux positif |
| `parser/NetworkParser.java` (68) | `unencrypted-socket` | Vrai, mais voulu |
| `utils/XMLReaderUtils.java` (307) | `xmlinputfactory-possible-xxe` (XXE) | Faux positif |

- **`MimeTypesReader`** : Semgrep cherche seulement l'option `disallow-doctype-decl`. Mais le code active
  `FEATURE_SECURE_PROCESSING` et la classe redéfinit `resolveEntity()` pour renvoyer un flux vide, donc une entité
  externe n'est jamais lue.
- **`NetworkParser`** : le socket n'est pas chiffré, mais il n'est utilisé que pour les URI `telnet`, un protocole
  qui n'est jamais chiffré. Les autres URI passent par `URLConnection`, qui accepte `https`.
- **`XMLReaderUtils`** : les entités externes sont bien désactivées quelques lignes plus bas
  (`IS_SUPPORTING_EXTERNAL_ENTITIES` et `SUPPORT_DTD` à `false`), mais par une méthode utilitaire
  (`tryToSetStaxProperty`) que Semgrep ne suit pas.

Le code de départ est donc plutôt propre. Ces 3 alertes ne doivent pas bloquer le build : seules les nouvelles
vulnérabilités doivent le faire.

## 2. Modification du workflow GitHub Actions

### 2.1 Ce qu'on a changé

Le workflow du cas d'étude est celui qu'on utilisait déjà pour la tâche 2 (`tache2-tests.yml` : compilation de
`tika-core`, tests, PIT). Les workflows d'origine de Tika avaient été retirés du fork pendant la tâche 2, parce
qu'ils prenaient plus d'une heure. On a renommé ce workflow « CI Tika - tests et Semgrep » et on lui a ajouté un
deuxième job, `semgrep` :

```yaml
semgrep:
  runs-on: ubuntu-latest
  container:
    image: semgrep/semgrep
  steps:
    - uses: actions/checkout@v4
      with:
        fetch-depth: 0
    - run: git config --global --add safe.directory "$GITHUB_WORKSPACE"
    - name: Semgrep - nouvelles vulnerabilites seulement
      run: >
        semgrep scan --config p/java --config p/secrets
        --baseline-commit "$(git rev-parse semgrep-base)"
        --error --text-output=semgrep-nouvelles.txt
        tika-core/src/main/java
    - name: Semgrep - rapport complet
      if: always()
      run: >
        semgrep scan --config p/java --config p/secrets
        --json-output=semgrep-complet.json --text-output=semgrep-complet.txt
        tika-core/src/main/java
    # + upload des rapports comme artifact
```

Si le job `semgrep` échoue, tout le run du workflow est marqué en échec, même si les tests passent.

### 2.2 Nos choix

- **Échouer seulement sur les nouvelles vulnérabilités.** Si on lançait juste `semgrep --error`, le build
  échouerait tout de suite à cause des 3 alertes déjà présentes dans Tika. On a utilisé le mode *diff-aware* de
  Semgrep : on a mis un tag Git `semgrep-base` sur le commit de départ, et `--baseline-commit` compare chaque
  commit à ce tag. Semgrep ne garde que les alertes qui n'existaient pas au moment du tag.
- **Un tag fixe plutôt que le commit précédent.** Si on comparait au commit précédent, une vulnérabilité ajoutée
  dans un commit disparaîtrait au commit suivant et le build redeviendrait vert. Avec un tag fixe, le build reste
  rouge tant que la vulnérabilité est dans le code.
- **`--error`** : Semgrep renvoie le code de sortie 1 s'il trouve au moins une alerte, ce qui fait échouer l'étape.
  Sans cette option, Semgrep termine toujours avec 0.
- **Un job séparé des tests.** Les deux jobs tournent en parallèle, et dans le résumé du run on voit tout de suite
  si l'échec vient des tests ou de la sécurité.
- **L'image Docker `semgrep/semgrep`** : Semgrep est déjà installé, pas besoin de Python ni de `pip`.
- **Les règles `p/java` et `p/secrets`** : ensembles officiels de Semgrep pour Java et pour les secrets. Ils ne
  gardent que les règles de confiance moyenne ou haute, donc peu de faux positifs (voir la section 5 pour la
  conséquence).
- **Un 2ᵉ scan complet** (`if: always()`) qui ne bloque rien, pour garder un rapport de toutes les alertes. C'est
  ce rapport qui sert pour la comparaison avant / après.

Deux problèmes techniques qu'on a dû régler :

- `actions/checkout` ne récupère par défaut que le dernier commit, donc Semgrep ne trouvait pas le tag. D'où
  `fetch-depth: 0`.
- Dans le conteneur Docker, Git refuse de travailler dans un dossier qui appartient à un autre utilisateur
  (*dubious ownership*). La commande `safe.directory` règle ça.

Au début, on avait mis Semgrep dans un workflow à part (`tache3-semgrep.yml`). On l'a ensuite déplacé dans le
workflow existant, puisque l'énoncé demande de modifier le workflow du cas d'étude. Le contenu des étapes n'a pas
changé.

### 2.3 Validation

On a testé le workflow avec des commits faits exprès :

| Run | Commit | Attendu | Obtenu |
|---|---|---|---|
| Tache 3 - Semgrep #4 | ajout d'une classe `ExempleHash` qui utilise MD5 | rouge | rouge : 1 alerte `use-of-md5`, `exit code 1` |
| Tache 3 - Semgrep #5 | suppression de cette classe | vert | vert |
| CI Tika #44 | les 7 vulnérabilités dans le code de Tika, Semgrep dans le workflow des tests | tests verts, semgrep rouge | tests ✅, semgrep ❌ (8 alertes), run en échec |

![Commit avec MD5 : rouge](docs/semgrep/captures/ga-rouge-md5.png)

![Retrait du MD5 : vert](docs/semgrep/captures/ga-vert.png)

![Run final : tests verts, Semgrep rouge](docs/semgrep/captures/ci-rouge-semgrep.png)

Dans les logs, on voit `Scanning 1 file tracked by git` pour le run #4 : en mode baseline, Semgrep n'analyse que
les fichiers modifiés depuis le tag. Les 3 alertes de départ n'apparaissent jamais dans le scan qui bloque, ce qui
montre que la comparaison avec le tag fonctionne.

## 3. Choix des vulnérabilités

On voulait 7 types différents (7 CWE différents) que les règles `p/java` / `p/secrets` détectent vraiment. Pour
chaque faille, on a ajouté **une nouvelle méthode** dans une classe existante de `tika-core` dont le rôle a un lien
avec la faille. On n'a pas modifié le code qui existait déjà, pour ne pas changer le comportement de Tika : les
tests de la tâche 2 passent toujours (job `tests` vert).

| # | Fichier modifié | Méthode ajoutée | Vulnérabilité | CWE | Règle Semgrep |
|---|---|---|---|---|---|
| 1 | `metadata/Database.java` | `METADATA_DB_URL` | Identifiants écrits dans l'URL | 798 | `detected-username-and-password-in-uri` |
| 2 | `metadata/Database.java` | `countRows` | Injection SQL | 89 | `formatted-sql-string` |
| 3 | `digest/DigestHelper.java` | `quickFingerprint` | Hachage faible (MD5) | 328 | `use-of-md5` |
| 4 | `detect/XmlRootExtractor.java` | `extractRootElementWithDom` | XXE | 611 | `documentbuilderfactory-disallow-doctype-decl-missing` |
| 5 | `parser/CryptoParser.java` | `encryptForCache` | Chiffrement cassé (DES en mode ECB) | 327 | `des-is-deprecated`, `ecb-cipher` |
| 6 | `parser/CryptoParser.java` | `generateTestKeyPair` | Clé trop courte (RSA 512 bits) | 326 | `use-of-weak-rsa-key` |
| 7 | `io/TikaInputStream.java` | `getFromSocket` | Données en clair sur le réseau | 319 | `unencrypted-socket` |

## 4. Détail de chaque vulnérabilité

### 4.1 Identifiants dans l'URL de connexion (CWE-798)

**Fichier** : `metadata/Database.java`. Cette interface définit les métadonnées que Tika extrait des bases de
données (nom de table, nombre de lignes…). C'est un endroit naturel pour une connexion à une base.

```java
String METADATA_DB_URL = "jdbc:postgresql://tikaadmin:Tika2026!@localhost:5432/tika";
```

**Pourquoi c'est vulnérable** : le nom d'utilisateur et le mot de passe sont dans le code source. Toute personne
qui a accès au dépôt (ici, il est public) peut se connecter à la base, et changer le mot de passe demande de
modifier le code. Il faudrait les lire dans une variable d'environnement ou un fichier de configuration privé.

**Semgrep** : `detected-username-and-password-in-uri`, ligne 36. Semgrep masque lui-même le mot de passe dans
son rapport (`jdbc:***localhost…`).

### 4.2 Injection SQL (CWE-89)

**Fichier** : `metadata/Database.java`, même thème.

```java
ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM " + tableName)
```

**Pourquoi c'est vulnérable** : `tableName` est collé directement dans la requête. Si un appelant passe
`documents; DROP TABLE documents`, la base exécute les deux commandes. Il faudrait valider le nom de table (une
liste de noms permis), puisqu'un nom de table ne peut pas être passé en paramètre d'un `PreparedStatement`.

**Semgrep** : `formatted-sql-string`, ligne 44.

### 4.3 Hachage MD5 (CWE-328)

**Fichier** : `digest/DigestHelper.java`, la classe de Tika qui calcule les empreintes (*digests*) des documents.

```java
MessageDigest md = MessageDigest.getInstance("MD5");
```

**Pourquoi c'est vulnérable** : MD5 n'est plus résistant aux collisions. On peut fabriquer deux documents
différents avec la même empreinte, donc un document malveillant pourrait être pris pour un document déjà traité.
Il faudrait utiliser SHA-256, que Tika propose déjà dans sa configuration.

**Semgrep** : `use-of-md5`, ligne 112 (Semgrep propose même de remplacer par `SHA-512`).

### 4.4 XXE (CWE-611)

**Fichier** : `detect/XmlRootExtractor.java`, la classe qui lit un fichier XML pour trouver son élément racine et
détecter son type. Elle lit des fichiers qui viennent des utilisateurs.

```java
DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
DocumentBuilder builder = factory.newDocumentBuilder();
Document document = builder.parse(stream);
```

**Pourquoi c'est vulnérable** : le parseur accepte les `DOCTYPE` et les entités externes. Un fichier XML qui
contient `<!ENTITY x SYSTEM "file:///etc/passwd">` fait lire un fichier du serveur. La méthode d'origine de cette
classe passe par `XMLReaderUtils`, qui désactive ces fonctions. La nôtre ne le fait pas.

**Semgrep** : `documentbuilderfactory-disallow-doctype-decl-missing`, ligne 99.

### 4.5 DES en mode ECB (CWE-327)

**Fichier** : `parser/CryptoParser.java`, la classe de Tika qui déchiffre des documents.

```java
Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
```

**Pourquoi c'est vulnérable** : DES utilise une clé de 56 bits, qu'on peut retrouver par force brute. Le mode ECB
chiffre deux blocs identiques de la même façon, donc on voit des motifs dans le texte chiffré, et il n'y a aucune
vérification d'intégrité. Il faudrait utiliser AES en mode GCM.

**Semgrep** : 2 alertes sur la ligne 115, `des-is-deprecated` et `ecb-cipher`.

### 4.6 Clé RSA de 512 bits (CWE-326)

**Fichier** : `parser/CryptoParser.java`, même classe de chiffrement.

```java
KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
generator.initialize(512);
```

**Pourquoi c'est vulnérable** : une clé RSA de 512 bits peut être factorisée en quelques heures avec du matériel
ordinaire. Le NIST recommande au moins 2048 bits.

**Semgrep** : `use-of-weak-rsa-key`, ligne 124.

### 4.7 Socket non chiffré (CWE-319)

**Fichier** : `io/TikaInputStream.java`, la classe qui ouvre les flux à analyser. Elle sait déjà lire un fichier
ou une URL, donc une méthode qui lit depuis le réseau y a sa place.

```java
Socket socket = new Socket(host, port);
return get(socket.getInputStream());
```

**Pourquoi c'est vulnérable** : le document passe en clair sur le réseau. Quelqu'un sur le même réseau peut le
lire ou le modifier avant que Tika l'analyse. Il faudrait un `SSLSocket`. Ce type d'alerte existait déjà dans
`NetworkParser`, mais Semgrep compte bien la nôtre comme nouvelle, parce qu'elle est à un autre endroit.

**Semgrep** : `unencrypted-socket`, ligne 211.

## 5. Ce que Semgrep ne détecte pas

Notre premier essai contenait d'autres failles, mises dans des classes à part. Semgrep n'en détectait que 3 sur 7.
Il ne voyait pas :

| Code | Vulnérabilité | CWE |
|---|---|---|
| `new ProcessBuilder("sh", "-c", "pdftotext " + nomFichier)` | Injection de commande | 78 |
| `new ObjectInputStream(donnees).readObject()` | Désérialisation non sûre | 502 |
| `new Random().nextLong()` pour créer un jeton | Aléatoire prévisible | 330 |
| `MOT_DE_PASSE_SECRET = "q8Zr2…"` | Mot de passe dans une constante | 798 |

Les fichiers étaient bien analysés, mais sans alerte. Des règles existent pour ces cas dans le dépôt public de
Semgrep, mais ce sont des règles d'« audit » avec `confidence: LOW`, et `p/java` / `p/secrets` ne gardent que les
règles de confiance moyenne ou haute. C'est pour ça qu'on a choisi d'autres types pour les 7 vulnérabilités.

Ça montre une limite importante : un build vert ne veut pas dire que le code est sûr. Le résultat dépend beaucoup
des règles choisies. Un ensemble plus large (comme `p/security-audit`) détecterait plus de cas, mais avec plus de
faux positifs dans le code existant de Tika.

## 6. Comparaison avant / après

Rapports complets : [`semgrep-avant.txt`](docs/semgrep/semgrep-avant.txt) et
[`semgrep-apres.txt`](docs/semgrep/semgrep-apres.txt) (et les versions `.json`).

| Fichier | Ligne | Règle | Avant | Après |
|---|---|---|---|---|
| `mime/MimeTypesReader.java` | 223 | `saxparserfactory-disallow-doctype-decl-missing` | ✔ | ✔ |
| `parser/NetworkParser.java` | 68 | `unencrypted-socket` | ✔ | ✔ |
| `utils/XMLReaderUtils.java` | 307 | `xmlinputfactory-possible-xxe` | ✔ | ✔ |
| `metadata/Database.java` | 36 | `detected-username-and-password-in-uri` | | **nouvelle** |
| `metadata/Database.java` | 44 | `formatted-sql-string` | | **nouvelle** |
| `digest/DigestHelper.java` | 112 | `use-of-md5` | | **nouvelle** |
| `detect/XmlRootExtractor.java` | 99 | `documentbuilderfactory-disallow-doctype-decl-missing` | | **nouvelle** |
| `parser/CryptoParser.java` | 115 | `des-is-deprecated` | | **nouvelle** |
| `parser/CryptoParser.java` | 115 | `ecb-cipher` | | **nouvelle** |
| `parser/CryptoParser.java` | 124 | `use-of-weak-rsa-key` | | **nouvelle** |
| `io/TikaInputStream.java` | 211 | `unencrypted-socket` | | **nouvelle** |
| **Total** | | | **3** | **11** |

Les 3 alertes de départ sont identiques avant et après, puisqu'on n'a pas touché à ces lignes. Les 8 nouvelles
correspondent exactement à nos 7 vulnérabilités (DES/ECB en donne 2). Le scan en mode baseline du workflow trouve
les mêmes 8 alertes et seulement celles-là.

Le workflow de la branche `tache3` reste donc rouge à la fin, et c'est voulu : il montre que le build bloque bien
le code qui ajoute des vulnérabilités.

## 7. Rickroll quand un test échoue

L'énoncé parle de la suite de tests de graphhopper ; dans notre cas, le cas d'étude est Tika, donc on l'a fait sur
la suite de tests de Tika lancée par le workflow.

### 7.1 L'action

On a écrit notre propre action réutilisable, [`.github/actions/rickroll/action.yml`](.github/actions/rickroll/action.yml).
C'est une action *composite* avec une seule étape `bash`, sans dépendance externe. Elle :

- écrit une erreur dans le log (`::error title=Tests en echec::…`) qui invite à ouvrir le résumé du run ;
- ajoute au résumé du run (`$GITHUB_STEP_SUMMARY`) le titre « Les tests ont échoué » et une image cliquable
  présentée comme « le détail de l'erreur ». Le lien ouvre en fait la vidéo *Never Gonna Give You Up*.

Elle est appelée dans le job `tests`, juste après l'étape qui lance Maven :

```yaml
- name: Rickroll si les tests echouent
  if: failure()
  uses: ./.github/actions/rickroll
```

`if: failure()` fait que l'étape ne s'exécute que si une étape précédente a échoué. On a préféré écrire notre
action plutôt qu'utiliser une action externe, pour savoir exactement quel code tourne dans le CI.

### 7.2 Validation

| Run | Commit | Tests | Rickroll |
|---|---|---|---|
| #39 | ajout de l'appel au rickroll | passent | sauté (*skipped*) |
| #40 | ajout de `EndianUtilsRickrollTest`, qui échoue exprès | 1 échec sur 131 | exécuté |
| #41 | suppression de ce test | passent | sauté |

Le test du run #40 vérifie `assertEquals(1, EndianUtils.ubyteToInt((byte) 0x02))`. Maven affiche
`EndianUtilsRickrollTest.testEchecVolontaire:28 expected: <1> but was: <2>`, l'étape Rickroll s'exécute et
l'analyse PIT qui suit est sautée.

![Rickroll sauté quand les tests passent](docs/semgrep/captures/rickroll-skipped.png)

![Étapes du run en échec](docs/semgrep/captures/rickroll-etapes.png)

![Résumé du run avec le rickroll](docs/semgrep/captures/rickroll-summary.png)

![Retour au vert](docs/semgrep/captures/rickroll-retour-vert.png)

## 8. Utilisation de l'IA

On a utilisé Claude (Anthropic) comme assistant pendant toute la tâche : pour comprendre Semgrep et son mode
baseline, écrire les fichiers YAML du workflow et de l'action rickroll, choisir les vulnérabilités et écrire le
code vulnérable, vérifier quelles règles Semgrep les détectent, analyser les rapports et rédiger ce README. On a
fait nous-mêmes tous les commits, lancé le CI et vérifié les résultats : les chiffres et les captures viennent de
nos propres runs GitHub Actions.
