# DAI_PW2 application réseau (TCP et/ou UDP)

Le projet décrit dans ce README a été réalisé par des étudiants de 2ème année à la HEIG-VD dans le cadre du cours de DAI 2026-2027. Il s'agit de réaliser une application 



- [
Lien directives lab](https://heigvd-dai-26.github.io/practical-work/pw2.html)
- [
Lien discussion](https://github.com/orgs/heigvd-dai-26/discussions/35)


>Auteurs: Noé Ledderey & Sadiku Enis


# Temporaire - objectifs



## 1. Objectifs principaux
 Logiciel d'enchères en direct, par rounds (un seul round au minimum, en théorie).


Nous serons notés sur le **protocole** et le **code réseau**:

| Catégorie | Poids |
|---|---|
| Application protocol | 32 % |
| Network programming | 24 % |
| Docker et GHCR | 16 % |
| Présentation | 16 % |
| Network concurrency | 8 % |
| Tests | 4 % |


### Protocole de l'application
- Doit pouvoir être implémenté par d'autres équipes.
- À définir dans une spécification (modèle : protocole SMS du chapitre 6).

### Protocole de transport
- Choisir : **UDP**, **TCP**, ou **les deux**.
    - ✓ Notre choix : TCP (à justifier dans la spécification).

### Implémentation en Java
- **Programme client**
  - Doit implémenter le principe **REPL** (Read-Eval-Print Loop : boucle qui lit une commande, l'exécute, affiche le résultat).
  - Plusieurs clients doivent pouvoir se connecter au serveur en même temps.
- **Programme serveur**
- **Programme main** avec **picocli**, deux commandes :
  - `server` : ouvre le serveur
  - `client` : ouvre le client

### Docker
- Packager l'application, la publier et l'exécuter dans Docker
- Publier l'image publique dans **GitHub Container Registry**


## 2. Rendu (01.12.26)
- Spécification : `docs/protocol.md`, avec 4 sections (aperçu, transport, messages, exemples) et diagrammes de séquence
- README : commandes explicites pour construire, publier et lancer l'image
- Tests JUnit : logique du protocole séparée du code réseau
- Annonce du groupe : GitHub Discussion (catégorie Show and tell)
- Remise : lien du dépôt et hash du commit dans la Discussion

---
## 3. Présentation (02.12.26)
- 8 minutes.
- Arriver 5 minutes à l'avance
- Démo client / serveur (avec un cas d'erreur)
- Expliquer le protocole choisi : l'idée et les choix, pas ligne par ligne

---