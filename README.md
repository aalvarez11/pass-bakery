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
   1. Create the docker container, or start it if it exists but is stopped:
   2. `docker run --name pass-bakery-postgres -e POSTGRES_USER=user -e POSTGRES_PASSWORD=pass -e POSTGRES_DB=pass_bakery_db -p 5432:5432 -d postgres:alpine`
   3. Access the docker postgres database (enter `pass` when promtped):
   4. `psql -h localhost -p 5432 -d pass_bakery_db -U user`
4. Launch the app on a separate terminal tab (command+T), navigate to the project root, and run with `sbt run`
5. Apply evolutions when prompted at any endpoint connection

### Docker Cheatsheet
1. **ONE TIME!** Download latest postgres alpine image: `docker pull postgres:alpine`
2. Create/start the Docker container: `docker run --name pass-bakery-postgres -e POSTGRES_USER=user -e POSTGRES_PASSWORD=pass -p 5432:5432 -d postgres:alpine`
3. Access the db: `psql -h localhost -p 5432 -d pass_bakery_db -U user` and enter `pass` when prompted for the password
4. Additional Notes:
   * Check if `pass_bakery_db` exists by listing databases with `\l`
   * If needed, connect to the database with `\c pass_bakery_db`
   * View tables with `\d`
