# IFT3913 – Tâche 3

**Équipe** : ROMDHANE Farah (`farah-romdhane`) et KAISSI Ayman (`Ayman4401`)

Tout le travail est sur la branche `tache3` (la branche `main` contient la tâche 2). Le README d'origine de Tika
est dans [`README-tika.md`](README-tika.md).

Les fichiers importants :

- le workflow modifié (tests, Semgrep et rickroll) : [`.github/workflows/tache2-tests.yml`](.github/workflows/tache2-tests.yml) ;
- l'action qui rickroll : [`.github/actions/rickroll/action.yml`](.github/actions/rickroll/action.yml) ;
- les rapports Semgrep avant et après : [`docs/semgrep/`](docs/semgrep/) ;
- les captures d'écran : [`docs/semgrep/captures/`](docs/semgrep/captures/).

## 1. Semgrep avant nos modifications

On a lancé Semgrep 1.183.0 avec les règles `p/java` (sécurité Java) et `p/secrets` (secrets dans le code) sur
`tika-core/src/main/java`. Rapport : [`semgrep-avant.txt`](docs/semgrep/semgrep-avant.txt). Il y a 3 alertes, et on
a regardé le code à chaque endroit :

- **`mime/MimeTypesReader.java:223` (XXE) : faux positif.** `FEATURE_SECURE_PROCESSING` est activé et
  `resolveEntity()` renvoie un flux vide, donc aucune entité externe n'est lue.
- **`parser/NetworkParser.java:68` (socket non chiffré) : vrai, mais voulu.** Il sert seulement aux URI `telnet`,
  qui ne sont jamais chiffrées. Les autres URI passent par `URLConnection`, qui accepte `https`.
- **`utils/XMLReaderUtils.java:307` (XXE) : faux positif.** Les entités externes sont désactivées quelques lignes
  plus bas, mais par une méthode (`tryToSetStaxProperty`) que Semgrep ne suit pas.

Ces 3 alertes existaient déjà : elles ne doivent pas bloquer le build.

## 2. Modification du workflow

Les workflows d'origine de Tika avaient été retirés du fork à la tâche 2 (plus d'une heure par run). On a donc
modifié le workflow du cas d'étude utilisé depuis la tâche 2, en lui ajoutant un job `semgrep` :

```yaml
semgrep:
  runs-on: ubuntu-latest
  container:
    image: semgrep/semgrep:1.183.0
  steps:
    - uses: actions/checkout@v4
      with:
        fetch-depth: 0
    - run: git config --global --add safe.directory "$GITHUB_WORKSPACE"
    - name: Choisir le commit de reference
      id: base
      run: |
        # push : commit d'avant le push ; pull request : branche de base
        # sinon (1er push, force-push, lancement manuel) : tag de depart
        ...
    - name: Semgrep - nouvelles vulnerabilites seulement
      run: >
        semgrep scan --config p/java --config p/secrets
        --baseline-commit "${{ steps.base.outputs.sha }}"
        --error tika-core/src/main/java
```

Si le job `semgrep` échoue, tout le run est en échec, même si les tests passent.

### Nos choix

- **`--baseline-commit`** : Semgrep ne signale que les alertes absentes du commit de référence. Les 3 alertes de
  départ ne bloquent donc jamais.
