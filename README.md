# IFT3913 – Tâche 3
 
Équipe : ROMDHANE Farah (`farah-romdhane`) et KAISSI Ayman (`Ayman4401`)
 
Le travail est sur la branche `tache3` (`main` contient la tâche 2). Fichiers utiles : le workflow
[`tache2-tests.yml`](.github/workflows/tache2-tests.yml), l'action [`rickroll`](.github/actions/rickroll/action.yml)
et les rapports Semgrep dans [`docs/semgrep/`](docs/semgrep/).
 
## 1. Le workflow échoue si un commit ajoute une vulnérabilité
 
Les workflows d'origine de Tika avaient été retirés du fork à la tâche 2, parce qu'un run prenait plus d'une
heure. On a donc modifié le workflow qui sert de CI au cas d'étude depuis la tâche 2 (compilation de `tika-core`,
tests, PIT) en lui ajoutant un job `semgrep`. Si ce job échoue, tout le run est rouge, même si les tests passent.
 
Le point important est de ne bloquer que les **nouvelles** alertes : Tika en contient déjà 5 (section 3). On
utilise donc le mode baseline de Semgrep, qui ne garde que les alertes absentes d'un commit de référence :
 
```yaml
- name: Semgrep - nouvelles vulnerabilites seulement
  run: >
    semgrep scan --config p/java --config p/secrets --config p/security-audit
    --config r/java.lang.security.audit.command-injection-process-builder.command-injection-process-builder
    --baseline-commit "${{ steps.base.outputs.sha }}"
    --error tika-core/src/main/java
```
 
Nos choix, et pourquoi :
 
| Choix | Raison |
|---|---|
| Référence = commit précédent (`github.event.before`, ou la base pour une PR) | L'énoncé parle d'*un commit* qui introduit une vulnérabilité. Seul ce commit est rouge ; le suivant redevient vert. |
| Tag `semgrep-base` en secours | Au premier push d'une branche ou lors d'un lancement manuel, il n'y a pas de commit précédent. |
| `--error` | Sans lui, Semgrep sort avec le code 0 même s'il trouve quelque chose. |
| Job séparé des tests | On voit tout de suite si l'échec vient des tests ou de la sécurité. |
| Image `semgrep/semgrep:1.183.0` (version fixe) | Le résultat ne change pas d'un run à l'autre. |
| `fetch-depth: 0` et `safe.directory` | Sans eux, Git ne trouve pas le commit de référence dans le conteneur. |
| `p/security-audit` et la règle d'injection de commande en plus de `p/java` / `p/secrets` | Voir la section 2 : sans elles, on ne pouvait presque introduire que de la crypto. |
 
Notre première version comparait toujours au tag de départ. Le défaut : une fois une vulnérabilité ajoutée, tous
les commits suivants restaient rouges, même une modification du README. Limite de la version actuelle : si on
pousse plusieurs commits d'un coup, ils sont comparés ensemble.
 
### Validation
 
Pour le vérifier, on a retiré les vulnérabilités, puis on les a remises **une par commit**, en poussant chaque
commit seul :
 
