# IFT3913 – Tâche 3

| Nom complet | Identifiant GitHub |
|---|---|
| ROMDHANE Farah | farah-romdhane |
| KAISSI Ayman | Ayman4401 |

Tout le travail est sur la branche `tache3` (la branche `main` contient la tâche 2). Le README d'origine de Tika
est dans [`README-tika.md`](README-tika.md).

| Élément | Où |
|---|---|
| Workflow modifié (tests + Semgrep + rickroll) | [`.github/workflows/tache2-tests.yml`](.github/workflows/tache2-tests.yml) |
| Action qui rickroll | [`.github/actions/rickroll/action.yml`](.github/actions/rickroll/action.yml) |
| Rapports Semgrep avant / après | [`docs/semgrep/`](docs/semgrep/) |
| Captures d'écran | [`docs/semgrep/captures/`](docs/semgrep/captures/) |

## 1. Semgrep avant nos modifications

Semgrep 1.183.0, règles `p/java` (sécurité Java) et `p/secrets` (secrets dans le code), sur
`tika-core/src/main/java`. Rapport : [`semgrep-avant.txt`](docs/semgrep/semgrep-avant.txt).

| Fichier | Alerte | Notre analyse |
|---|---|---|
| `mime/MimeTypesReader.java:223` | XXE | Faux positif : `FEATURE_SECURE_PROCESSING` est activé et `resolveEntity()` renvoie un flux vide. |
| `parser/NetworkParser.java:68` | Socket non chiffré | Vrai mais voulu : sert seulement aux URI `telnet`, qui ne sont jamais chiffrées. |
| `utils/XMLReaderUtils.java:307` | XXE | Faux positif : les entités sont désactivées plus bas, par une méthode que Semgrep ne suit pas. |

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

| Choix | Pourquoi |
|---|---|
| `--baseline-commit` | Semgrep ne signale que les alertes absentes du commit de référence. Les 3 alertes de départ ne bloquent donc jamais. |
| Référence = commit précédent | L'énoncé demande d'échouer quand un commit introduit une vulnérabilité. Seul ce commit est rouge, les suivants redeviennent verts. |
| `--error` | Sans cette option, Semgrep termine avec le code 0 même s'il trouve des alertes. |
| Job séparé des tests | On voit tout de suite si l'échec vient des tests ou de la sécurité. |
| Version de Semgrep fixée | Le résultat ne change pas d'un run à l'autre. |
| `fetch-depth: 0` et `safe.directory` | Sans eux, Git ne trouve pas le commit de référence ou refuse de travailler dans le conteneur. |

Un 2ᵉ scan complet, qui ne bloque rien, produit le rapport utilisé pour la comparaison avant / après.

Limite : si plusieurs commits sont poussés d'un coup, ils sont comparés ensemble. Notre première version
comparait toujours au tag de départ, mais alors tous les commits suivants restaient rouges, même un simple
changement du README.

### Validation

Chaque commit a été poussé seul :