- **Le commit de référence est le commit précédent** (`github.event.before` pour un push, la branche de base pour
  une pull request). L'énoncé demande que le build échoue quand *un commit* introduit une vulnérabilité : seul ce
  commit est rouge, les suivants redeviennent verts. S'il n'y a pas de commit précédent utilisable (premier push
  d'une branche, force-push, lancement manuel), on compare au tag `semgrep-base`, posé sur le commit de départ.
- **`--error`** : sans cette option, Semgrep termine avec le code 0 même s'il trouve des alertes.
- **Un job séparé des tests** : on voit tout de suite si l'échec vient des tests ou de la sécurité.
- **La version de Semgrep est fixée** (`1.183.0`), pour que le résultat ne change pas d'un run à l'autre.
- **`fetch-depth: 0` et `safe.directory`** : sans eux, Git ne trouve pas le commit de référence, ou refuse de
  travailler dans le conteneur Docker.

Un 2ᵉ scan complet, qui ne bloque rien, produit le rapport utilisé pour la comparaison avant / après.

Limite : si plusieurs commits sont poussés d'un coup, ils sont comparés ensemble. Notre première version comparait
toujours au tag de départ, mais alors tous les commits suivants restaient rouges, même un simple changement du
README.

### Validation

On a poussé chaque commit seul, pour voir le résultat de chacun :

1. [Modification du workflow](https://github.com/farah-romdhane/tika/actions/runs/43128097270) (le `.yml`
   seulement) : ✅ vert.
2. [Commit A](https://github.com/farah-romdhane/tika/commit/beb69e37b8f0de6f0ac9f3582f2e6b9a04d665d5) : ajoute une
   classe qui utilise MD5. ❌ Le job `semgrep` est rouge avec 1 alerte `use-of-md5` et `exit code 1` ; le job
   `tests` reste vert.
3. [Commit B](https://github.com/farah-romdhane/tika/actions/runs/43131977982) : modifie seulement le README.
   ✅ vert, même si le MD5 est encore dans le code. Ça montre que seul le commit qui introduit la faille échoue.
4. [Commit C](https://github.com/farah-romdhane/tika/actions/runs/43132357647) : retire la classe MD5. ✅ vert.

## 3. Les 7 vulnérabilités

Chaque vulnérabilité est une nouvelle méthode ou constante, ajoutée dans une classe existante de `tika-core` dont
le rôle a un lien avec la faille. Elles ne sont appelées nulle part : Semgrep analyse le code source, et le
comportement de Tika ne change pas (les tests de la tâche 2 passent toujours).

### 3.1 Identifiants dans le code (CWE-798)

- **Fichier** : `metadata/Database.java`. Cette interface regroupe les métadonnées que Tika extrait des bases de
  données ; c'est l'endroit le plus proche d'une connexion à une base dans `tika-core`.
- **Code** : `String METADATA_DB_URL = "jdbc:postgresql://tikaadmin:Tika2026!@localhost:5432/tika";`
- **Pourquoi c'est vulnérable** : l'utilisateur et le mot de passe sont dans le code source, visibles par quiconque
  lit le dépôt (ici, il est public).
- **Semgrep** : `detected-username-and-password-in-uri`, ligne 36. Semgrep masque lui-même le mot de passe dans son
  rapport (`jdbc:***localhost…`).

### 3.2 Injection SQL (CWE-89)

- **Fichier** : `metadata/Database.java`, même thème que la vulnérabilité précédente.
- **Code** : `statement.executeQuery("SELECT COUNT(*) FROM " + tableName)`
- **Pourquoi c'est vulnérable** : `tableName` est collé dans la requête, donc l'appelant peut en changer le sens
  (par exemple avec `documents UNION SELECT …`).
- **Semgrep** : `formatted-sql-string`, ligne 44.

### 3.3 Hachage MD5 (CWE-328)

- **Fichier** : `digest/DigestHelper.java`, la classe de Tika qui calcule les empreintes (*digests*) des documents.
- **Code** : `MessageDigest.getInstance("MD5")`
- **Pourquoi c'est vulnérable** : MD5 a des collisions connues, donc deux documents différents peuvent avoir la
  même empreinte. Tika propose déjà SHA-256 dans sa configuration.
- **Semgrep** : `use-of-md5`, ligne 112.

### 3.4 XXE (CWE-611)

- **Fichier** : `detect/XmlRootExtractor.java`, la classe qui lit un fichier XML envoyé par l'utilisateur pour
  détecter son type. Sa méthode d'origine passe par `XMLReaderUtils`, qui est protégé ; la nôtre ne l'est pas.
- **Code** : `DocumentBuilderFactory.newInstance()` puis `parse(...)`, sans désactiver les `DOCTYPE`.
- **Pourquoi c'est vulnérable** : un fichier qui déclare une entité externe (`file:///etc/passwd`) peut faire lire
  un fichier du serveur.
- **Semgrep** : `documentbuilderfactory-disallow-doctype-decl-missing`, ligne 99.

### 3.5 DES en mode ECB (CWE-327)

- **Fichier** : `parser/CryptoParser.java`, la classe de Tika qui déchiffre des documents.
- **Code** : `Cipher.getInstance("DES/ECB/PKCS5Padding")`
- **Pourquoi c'est vulnérable** : la clé DES fait 56 bits et se casse par force brute ; le mode ECB chiffre deux
  blocs identiques de la même façon, ce qui laisse voir des motifs.
- **Semgrep** : `des-is-deprecated` et `ecb-cipher`, ligne 115.

### 3.6 Clé RSA de 512 bits (CWE-326)

- **Fichier** : `parser/CryptoParser.java`, même classe de chiffrement.
- **Code** : `KeyPairGenerator.getInstance("RSA")` avec `initialize(512)`
- **Pourquoi c'est vulnérable** : une clé RSA de 512 bits peut être factorisée ; le NIST demande au moins 2048
  bits.
- **Semgrep** : `use-of-weak-rsa-key`, ligne 124.

### 3.7 Socket non chiffré (CWE-319)

- **Fichier** : `io/TikaInputStream.java`, la classe qui ouvre les flux à analyser. Elle lit déjà des fichiers et
  des URL, donc une méthode qui lit depuis le réseau y a sa place.
- **Code** : `new Socket(host, port)` pour recevoir un document.
- **Pourquoi c'est vulnérable** : le document passe en clair, il peut être lu ou modifié sur le réseau.
- **Semgrep** : `unencrypted-socket`, ligne 211. Ce type d'alerte existait déjà dans `NetworkParser`, mais Semgrep
  compte bien la nôtre comme nouvelle.

### Pourquoi 4 vulnérabilités cryptographiques ?

On a d'abord essayé une injection de commande (`ProcessBuilder`), une désérialisation (`ObjectInputStream`), un
aléatoire prévisible (`new Random()`) et un mot de passe dans une constante : Semgrep ne les a pas détectés. Dans
le [dépôt des règles Semgrep](https://github.com/semgrep/semgrep-rules), les règles correspondantes ont
`confidence: LOW` et ne font pas partie de `p/java`. Le reste des règles de `p/java` vise surtout des applications
web (servlets), que `tika-core` n'a pas. Nos 4 cas de crypto restent des erreurs différentes, avec des CWE
différents : algorithme de hachage, algorithme et mode de chiffrement, taille de clé, transport.

Conclusion : un build vert ne veut pas dire que le code est sûr, le résultat dépend des règles choisies.

## 4. Comparaison avant / après

Rapports : [`semgrep-avant.txt`](docs/semgrep/semgrep-avant.txt) et [`semgrep-apres.txt`](docs/semgrep/semgrep-apres.txt).

| Fichier | Ligne | Règle | Avant | Après |
|---|---|---|---|---|
| `mime/MimeTypesReader.java` | 223 | `saxparserfactory-disallow-doctype-decl-missing` | ✔ | ✔ |
| `parser/NetworkParser.java` | 68 | `unencrypted-socket` | ✔ | ✔ |
| `utils/XMLReaderUtils.java` | 307 | `xmlinputfactory-possible-xxe` | ✔ | ✔ |
| `metadata/Database.java` | 36 | `detected-username-and-password-in-uri` | | nouvelle |
| `metadata/Database.java` | 44 | `formatted-sql-string` | | nouvelle |
| `digest/DigestHelper.java` | 112 | `use-of-md5` | | nouvelle |
| `detect/XmlRootExtractor.java` | 99 | `documentbuilderfactory-disallow-doctype-decl-missing` | | nouvelle |
| `parser/CryptoParser.java` | 115 | `des-is-deprecated` | | nouvelle |
| `parser/CryptoParser.java` | 115 | `ecb-cipher` | | nouvelle |
| `parser/CryptoParser.java` | 124 | `use-of-weak-rsa-key` | | nouvelle |
| `io/TikaInputStream.java` | 211 | `unencrypted-socket` | | nouvelle |
| **Total** | | | **3** | **11** |

Les 8 nouvelles alertes correspondent exactement à nos 7 vulnérabilités (DES/ECB en donne 2). Dans le run où elles
ont été ajoutées (#44), `tests` est vert et `semgrep` est rouge avec ces 8 alertes :

![Run #44 : tests verts, Semgrep rouge](docs/semgrep/captures/ci-rouge-semgrep.png)

## 5. Rickroll quand un test échoue

L'énoncé parle de graphhopper ; notre cas d'étude étant Tika, on l'a fait sur les tests de Tika.

Notre action [`rickroll`](.github/actions/rickroll/action.yml) est une action *composite* en `bash`, sans
dépendance externe. Elle écrit une erreur dans le log et ajoute au résumé du run une image présentée comme « le
détail de l'erreur », dont le lien ouvre *Never Gonna Give You Up*. Elle est appelée juste après les tests :

```yaml
- name: Executer les tests (originaux + generes + manuels)
  id: tests
  ...
- name: Rickroll si les tests echouent
  if: failure() && steps.tests.outcome == 'failure'
  uses: ./.github/actions/rickroll
```

La condition `steps.tests.outcome` limite le rickroll à un échec des tests, et pas de la compilation.

Validation, faite avec la première version de la condition (`if: failure()`) :

- **run #39** : tous les tests passent, le rickroll est sauté et le run est vert ;
- **run #40** : un test échoue exprès (`expected: <1> but was: <2>`), le rickroll s'exécute ;
- **run #41** : le test est retiré, le rickroll est sauté et le run est vert.

![Résumé du run #40 avec le rickroll](docs/semgrep/captures/rickroll-summary.png)

## 6. Utilisation de l'IA

On a utilisé Claude (Anthropic) pour comprendre Semgrep, écrire le workflow et l'action, choisir et écrire le code
vulnérable et rédiger ce README. On a fait nous-mêmes les commits, lancé le CI et vérifié les résultats : les
chiffres, les logs et les captures viennent de nos propres runs.