| Run | Commit | `semgrep` | `tests` |
|---|---|---|---|
| [0](https://github.com/farah-romdhane/tika/actions/runs/44017571436) | retrait des vulnérabilités | succès | succès |
| [1](https://github.com/farah-romdhane/tika/actions/runs/44020230917) | nouvelles règles (seulement le `.yml`) | succès | succès |
| [V1](https://github.com/farah-romdhane/tika/actions/runs/44020331763) … [V7](https://github.com/farah-romdhane/tika/actions/runs/44020550950) | une vulnérabilité par commit (liens dans le tableau de la section 2) | échec | succès |
| [Rapports](https://github.com/farah-romdhane/tika/actions/runs/44023248954) | rapports avant / après, aucun code Java | succès | succès |
 
Chaque run rouge ne signale que l'alerte de son commit : celles des commits précédents sont déjà dans la
référence. Et un commit qui ne touche pas au code est vert, même avec 7 vulnérabilités dans le code.
 
## 2. Les 7 vulnérabilités
 
Chacune est une méthode ou une constante ajoutée dans une classe existante de `tika-core` dont le rôle est lié à
la faille. Elles ne sont appelées nulle part : Semgrep analyse le code source, donc ça suffit, et le comportement
de Tika ne change pas (les tests passent toujours).
 
| # | Type (CWE) | Fichier, et pourquoi lui | Code ajouté | Ce qui le rend vulnérable | Règle, ligne | Run |
|---|---|---|---|---|---|---|
| V1 | Identifiants dans le code (798) | `metadata/Database.java`, les métadonnées de bases de données | URL JDBC `tikaadmin:Tika2026!@…` | Le mot de passe est lisible par quiconque lit le dépôt (public). | `detected-username-and-password-in-uri`, 36 | [lien](https://github.com/farah-romdhane/tika/actions/runs/44020331763) |
| V2 | Injection SQL (89) | `metadata/Database.java`, même thème | `executeQuery("SELECT COUNT(*) FROM " + tableName)` | L'appelant peut changer la requête, ex. `documents UNION SELECT …`. | `formatted-sql-string`, 44 | [lien](https://github.com/farah-romdhane/tika/actions/runs/44020476678) |
| V3 | Injection de commande (78) | `utils/ProcessUtils.java`, qui lance les processus externes | `new ProcessBuilder("sh", "-c", "pdftotext " + fileName)` | Un nom comme `a.pdf; rm -rf ~` exécute une 2ᵉ commande dans le shell. | `command-injection-process-builder`, 300 | [lien](https://github.com/farah-romdhane/tika/actions/runs/44020490149) |
| V4 | XXE (611) | `detect/XmlRootExtractor.java`, qui lit le XML envoyé par l'utilisateur | `DocumentBuilderFactory` sans désactiver `DOCTYPE` | Une entité `file:///etc/passwd` fait lire un fichier du serveur. La méthode d'origine passe par `XMLReaderUtils`, protégé. | `documentbuilderfactory-disallow-doctype-decl-missing`, 99 | [lien](https://github.com/farah-romdhane/tika/actions/runs/44020504115) |
| V5 | Désérialisation (502) | `metadata/Metadata.java`, une classe `Serializable` | `new ObjectInputStream(...).readObject()` | Java crée les objets décrits dans les octets avant le cast : un attaquant peut faire instancier d'autres classes, voire exécuter du code. | `object-deserialization`, 807 | [lien](https://github.com/farah-romdhane/tika/actions/runs/44020526725) |
| V6 | Hachage faible (328) | `digest/DigestHelper.java`, qui calcule les empreintes | `MessageDigest.getInstance("MD5")` | Collisions connues : deux documents différents peuvent avoir la même empreinte. | `use-of-md5`, 112 | [lien](https://github.com/farah-romdhane/tika/actions/runs/44020536838) |
| V7 | Chiffrement cassé (327) | `parser/CryptoParser.java`, qui déchiffre des documents | `Cipher.getInstance("DES/ECB/PKCS5Padding")` | Clé de 56 bits cassable par force brute ; ECB chiffre deux blocs identiques de la même façon. | `des-is-deprecated` et `ecb-cipher`, 113 | [lien](https://github.com/farah-romdhane/tika/actions/runs/44020550950) |
 
Ce sont 7 CWE différents, dans 5 catégories OWASP (injection, identifiants, XXE, intégrité des données,
cryptographie). Seules V6 et V7 sont de la crypto.
 
**Pourquoi on a élargi les règles.** Avec seulement `p/java` et `p/secrets`, V3 et V5 n'étaient pas détectées,
pas plus qu'un `new Random()` pour un jeton : leurs règles ont `confidence: LOW` dans le
[dépôt de Semgrep](https://github.com/semgrep/semgrep-rules) et ne font pas partie de `p/java`. Notre première
version comptait donc 4 vulnérabilités crypto sur 7. Avec `p/security-audit`, la désérialisation est détectée ; la
règle d'injection de commande n'y est pas non plus, alors on l'a ajoutée nommément. C'est aussi la leçon de cette
tâche : un build vert dépend des règles choisies, pas seulement du code.
 
## 3. Semgrep avant et après
 
Rapports, avec les mêmes règles et la même version : [`semgrep-avant.txt`](docs/semgrep/semgrep-avant.txt) (5
alertes) et [`semgrep-apres.txt`](docs/semgrep/semgrep-apres.txt) (13 alertes), aussi en `.json`.
 
Les 5 alertes de départ, qu'on a vérifiées dans le code :
 
| Fichier : ligne | Alerte | Notre analyse |
|---|---|---|
| `mime/MimeTypesReader.java:223` | XXE | Faux positif : `FEATURE_SECURE_PROCESSING` est activé et `resolveEntity()` renvoie un flux vide. |
| `utils/XMLReaderUtils.java:307` | XXE | Faux positif : les entités sont désactivées plus bas, par une méthode que Semgrep ne suit pas. |
| `parser/NetworkParser.java:68` | Socket non chiffré | Vrai, mais voulu : ne sert qu'aux URI `telnet`, qui ne sont jamais chiffrées. |
| `detect/FileCommandDetector.java:110` | Injection de commande | Faux positif : pas de shell. Le programme (`file`) vient de la configuration de Tika, et le chemin du fichier est un argument séparé, échappé par `ProcessUtils.escapeCommandLine`. |
| `parser/external/ExternalParser.java:193` | Injection de commande | Vrai, mais voulu : la classe sert à lancer des outils externes, et la commande vient de la configuration de Tika, pas du document. |
 
Après nos modifications, ces 5 alertes sont toujours là, aux mêmes lignes, et il y en a **8 nouvelles** : celles
du tableau de la section 2 (DES/ECB en donne 2). Ce sont exactement les alertes qui ont fait échouer les runs V1 à
V7.
 
## 4. Rickroll quand un test échoue
 
L'énoncé parle de graphhopper ; notre cas d'étude étant Tika, on l'a fait sur les tests de Tika. Notre action
[`rickroll`](.github/actions/rickroll/action.yml) est une petite action *composite* en `bash`, sans dépendance
externe. Elle écrit une erreur dans le log et ajoute au résumé du run une image présentée comme « le détail de
l'erreur », dont le lien ouvre *Never Gonna Give You Up*.
 
```yaml
- name: Rickroll si les tests echouent
  if: failure() && steps.tests.outcome == 'failure'
  uses: ./.github/actions/rickroll
```
 
Avec un simple `if: failure()`, elle se déclencherait aussi pour une erreur de compilation ; la condition sur
`steps.tests` la limite à l'échec des tests.
 
Validation : un test qui échoue exprès ([R1](https://github.com/farah-romdhane/tika/actions/runs/44024702673) : `tests` rouge, rickroll exécuté), puis son retrait
([R2](https://github.com/farah-romdhane/tika/actions/runs/44024726404) : vert, rickroll sauté).
 
 
## 5. Utilisation de l'IA
 
On a utilisé Claude (Anthropic) pour comprendre Semgrep, écrire le workflow et l'action, préparer le code
vulnérable et rédiger ce README. Les commits, les runs et les vérifications sont les nôtres : chaque chiffre de ce
document vient d'un run ou d'un rapport lié ici.
 