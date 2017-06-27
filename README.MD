tenso - Tenhou cloud bot project
===================


Riichi-mahjong bot realization for a popular online game server tenhou.net. Includes Java wrapper over a tenhou protocol.

The project contains of four main modules:
* **app** -- heroku-compatible simple JSON API for running the bot on cloud with some parameters (i.e. lobby number, bot name, particular game type)
* **mahjongai** -- some independent bot logic, **undone yet**
* **mityabot** -- bot logic which uses external service (https://tempai.net/eff) for an efficiency calculation
* **tenhouj** -- Java interface for a tenhou.net protocol

To deploy it on Heroku you just need to upload the sources on your instance. It will automatically build and install the project. See the "Procfile" and a "stage" task inside the "build.gradle" for details.

The project is published under the terms of the WTFPL.