| Commit | Contenu | Résultat |
|---|---|---|
| [Workflow](https://github.com/farah-romdhane/tika/actions/runs/43128097270) | modification du `.yml` seulement | ✅ vert |
| [A](RUN_A) | ajoute une classe qui utilise MD5 | ❌ `semgrep` rouge : 1 alerte `use-of-md5`, `exit code 1` (`tests` vert) |
| [B](https://github.com/farah-romdhane/tika/actions/runs/43131977982) | modifie seulement le README | ✅ vert, même si le MD5 est encore dans le code |
| [C](https://github.com/farah-romdhane/tika/actions/runs/43132357647) | retire la classe MD5 | ✅ vert |

## 3. Les 7 vulnérabilités

Chaque vulnérabilité est une nouvelle méthode ou constante, ajoutée dans une classe existante de `tika-core` liée
au sujet. Elles ne sont appelées nulle part : Semgrep analyse le code source, et le comportement de Tika ne
change pas (les tests de la tâche 2 passent toujours).

| # | Fichier | Code ajouté | Pourquoi c'est vulnérable | Règle Semgrep |
|---|---|---|---|---|
| 1 | `metadata/Database.java` | `"jdbc:postgresql://tikaadmin:Tika2026!@localhost:5432/tika"` | **CWE-798** : identifiants dans le code source, visibles par quiconque lit le dépôt. | `detected-username-and-password-in-uri` |
| 2 | `metadata/Database.java` | `executeQuery("SELECT COUNT(*) FROM " + tableName)` | **CWE-89** : `tableName` est collé dans la requête, l'appelant peut en changer le sens (ex. `UNION SELECT …`). | `formatted-sql-string` |
| 3 | `digest/DigestHelper.java` | `MessageDigest.getInstance("MD5")` | **CWE-328** : MD5 a des collisions connues, deux documents différents peuvent avoir la même empreinte. | `use-of-md5` |
| 4 | `detect/XmlRootExtractor.java` | `DocumentBuilderFactory.newInstance()` sans désactiver les `DOCTYPE` | **CWE-611** : une entité externe (`file:///etc/passwd`) peut faire lire un fichier du serveur. | `documentbuilderfactory-disallow-doctype-decl-missing` |
| 5 | `parser/CryptoParser.java` | `Cipher.getInstance("DES/ECB/PKCS5Padding")` | **CWE-327** : clé DES de 56 bits cassable par force brute ; ECB chiffre deux blocs identiques de la même façon. | `des-is-deprecated`, `ecb-cipher` |
| 6 | `parser/CryptoParser.java` | `KeyPairGenerator` RSA avec `initialize(512)` | **CWE-326** : une clé RSA de 512 bits est factorisable ; le NIST demande au moins 2048 bits. | `use-of-weak-rsa-key` |
| 7 | `io/TikaInputStream.java` | `new Socket(host, port)` pour lire un document | **CWE-319** : le document passe en clair et peut être lu ou modifié sur le réseau. | `unencrypted-socket` |

**Pourquoi 4 vulnérabilités cryptographiques ?** On a d'abord essayé une injection de commande
(`ProcessBuilder`), une désérialisation (`ObjectInputStream`), un aléatoire prévisible (`new Random()`) et un mot
de passe dans une constante : Semgrep ne les a pas détectés. Dans le
[dépôt des règles Semgrep](https://github.com/semgrep/semgrep-rules), les règles correspondantes ont
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

Les 8 nouvelles alertes correspondent exactement à nos 7 vulnérabilités (DES/ECB en donne 2). Dans le run où
elles ont été ajoutées (#44), `tests` est vert et `semgrep` rouge avec ces 8 alertes :

![Run #44 : tests verts, Semgrep rouge](docs/semgrep/captures/ci-rouge-semgrep.png)

## 5. Rickroll quand un test échoue

L'énoncé parle de graphhopper ; notre cas d'étude étant Tika, on l'a fait sur les tests de Tika.

Notre action [`rickroll`](.github/actions/rickroll/action.yml) est une action *composite* en `bash`, sans
dépendance externe. Elle écrit une erreur dans le log et ajoute au résumé du run une image présentée comme
« le détail de l'erreur », dont le lien ouvre *Never Gonna Give You Up*.

```yaml
- name: Executer les tests (originaux + generes + manuels)
  id: tests
  ...
- name: Rickroll si les tests echouent
  if: failure() && steps.tests.outcome == 'failure'
  uses: ./.github/actions/rickroll
```

La condition `steps.tests.outcome` limite le rickroll à un échec des tests (pas de la compilation).

| Run | Situation | Résultat |
|---|---|---|
| #39 | tous les tests passent | Rickroll sauté, run vert |
| #40 | un test qui échoue exprès (`expected: <1> but was: <2>`) | Rickroll exécuté |
| #41 | test retiré | Rickroll sauté, run vert |

![Résumé du run #40 avec le rickroll](docs/semgrep/captures/rickroll-summary.png)

## 6. Utilisation de l'IA

On a utilisé Claude (Anthropic) pour comprendre Semgrep, écrire le workflow et l'action, choisir et écrire le code
vulnérable et rédiger ce README. On a fait nous-mêmes les commits, lancé le CI et vérifié les résultats : les
chiffres, les logs et les captures viennent de nos propres runs.