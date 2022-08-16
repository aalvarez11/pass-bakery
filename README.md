# Pass-Bakery
Scala training project

## Project Dependencies
* play json
* jdbc (used by Scala in the Play Framework)
* evolutions
* postgres
* doobie

## Running the Application
1. Install [Docker](https://www.docker.com/products/docker-desktop/) and  [postgresql](https://www.postgresql.org/download/) on your machine, if not installed already.
2. Launch Docker (this app expects the latest postgres alpine)
3. Run the database through terminal commands
   1. If setting up the Docker container for the first time:
   2. `docker run --name pass-bakery-postgres -e POSTGRES_USER=user -e POSTGRES_PASSWORD=pass -p 5432:5432 -d postgres:alpine`
   3. If the Docker container has already been set up, and is running:
   4. `docker exec -it pass-bakery-postgres bash`
4. Launch the app on a separate terminal tab/window inside the project root with `sbt run`

### Docker Setup Cheatsheet
1. **ONE TIME!** Download latest postgres alpine image: `docker pull postgres:alpine`
2. **ONE TIME!** Create a Docker container: `docker run --name pass-bakery-postgres -e POSTGRES_USER=user -e POSTGRES_PASSWORD=pass -p 5432:5432 -d postgres:alpine`
3. Access the db in the terminal using bash: `docker exec -it pass-bakery-postgres bash` followed by `psql -U user` (provide password if prompted)
4. Create the db: `create database pass_bakery_db`
   * Check if it exists by listing databases with `\l`
   * Connect with `\c pass_bakery_db`
   * View tables with `\d`
